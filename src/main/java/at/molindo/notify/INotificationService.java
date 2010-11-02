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

import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.channel.IPushChannel.PushException;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Preferences;

public interface INotificationService {
	
	Preferences getPreferences(String userId);
	
	Preferences newPreferences(String userId);
	
	void setPreferences(Preferences prefs);
	
	void removePreferences(String userId);
	
	void notify(Notification notification);
	
	void notifyNow(Notification notification) throws NotifyException;
	
	void addErrorListener(IErrorListener listener);

	void removeErrorListener(IErrorListener listener);
	
	void addNotificationListener(INotificationListner listner);
	
	void removeNotificationListener(INotificationListner listner);
	
	public interface INotificationListner {
		void notification(Notification notification);
	}
	
	public interface IErrorListener {
		void error(Notification notification, IPushChannel channel, PushException e);
	}

	public static class NotifyException extends Exception {

		private static final long serialVersionUID = 1L;

		public NotifyException() {
			super();
		}

		public NotifyException(String message, Throwable cause) {
			super(message, cause);
		}

		public NotifyException(String message) {
			super(message);
		}

		public NotifyException(Throwable cause) {
			super(cause);
		}

	}
	
	public static class NotifyRuntimeException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public NotifyRuntimeException() {
			super();
		}

		public NotifyRuntimeException(String message, Throwable cause) {
			super(message, cause);
		}

		public NotifyRuntimeException(String message) {
			super(message);
		}

		public NotifyRuntimeException(Throwable cause) {
			super(cause);
		}

	}

}
