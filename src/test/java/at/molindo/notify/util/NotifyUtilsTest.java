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

import static at.molindo.notify.util.NotifyUtils.choose;
import static at.molindo.notify.util.NotifyUtils.html2text;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import at.molindo.notify.model.Template;
import at.molindo.notify.render.IRenderService.Type;
import at.molindo.notify.render.IRenderService.Version;

public class NotifyUtilsTest {

	@Test
	public void html2Test() {

		assertEquals("this is a link <http://example.com/>",
				html2text("<p>this is a <a href='http://example.com/'>link</a></p>"));

		assertEquals("    * item 1\r\n    * item 2",
				html2text("<html><body><ul><li>item 1</li><li>item 2</li></ul></html></body>"));

		assertEquals("foobar baz qux", html2text("foobar <strong>baz</strong> qux"));
	}

	@Test
	public void chooseTemplate() {

		String key = "key";
		Locale l = Locale.ENGLISH;

		Template longHtml = new Template(key, Type.HTML, Version.LONG, l, "<strong>long</strong>");
		Template shortHtml = new Template(key, Type.HTML, Version.SHORT, l, "<strong>short</strong>");
		Template longText = new Template(key, Type.TEXT, Version.LONG, l, "long");
		Template shortText = new Template(key, Type.TEXT, Version.SHORT, l, "short");

		List<Template> all = Arrays.asList(shortText, longText, shortHtml, longHtml);

		assertSame(longHtml, choose(all, null, null));

		assertSame(longText, choose(all, Type.TEXT, null));
		assertSame(longHtml, choose(all, Type.HTML, null));

		assertSame(longHtml, choose(all, null, Version.LONG));
		assertSame(shortHtml, choose(all, null, Version.SHORT));

		assertSame(longHtml, choose(all, Type.HTML, Version.LONG));
		assertSame(shortHtml, choose(all, Type.HTML, Version.SHORT));
		assertSame(longText, choose(all, Type.TEXT, Version.LONG));
		assertSame(shortText, choose(all, Type.TEXT, Version.SHORT));
	}
}
