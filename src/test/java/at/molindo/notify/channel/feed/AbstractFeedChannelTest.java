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

package at.molindo.notify.channel.feed;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import at.molindo.notify.dao.INotificationsDAO;
import at.molindo.notify.dao.IPreferencesDAO;
import at.molindo.notify.model.ChannelPreferences;
import at.molindo.notify.model.IRequestConfigurable;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.render.IRenderService;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.IRenderService.Version;
import at.molindo.notify.test.util.EasyMockContext;
import at.molindo.notify.test.util.MockTest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Feed;

public class AbstractFeedChannelTest {

	private static final String NOTIFICATION_KEY = "some.key";
	private static final String USER_ID = "JohnDoe";

	public static AbstractFeedChannel c() {
		AbstractFeedChannel c = new AbstractFeedChannel() {

			@Override
			public String getId() {
				return "dummy";
			}

			@Override
			public ImmutableSet<Type> getNotificationTypes() {
				return Type.TYPES_ALL;
			}
		};
		c.setAuthorName("dummy author");
		c.setDefaultAmount(20);
		return c;
	}

	private static Preferences p() {
		Preferences p = new Preferences();
		p.setUserId(USER_ID);
		return p;
	}

	private static List<Notification> n() {
		List<Notification> n = Lists.newArrayList();

		Notification n1 = new Notification();
		n1.setDate(new Date());
		n1.setKey(NOTIFICATION_KEY);
		n1.setType(Type.PRIVATE);
		n1.setUserId(USER_ID);
		n.add(n1);

		return n;
	}

	private static Message m() throws RenderException {
		return Message.parse("Subject: Test\n\nThis is a test",
				IRenderService.Type.TEXT);
	}

	@Test
	public void testPull() throws Exception {
		new FeedMockTest() {

			@Override
			protected void setup(EasyMockContext context) throws Exception {
				super.setup(context);

				expect(
						context.get(IPreferencesDAO.class).getPreferences(
								USER_ID)).andReturn(p());
				expect(
						context.get(INotificationsDAO.class).getRecent(USER_ID,
								Type.TYPES_ALL, 0, 20)).andReturn(n());
				expect(
						context.get(IRenderService.class).render(
								eq(NOTIFICATION_KEY), same(Version.LONG),
								anyObject(Params.class))).andReturn(m());
			}

			@Override
			protected void test(EasyMockContext context) throws Exception {
				String str = c.pull(USER_ID, c.newDefaultPreferences());
				assertNotNull(str);
				str = str.trim();
				assertTrue(str, str.startsWith("<feed"));
				assertTrue(str, str.endsWith("</feed>"));
			}

		}.run();
	}

	@Test
	public void testToFeed() throws Exception {

		new FeedMockTest() {

			@Override
			protected void setup(EasyMockContext context) throws Exception {
				super.setup(context);
				expect(
						context.get(IRenderService.class).render(
								eq(NOTIFICATION_KEY), same(Version.LONG),
								anyObject(Params.class))).andReturn(m());
			}

			@Override
			protected void test(EasyMockContext context) throws Exception {
				WireFeed f = c.toFeed(n(), p(), c.newDefaultPreferences());
				assertNotNull(f);
				assertTrue(f instanceof Feed);
				assertEquals(1, ((Feed) f).getEntries().size());
			}
		}.run();
	}

	@Test
	public void testNewDefaultPreferences() {
		ChannelPreferences p = c().newDefaultPreferences();

		assertTrue(p instanceof IRequestConfigurable);
		// configured by default
		assertTrue(c().isConfigured(p));
	}

	@Test
	public void testIsConfigured() {
		ChannelPreferences p = c().newDefaultPreferences();
		assertTrue(c().isConfigured(p));
	}

	private abstract static class FeedMockTest extends MockTest {
		AbstractFeedChannel c;

		@Override
		protected void setup(EasyMockContext context) throws Exception {
			c = c();
			c.setNotificationsDAO(context.create(INotificationsDAO.class));
			c.setPreferencesDAO(context.create(IPreferencesDAO.class));
			c.setRenderService(context.create(IRenderService.class));
		}
	}

}
