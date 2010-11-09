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

package at.molindo.notify.dao.memory;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import at.molindo.notify.dao.INotificationDAO;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.PushState;
import at.molindo.utils.collections.CollectionUtils;

import com.google.common.collect.Lists;

public class MemoryNotificationDAO implements INotificationDAO {

	private LinkedList<Notification> _queue = Lists.newLinkedList();

	private AtomicLong _idCounter = new AtomicLong(1);

	@Override
	public void save(Notification notification) {
		if (notification == null) {
			throw new NullPointerException("notification");
		}

		notification.setId(_idCounter.getAndIncrement());

		synchronized (_queue) {
			// TODO clone?
			_queue.add(notification);
		}
	}

	@Override
	public void update(Notification notification) {
		if (notification.getId() == null) {
			throw new IllegalArgumentException("can't update transient object: " + notification);
		}

		synchronized (_queue) {
			ListIterator<Notification> iter = _queue.listIterator();
			while (iter.hasNext()) {
				if (iter.next().getId().equals(notification.getId())) {
					// TODO clone?
					iter.set(notification);
					break;
				}
			}
		}
	}

	@Override
	public void delete(Notification notification) {
		if (notification.getId() == null) {
			throw new IllegalArgumentException("can't delete transient object: " + notification);
		}

		synchronized (_queue) {
			ListIterator<Notification> iter = _queue.listIterator();
			while (iter.hasNext()) {
				if (iter.next().getId().equals(notification.getId())) {
					iter.remove();
					break;
				}
			}
		}
	}

	@Override
	public Notification getNext() {
		synchronized (this) {
			Collections.sort(_queue, new Comparator<Notification>() {
				@Override
				public int compare(Notification o1, Notification o2) {
					// QUEUED to front
					int val = o1.getPushState().compareTo(o2.getPushState());
					if (val != 0) {
						return val;
					}
					return o1.getPushScheduled().compareTo(o2.getPushScheduled());
				}
			});
			Notification n = _queue.peek();
			if (n != null && n.getPushState() == PushState.QUEUED && n.getPushScheduled() != null
					&& n.getPushScheduled().before(new Date())) {
				return n;
			} else {
				return null;
			}
		}
	}

	@Override
	public List<Notification> getRecent(String userId, Set<Type> types, int first, int count) {
		List<Notification> list = Lists.newArrayListWithCapacity(Math.max(_queue.size(), 100));

		synchronized (_queue) {
			ListIterator<Notification> iter = _queue.listIterator();
			while (iter.hasNext()) {
				Notification n = iter.next();
				if (n.getUserId().equals(userId) && types.contains(n.getType())) {
					list.add(n);
				}
			}
		}

		return CollectionUtils.subList(list, first, count);
	}

}
