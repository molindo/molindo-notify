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

package at.molindo.notify.model;

public class Dispatch {
	private final Message _message;
	private final Params _params;

	public Dispatch(Message message, Params params) {
		if (message == null) {
			throw new NullPointerException("message");
		}
		if (params == null) {
			throw new NullPointerException("params");
		}
		_message = message;
		_params = params;
	}

	public Message getMessage() {
		return _message;
	}

	public Params getParams() {
		return _params;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (_message == null ? 0 : _message.hashCode());
		result = prime * result + (_params == null ? 0 : _params.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Dispatch)) {
			return false;
		}
		Dispatch other = (Dispatch) obj;
		if (_message == null) {
			if (other._message != null) {
				return false;
			}
		} else if (!_message.equals(other._message)) {
			return false;
		}
		if (_params == null) {
			if (other._params != null) {
				return false;
			}
		} else if (!_params.equals(other._params)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return _message.toString();
	}

}
