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

package at.molindo.notify.util;

import at.molindo.notify.model.ChannelPreferences;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.render.IRenderService;
import at.molindo.notify.render.IRenderService.RenderException;

public class NotifyUtils {

	private NotifyUtils() {};
	
	public static String render(IRenderService renderService,  Notification notification, Preferences prefs,
			ChannelPreferences cPrefs) throws RenderException {
		
		Params params = new Params().setAll(prefs.getParams()).setAll(cPrefs.getParams()).setAll(notification.getParams());
		String msg = renderService.render(notification.getKey(), cPrefs.getVersion(), params);
		return msg;
		
	}
}
