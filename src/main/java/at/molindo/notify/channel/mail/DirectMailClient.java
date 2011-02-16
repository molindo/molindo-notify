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

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.naming.NamingException;

import org.springframework.beans.factory.InitializingBean;

import at.molindo.utils.collections.CollectionUtils;
import at.molindo.utils.data.ExceptionUtils;
import at.molindo.utils.net.DnsUtils;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPSendFailedException;

public class DirectMailClient extends AbstractMailClient implements InitializingBean {

	private static final int DEFAULT_CACHE_CONCURRENCY = 4;
	private static final long DEFAULT_CACHE_EXPIRATION_MIN = 10;

	private static final String CONNECTION_TIMEOUT_MS = "60000";
	private static final String READ_TIMEOUT_MS = "60000";

	// permanent errors
	private static final int MAILBOX_UNAVAILABLE = 550;
	private static final int MAILBOX_NOT_LOCAL = 551;
	private static final int MAILBOX_NAME_NOT_ALLOWED = 553;
	private static final int TRANSACTION_FAILED = 554;
	private static final Set<Integer> PERMANENT_ERROR_CODES = Collections.unmodifiableSet(CollectionUtils.set(
			MAILBOX_UNAVAILABLE, MAILBOX_NOT_LOCAL, MAILBOX_NAME_NOT_ALLOWED, TRANSACTION_FAILED));

	private Map<String, Session> _sessionCache;
	private int _cacheConcurrency = DEFAULT_CACHE_CONCURRENCY;
	private long _cacheExpirationMin = DEFAULT_CACHE_EXPIRATION_MIN;
	private String _localAddress;
	private String _socksProxyHost;
	private String _socksProxyPort;
	private Boolean _proxySet;
	private String _localHost;

	@Override
	public DirectMailClient init() throws MailException {
		super.init();
		_sessionCache = new MapMaker().concurrencyLevel(_cacheConcurrency)
				.expiration(_cacheExpirationMin, TimeUnit.MINUTES).makeComputingMap(new Function<String, Session>() {
					@Override
					public Session apply(String domain) {
						try {
							return createSmtpSession(domain);
						} catch (MailException e) {
							throw new ComputationException(e);
						}
					}
				});
		return this;
	}

	@Override
	protected Session getSmtpSession(String recipient) throws MailException {
		try {
			return _sessionCache.get(MailUtils.domainFromAddress(recipient));
		} catch (ComputationException e) {
			throw (MailException) e.getCause();
		}
	}

	protected Session createSmtpSession(String domain) throws MailException {
		try {
			final Properties props = new Properties();
			props.setProperty("mail.smtp.host", DnsUtils.lookupMailHosts(domain)[0]);
			props.setProperty("mail.smtp.port", "25");
			props.setProperty("mail.smtp.auth", "false");
			props.setProperty("mail.smtp.starttls.enable", "true");

			// set proxy
			if (getProxySet() != null && getProxySet()) {
				props.setProperty("proxySet", "true");
				props.setProperty("socksProxyHost", getSocksProxyHost());
				props.setProperty("socksProxyPort", getSocksProxyPort());
			}

			if (getLocalHost() != null) {
				props.setProperty("mail.smtp.localhost", getLocalHost());
			}
			if (getLocalAddress() != null) {
				props.setProperty("mail.smtp.localaddress", getLocalAddress());
			}

			props.setProperty("mail.smtp.connectiontimeout", CONNECTION_TIMEOUT_MS);
			props.setProperty("mail.smtp.timeout", READ_TIMEOUT_MS);

			// props.put("mail.debug", "true");
			return Session.getInstance(props);
		} catch (NamingException e) {
			throw new MailException("can't lookup mail host: " + domain, e, true);
		}
	}

	@Override
	protected String toErrorMessage(MessagingException e) {
		if (e instanceof SendFailedException) {
			if (e.getNextException() instanceof SMTPSendFailedException) {
				final SMTPSendFailedException se = (SMTPSendFailedException) e.getNextException();
				return se.getCommand() + " failed " + " with " + se.getReturnCode() + " (" + e.getMessage() + ")";
			} else if (e.getNextException() instanceof SMTPAddressFailedException) {
				// copied from above, as there is no common base class but same
				// methods
				final SMTPAddressFailedException se = (SMTPAddressFailedException) e.getNextException();
				return se.getCommand() + " failed " + " with " + se.getReturnCode() + " (" + e.getMessage() + ")";
			} else {
				final StringBuilder buf = new StringBuilder();
				Address[] addresses = ((SendFailedException) e).getInvalidAddresses();
				if (addresses != null) {
					for (final Address a : addresses) {
						buf.append(a).append(" ");
					}
				}
				return "invalied address(es): " + buf + "(" + ExceptionUtils.getAllMessages(e) + ")";
			}
		} else {
			return super.toErrorMessage(e);
		}
	}

	@Override
	protected boolean isTemporary(MessagingException e) {
		if (e instanceof SendFailedException) {
			if (e.getNextException() instanceof SMTPSendFailedException) {
				final SMTPSendFailedException se = (SMTPSendFailedException) e.getNextException();
				final int rc = se.getReturnCode();
				return !PERMANENT_ERROR_CODES.contains(rc);
			} else if (e.getNextException() instanceof SMTPAddressFailedException) {
				// copied from above, as there is no common base class but same
				// methods
				final SMTPAddressFailedException se = (SMTPAddressFailedException) e.getNextException();
				final int rc = se.getReturnCode();
				return !PERMANENT_ERROR_CODES.contains(rc);
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	public int getCacheConcurrency() {
		return _cacheConcurrency;
	}

	public void setCacheConcurrency(int cacheConcurrency) {
		_cacheConcurrency = cacheConcurrency;
	}

	public long getCacheExpirationMin() {
		return _cacheExpirationMin;
	}

	public void setCacheExpirationMin(long cacheExpirationMin) {
		_cacheExpirationMin = cacheExpirationMin;
	}

	public void setLocalAddress(final String localAddress) {
		_localAddress = localAddress;
	}

	public void setLocalHost(final String localHost) {
		_localHost = localHost;
	}

	private String getLocalAddress() {
		return _localAddress;
	}

	private String getLocalHost() {
		return _localHost;
	}

	public void setSocksProxyHost(final String socksProxyHost) {
		_socksProxyHost = socksProxyHost;
	}

	public void setSocksProxyPort(final String socksProxyPort) {
		_socksProxyPort = socksProxyPort;
	}

	public void setProxySet(final Boolean proxySet) {
		_proxySet = proxySet;
	}

	private String getSocksProxyHost() {
		return _socksProxyHost;
	}

	private String getSocksProxyPort() {
		return _socksProxyPort;
	}

	private Boolean getProxySet() {
		return _proxySet;
	}
}
