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

package at.molindo.notify.dispatch;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import at.molindo.notify.INotificationService;
import at.molindo.notify.INotificationService.IErrorListener;
import at.molindo.notify.INotificationService.NotifyException;
import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.channel.IPushChannel.PushException;
import at.molindo.notify.dao.INotificationDAO;
import at.molindo.notify.dao.IPreferencesDAO;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.notify.model.PushChannelPreferences.Frequency;
import at.molindo.notify.model.PushState;
import at.molindo.notify.render.IRenderService;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.util.NotifyUtils;

public class PollingPushDispatcher implements IPushDispatcher,
		InitializingBean, DisposableBean {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(PollingPushDispatcher.class);

	private static final int DEFAULT_POOL_SIZE = 1;
	private static final int DEFAULT_MAX_ERROR = 3;

	private int _poolSize = DEFAULT_POOL_SIZE;

	private INotificationService _notificationService;
	private IRenderService _renderService;

	private INotificationDAO _notificationDAO;
	private IPreferencesDAO _preferencesDAO;

	private Set<IPushChannel> _pushChannels = new CopyOnWriteArraySet<IPushChannel>();

	private ThreadPoolExecutor _executor;
	private final Object _wait = new Object();

	private IErrorListener _errorListener;

	private int _maxErrorCount = DEFAULT_MAX_ERROR;

	enum PushResult {
		SUCCESS, TEMPORARY_ERROR, PERSISTENT_ERROR;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (_pushChannels.size() == 0) {
			throw new IllegalStateException("no push channels configured");
		}
		if (_renderService == null) {
			throw new IllegalStateException("no renderService configured");
		}
		if (_notificationDAO == null) {
			throw new IllegalStateException("no notificationDAO configured");
		}
		if (_preferencesDAO == null) {
			throw new IllegalStateException("no preferencesDAO configured");
		}
		
		_executor = new ThreadPoolExecutor(_poolSize, _poolSize, 3,
				TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1)) {

					@Override
					protected void afterExecute(Runnable r, Throwable t) {
						if (!isShutdown()) {
							execute(new Polling());
						}
					}
			
		};
		for (int i = 0; i < _poolSize; i++) {
			_executor.execute(new Polling());
		}
		
		_notificationService.addNotificationListener(this);
	}

	@Override
	public void destroy() {
		_notificationService.removeNotificationListener(this);
		
		_executor.shutdown();

		synchronized (_wait) {
			_wait.notifyAll();
		}

		if (_executor.isTerminating()) {
			log.info("waiting for termination of running notification tasks");
			try {
				if (_executor.awaitTermination(30, TimeUnit.SECONDS)) {
					log.info("all running notification tasks terminated");
				} else {
					log.warn("still running notification taks");
				}
			} catch (InterruptedException e) {
				log.warn("interrupted while waiting for termination of notificaiton tasks");
			}
		}
	}

	@Override
	public void notification(Notification notification) {
		synchronized (_wait) {
			_wait.notify();
		}
	}

	@Override
	public void dispatchNow(Notification notification) throws NotifyException {
		if (push(new DispatchConf(notification, true)) != PushResult.SUCCESS) {
			throw new NotifyException("failed to dispatch now: " + notification);
		}
	}

	@Override
	public void setErrorListener(IErrorListener errorListener) {
		_errorListener = errorListener;
	}

	PushResult push(DispatchConf dc) {
		Preferences prefs = _preferencesDAO.getPreferences(dc.notification
				.getUserId());
		if (prefs == null) {
			log.warn("can't push to unknown user "
					+ dc.notification.getUserId());
			return PushResult.PERSISTENT_ERROR;
		}

		PushResult result = PushResult.PERSISTENT_ERROR;
		for (IPushChannel channel : _pushChannels) {
			PushChannelPreferences cPrefs = prefs.getChannelPrefs().get(
					channel.getId());

			if (isAllowed(dc, channel, cPrefs, Frequency.INSTANT)) {
				try {

					channel.push(NotifyUtils.render(_renderService,
							dc.notification, prefs, cPrefs), cPrefs);
					result = PushResult.SUCCESS;
				} catch (PushException e) {
					if (e.isTemporaryError()) {
						result = PushResult.TEMPORARY_ERROR;
					}
					if (_errorListener != null) {
						_errorListener.error(dc.notification, channel, e);
					} else {
						log.warn(
								"failed to deliver notification "
										+ dc.notification + " on channel "
										+ channel.getId(), e);
					}
				} catch (RenderException e) {
					log.error("failed to render notification "
							+ dc.notification, e);
				}
			}
		}

		return result;
	}

	boolean isAllowed(DispatchConf dc, IPushChannel channel,
			PushChannelPreferences prefs, Frequency frequency) {

		if (prefs == null) {
			// no preferences for this channel
			return false;
		}

		if (!dc.instant && !frequency.equals(prefs.getFrequency())) {
			return false;
		}

		if (!channel.isConfigured(dc.notification.getUserId(), prefs)) {
			// prefs not complete, e.g. recipient address missing
			return false;
		}

		if (!channel.getNotificationTypes().contains(dc.notification.getType())) {
			// channel not applicable for type
			return false;
		}

		return true;
	}

	public int getPoolSize() {
		return _poolSize;
	}

	public void setPoolSize(int poolSize) {
		_poolSize = poolSize;
	}

	public void setRenderService(IRenderService renderService) {
		_renderService = renderService;
	}

	public void setNotificationDAO(INotificationDAO notificationDAO) {
		_notificationDAO = notificationDAO;
	}

	public void setPreferencesDAO(IPreferencesDAO preferencesDAO) {
		_preferencesDAO = preferencesDAO;
	}

	public void setPushChannels(Set<IPushChannel> pushChannels) {
		_pushChannels = pushChannels;
	}

	class Polling implements Runnable {
		@Override
		public void run() {
			// FIXME don't push notification with multiple threads at once!!
			Notification notification = _notificationDAO.getNext();
			if (notification != null) {
				recordPushAttempt(notification, push(new DispatchConf(
						notification)));
			} else {
				// add a polling delay
				delay();
			}
		}

		/**
		 * overwrite for testing
		 */
		protected void delay() {
			synchronized (_wait) {
				try {
					_wait.wait(TimeUnit.MINUTES.toMillis(3));
				} catch (InterruptedException e) {
					log.debug("polling thread interrupted", e);
				}
			}
		}

	}

	static class DispatchConf {

		final Notification notification;
		final boolean instant;

		DispatchConf(Notification notification) {
			this(notification, false);
		}

		DispatchConf(Notification notification, boolean instant) {
			if (notification == null) {
				throw new NullPointerException("notification");
			}
			this.notification = notification;
			this.instant = instant;
		}

	}

	public void recordPushAttempt(Notification notification, PushResult result) {

		if (result == PushResult.SUCCESS) {
			notification.setPushState(PushState.PUSHED);
			notification.setPushDate(new Date());
		} else {
			notification.setPushDate(null);
			int errorCount = notification.recordPushError();

			if (errorCount > _maxErrorCount
					|| result == PushResult.PERSISTENT_ERROR) {
				notification.setPushState(PushState.UNDELIVERABLE);
				notification.setPushScheduled(null);
			} else {
				notification.setPushState(PushState.QUEUED);
				notification.setPushScheduled(new Date(System
						.currentTimeMillis() + waitAfter(errorCount)));
			}

		}

		_notificationDAO.update(notification);

	}

	private long waitAfter(final int errorCount) {
		switch (errorCount) {
		case 0:
			return 0;
		case 1:
			return 60000; // 60 seconds
		case 2:
			return 900000; // 15 minutes
		case 3:
			return 7200000; // 2 hours
		case 4:
			return 86400000; // 1 day
		default:
			return 259200000; // 3 days
		}
	}

	public void setMaxErrorCount(int maxErrorCount) {
		_maxErrorCount = maxErrorCount;
	}

	public void setNotificationService(INotificationService notificationService) {
		_notificationService = notificationService;
	}
}
