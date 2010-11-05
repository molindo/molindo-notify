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

package at.molindo.notify.render.velocity;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import at.molindo.notify.model.Param;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.Template;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.IRenderService.Version;

public class VelocityTemplateRendererTest {

	public static VelocityTemplateRenderer r() {
		return new VelocityTemplateRenderer();
	}

	public static Template t() {
		Template t = new Template();
		t.setKey("test");
		t.setLastModified(new Date());
		t.setVersion(Version.LONG);
		t.setContent("this is a ${word}");
		return t;
	}

	@Test
	public void testRenderTemplateParams() throws RenderException {
		Template t = t();
		VelocityTemplateRenderer r = r();

		Params params = new Params();
		params.set(Param.p("word", String.class), "test");

		String result = r.render(t, params);

		assertEquals("this is a test", result);

	}

}
