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

package at.molindo.notify.dao.dummy;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import at.molindo.notify.dao.INotificationDAO;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.Params;

import com.google.common.collect.Lists;

public class DummyNotificationDAO implements INotificationDAO {

	private long _last = 0;
	
	@Override
	public void save(Notification notification) {
		// do nothing
	}

	@Override
	public void update(Notification notification) {
		// do nothing
	}

	@Override
	public void delete(Notification notification) {
		// do nothing
	}

	@Override
	public Notification getNext() {
		synchronized (this) {
			long now = System.currentTimeMillis();
			if (now - _last > TimeUnit.SECONDS.toMillis(10)) {
				_last = now;
				return new Notification().setUserId(DummyUtils.USER_ID)
					.setDate(new Date()).setKey(DummyUtils.KEY)
					.setParams(new Params().set(Param.pString("word"), "Test"))
					.setType(Type.PRIVATE);
			} else {
				return null;
			}
		}
	}

	@Override
	public List<Notification> getRecent(String userId, Set<Type> types,
			int first, int count) {
		if (DummyUtils.USER_ID.equals(userId) && types.contains(Type.PRIVATE) && first == 0 && count > 0) {
			return Lists.newArrayList(getNext());
		} else {
			return Lists.newArrayListWithCapacity(0);
		}
	}

}
