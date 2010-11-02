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

package at.molindo.notify.channel;

import at.molindo.notify.model.ChannelPreferences;
import at.molindo.notify.model.Notification;
import com.google.common.collect.ImmutableSet;

public interface IChannel {
	String getId();
	
	ImmutableSet<Notification.Type> getNotificationTypes();

	/**
	 * create default preferences for this channel
	 * 
	 * @return
	 */
	ChannelPreferences newDefaultPreferences();
	
	/**
	 * @param prefs
	 * @return true if channel is sufficiently configured
	 */
	boolean isConfigured(ChannelPreferences prefs);
	
}
