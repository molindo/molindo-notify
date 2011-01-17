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

package at.molindo.notify.confirm;

import at.molindo.notify.INotifyService.IConfirmationListener;

public interface IConfirmationService {

	/**
	 * 
	 * @param key
	 *            confirmation key
	 * @return null if no confirmation exists or confirmation expired, redirect
	 *         path otherwise
	 * @throws ConfirmationException
	 *             if confirmation exists but wasn't handled
	 */
	String confirm(String key) throws ConfirmationException;

	void addConfirmationListener(IConfirmationListener listener);

	void removeConfirmationListener(IConfirmationListener listener);

	public static class ConfirmationException extends Exception {

		private static final long serialVersionUID = 1L;

		public ConfirmationException() {
			super();
		}

		public ConfirmationException(String message, Throwable cause) {
			super(message, cause);
		}

		public ConfirmationException(String message) {
			super(message);
		}

		public ConfirmationException(Throwable cause) {
			super(cause);
		}
	}
}
