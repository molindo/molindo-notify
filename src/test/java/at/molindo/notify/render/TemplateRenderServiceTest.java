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

package at.molindo.notify.render;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import at.molindo.notify.dao.ITemplateDAO;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.Template;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.IRenderService.Version;
import at.molindo.notify.test.util.EasyMockContext;
import at.molindo.notify.test.util.MockTest;

public class TemplateRenderServiceTest {

	private static final Date START = new Date();

	public static TemplateRenderService svc(EasyMockContext context) {
		TemplateRenderService svc = new TemplateRenderService();
		svc.setTemplateDAO(context.create(ITemplateDAO.class));
		svc.setRenderer(context.create(ITemplateRenderer.class));
		return svc;
	}

	public static Template t() {
		Template t = new Template();
		t.setKey("test");
		t.setLastModified(START);
		t.setVersion(Version.LONG);
		t.setContent("Subject: Test\n\nthis is a ${word}");
		return t;
	}

	public static Message result() {
		return new Message("Test", "this is a test", IRenderService.Type.HTML, START);
	}

	@Test
	public void testRenderStringVersionParams() throws Exception {
		new MockTest() {

			private TemplateRenderService _svc;
			private Template _t;
			private Params _params;

			@Override
			protected void setup(EasyMockContext context) throws RenderException {
				_svc = svc(context);
				_t = t();

				_params = new Params();
				_params.set(Param.pString("word"), "test");

				expect(context.get(ITemplateDAO.class).findTemplates(_t.getKey())).andReturn(Arrays.asList(t()));
				expect(context.get(ITemplateRenderer.class).render(_t, _params)).andReturn(
						"Subject: Test\n\nthis is a test");
			}

			@Override
			protected void test(EasyMockContext context) throws RenderException {
				Message result = _svc.render(_t.getKey(), _t.getVersion(), _params);
				assertEquals(result(), result);
			}

		}.run();
	}

}
