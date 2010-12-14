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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.springframework.beans.factory.DisposableBean;

import at.molindo.notify.INotifyService;
import at.molindo.notify.INotifyService.NotifyRuntimeException;
import at.molindo.notify.model.Notification;
import at.molindo.utils.concurrent.FactoryThread;
import at.molindo.utils.concurrent.FactoryThread.FactoryThreadGroup;
import at.molindo.utils.concurrent.KeyLock;

public class PollingPushDispatcher extends AbstractPushDispatcher implements INotifyService.INotificationListner,
		DisposableBean {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PollingPushDispatcher.class);

	private static final int DEFAULT_POOL_SIZE = 1;

	private int _poolSize = DEFAULT_POOL_SIZE;

	private INotifyService _notifyService;

	private final Object _wait = new Object();
	private final KeyLock<Long, Void> _notificationLock = KeyLock.newKeyLock();

	private FactoryThreadGroup _threadGroup;

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		_threadGroup = new FactoryThread.FactoryThreadGroup(PollingPushDispatcher.class.getSimpleName(), _poolSize,
				new FactoryThread.IRunnableFactory() {

					@Override
					public Runnable newRunnable() {
						return new Polling();
					}
				}).start();

		_notifyService.addNotificationListener(this);
	}

	@Override
	public void destroy() {
		_notifyService.removeNotificationListener(this);

		_threadGroup.setInactive();
		synchronized (_wait) {
			_wait.notifyAll();
		}
		try {
			log.info("waiting for termination of running notification tasks");
			_threadGroup.join();
			log.info("all running notification tasks terminated");
		} catch (InterruptedException e1) {
			log.warn("interrupted while waiting for termination of notificaiton tasks");
		}

	}

	@Override
	public void notification(Notification notification) {
		synchronized (_wait) {
			_wait.notify();
		}
	}

	public void setNotifyService(INotifyService notifyService) {
		_notifyService = notifyService;
	}

	public int getPoolSize() {
		return _poolSize;
	}

	public void setPoolSize(int poolSize) {
		_poolSize = poolSize;
	}

	class Polling implements Runnable {
		@Override
		public void run() {
			Notification notification = getNotificationDAO().getNext();
			if (notification != null) {
				doPush(notification);
			} else {
				// add a polling delay
				delay();
			}
		}

		/**
		 * wraps a {@link KeyLock} around {@link #push(DispatchConf)}
		 * 
		 * @see #doPush(DispatchConf)
		 * @return null if notification already gets pushed by another thread
		 */
		@CheckForNull
		private void doPush(final @Nonnull Notification notification) {
			try {
				_notificationLock.withLock(notification.getId(), new Callable<Void>() {

					@Override
					public Void call() {
						dispatch(notification);
						return null;
					}
				});
			} catch (Exception e) {
				throw new NotifyRuntimeException("unexepcted exception from doPush()", e);
			}
		}

		/**
		 * overwrite for testing
		 */
		protected void delay() {
			synchronized (_wait) {
				try {
					_wait.wait(TimeUnit.SECONDS.toMillis(20));
				} catch (InterruptedException e) {
					log.debug("polling thread interrupted", e);
				}
			}
		}

	}

}
