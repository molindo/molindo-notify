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
import java.util.UUID;

public class Confirmation {

	private static final long serialVersionUID = 1L;

	private Notification _notification;

	private String _key;

	private Date _touched;

	public Confirmation() {
		setTouched(new Date());
		setKey(UUID.randomUUID().toString());
	}

	public Confirmation(Notification notification) {
		this();
		setNotification(notification);
	}

	public Notification getNotification() {
		return _notification;
	}

	public Confirmation setNotification(Notification notification) {
		_notification = notification;
		if (_notification != null && _notification.getConfirmation() != this) {
			_notification.setConfirmation(this);
		}
		return this;
	}

	public Date getTouched() {
		return _touched;
	}

	public Confirmation setTouched(Date touched) {
		_touched = touched;
		return this;
	}

	public String getKey() {
		return _key;
	}

	public Confirmation setKey(String key) {
		_key = key;
		return this;
	}

}
