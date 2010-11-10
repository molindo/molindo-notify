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

import static at.molindo.notify.channel.feed.AbstractPullChannel.AMOUNT;
import static at.molindo.notify.channel.feed.AbstractPullChannel.MAX_AMOUNT;
import static at.molindo.notify.channel.feed.AbstractPullChannel.SECRET;
import at.molindo.notify.INotifyService;
import at.molindo.notify.INotifyService.NotifyException;
import at.molindo.notify.channel.feed.AbstractFeedChannel;

public class ConfigurableChannelPreferences extends ChannelPreferences implements IRequestConfigurable {

	public ConfigurableChannelPreferences() {
		super();
	}

	public ConfigurableChannelPreferences(Params defaults) {
		super(defaults);
	}

	@Override
	public void setParam(String name, String value) throws NotifyException {
		if (value == null) {
			return;
		}

		if (AMOUNT.getName().equals(name)) {
			try {
				int amount = AMOUNT.toObject(value);
				if (amount > 0 && amount <= MAX_AMOUNT) {
					getParams().set(AbstractFeedChannel.AMOUNT, amount);
				} else {
					throw new INotifyService.NotifyException("illegal amount: " + amount);
				}
			} catch (NumberFormatException e) {
				throw new INotifyService.NotifyException("can't convert value to Number: " + value, e);
			}
		} else if (SECRET.getName().equals(name)) {
			getParams().set(AbstractFeedChannel.SECRET, value);
		}
	}

}
