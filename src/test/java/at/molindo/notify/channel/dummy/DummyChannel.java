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

package at.molindo.notify.channel.dummy;

import java.util.Date;
import java.util.List;

import at.molindo.notify.channel.IPullChannel;
import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.channel.feed.AbstractPullChannel;
import at.molindo.notify.model.Dispatch;
import at.molindo.notify.model.IChannelPreferences;
import at.molindo.notify.model.IPreferences;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.PushChannelPreferences;

import com.google.common.collect.ImmutableSet;

public class DummyChannel extends AbstractPullChannel implements IPushChannel, IPullChannel {

	public static final String CHANNEL_ID = DummyChannel.class.getSimpleName();

	@Override
	public String getId() {
		return CHANNEL_ID;
	}

	@Override
	public ImmutableSet<Type> getNotificationTypes() {
		return Type.TYPES_ALL;
	}

	@Override
	public boolean isConfigured(String userId, IChannelPreferences prefs) {
		return true;
	}

	@Override
	public PushChannelPreferences newDefaultPreferences() {
		return new PushChannelPreferences();
	}

	@Override
	public void push(Dispatch dispatch) throws PushException {
		System.out.println(dispatch.getMessage());
	}

	@Override
	protected String pull(List<Message> messages, Date lastModified, IChannelPreferences cPrefs, IPreferences prefs)
			throws PullException {

		StringBuilder buf = new StringBuilder();

		for (Message message : messages) {
			buf.append(message.getSubject()).append("\n").append(message.getText()).append("\n\n");
		}
		buf.append("last modififed: ").append(lastModified).append("\n");

		return buf.toString();
	}

}
