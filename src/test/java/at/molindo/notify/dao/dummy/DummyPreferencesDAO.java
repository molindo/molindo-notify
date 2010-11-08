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

import at.molindo.notify.channel.dummy.DummyChannel;
import at.molindo.notify.channel.mail.MailChannel;
import at.molindo.notify.dao.IPreferencesDAO;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.model.PushChannelPreferences;

public class DummyPreferencesDAO implements IPreferencesDAO {

	@Override
	public Preferences getPreferences(String userId) {
		if (DummyUtils.USER_ID.equals(userId)) {
			Preferences p = new Preferences().setUserId(userId).setParams(new Params());
			
			PushChannelPreferences mailPrefs = new PushChannelPreferences();
			MailChannel.setRecipient(mailPrefs, "stf+johndoe@molindo.at");
			MailChannel.setRecipientName(mailPrefs, "John Doe");
			p.addChannelPrefs(MailChannel.CHANNEL_ID, mailPrefs);
			
			PushChannelPreferences dummyPrefs = new PushChannelPreferences();
			p.addChannelPrefs(DummyChannel.CHANNEL_ID, dummyPrefs);
			
			return p;
		} else {
			return null;
		}
	}

	@Override
	public void savePreferences(Preferences prefs) {
		// do nothing
	}

	@Override
	public void removePreferences(String userId) {
		// do nothing
	}

}
