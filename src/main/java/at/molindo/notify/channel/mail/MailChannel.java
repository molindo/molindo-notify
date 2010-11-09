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

import at.molindo.notify.INotificationService;
import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.model.ChannelPreferences;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.utils.net.DnsUtils;

import com.google.common.collect.ImmutableSet;

public class MailChannel implements IPushChannel, InitializingBean {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailChannel.class);

	public static final Param<String> RECIPIENT = new Param<String>("recipient", String.class);
	public static final Param<String> RECIPIENT_NAME = new Param<String>("name", String.class);

	public static final String CHANNEL_ID = INotificationService.MAIL_CHANNEL;

	private PushChannelPreferences _defaultPreferences;

	private IMailClient _mailClient;

	public static void setRecipient(PushChannelPreferences cPrefs, String recipient) {
		cPrefs.getParams().set(RECIPIENT, recipient);
	}

	public static void setRecipientName(PushChannelPreferences cPrefs, String recipientName) {
		cPrefs.getParams().set(RECIPIENT_NAME, recipientName);
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
		return INotificationService.MAIL_CHANNEL;
	}

	@Override
	public ImmutableSet<Type> getNotificationTypes() {
		return Type.TYPES_PRIVATE;
	}

	@Override
	public PushChannelPreferences newDefaultPreferences() {
		return _defaultPreferences != null ? _defaultPreferences.clone() : new PushChannelPreferences();
	}

	@Override
	public void push(Message message, PushChannelPreferences cPrefs) throws PushException {
		_mailClient.send(message, cPrefs);
	}

	@Override
	public boolean isConfigured(String userId, ChannelPreferences prefs) {
		return prefs.getParams().containsAll(RECIPIENT);
	}

	public PushChannelPreferences getDefaultPreferences() {
		return _defaultPreferences;
	}

	public void setDefaultPreferences(PushChannelPreferences defaultPreferences) {
		_defaultPreferences = defaultPreferences;
	}

	public IMailClient getMailClient() {
		return _mailClient;
	}

	public void setMailClient(IMailClient mailClient) {
		_mailClient = mailClient;
	}

}
