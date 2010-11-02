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

package at.molindo.notify;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.channel.IPushChannel.PushException;
import at.molindo.notify.dao.INotificationsDAO;
import at.molindo.notify.dao.IPreferencesDAO;
import at.molindo.notify.dispatch.IPushDispatcher;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Preferences;

public class NotificationService implements INotificationService, INotificationService.IErrorListener {

	private IPreferencesDAO _preferencesDAO;
	private INotificationsDAO _notificationDAO;
	
	private Set<IErrorListener> _errorListeners = new CopyOnWriteArraySet<IErrorListener>();
	private Set<INotificationListner> _notificationListeners = new CopyOnWriteArraySet<INotificationListner>();

	private IPushDispatcher _instantDispatcher;
	
	private Preferences _defaultPreferences;
	
	@Override
	public Preferences getPreferences(String userId) {
		return _preferencesDAO.getPreferences(userId);
	}

	@Override
	public Preferences newPreferences(String userId) {
		Preferences p = _defaultPreferences.clone();
		p.setUserId(userId);
		return p;
	}

	@Override
	public void setPreferences(Preferences prefs) {
		_preferencesDAO.savePreferences(prefs);
	}

	@Override
	public void removePreferences(String userId) {
		_preferencesDAO.removePreferences(userId);
	}

	@Override
	public void notify(Notification notification) {
		_notificationDAO.save(notification);
		for (INotificationListner l : _notificationListeners) {
			l.notification(notification);
		}
	}
	
	@Override
	public void notifyNow(Notification notification) throws NotifyException {
		_instantDispatcher.dispatchNow(notification);
	}

	@Override
	public void error(Notification notification, IPushChannel channel, PushException e) {
		for (IErrorListener l : _errorListeners) {
			l.error(notification, channel, e);
		}
	}

	@Override
	public void addErrorListener(IErrorListener listener) {
		_errorListeners.add(listener);
	}

	@Override
	public void removeErrorListener(IErrorListener listener) {
		_errorListeners.remove(listener);
	}

	@Override
	public void addNotificationListener(INotificationListner listner) {
		_notificationListeners.add(listner);
	}

	@Override
	public void removeNotificationListener(INotificationListner listner) {
		_notificationListeners.remove(listner);
	}

	public void setInstantDispatcher(IPushDispatcher instantDispatcher) {
		_instantDispatcher = instantDispatcher;
	}

}
