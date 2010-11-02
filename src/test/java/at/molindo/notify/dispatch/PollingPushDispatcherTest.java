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

import static org.easymock.EasyMock.*;

import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import at.molindo.notify.INotificationService.IErrorListener;
import at.molindo.notify.channel.IPushChannel;
import at.molindo.notify.channel.IPushChannel.PushException;
import at.molindo.notify.dao.INotificationsDAO;
import at.molindo.notify.dao.IPreferencesDAO;
import at.molindo.notify.dispatch.PollingPushDispatcher.Polling;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.notify.render.IRenderService;
import at.molindo.notify.render.IRenderService.Version;
import at.molindo.notify.test.util.EasyMockContext;
import at.molindo.notify.test.util.MockTest;

public class PollingPushDispatcherTest {

	private static final String CHANNEL_ID = "dummy";
	private static final String USERID = "JohnDoe";
	private static final String MSG = "this is a test";
	private static final Date START = new Date();

	private static Notification n() {
		Notification n = new Notification();

		n.setUserId(USERID);
		n.setDate(START);
		n.setKey("test");
		n.setType(Type.PRIVATE);
		n.setParams(new Params().set(Param.p("test", String.class), "this is a test"));
		
		return n;
	}
	
	private static Preferences p() {
		Preferences p = new Preferences();
		
		p.setUserId(USERID);
		
		Map<String, PushChannelPreferences> map = Maps.newHashMap();
		map.put(CHANNEL_ID, new PushChannelPreferences());
		p.setChannelPrefs(map);
		
		return p ;
	
	}

	private abstract class PollingPushDispatcherMockTest extends MockTest {
		PollingPushDispatcher dispatcher;
		PushException ex = new IPushChannel.PushException();
		
		@Override
		protected void setup(EasyMockContext context) throws Exception {

			dispatcher = new PollingPushDispatcher();
			dispatcher.setPoolSize(1);
			dispatcher.setErrorListener(context.create(IErrorListener.class));
			dispatcher.setRenderService(context.create(IRenderService.class));
			dispatcher.setNotificationsDAO(context
					.create(INotificationsDAO.class));
			dispatcher.setPreferencesDAO(context.create(IPreferencesDAO.class));
			dispatcher.setPushChannels(Sets.newHashSet(context
					.create(IPushChannel.class)));
		}

	}

	@Test
	public void test() throws Exception {
		new PollingPushDispatcherMockTest() {

			@Override
			protected void setup(EasyMockContext context) throws Exception {
				super.setup(context);
				
				expect(context.get(INotificationsDAO.class).getNext()).andReturn(n());
				expect(context.get(IPreferencesDAO.class).getPreferences(n().getUserId())).andReturn(p());
				expect(context.get(IPushChannel.class).getId()).andReturn(CHANNEL_ID);
				expect(context.get(IPushChannel.class).isConfigured(anyObject(PushChannelPreferences.class))).andReturn(true);
				expect(context.get(IPushChannel.class).getNotificationTypes()).andReturn(Type.TYPES_ALL);
				
				expect(context.get(IRenderService.class).render(eq(n().getKey()), anyObject(Version.class), anyObject(Params.class))).andReturn(MSG);
				
				context.get(IPushChannel.class).push(eq(MSG), anyObject(PushChannelPreferences.class));
				expectLastCall().andThrow(ex);

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

			PushException ex = new IPushChannel.PushException();
			
			@Override
			protected void setup(EasyMockContext context) throws Exception {
				super.setup(context);
				
				expect(context.get(INotificationsDAO.class).getNext()).andReturn(null);

				// verify delay
				context.get(IErrorListener.class).error(null, null, ex);
			}

			@Override
			protected void test(final EasyMockContext context) throws Exception {
				Polling polling = dispatcher.new Polling() {

					@Override
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
