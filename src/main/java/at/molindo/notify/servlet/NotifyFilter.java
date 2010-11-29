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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

import at.molindo.notify.INotifyService.NotifyException;
import at.molindo.notify.channel.IPullChannel;
import at.molindo.notify.channel.IPullChannel.PullException;
import at.molindo.notify.confirm.ConfirmationService;
import at.molindo.notify.confirm.IConfirmationService;
import at.molindo.notify.confirm.IConfirmationService.ConfirmationException;
import at.molindo.notify.model.IChannelPreferences;
import at.molindo.notify.model.IParams;
import at.molindo.notify.model.IRequestConfigurable;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.ParamValue;
import at.molindo.notify.model.Params;
import at.molindo.utils.data.StringUtils;
import at.molindo.utils.io.CharsetUtils;

import com.google.common.collect.Maps;

public class NotifyFilter implements Filter {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotifyFilter.class);

	private static final String ATTRIBUTE_FILTER = NotifyFilter.class.getName() + ".filter";
	private static final String ATTRIBUTE_CHANNEL = NotifyFilter.class.getName() + ".channel";
	private static final String ATTRIBUTE_CONFIRM_SERVICE = NotifyFilter.class.getName() + ".confirmService";

	static final String DEFAULT_MOUNT_PATH = "notify";
	static final String PARAMTER_MOUNT_PATH = "mountPath";

	static final String DEFAULT_PULL_PREFIX = "pull";
	static final String PARAMTER_PULL_PREFIX = "pullPrefix";

	static final String DEFAULT_CONFIRM_PREFIX = "confirm";
	static final String PARAMTER_CONFIRM_PREFIX = "confirmPrefix";

	private String _mountPath;

	private String _pullPrefix;
	private Pattern _pullPattern;

	private String _confirmPrefix;
	private Pattern _confirmPattern;

	private ServletContext _context;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_context = filterConfig.getServletContext();
		_context.setAttribute(ATTRIBUTE_FILTER, this);

		{
			String mountPath = filterConfig.getInitParameter(PARAMTER_MOUNT_PATH);
			if (StringUtils.empty(mountPath)) {
				mountPath = DEFAULT_MOUNT_PATH;
			}
			_mountPath = StringUtils.startWith(mountPath, "/");
		}

		{
			String pullPrefix = filterConfig.getInitParameter(PARAMTER_PULL_PREFIX);
			if (StringUtils.empty(pullPrefix)) {
				pullPrefix = DEFAULT_PULL_PREFIX;
			}
			_pullPrefix = StringUtils.startWith(pullPrefix, "/");
			_pullPattern = Pattern.compile("^" + Pattern.quote(_pullPrefix) + "/([^/?]+)/([^/?]+).*$");
		}

		{
			String confirmPrefix = filterConfig.getInitParameter(PARAMTER_CONFIRM_PREFIX);
			if (StringUtils.empty(confirmPrefix)) {
				confirmPrefix = DEFAULT_CONFIRM_PREFIX;
			}
			_confirmPrefix = StringUtils.startWith(confirmPrefix, "/");
			_confirmPattern = Pattern.compile("^" + Pattern.quote(_confirmPrefix) + "/([^/?]+)/([^/?]+).*$");
		}
	}

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

	public static void setConfirmationService(ConfirmationService confirmationService, ServletContext context) {
		context.setAttribute(ATTRIBUTE_CONFIRM_SERVICE, confirmationService);
	}

	public static NotifyFilter getFilter(ServletContext context) {
		NotifyFilter filter = (NotifyFilter) context.getAttribute(ATTRIBUTE_FILTER);
		if (filter == null) {
			throw new IllegalStateException("no filter configured");
		}
		return filter;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
	}

	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String path = request.getRequestURI();
		if (StringUtils.empty(path) || !path.startsWith(_mountPath)) {
			response.sendError(404);
			return;
		}
		path = path.substring(_mountPath.length());

		if (path.startsWith(_pullPrefix)) {
			pull(request, response, path);
		} else if (path.startsWith(_confirmPrefix)) {
			confirm(request, response, path);
		} else {
			chain.doFilter(request, response);
		}
	}

	protected void pull(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
		Matcher m = _pullPattern.matcher(path);
		if (!m.matches()) {
			response.sendError(404);
			return;
		}

		String channelId = decode(m.group(1));
		String userId = decode(m.group(2));

		if (StringUtils.empty(userId) || StringUtils.empty(channelId)) {
			response.sendError(404);
			return;
		}

		IPullChannel channel = getChannel(channelId);
		if (channel == null) {
			response.sendError(404);
			return;
		}

		IChannelPreferences prefs = channel.newDefaultPreferences();
		if (prefs instanceof IRequestConfigurable) {
			try {
				Map<?, ?> queryParams = request.getParameterMap();

				for (Map.Entry<?, ?> e : queryParams.entrySet()) {
					Object value = e.getValue();
					if (value != null && value.getClass().isArray()) {
						String[] vals = (String[]) value;
						value = vals.length > 0 ? vals[0] : null;
					}
					((IRequestConfigurable) prefs).setParam((String) e.getKey(), (String) value);
				}
			} catch (NotifyException e) {
				response.sendError(404);
				return;
			}
		}

		if (channel.getNotificationTypes().contains(Type.PRIVATE) && !channel.isAuthorized(userId, prefs)) {
			response.sendError(403);
		} else if (channel.isConfigured(userId, prefs)) {
			try {
				String output = channel.pull(userId, prefs);
				if (StringUtils.empty(output)) {
					response.sendError(404);
				} else {
					response.getWriter().write(output);
				}
			} catch (PullException e) {
				log.info("failed to pull notifications", e);
				response.sendError(500);
			}
		} else {
			response.sendError(404);
		}
	}

	protected void confirm(HttpServletRequest request, HttpServletResponse response, String path) throws IOException,
			ServletException {
		Matcher m = _confirmPattern.matcher(path);
		if (!m.matches()) {
			response.sendError(404);
			return;
		}

		String key = decode(m.group(1));

		String confirmPath;
		try {
			confirmPath = getConfirmationService().confirm(key);
			if (confirmPath == null) {
				response.sendError(404);
			} else {
				request.getRequestDispatcher(confirmPath).forward(request, response);
			}
		} catch (ConfirmationException e) {
			throw new ServletException("can't confirm key " + key, e);
		}
	}

	private String decode(String string) {
		try {
			return URLDecoder.decode(string, CharsetUtils.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported?", e);
		}
	}

	private String encode(String string) {
		try {
			return URLEncoder.encode(string, CharsetUtils.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported?", e);
		}
	}

	private IConfirmationService getConfirmationService() {
		return (IConfirmationService) _context.getAttribute(ATTRIBUTE_CONFIRM_SERVICE);
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

		private final Map<String, IPullChannel> _channels = Maps.newHashMap();

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

	public String toPullPath(String channelId, String userId, IParams params) {
		IPullChannel channel = getChannel(channelId);
		if (channel == null) {
			return null;
		}

		IChannelPreferences cPrefs = channel.newDefaultPreferences();

		IParams fullParams = new Params().setAll(cPrefs.getParams()).setAll(params);

		StringBuilder buf = new StringBuilder();
		buf.append(_mountPath).append(_pullPrefix).append(channelId).append("/").append(userId);
		if (fullParams.size() > 0) {
			buf.append("?");
			for (final ParamValue pv : params) {
				final String encodedName = encode(pv.getName());
				final String value = pv.getStringValue();
				final String encodedValue = value != null ? encode(value) : "";

				buf.append(encodedName);
				buf.append("=");
				buf.append(encodedValue);
				buf.append("&");
			}
			buf.setLength(buf.length() - 1);
		}
		return buf.toString();
	}
}
