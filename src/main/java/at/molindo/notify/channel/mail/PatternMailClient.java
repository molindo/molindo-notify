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

package at.molindo.notify.channel.mail;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;

import at.molindo.notify.model.Dispatch;
import at.molindo.utils.data.StringUtils;

public class PatternMailClient implements IMailClient, InitializingBean {

	private IMailClient _defaultClient;
	private IMailClient _alternativeClient;

	private Pattern[] _clientPatterns = new Pattern[0];

	@Override
	public void afterPropertiesSet() throws Exception {
		if (_defaultClient == null) {
			throw new IllegalStateException("defaultClient not configured");
		}
		if (_clientPatterns.length > 0 && _alternativeClient == null) {
			throw new IllegalStateException("clientPatterns but no alternativeClient configured");
		}
	}

	@Override
	public void send(Dispatch dispatch) throws MailException {
		getClient(getServer(dispatch.getParams().get(MailChannel.RECIPIENT))).send(dispatch);
	}

	String getServer(String recipient) {
		return StringUtils.empty(recipient) ? "" : StringUtils.afterLast(recipient, "@");
	}

	IMailClient getClient(String server) {
		if (!StringUtils.empty(server)) {
			for (final Pattern p : _clientPatterns) {
				if (p.matcher(server).find()) {
					return _alternativeClient;
				}
			}
		}
		return _defaultClient;
	}

	public void setClientPatterns(final List<String> smartClientPatterns) {
		_clientPatterns = new Pattern[smartClientPatterns.size()];
		for (int i = 0; i < _clientPatterns.length; i++) {
			_clientPatterns[i] = Pattern.compile(smartClientPatterns.get(i), Pattern.CASE_INSENSITIVE);
		}
	}

	public void setDefaultClient(IMailClient defaultClient) {
		_defaultClient = defaultClient;
	}

	public void setAlternativeClient(IMailClient alternativeClient) {
		_alternativeClient = alternativeClient;
	}

	protected IMailClient getDefaultClient() {
		return _defaultClient;
	}

	protected IMailClient getAlternativeClient() {
		return _alternativeClient;
	}

}
