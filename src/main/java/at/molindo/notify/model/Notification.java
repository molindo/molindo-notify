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

import java.util.Date;

import com.google.common.collect.ImmutableSet;

public class Notification {

	public enum Type {
		PUBLIC, PRIVATE;

		public static ImmutableSet<Type> TYPES_ALL = ImmutableSet.of(values());
		public static ImmutableSet<Type> TYPES_PUBLIC = ImmutableSet.of(PUBLIC);
		public static ImmutableSet<Type> TYPES_PRIVATE = ImmutableSet
				.of(PRIVATE);
	}

	private String _userId;
	private String _key;
	private Type _type = Type.PRIVATE;
	private Date _date = new Date();
	private Params _params;
	private Confirmation _confirmation;

	// push
	private PushState _pushState = PushState.QUEUED;
	private Date _pushScheduled = new Date();
	private Date _pushDate;
	private int _pushErrors = 0;

	public String getUserId() {
		return _userId;
	}

	public void setUserId(String userId) {
		_userId = userId;
	}

	public String getKey() {
		return _key;
	}

	public void setKey(String key) {
		_key = key;
	}

	public Type getType() {
		return _type;
	}

	public void setType(Type type) {
		_type = type;
	}

	public Date getDate() {
		return _date;
	}

	public void setDate(Date date) {
		_date = date;
	}

	public Params getParams() {
		return _params;
	}

	public void setParams(Params params) {
		_params = params;
	}

	public Confirmation getConfirmation() {
		return _confirmation;
	}

	public void setConfirmation(Confirmation confirmation) {
		_confirmation = confirmation;
		if (_confirmation != null && _confirmation.getNotification() != this) {
			_confirmation.setNotification(this);
		}
	}

	public PushState getPushState() {
		return _pushState;
	}

	public void setPushState(PushState pushState) {
		if (pushState == null) {
			throw new NullPointerException("pushState");
		}
		_pushState = pushState;
	}

	public Date getPushScheduled() {
		return _pushScheduled;
	}

	public void setPushScheduled(Date pushScheduled) {
		_pushScheduled = pushScheduled;
	}

	public Date getPushDate() {
		return _pushDate;
	}

	public void setPushDate(Date pushDate) {
		_pushDate = pushDate;
	}

	public int getPushErrors() {
		return _pushErrors;
	}

	void setPushErrors(int pushErrors) {
		_pushErrors = pushErrors;
	}

	/**
	 * @return new error count
	 */
	public int recordPushError() {
		return ++_pushErrors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getDate() == null) ? 0 : getDate().hashCode());
		result = prime * result
				+ ((getKey() == null) ? 0 : getKey().hashCode());
		result = prime * result
				+ ((getType() == null) ? 0 : getType().hashCode());
		result = prime * result
				+ ((getUserId() == null) ? 0 : getUserId().hashCode());
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
		if (!(obj instanceof Notification)) {
			return false;
		}
		Notification other = (Notification) obj;
		if (getType() != other.getType()) {
			return false;
		}
		if (getDate() == null) {
			if (other.getDate() != null) {
				return false;
			}
		} else if (!getDate().equals(other.getDate())) {
			return false;
		}
		if (getKey() == null) {
			if (other.getKey() != null) {
				return false;
			}
		} else if (!getKey().equals(other.getKey())) {
			return false;
		}
		if (getParams() == null) {
			if (other.getParams() != null) {
				return false;
			}
		} else if (!getParams().equals(other.getParams())) {
			return false;
		}
		if (getUserId() == null) {
			if (other.getUserId() != null) {
				return false;
			}
		} else if (!getUserId().equals(other.getUserId())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Notification [userId=" + _userId + ", key=" + _key + ", type="
				+ _type + ", date=" + _date + "]";
	}

}
