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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import at.molindo.notify.model.IParams;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.Template;
import at.molindo.notify.render.IRenderService.Type;
import at.molindo.notify.render.IRenderService.Version;
import at.molindo.notify.test.util.EasyMockContext;
import at.molindo.notify.test.util.MockTest;

public class MasterRenderServiceTest {

	private static final Date START = new Date();

	public static MasterRenderService svc(EasyMockContext context) {
		MasterRenderService svc = new MasterRenderService();
		svc.setRenderService(context.create(IRenderService.class));
		return svc;
	}

	public static Template t() {
		Template t = new Template();
		t.setKey("test");
		t.setLastModified(START);
		t.setVersion(Version.LONG);
		t.setContent("${orig} \n\nBrought to you by molindo-notify");
		return t;
	}

	@Test
	public void testRender() throws Exception {
		new MockTest() {

			private MasterRenderService _svc;
			private Template _t;

			@Override
			protected void setup(EasyMockContext context) throws Exception {
				_svc = svc(context);
				_t = t();

				expect(
						context.get(IRenderService.class).render(eq(_t.getKey()), eq(_t.getVersion()),
								anyObject(IParams.class))).andReturn(new Message("expected", "raw message", Type.TEXT));

				expect(
						context.get(IRenderService.class).render(eq(_svc.getMasterTemplateKey()), eq(_t.getVersion()),
								eq(new Params().set(Param.pString(_svc.getMasterTemplateContent()), "raw message"))))
						.andReturn(new Message("template subject", "raw message + footer", Type.TEXT));
			}

			@Override
			protected void test(EasyMockContext context) throws Exception {
				Message result = _svc.render(_t.getKey(), _t.getVersion(), new Params());
				assertEquals(new Message("expected", "raw message + footer", Type.TEXT), result);
			}

		}.run();
	}
}
