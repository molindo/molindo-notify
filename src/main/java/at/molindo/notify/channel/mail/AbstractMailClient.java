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

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.InitializingBean;

import at.molindo.notify.model.Message;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.utils.io.CharsetUtils;

public abstract class AbstractMailClient implements IMailClient,
		InitializingBean {

	public enum Security {
		NONE(25), SSL(465), TLS(587);

		private int _defaultPort;

		private Security(int defaultPort) {
			_defaultPort = defaultPort;
		}

		public int getDefaultPort() {
			return _defaultPort;
		}

	}

	public enum Format {
		MULTI, HTML, TEXT;
	}

	// sender config
	private InternetAddress _from;
	private InternetAddress _replyTo;

	// message config
	private Format _format = Format.MULTI;

	@Override
	public final void afterPropertiesSet() throws Exception {
		init();
	}

	public AbstractMailClient init() throws MailException {
		return this;
	}

	@Override
	public synchronized void send(Message message, PushChannelPreferences cPrefs)
			throws MailException {

		String recipient = cPrefs.getParams().get(MailChannel.RECIPIENT);
		String recipientName = cPrefs.getParams().get(
				MailChannel.RECIPIENT_NAME);
		String subject = message.getSubject();

		try {
			MimeMessage mm = new MimeMessage(getSmtpSession(recipient)) {
				@Override
				protected void updateMessageID() throws MessagingException {
					String domain = _from.getAddress();
					int idx = _from.getAddress().indexOf('@');
					if (idx >= 0) {
						domain = domain.substring(idx + 1);
					}
					setHeader("Message-ID", "<" + UUID.randomUUID() + "@"
							+ domain + ">");
				}
			};
			mm.setFrom(_from);
			mm.setSender(_from);

			InternetAddress replyTo = getReplyTo();
			if (replyTo != null) {
				mm.setReplyTo(new Address[] { replyTo });
			}
			mm.setHeader("X-Mailer", "molindo-notify");
			mm.setSentDate(new Date());

			mm.setRecipient(RecipientType.TO, new InternetAddress(recipient,
					recipientName, CharsetUtils.UTF_8.displayName()));
			mm.setSubject(subject, CharsetUtils.UTF_8.displayName());

			switch (_format) {
			case HTML:
				mm.setText(message.getHtml(), CharsetUtils.UTF_8.displayName(),
						"html");
				break;
			case TEXT:
				mm.setText(message.getText(), CharsetUtils.UTF_8.displayName());
				break;
			case MULTI:
				MimeBodyPart html = new MimeBodyPart();
				html.setText(message.getHtml(),
						CharsetUtils.UTF_8.displayName(), "html");

				MimeBodyPart text = new MimeBodyPart();
				text.setText(message.getText(),
						CharsetUtils.UTF_8.displayName());

				/*
				 * The formats are ordered by how faithful they are to the
				 * original, with the least faithful first and the most faithful
				 * last. (http://en.wikipedia.org/wiki/MIME#Alternative)
				 */
				MimeMultipart mmp = new MimeMultipart();
				mmp.setSubType("alternative");

				mmp.addBodyPart(text);
				mmp.addBodyPart(html);

				mm.setContent(mmp);

				break;
			}

			send(mm);

		} catch (final MessagingException e) {
			throw new MailException("could not send mail to " + recipient, e,
					isTemporary(e));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("utf8 unknown?", e);
		}
	}

	protected void send(MimeMessage mm) throws MessagingException {
		Transport.send(mm);
	}

	private boolean isTemporary(MessagingException e) {
		return true;
	}

	protected abstract Session getSmtpSession(String recipient)
			throws MailException;

	public InternetAddress getFrom() {
		return _from;
	}

	public AbstractMailClient setFrom(InternetAddress from) {
		_from = from;
		return this;
	}

	public AbstractMailClient setFrom(String address) throws AddressException {
		return setFrom(new InternetAddress(address));
	}

	public AbstractMailClient setFrom(String address, String personal)
			throws AddressException {
		try {
			return setFrom(new InternetAddress(address, personal,
					CharsetUtils.UTF_8.displayName()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("utf8 not supported?", e);
		}
	}

	public InternetAddress getReplyTo() {
		return _replyTo;
	}

	public AbstractMailClient setReplyTo(InternetAddress replyTo) {
		_replyTo = replyTo;
		return this;
	}

	public AbstractMailClient setReplyTo(String address)
			throws AddressException {
		return setReplyTo(new InternetAddress(address));
	}

	public AbstractMailClient setReplyTo(String address, String personal)
			throws AddressException {
		try {
			return setReplyTo(new InternetAddress(address, personal,
					CharsetUtils.UTF_8.displayName()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("utf8 not supported?", e);
		}
	}

	public Format getFormat() {
		return _format;
	}

	public AbstractMailClient setFormat(Format format) {
		_format = format;
		return this;
	}
}
