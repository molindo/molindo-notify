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

package at.molindo.notify.dispatch;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.reportMatcher;

import java.util.Date;

import org.easymock.IArgumentMatcher;
import org.junit.Test;

import at.molindo.notify.INotifyService.IErrorListener;
import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.channel.IPushChannel.PushException;
import at.molindo.notify.dao.INotificationDAO;
import at.molindo.notify.dao.IPreferencesDAO;
import at.molindo.notify.dispatch.PollingPushDispatcher.Polling;
import at.molindo.notify.message.INotificationRenderService;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.notify.model.PushState;
import at.molindo.notify.render.IRenderService;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.test.util.EasyMockContext;
import at.molindo.notify.test.util.MockTest;

import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class PollingPushDispatcherTest {

	private static final String CHANNEL_ID = "dummy";
	private static final String USERID = "JohnDoe";
	private static final Date START = new Date();

	private static Notification n() {
		Notification n = new Notification();

		n.setId(42L);
		n.setUserId(USERID);
		n.setDate(START);
		n.setPushDate(START);
		n.setKey("test");
		n.setType(Type.PRIVATE);
		n.getParams().set(Param.pString("test"), "this is a test");

		return n;
	}

	private static Preferences p() {
		Preferences p = new Preferences();
		p.setUserId(USERID);
		p.getChannelPrefs().put(CHANNEL_ID, new PushChannelPreferences());
		return p;

	}

	private static Message m() throws RenderException {
		return Message.parse("Subject: Test\n\nThis is a test", IRenderService.Type.TEXT);
	}

	private abstract class PollingPushDispatcherMockTest extends MockTest {
		PollingPushDispatcher dispatcher;
		PushException ex = new IPushChannel.PushException(true);

		@Override
		protected void setup(EasyMockContext context) throws Exception {

			dispatcher = new PollingPushDispatcher();
			dispatcher.setPoolSize(1);
			dispatcher.setErrorListener(context.create(IErrorListener.class));
			dispatcher.setNotificationRenderService(context.create(INotificationRenderService.class));
			dispatcher.setNotificationDAO(context.create(INotificationDAO.class));
			dispatcher.setPreferencesDAO(context.create(IPreferencesDAO.class));
			dispatcher.setPushChannels(Sets.newHashSet(context.create(IPushChannel.class)));
		}

	}

	@Test
	public void test() throws Exception {
		new PollingPushDispatcherMockTest() {

			@Override
			@SuppressWarnings(value = "NP_NONNULL_PARAM_VIOLATION", justification = "mocks accept null")
			protected void setup(EasyMockContext context) throws Exception {
				super.setup(context);

				expect(context.get(INotificationDAO.class).getNext()).andReturn(n());

				expect(context.get(IPreferencesDAO.class).getPreferences(n().getUserId())).andReturn(p());
				expect(context.get(IPushChannel.class).getId()).andReturn(CHANNEL_ID);
				expect(
						context.get(IPushChannel.class).isConfigured(eq(USERID),
								anyObject(PushChannelPreferences.class))).andReturn(true);
				expect(context.get(IPushChannel.class).getNotificationTypes()).andReturn(Type.TYPES_ALL);

				expect(
						context.get(INotificationRenderService.class).render(eq(n()), eq(p()),
								anyObject(PushChannelPreferences.class))).andReturn(m());

				context.get(IPushChannel.class).push(eq(m()), anyObject(PushChannelPreferences.class));
				expectLastCall().andThrow(ex);

				reportMatcher(new IArgumentMatcher() {

					@Override
					public boolean matches(Object argument) {
						Notification n = (Notification) argument;

						if (n.getPushErrors() != 1) {
							return false;
						}

						if (n.getPushState() != PushState.QUEUED) {
							return false;
						}

						if (!n.getPushDate().after(n().getPushDate())) {
							return false;
						}

						return true;
					}

					@Override
					public void appendTo(StringBuffer buffer) {
						buffer.append("rescheduling failed");
					}
				});
				context.get(INotificationDAO.class).update(null);

				context.get(IErrorListener.class).error(n(), context.get(IPushChannel.class), ex);
			}

			@Override
			protected void test(EasyMockContext context) throws Exception {
				Polling polling = dispatcher.new Polling();
				polling.run();
			}

		}.run();
	}

	@Test
	public void testIdle() throws Exception {
		new PollingPushDispatcherMockTest() {

			PushException ex = new IPushChannel.PushException(true);

			@Override
			@SuppressWarnings(value = "NP_NONNULL_PARAM_VIOLATION", justification = "mocks accept null")
			protected void setup(EasyMockContext context) throws Exception {
				super.setup(context);

				expect(context.get(INotificationDAO.class).getNext()).andReturn(null);

				// verify delay
				context.get(IErrorListener.class).error(null, null, ex);
			}

			@Override
			protected void test(final EasyMockContext context) throws Exception {
				Polling polling = dispatcher.new Polling() {

					@Override
					@SuppressWarnings(value = "NP_NONNULL_PARAM_VIOLATION", justification = "mocks accept null")
					protected void delay() {
						// verify delay
						context.get(IErrorListener.class).error(null, null, ex);
					}

				};
				polling.run();
			}

		}.run();
	}
}
