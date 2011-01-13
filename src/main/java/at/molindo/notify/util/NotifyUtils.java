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

import java.util.List;

import javax.annotation.Nonnull;

import at.molindo.notify.model.Template;
import at.molindo.notify.render.IRenderService.Version;

public class NotifyUtils {

	private NotifyUtils() {
	};

	public static String html2text(String html) {
		// TODO convert html to text
		return html.replaceAll("\\<.*?>", "");
	}

	public static String text2html(String text) {
		// TODO convert text to html
		return text.replaceAll("\n", "<br />");
	}

	public static Template choose(@Nonnull List<Template> templates, @Nonnull Version version) {
		// TODO use fallbacks
		for (Template t : templates) {
			if (version.equals(t.getVersion())) {
				return t;
			}
		}
		return null;
	}
}
