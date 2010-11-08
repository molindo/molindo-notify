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

package at.molindo.notify.servlet;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reportMatcher;
import static org.junit.Assert.assertEquals;

import javax.servlet.FilterChain;

import org.easymock.IArgumentMatcher;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import at.molindo.notify.channel.IPullChannel;
import at.molindo.notify.channel.feed.AbstractPullChannel;
import at.molindo.notify.model.ConfigurableChannelPreferences;
import at.molindo.notify.model.Notification.Type;
import at.molindo.notify.test.util.EasyMockContext;
import at.molindo.notify.test.util.MockTest;


public class NotifyFilterTest {
	
	private static final String SECRET = "test-secret";
	private static final String CHANNELID = "test-channel";
	private static final String USERID = "test-user";
	private static final String BODY = "{body: 'this is a test'}";

	@Test
	public void pull() throws Exception {
		new MockTest() {
			
			NotifyFilter filter;
			FilterChain mockFilterChain;
			MockFilterConfig config;
			
			@Override
			protected void setup(EasyMockContext context) throws Exception {
				filter = new NotifyFilter();
				mockFilterChain = new MockFilterChain();
				config = new MockFilterConfig();
				
				context.create(IPullChannel.class);
				
				expect(context.get(IPullChannel.class).getId()).andReturn(CHANNELID).anyTimes();
				expect(context.get(IPullChannel.class).getNotificationTypes()).andReturn(Type.TYPES_ALL).anyTimes();
				expect(context.get(IPullChannel.class).pull(eq(USERID), anyObject(ConfigurableChannelPreferences.class))).andReturn(BODY);
				expect(context.get(IPullChannel.class).newDefaultPreferences()).andReturn(new ConfigurableChannelPreferences());
				
				eq(USERID);
				reportMatcher(new IArgumentMatcher() {

					@Override
					public boolean matches(Object argument) {
						ConfigurableChannelPreferences cPrefs = (ConfigurableChannelPreferences) argument;
						return SECRET.equals(cPrefs.getParams().get(AbstractPullChannel.SECRET));
					}

					@Override
					public void appendTo(StringBuffer buffer) {
						buffer.append("secret not included");
					}
				});
				expect(context.get(IPullChannel.class).isAuthorized(null, null)).andReturn(true);

				expect(context.get(IPullChannel.class).isConfigured(eq(USERID), anyObject(ConfigurableChannelPreferences.class))).andReturn(true);
			}
			
			@Override
			protected void test(EasyMockContext context) throws Exception {
				NotifyFilter.addChannel(context.get(IPullChannel.class), config.getServletContext());
				filter.init(config);
				
				MockHttpServletRequest request = new MockHttpServletRequest("GET", "/notify/" + CHANNELID + "/" + USERID);
				request.setServletPath("/notify");
				request.setParameter(AbstractPullChannel.SECRET.getName(), SECRET);
				
				MockHttpServletResponse response = new MockHttpServletResponse();
				
				filter.doFilter(request , response, mockFilterChain);
				
				assertEquals(200, response.getStatus());
				assertEquals(BODY, response.getContentAsString());
			}
			
		}.run();
	}

}
