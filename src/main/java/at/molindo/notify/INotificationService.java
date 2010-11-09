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

import javax.annotation.Nonnull;

import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.channel.IPushChannel.PushException;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Preferences;

public interface INotificationService {

	public static final String MAIL_CHANNEL = "mail";
	public static final String PRIVATE_FEED_CHANNEL = "private-feed";
	public static final String PUBLIC_FEED_CHANNEL = "public-feed";

	Preferences getPreferences(@Nonnull String userId);

	@Nonnull Preferences newPreferences(@Nonnull String userId);

	void setPreferences(@Nonnull Preferences prefs);

	void removePreferences(@Nonnull String userId);

	void notify(@Nonnull Notification notification);

	void notifyNow(@Nonnull Notification notification) throws NotifyException;

	void confirm(@Nonnull Notification notification);

	void confirmNow(@Nonnull Notification notification) throws NotifyException;

	void addErrorListener(@Nonnull IErrorListener listener);

	void removeErrorListener(@Nonnull IErrorListener listener);

	void addNotificationListener(@Nonnull INotificationListner listner);

	void removeNotificationListener(@Nonnull INotificationListner listner);

	public interface INotificationListner {
		void notification(@Nonnull Notification notification);
	}

	public interface IConfirmationListener {
		void confirm(@Nonnull Notification notification);
	}

	public interface IErrorListener {

		/**
		 * called if notification can't be pushed to any channel
		 * 
		 * @param notification
		 * @param channel
		 * @param e
		 */
		void error(@Nonnull Notification notification, @Nonnull IPushChannel channel,
				@Nonnull PushException e);
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
