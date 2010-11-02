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

import at.molindo.notify.INotificationService.NotifyException;
import at.molindo.notify.model.PushChannelPreferences;

public interface IPushChannel extends IChannel {

	PushChannelPreferences newDefaultPreferences();
	
	/**
	 * deliver notification
	 * 
	 * @param notification
	 * @param cPrefs
	 * @throws PushException if delivery failed
	 */
	void push(String message, PushChannelPreferences cPrefs) throws PushException;

	public class PushException extends NotifyException {

		private static final long serialVersionUID = 1L;

		public PushException() {
			super();
		}

		public PushException(String message, Throwable cause) {
			super(message, cause);
		}

		public PushException(String message) {
			super(message);
		}

		public PushException(Throwable cause) {
			super(cause);
		}
		
	}
}
