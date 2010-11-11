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

package at.molindo.notify.context;

public class ContextUtils {

	public static final String ROOT = "/at/molindo/notify/context";
	public static final String MAIN = ROOT + "/notify.xml";
	public static final String CHANNELS = ROOT + "/channels.xml";
	public static final String DAOS = ROOT + "/daos.xml";
	public static final String RENDER = ROOT + "/notify-render-velocity.xml";

	private ContextUtils() {
	};
}
