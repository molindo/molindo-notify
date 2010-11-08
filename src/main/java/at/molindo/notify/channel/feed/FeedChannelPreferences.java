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

package at.molindo.notify.channel.feed;

import static at.molindo.notify.channel.feed.AbstractPullChannel.AMOUNT;
import static at.molindo.notify.channel.feed.AbstractPullChannel.MAX_AMOUNT;
import at.molindo.notify.INotificationService;
import at.molindo.notify.INotificationService.NotifyException;
import at.molindo.notify.model.ChannelPreferences;
import at.molindo.notify.model.IRequestConfigurable;
import at.molindo.notify.model.Params;

public final class FeedChannelPreferences extends ChannelPreferences implements
		IRequestConfigurable {

	FeedChannelPreferences(Params defaults) {
		super(defaults);
	}

	@Override
	public void setParam(String name, String value) throws NotifyException {
		try {
			if (AMOUNT.getName().equals(name)) {
				int amount = Integer.parseInt(value);
				if (amount > 0 && amount <= MAX_AMOUNT) {
					getParams().set(AbstractFeedChannel.AMOUNT, amount);
				} else {
					throw new INotificationService.NotifyException(
							"illegal amount: " + amount);
				}
			}
		} catch (NumberFormatException e) {
			throw new INotificationService.NotifyException(
					"can't convert value to Number: " + value, e);
		}
	}
}