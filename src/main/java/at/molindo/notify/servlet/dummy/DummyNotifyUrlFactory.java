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

package at.molindo.notify.servlet.dummy;

import at.molindo.notify.model.Confirmation;
import at.molindo.notify.model.IParams;
import at.molindo.notify.servlet.INotifyUrlFactory;

public class DummyNotifyUrlFactory implements INotifyUrlFactory {

	@Override
	public String toPullPath(final String channelId, final String userId, final IParams params) {
		return "http://www.example.com/notify/pull/" + channelId + "/" + userId;
	}

	@Override
	public String toConfirmPath(final Confirmation confirmation) {
		return "http://www.example.com/notify/confirm/" + confirmation.getKey();
	}
}
