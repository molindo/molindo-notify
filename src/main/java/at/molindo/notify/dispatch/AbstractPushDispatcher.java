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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.InitializingBean;

import at.molindo.notify.INotifyService;
import at.molindo.notify.INotifyService.IErrorListener;
import at.molindo.notify.INotifyService.NotifyException;
import at.molindo.notify.INotifyService.NotifyRuntimeException;
import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.channel.IPushChannel.PushException;
import at.molindo.notify.dao.INotificationDAO;
import at.molindo.notify.dao.IPreferencesDAO;
import at.molindo.notify.model.Dispatch;
import at.molindo.notify.model.IPreferences;
import at.molindo.notify.model.IPushChannelPreferences;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.model.PushChannelPreferences.Frequency;
import at.molindo.notify.model.PushState;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.utils.data.ExceptionUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class AbstractPushDispatcher implements IPushDispatcher, InitializingBean {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractPushDispatcher.class);

	private static final int DEFAULT_MAX_ERROR = 3;

	private Set<IPushChannel> _pushChannels = new CopyOnWriteArraySet<IPushChannel>();
	private IErrorListener _errorListener;

	private IDispatchService _dispatchService;
	private IPreferencesDAO _preferencesDAO;
	private INotificationDAO _notificationDAO;

	private int _maxErrorCount = DEFAULT_MAX_ERROR;

	@Override
	public void afterPropertiesSet() {
		if (_pushChannels.size() == 0) {
			throw new IllegalStateException("no push channels configured");
		}
		if (_dispatchService == null) {
			throw new IllegalStateException("no dispatchService configured");
		}
		if (_notificationDAO == null) {
			throw new IllegalStateException("no notificationDAO configured");
		}
	}

	@Nonnull
	private PushResultMessage push(@Nonnull Notification notification, boolean ignoreFrequency) {
		IPreferences prefs;

		final String unknownChannel = notification.getParams().get(INotifyService.NOTIFY_UNKNOWN);
		if (unknownChannel != null) {
			prefs = new Preferences().setUserId(notification.getUserId());
		} else {
			prefs = _preferencesDAO.getPreferences(notification.getUserId());
		}

		if (prefs == null) {
			log.warn("can't push to unknown user " + notification.getUserId());
			return PushResultMessage.persistent("unknown user " + notification.getUserId());
		}

		Set<String> successChannels = Sets.newHashSet();
		Map<String, String> temporaryChannels = Maps.newHashMap();
		Map<String, String> persistentChannels = Maps.newHashMap();

		Iterable<IPushChannel> channels = Iterables.filter(_pushChannels, new Predicate<IPushChannel>() {

			@Override
			public boolean apply(IPushChannel channel) {
				return unknownChannel == null || channel.getId().equals(unknownChannel);
			}
		});

		for (IPushChannel channel : channels) {
			try {
				pushChannel(channel, prefs, notification, ignoreFrequency);
				successChannels.add(channel.getId());
			} catch (PushException e) {
				if (e.isTemporaryError()) {
					temporaryChannels.put(channel.getId(), e.getMessage());
				} else {
					persistentChannels.put(channel.getId(), e.getMessage());
				}
				if (_errorListener != null) {
					_errorListener.error(notification, channel, e);
				} else {
					log.info("failed to deliver notification " + notification + " on channel " + channel.getId() + ": "
							+ ExceptionUtils.getAllMessages(e));
				}
			} catch (RenderException e) {
				log.error("failed to render notification " + notification, e);
				temporaryChannels.put(channel.getId(), ExceptionUtils.getAllMessages(e));
			}
		}

		if (successChannels.size() > 0) {
			return PushResultMessage.success("channels: " + successChannels);
		} else if (temporaryChannels.size() > 0) {
			return PushResultMessage.temporary("temporary error, channels: " + temporaryChannels);
		} else if (persistentChannels.size() > 0) {
			return PushResultMessage.persistent("persistent error, channels: " + persistentChannels);
		} else {
			return PushResultMessage.temporary("no allowed channels available");
		}
	}

	private void pushChannel(IPushChannel channel, IPreferences prefs, Notification notification,
			boolean ignoreFrequency) throws PushException, RenderException {

		IPushChannelPreferences cPrefs = prefs.getChannelPrefs().get(channel.getId());
		if (cPrefs == null) {
			cPrefs = channel.newDefaultPreferences();
			if (cPrefs == null) {
				// don't flood user after he configures this channel
				throw new PushException("channel not configured for user", false);
			}
		}

		if (!channel.getNotificationTypes().contains(notification.getType())) {
			// channel not applicable for type
			throw new PushException("channel not applicable for type " + notification.getType(), false);
		}

		Dispatch dispatch = _dispatchService.create(notification, prefs, cPrefs);

		if (!channel.isConfigured(dispatch.getParams())) {
			// don't flood user after he configures this channel
			throw new PushException("channel not configured for user", false);
		}

		if (!ignoreFrequency && !Frequency.INSTANT.equals(cPrefs.getFrequency())) {
			// temporary as other dispatcher will handle this
			throw new PushException("channel not configured for this frequency", true);
		}

		channel.push(dispatch);
	}

	@Override
	public void dispatchNow(Notification notification) throws NotifyException {
		dispatch(notification, true);
	}

	protected void dispatch(@Nonnull Notification notification) {
		try {
			dispatch(notification, false);
		} catch (NotifyException e) {
			throw new NotifyRuntimeException("unexpected NotifyException", e);
		}
	}

	/**
	 *
	 * @param notification
	 * @param ignoreFrequency
	 * @throws NotifyException
	 *             only if ignoreFrequency is true
	 */
	private void dispatch(Notification notification, boolean ignoreFrequency) throws NotifyException {
		PushResultMessage rm = push(notification, ignoreFrequency);

		if (ignoreFrequency && rm.getResult() != PushResult.SUCCESS) {
			// only record success of dispatchNow as failed notifications must
			// not be stored for later use
			throw new NotifyException("failed to dispatch now: " + notification + " (" + rm.getMessage() + ")");
		} else {
			recordPushAttempt(notification, rm);
		}
	}

	private void recordPushAttempt(Notification notification, @Nonnull PushResultMessage rm) {

		if (rm.getResult() == PushResult.SUCCESS) {
			notification.setPushState(PushState.PUSHED);
			notification.setPushDate(new Date());
			notification.setPushErrorMessage(rm.getMessage());
		} else {
			int errorCount = notification.recordPushError(rm.getMessage());

			if (errorCount > _maxErrorCount || rm.getResult() == PushResult.PERSISTENT_ERROR) {
				notification.setPushState(PushState.UNDELIVERABLE);
				notification.setPushDate(new Date());
			} else {
				notification.setPushState(PushState.QUEUED);
				notification.setPushDate(new Date(System.currentTimeMillis() + waitAfter(errorCount)));
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

	@Override
	public void setErrorListener(IErrorListener errorListener) {
		_errorListener = errorListener;
	}

	@Override
	public Map<String, IPushChannelPreferences> newDefaultPreferences() {
		Map<String, IPushChannelPreferences> map = Maps.newHashMap();
		for (IPushChannel channel : _pushChannels) {
			IPushChannelPreferences cPrefs = channel.newDefaultPreferences();
			if (cPrefs != null) {
				map.put(channel.getId(), cPrefs);
			}
		}
		return map;
	}

	public void setDispatchService(IDispatchService dispatchService) {
		_dispatchService = dispatchService;
	}

	public void setPreferencesDAO(IPreferencesDAO preferencesDAO) {
		_preferencesDAO = preferencesDAO;
	}

	public void setPushChannels(Set<IPushChannel> pushChannels) {
		_pushChannels = pushChannels;
	}

	public void setMaxErrorCount(int maxErrorCount) {
		_maxErrorCount = maxErrorCount;
	}

	public void setNotificationDAO(INotificationDAO notificationDAO) {
		_notificationDAO = notificationDAO;
	}

	protected Set<IPushChannel> getPushChannels() {
		return _pushChannels;
	}

	protected IErrorListener getErrorListener() {
		return _errorListener;
	}

	protected IDispatchService getDispatchService() {
		return _dispatchService;
	}

	protected IPreferencesDAO getPreferencesDAO() {
		return _preferencesDAO;
	}

	protected INotificationDAO getNotificationDAO() {
		return _notificationDAO;
	}

	protected int getMaxErrorCount() {
		return _maxErrorCount;
	}

	protected enum PushResult {
		SUCCESS, TEMPORARY_ERROR, PERSISTENT_ERROR;
	}

	protected static class PushResultMessage {

		private static PushResultMessage success(String message) {
			return new PushResultMessage(message, PushResult.SUCCESS);
		}

		private static PushResultMessage persistent(String message) {
			return new PushResultMessage(message, PushResult.PERSISTENT_ERROR);
		}

		private static PushResultMessage temporary(String message) {
			return new PushResultMessage(message, PushResult.TEMPORARY_ERROR);
		}

		private final String _message;
		private final PushResult _result;

		private PushResultMessage(String message, PushResult result) {
			if (result == null) {
				throw new NullPointerException("result");
			}

			_message = message;
			_result = result;
		}

		public String getMessage() {
			return _message;
		}

		public PushResult getResult() {
			return _result;
		}

		@Override
		public String toString() {
			return _result + " (" + _message + ")";
		}

	}
}
