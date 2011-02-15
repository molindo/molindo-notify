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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import at.molindo.notify.INotifyService.NotifyException;
import at.molindo.notify.channel.IPullChannel;
import at.molindo.notify.channel.IPullChannel.PullException;
import at.molindo.notify.confirm.IConfirmationService;
import at.molindo.notify.confirm.IConfirmationService.ConfirmationException;
import at.molindo.notify.model.Confirmation;
import at.molindo.notify.model.IChannelPreferences;
import at.molindo.notify.model.IParams;
import at.molindo.notify.model.IRequestConfigurable;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.ParamValue;
import at.molindo.notify.model.Params;
import at.molindo.utils.data.StringUtils;
import at.molindo.utils.io.CharsetUtils;

public class NotifyFilterBean extends GenericFilterBean implements INotifyUrlFactory {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotifyFilterBean.class);

	static final String PARAMTER_BASE_URL_PROPERTY = "baseUrlProperty";
	static final String PARAMTER_BASE_URL = "baseUrl";

	static final String DEFAULT_MOUNT_PATH = "notify";
	static final String PARAMTER_MOUNT_PATH = "mountPath";

	static final String DEFAULT_PULL_PREFIX = "pull";
	static final String PARAMTER_PULL_PREFIX = "pullPrefix";

	static final String DEFAULT_CONFIRM_PREFIX = "confirm";
	static final String PARAMTER_CONFIRM_PREFIX = "confirmPrefix";

	private String _baseUrl;
	private String _baseUrlProperty;

	private String _mountPath;

	private String _pullPrefix;
	private Pattern _pullPattern;

	private String _confirmPrefix;
	private Pattern _confirmPattern;

	private final Map<String, IPullChannel> _channels = new HashMap<String, IPullChannel>();
	private IConfirmationService _confirmationService;

	@Override
	public void initFilterBean() throws ServletException {
		{
			if (!StringUtils.empty(_baseUrlProperty)) {
				String baseUrl = System.getProperty(_baseUrlProperty);
				if (!StringUtils.empty(baseUrl)) {
					_baseUrl = baseUrl;
				}
			}

			if (StringUtils.empty(_baseUrl)) {
				throw new ServletException(String.format("parameter %s is required", PARAMTER_BASE_URL));
			}

			try {
				_baseUrl = StringUtils.stripTrailing(new URL(_baseUrl).toString(), "/");
			} catch (MalformedURLException e) {
				throw new ServletException(String.format("illegal value for parameter %s: '%s'", PARAMTER_BASE_URL,
						_baseUrl), e);
			}
		}

		{
			if (StringUtils.empty(_mountPath)) {
				_mountPath = DEFAULT_MOUNT_PATH;
			}
			_mountPath = StringUtils.stripTrailing(StringUtils.startWith(_mountPath, "/"), "/");
		}

		{
			if (StringUtils.empty(_pullPrefix)) {
				_pullPrefix = DEFAULT_PULL_PREFIX;
			}
			_pullPrefix = StringUtils.endWith(StringUtils.startWith(_pullPrefix, "/"), "/");
			_pullPattern = Pattern.compile("^" + Pattern.quote(_pullPrefix) + "([^/?]+)/([^/?]+).*$");
		}

		{
			if (StringUtils.empty(_confirmPrefix)) {
				_confirmPrefix = DEFAULT_CONFIRM_PREFIX;
			}
			_confirmPrefix = StringUtils.endWith(StringUtils.startWith(_confirmPrefix, "/"), "/");
			_confirmPattern = Pattern.compile("^" + Pattern.quote(_confirmPrefix) + "([^/?]+)/?$");
		}
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
			if (!confirm(request, response, path)) {
				chain.doFilter(request, response);
			}
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
		} else if (channel.isConfigured(new Params(prefs.getParams()))) {
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

	protected boolean confirm(HttpServletRequest request, HttpServletResponse response, String path)
			throws IOException, ServletException {
		Matcher m = _confirmPattern.matcher(path);
		if (!m.matches()) {
			return false;
		}

		String key = decode(m.group(1));

		String confirmPath;
		try {
			confirmPath = _confirmationService.confirm(key);
			if (confirmPath != null) {
				response.sendRedirect(confirmPath);
			} else {
				// handled, but not a valid confirmation
				response.sendError(404);
			}
			return true;
		} catch (ConfirmationException e) {
			// not handled by confirmation service, follow filter chain
			return false;
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

	private IPullChannel getChannel(String channelId) {
		return _channels.get(channelId);
	}

	@Override
	public String toPullPath(String channelId, String userId, IParams params) {
		IPullChannel channel = getChannel(channelId);
		if (channel == null) {
			return null;
		}

		IChannelPreferences cPrefs = channel.newDefaultPreferences();

		IParams fullParams = new Params().setAll(cPrefs.getParams()).setAll(params);

		StringBuilder buf = new StringBuilder(_baseUrl);
		buf.append(_mountPath).append(_pullPrefix).append(channelId).append("/").append(userId);
		buf.append("?");
		for (final ParamValue pv : fullParams) {
			final String encodedName = encode(pv.getName());
			final String value = pv.getStringValue();
			final String encodedValue = value != null ? encode(value) : "";

			buf.append(encodedName);
			buf.append("=");
			buf.append(encodedValue);
			buf.append("&");
		}
		buf.setLength(buf.length() - 1);
		return buf.toString();
	}

	@Override
	public String toConfirmPath(Confirmation confirmation) {
		return _baseUrl + _mountPath + _confirmPrefix + confirmation.getKey();
	}

	public String getBaseUrl() {
		return _baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		_baseUrl = baseUrl;
	}

	public String getBaseUrlProperty() {
		return _baseUrlProperty;
	}

	public void setBaseUrlProperty(String baseUrlProperty) {
		_baseUrlProperty = baseUrlProperty;
	}

	public String getMountPath() {
		return _mountPath;
	}

	public void setMountPath(String mountPath) {
		_mountPath = mountPath;
	}

	public String getPullPrefix() {
		return _pullPrefix;
	}

	public void setPullPrefix(String pullPrefix) {
		_pullPrefix = pullPrefix;
	}

	public String getConfirmPrefix() {
		return _confirmPrefix;
	}

	public void setConfirmPrefix(String confirmPrefix) {
		_confirmPrefix = confirmPrefix;
	}

	public void setChannels(Set<IPullChannel> channels) {
		_channels.clear();
		for (IPullChannel channel : channels) {
			_channels.put(channel.getId(), channel);
		}
	}

	public void setConfirmationService(IConfirmationService confirmationService) {
		_confirmationService = confirmationService;
	}

}
