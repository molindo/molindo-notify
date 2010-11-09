/**
 * Copyright 2010 Molindo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.molindo.notify.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.molindo.notify.INotificationService.NotifyException;
import at.molindo.notify.channel.IPullChannel;
import at.molindo.notify.channel.IPullChannel.PullException;
import at.molindo.notify.model.ChannelPreferences;
import at.molindo.notify.model.IRequestConfigurable;
import at.molindo.notify.model.Notification.Type;
import at.molindo.utils.data.StringUtils;

import com.google.common.collect.Maps;

public class NotifyFilter implements Filter {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotifyFilter.class);

	private static final String ATTRIBUTE_CHANNEL = NotifyFilter.class.getName() + ".channel";

	private static final Pattern PATTERN = Pattern.compile("^/([^/?]+)/([^/?]+).*$");

	private ServletContext _context;

	public static void addChannel(IPullChannel channel, ServletContext context) {
		if (channel == null) {
			throw new NullPointerException("channel");
		}
		if (context == null) {
			throw new NullPointerException("context");
		}

		PullChannels channels = (PullChannels) context.getAttribute(ATTRIBUTE_CHANNEL);
		if (channels == null) {
			context.setAttribute(ATTRIBUTE_CHANNEL, new PullChannels(channel));
		} else {
			channels.add(channel);
		}
	}

	public static void removeChannel(IPullChannel channel, ServletContext context) {
		if (channel == null) {
			throw new NullPointerException("channel");
		}
		if (context == null) {
			throw new NullPointerException("context");
		}

		PullChannels channels = (PullChannels) context.getAttribute(ATTRIBUTE_CHANNEL);
		if (channels != null) {
			channels.remove(channel);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_context = filterConfig.getServletContext();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		String path = StringUtils.afterFirst(req.getRequestURI(), req.getServletPath());
		if (path == null) {
			resp.sendError(404);
			return;
		}

		Matcher m = PATTERN.matcher(path);
		if (!m.matches()) {
			resp.sendError(404);
			return;
		}

		String channelId = m.group(1);
		String userId = m.group(2);

		if (StringUtils.empty(userId) || StringUtils.empty(channelId)) {
			resp.sendError(404);
			return;
		}

		IPullChannel channel = getChannel(channelId);
		if (channel == null) {
			resp.sendError(404);
			return;
		}

		ChannelPreferences prefs = channel.newDefaultPreferences();
		if (prefs instanceof IRequestConfigurable) {
			try {
				Map<?, ?> queryParams = req.getParameterMap();

				for (Map.Entry<?, ?> e : queryParams.entrySet()) {
					Object value = e.getValue();
					if (value != null && value.getClass().isArray()) {
						String[] vals = (String[]) value;
						value = vals.length > 0 ? vals[0] : null;
					}
					((IRequestConfigurable) prefs).setParam((String) e.getKey(), (String) value);
				}
			} catch (NotifyException e) {
				resp.sendError(404);
				return;
			}
		}

		if (channel.getNotificationTypes().contains(Type.PRIVATE) && !channel.isAuthorized(userId, prefs)) {
			resp.sendError(403);
		} else if (channel.isConfigured(userId, prefs)) {
			try {
				String output = channel.pull(userId, prefs);
				if (StringUtils.empty(output)) {
					resp.sendError(404);
				} else {
					response.getWriter().write(output);
				}
			} catch (PullException e) {
				log.info("failed to pull notifications", e);
				resp.sendError(500);
			}
		} else {
			resp.sendError(404);
		}
	}

	private IPullChannel getChannel(String channelId) {

		PullChannels channels = (PullChannels) _context.getAttribute(ATTRIBUTE_CHANNEL);
		if (channels == null) {
			return null;
		}

		return channels.get(channelId);
	}

	@Override
	public void destroy() {
		if (_context != null) {
			_context = null;
		}
	}

	private static class PullChannels {

		private Map<String, IPullChannel> _channels = Maps.newHashMap();

		public PullChannels(IPullChannel channel) {
			add(channel);
		}

		public IPullChannel get(String channelId) {
			return _channels.get(channelId);
		}

		public void add(IPullChannel channel) {
			IPullChannel current = _channels.get(channel.getId());
			if (current == null) {
				_channels.put(channel.getId(), channel);
			} else if (current != channel) {
				throw new IllegalStateException("duplicate id: " + channel.getId());
			}
		}

		public void remove(IPullChannel channel) {
			if (_channels.get(channel.getId()) == channel) {
				_channels.remove(channel.getId());
			}
		}
	}
}
