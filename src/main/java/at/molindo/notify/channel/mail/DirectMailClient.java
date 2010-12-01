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

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Session;
import javax.naming.NamingException;

import org.springframework.beans.factory.InitializingBean;

import at.molindo.utils.net.DnsUtils;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class DirectMailClient extends AbstractMailClient implements InitializingBean {

	private static final int DEFAULT_CACHE_CONCURRENCY = 4;
	private static final long DEFAULT_CACHE_EXPIRATION_MIN = 10;

	private static final String CONNECTION_TIMEOUT_MS = "60000";
	private static final String READ_TIMEOUT_MS = "60000";

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
							throw new WrapException(e);
						}
					}
				});
		return this;
	}

	@Override
	protected Session getSmtpSession(String recipient) throws MailException {
		try {
			return _sessionCache.get(MailUtils.domainFromAddress(recipient));
		} catch (WrapException e) {
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
			throw new MailException("can't lookup mail host", e, true);
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

	private static class WrapException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public WrapException(MailException e) {
			super(e);
		}

	}
}
