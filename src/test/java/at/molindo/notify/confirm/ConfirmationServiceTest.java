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

package at.molindo.notify.confirm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.easymock.EasyMock.expect;

import java.util.Date;

import org.junit.Test;

import at.molindo.notify.INotifyService.IConfirmationListener;
import at.molindo.notify.dao.INotificationDAO;
import at.molindo.notify.model.Confirmation;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.Params;
import at.molindo.notify.test.util.EasyMockContext;
import at.molindo.notify.test.util.MockTest;

public class ConfirmationServiceTest {

	private static final Date START = new Date();

	private static Notification n() {
		Notification n = new Notification();

		n.setId(42L);
		n.setUserId("JohnDoe");
		n.setDate(START);
		n.setPushScheduled(START);
		n.setKey("test");
		n.setType(Type.PRIVATE);
		n.setParams(new Params().set(Param.p("test", String.class), "this is a test"));

		n.setConfirmation(new Confirmation().setKey("foo"));
		return n;
	}

	@Test
	public void testConfirm() throws Exception {
		new MockTest() {

			ConfirmationService _svc;

			@Override
			protected void setup(EasyMockContext context) throws Exception {
				_svc = new ConfirmationService();
				_svc.setNotificationDAO(context.create(INotificationDAO.class));

				_svc.addConfirmationListener(new IConfirmationListener() {

					@Override
					public String confirm(Notification notification) {
						assertEquals(n(), notification);
						return "/foo";
					}
				});

				expect(context.get(INotificationDAO.class).getByConfirmationKey("foo")).andReturn(n());
				expect(context.get(INotificationDAO.class).getByConfirmationKey("bar")).andReturn(null);
			}

			@Override
			protected void test(EasyMockContext context) throws Exception {
				assertEquals("/foo", _svc.confirm("foo"));
				assertNull(_svc.confirm("bar"));
			}

		}.run();
	}
}
