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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import at.molindo.notify.INotificationService.IErrorListener;
import at.molindo.notify.INotificationService.NotifyException;
import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.channel.IPushChannel.PushException;
import at.molindo.notify.dao.INotificationsDAO;
import at.molindo.notify.dao.IPreferencesDAO;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.notify.model.PushChannelPreferences.Frequency;
import at.molindo.notify.render.IRenderService;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.util.NotifyUtils;
import at.molindo.utils.concurrent.FactoryBlockingQueue;

public class PollingPushDispatcher implements IPushDispatcher,
		InitializingBean, DisposableBean {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(PollingPushDispatcher.class);

	private static final int DEFAULT_POOL_SIZE = 3;

	private int _poolSize = DEFAULT_POOL_SIZE;

	private IRenderService _renderService;
	
	private INotificationsDAO _notificationsDAO;
	private IPreferencesDAO _preferencesDAO;

	private Set<IPushChannel> _pushChannels = new CopyOnWriteArraySet<IPushChannel>();

	private ThreadPoolExecutor _executor;
	private final Object _wait = new Object();

	private IErrorListener _errorListener;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (_pushChannels.size() == 0) {
			throw new IllegalStateException("no push channels configured");
		}

		_executor = new ThreadPoolExecutor(_poolSize, _poolSize, 3,
				TimeUnit.MINUTES, new FactoryBlockingQueue<Runnable>() {

					@Override
					protected Runnable create() {
						return new Polling();
					}
				});
	}

	@Override
	public void destroy() {
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
		if (!push(new DispatchConf(notification, true))) {
			throw new NotifyException("failed to dispatch now: " + notification);
		}
	}

	@Override
	public void setErrorListener(IErrorListener errorListener) {
		_errorListener = errorListener;
	}

	boolean push(DispatchConf dc) {
		Preferences prefs = _preferencesDAO.getPreferences(dc.notification
				.getUserId());
		if (prefs == null) {
			log.warn("can't push to unknown user "
					+ dc.notification.getUserId());
			return false;
		}

		boolean dispatched = false;
		for (IPushChannel channel : _pushChannels) {
			PushChannelPreferences cPrefs = prefs.getChannelPrefs().get(channel.getId());
			
			if (isAllowed(dc, channel, cPrefs, Frequency.INSTANT)) {
				try {
					
					channel.push(NotifyUtils.render(_renderService, dc.notification, prefs, cPrefs), cPrefs);
					dispatched = true;
				} catch (PushException e) {
					if (_errorListener != null) {
						_errorListener.error(dc.notification, channel, e);
					} else {
						log.warn("failed to deliver notification " + dc.notification + " on channel " + channel.getId(), e);
					}
				} catch (RenderException e) {
					log.error("failed to render notification " + dc.notification, e);
				}
			}
		}
		return dispatched;
	}

	boolean isAllowed(DispatchConf dc,
			IPushChannel channel, PushChannelPreferences prefs,
			Frequency frequency) {

		if (prefs == null) {
			// no preferences for this channel
			return false;
		}

		if (!dc.instant && !frequency.equals(prefs.getFrequency())) {
			return false;
		}

		if (!channel.isConfigured(prefs)) {
			// prefs not complete, e.g. recipient address missing
			return false;
		}

		if (!channel.getNotificationTypes()
				.contains(dc.notification.getType())) {
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

	public void setNotificationsDAO(INotificationsDAO notificationsDAO) {
		_notificationsDAO = notificationsDAO;
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
			Notification notification = _notificationsDAO.getNext();
			if (notification != null) {
				push(new DispatchConf(notification));
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
}
