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

import org.springframework.beans.factory.InitializingBean;

import at.molindo.notify.INotifyService;
import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.model.Dispatch;
import at.molindo.notify.model.IParams;
import at.molindo.notify.model.IPushChannelPreferences;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.utils.net.DnsUtils;

import com.google.common.collect.ImmutableSet;

public class MailChannel implements IPushChannel, InitializingBean {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailChannel.class);

	public static final Param<String> RECIPIENT = Param.pString("recipient");
	public static final Param<String> RECIPIENT_NAME = Param.pString("name");

	public static final String CHANNEL_ID = INotifyService.MAIL_CHANNEL;

	private IMailFilter _mailFilter;

	private IPushChannelPreferences _defaultPreferences;

	private IMailClient _mailClient;

	private boolean _disabled = false;

	public static void setRecipient(IPushChannelPreferences cPrefs, String recipient) {
		setRecipient(cPrefs.getParams(), recipient);
	}

	public static void setRecipientName(IPushChannelPreferences cPrefs, String recipientName) {
		setRecipientName(cPrefs.getParams(), recipientName);
	}

	public static void setRecipient(IParams params, String recipient) {
		params.set(RECIPIENT, recipient);
	}

	public static void setRecipientName(IParams params, String recipientName) {
		params.set(RECIPIENT_NAME, recipientName);
	}

	public static String getRecipient(IPushChannelPreferences cPrefs) {
		return getRecipient(cPrefs.getParams());
	}

	public static String getRecipientName(IPushChannelPreferences cPrefs) {
		return getRecipientName(cPrefs.getParams());
	}

	public static String getRecipient(IParams params) {
		return params.get(RECIPIENT);
	}

	public static String getRecipientName(IParams params) {
		return params.get(RECIPIENT_NAME);
	}

	public MailChannel() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// bad hostname increases spam probability

		final String localHostName = DnsUtils.getLocalHostName();
		if (localHostName == null || localHostName.indexOf('.') < 0) {
			log.warn("hostname of localhost seems not to be correct: " + localHostName);
		} else {
			log.info("hostname of localhost seems to be correct: " + localHostName);
		}

		if (_mailClient == null) {
			throw new Exception("mailClient not configured");
		}
	}

	@Override
	public String getId() {
		return INotifyService.MAIL_CHANNEL;
	}

	@Override
	public ImmutableSet<Type> getNotificationTypes() {
		return Type.TYPES_PRIVATE;
	}

	@Override
	public IPushChannelPreferences newDefaultPreferences() {
		return _defaultPreferences != null ? _defaultPreferences.clone() : new PushChannelPreferences();
	}

	@Override
	public void push(Dispatch dispatch) throws PushException {
		if (isDisabled()) {
			throw new PushException("channel is disabled", true);
		}

		try {
			_mailClient.send(dispatch);

			if (log.isTraceEnabled()) {
				log.trace("successfully pushed mail:\n" + dispatch.getMessage());
			}
		} catch (PushException e) {
			if (log.isTraceEnabled()) {
				log.trace("failed to push mail:\n" + dispatch.getMessage(), e);
			}
			throw e;
		}
	}

	@Override
	public boolean isConfigured(Params params) {
		if (_mailFilter != null && !_mailFilter.isAllowed(params)) {
			return false;
		}

		return params.containsAll(RECIPIENT);
	}

	public IPushChannelPreferences getDefaultPreferences() {
		return _defaultPreferences;
	}

	public void setDefaultPreferences(IPushChannelPreferences defaultPreferences) {
		_defaultPreferences = defaultPreferences;
	}

	public IMailClient getMailClient() {
		return _mailClient;
	}

	public void setMailClient(IMailClient mailClient) {
		_mailClient = mailClient;
	}

	public boolean isDisabled() {
		return _disabled;
	}

	public void setDisabled(boolean disabled) {
		_disabled = disabled;
	}

	public void setMailFilter(IMailFilter mailFilter) {
		_mailFilter = mailFilter;
	}

}
