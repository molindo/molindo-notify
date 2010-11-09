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

package at.molindo.notify.channel;

import at.molindo.notify.INotifyService.NotifyException;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.PushChannelPreferences;

public interface IPushChannel extends IChannel {

	@Override
	PushChannelPreferences newDefaultPreferences();

	/**
	 * deliver notification
	 * 
	 * @param notification
	 * @param cPrefs
	 * @throws PushException
	 *             if delivery failed
	 */
	void push(Message message, PushChannelPreferences cPrefs) throws PushException;

	public class PushException extends NotifyException {

		private static final long serialVersionUID = 1L;
		private final boolean _temporaryError;

		public PushException(boolean temporaryError) {
			super();
			_temporaryError = temporaryError;
		}

		public PushException(String message, Throwable cause, boolean temporaryError) {
			super(message, cause);
			_temporaryError = temporaryError;
		}

		public PushException(String message, boolean temporaryError) {
			super(message);
			_temporaryError = temporaryError;
		}

		public PushException(Throwable cause, boolean temporaryError) {
			super(cause);
			_temporaryError = temporaryError;
		}

		public boolean isTemporaryError() {
			return _temporaryError;
		}

	}
}
