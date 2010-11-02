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
import at.molindo.utils.data.StringUtils;

public class NotifyFilter implements Filter {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(NotifyFilter.class);
	
	private static final String ATTRIBUTE_CHANNEL = NotifyFilter.class.getName() + ".channel";
	private ServletContext _context;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_context = filterConfig.getServletContext();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
	
		// TODO get channelId and userId from request
		String channelId = "feed-public";
		String userId = "JohnDoe";
		
		IPullChannel channel = getChannel(channelId);
		if (StringUtils.empty(userId) || StringUtils.empty(channelId)) {
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
					((IRequestConfigurable)prefs).setParam((String)e.getKey(), (String)value);
				}
			} catch (NotifyException e) {
				resp.sendError(404);
				return;
			}
		}
		
		if (channel.isConfigured(prefs)) {
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
		
		Object attr = _context.getAttribute(ATTRIBUTE_CHANNEL);
		if (attr instanceof IPullChannel) {
			IPullChannel c = (IPullChannel) attr;
			return channelId.equals(c.getId()) ? c : null;
		} else if (attr instanceof Iterable<?>) {
			for (Object o : (Iterable<?>)attr) {
				if (o instanceof IPullChannel) {
					IPullChannel c = (IPullChannel) o;
					return channelId.equals(c.getId()) ? c : null;
				}
			}
		}
		return null;
	}

	@Override
	public void destroy() {
		_context = null;
	}

}
