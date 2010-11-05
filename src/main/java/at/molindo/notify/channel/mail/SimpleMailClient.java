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

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;

import org.springframework.beans.factory.InitializingBean;

public class SimpleMailClient extends AbstractMailClient implements
		InitializingBean {

	// server config
	private String _server;
	private Integer _port;
	private String _user;
	private String _password;
	private Security _security = Security.NONE;

	private Session _smtpSession = null;

	public SimpleMailClient() {

	}

	public SimpleMailClient(String server, String from)
			throws AddressException, MailException {
		setServer(server);
		setFrom(from);
		init();
	}

	public SimpleMailClient init() throws MailException {
		if (_smtpSession == null) {
			_smtpSession = createSmtpSession();

			Transport t;
			try {
				t = _smtpSession.getTransport("smtp");
				t.connect();
			} catch (NoSuchProviderException e) {
				throw new RuntimeException("no SMTP provider?", e);
			} catch (MessagingException e) {
				throw new MailException("can't connect to SMTP server", e, true);
			}
		}
		return this;
	}

	protected Session createSmtpSession() {
		final Properties props = new Properties();
		props.setProperty("mail.smtp.host", _server);
		props.setProperty("mail.smtp.port", Integer
				.toString(_port != null ? _port : _security.getDefaultPort()));
		props.setProperty("mail.smtp.auth", Boolean.toString(_user != null));
		if (_security == Security.TLS) {
			props.setProperty("mail.smtp.starttls.enable", "true");
		} else if (_security == Security.SSL) {
			props.put("mail.smtp.ssl", "true");
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
		}
		// props.put("mail.debug", "true");
		return Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {

					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						if (_user != null) {
							return new PasswordAuthentication(_user, _password);
						} else {
							return null;
						}
					}
				});
	}

	@Override
	protected Session getSmtpSession(String recipient) {
		return _smtpSession;
	}

	public String getServer() {
		return _server;
	}

	public SimpleMailClient setServer(String server) {
		_server = server;
		return this;
	}

	public Integer getPort() {
		return _port;
	}

	public SimpleMailClient setPort(Integer port) {
		_port = port;
		return this;
	}

	public String getUser() {
		return _user;
	}

	public SimpleMailClient setUser(String user) {
		_user = user;
		return this;
	}

	public String getPassword() {
		return _password;
	}

	public SimpleMailClient setPassword(String password) {
		_password = password;
		return this;
	}

	public Security getSecurity() {
		return _security;
	}

	public SimpleMailClient setSecurity(Security security) {
		_security = security;
		return this;
	}

}
