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

import static at.molindo.notify.util.NotifyUtils.html2text;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NotifyUtilsTest {

	@Test
	public void html2Test() {

		assertEquals("this is a link <http://example.com/>",
				html2text("<p>this is a <a href='http://example.com/'>link</a></p>"));

		assertEquals("    * item 1\r\n    * item 2",
				html2text("<html><body><ul><li>item 1</li><li>item 2</li></ul></html></body>"));

		assertEquals("foobar baz qux", html2text("foobar <strong>baz</strong> qux"));
	}
}
