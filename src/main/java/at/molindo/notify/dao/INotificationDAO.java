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

package at.molindo.notify.dao;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Notification.Type;

public interface INotificationDAO {

	void save(@Nonnull Notification notification);

	void update(@Nonnull Notification notification);

	void delete(@Nonnull Notification notification);

	/**
	 * @return next notification from push queue
	 */
	Notification getNext();

	/**
	 * @return recent notifications for userId (pull)
	 */
	@Nonnull
	List<Notification> getRecent(@Nonnull String userId, @Nonnull Set<Type> types, @Nonnegative int first,
			@Nonnegative int count);

}
