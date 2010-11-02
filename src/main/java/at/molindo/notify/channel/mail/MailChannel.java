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

package at.molindo.notify.channel.mail;

import javax.mail.Address;

import com.google.common.collect.ImmutableSet;

import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.model.ChannelPreferences;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.PushChannelPreferences;

public class MailChannel implements IPushChannel {

	private static final String CHANNEL_ID = "mail";
	
	public static final Param<Address> RECIPIENT = new Param<Address>("recipient", Address.class);
	
	private ImmutableSet<Type> _notificationTypes = ImmutableSet.of(Type.PRIVATE);

	private PushChannelPreferences _defaultPreferences;
	
	public MailChannel() {
		
	}
	
	@Override
	public String getId() {
		return CHANNEL_ID;
	}

	@Override
	public ImmutableSet<Type> getNotificationTypes() {
		return _notificationTypes;
	}

	@Override
	public PushChannelPreferences newDefaultPreferences() {
		return _defaultPreferences != null ? _defaultPreferences.clone() : new PushChannelPreferences();
	}

	@Override
	public void push(String message, PushChannelPreferences prefs)
			throws PushException {
		
		System.err.println("send: " + message + " to " + prefs.getParams().get(RECIPIENT));
	}

	@Override
	public boolean isConfigured(ChannelPreferences prefs) {
		return prefs.getParams().containsAll(RECIPIENT);
	}

	public PushChannelPreferences getDefaultPreferences() {
		return _defaultPreferences;
	}

	public void setDefaultPreferences(PushChannelPreferences defaultPreferences) {
		_defaultPreferences = defaultPreferences;
	}

}
