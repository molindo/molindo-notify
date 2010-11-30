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
import at.molindo.notify.model.IChannelPreferences;
import at.molindo.notify.model.IParams;
import at.molindo.notify.model.IPushChannelPreferences;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.utils.net.DnsUtils;

import com.google.common.collect.ImmutableSet;

public class MailChannel implements IPushChannel, InitializingBean {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailChannel.class);

	public static final Param<String> RECIPIENT = Param.pString("recipient");
	public static final Param<String> RECIPIENT_NAME = Param.pString("name");

	public static final String CHANNEL_ID = INotifyService.MAIL_CHANNEL;

	private IPushChannelPreferences _defaultPreferences;

	private IMailClient _mailClient;

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
	public void push(Message message, IPushChannelPreferences cPrefs) throws PushException {
		try {
			_mailClient.send(message, cPrefs);

			if (log.isDebugEnabled()) {
				log.debug("successfully pushed mail:\n" + message);
			}
		} catch (PushException e) {
			if (log.isDebugEnabled()) {
				log.debug("failed to push mail:\n" + message, e);
			}
			throw e;
		}
	}

	@Override
	public boolean isConfigured(String userId, IChannelPreferences prefs) {
		return prefs.getParams().containsAll(RECIPIENT);
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

}
