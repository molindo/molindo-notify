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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import at.molindo.notify.model.Template;
import at.molindo.notify.render.IRenderService.Type;
import at.molindo.notify.render.IRenderService.Version;
import at.molindo.utils.collections.IteratorUtils;
import at.molindo.utils.data.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class NotifyUtils {

	private static final Comparator<Template> TEMPLATE_COMPARATOR = new Comparator<Template>() {

		@Override
		public int compare(Template t1, Template t2) {
			if (t1 == t2) {
				return 0;
			}
			int val = t1.getType().compareTo(t2.getType());
			if (val != 0) {
				return val;
			}
			return t1.getVersion().compareTo(t2.getVersion());
		}
	};

	private NotifyUtils() {
	};

	/**
	 * TODO this should become customizable
	 * 
	 * @param html
	 * @return
	 */
	public static String html2text(String html) {

		Renderer renderer = new Renderer(new Source(html)) {

			{
				setIncludeHyperlinkURLs(true);
			}

			@Override
			public String renderHyperlinkURL(StartTag startTag) {
				final String href = startTag.getAttributeValue("href");
				if (href == null || href.equals("#") || href.startsWith("javascript:")) {
					return null;
				}
				// TODO customize?
				return '<' + href + '>';
			}

		};

		String text = renderer.toString();

		String newLine = renderer.getNewLine();

		// strip leading new lines
		while (text != (text = StringUtils.stripLeading(text, newLine))) {
		}

		// strip trailing new lines
		while (text != (text = StringUtils.stripTrailing(text, newLine))) {
		}

		return text;
	}

	public static String text2html(String text) {
		// TODO anything smarter than that?
		return text.replaceAll("\n", "<br />");
	}

	/**
	 * @param templates
	 *            a list of templates to choose from. keys needn't be equal
	 * @param type
	 *            null if not required, prefers HTML over TEXT
	 * @param version
	 *            null if not required, prefers LONG over SHORT
	 * 
	 * @return the best matching template from list. type is more important than
	 *         version
	 */
	public static Template choose(@Nonnull List<Template> templates, final Type type, final Version version) {

		List<Template> filtered = IteratorUtils.list(Iterators.filter(templates.iterator(), new Predicate<Template>() {

			@Override
			public boolean apply(Template input) {
				if (type != null && type != input.getType()) {
					return false;
				}
				if (version != null && version != input.getVersion()) {
					return false;
				}
				return true;
			}
		}), templates.size());

		switch (filtered.size()) {
		case 0:
			return null;
		case 1:
			return filtered.get(0);
		default:
			Collections.sort(filtered, TEMPLATE_COMPARATOR);
			return filtered.get(0);
		}
	}
}
