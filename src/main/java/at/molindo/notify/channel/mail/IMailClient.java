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

import at.molindo.notify.channel.IPushChannel.PushException;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.PushChannelPreferences;

public interface IMailClient {

	void send(Message message, PushChannelPreferences cPrefs)
			throws MailException;

	public static class MailException extends PushException {

		private static final long serialVersionUID = 1L;

		public MailException(boolean temporaryError) {
			super(temporaryError);
		}

		public MailException(String message, boolean temporaryError) {
			super(message, temporaryError);
		}

		public MailException(String message, Throwable cause,
				boolean temporaryError) {
			super(message, cause, temporaryError);
		}

		public MailException(Throwable cause, boolean temporaryError) {
			super(cause, temporaryError);
		}

	}
}
