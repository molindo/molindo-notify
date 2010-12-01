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

package at.molindo.notify.channel.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.Test;

import at.molindo.notify.model.Message;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.notify.render.IRenderService.Type;
import at.molindo.notify.test.util.EasyMockContext;
import at.molindo.notify.test.util.MockTest;

public class PatternMailClientTest {
	private PatternMailClient client() {
		PatternMailClient client = new PatternMailClient();
		client.setDefaultClient(new SimpleMailClient());
		client.setAlternativeClient(new SimpleMailClient());
		client.setClientPatterns(Arrays.asList("hotmail\\.[a-z]{2,4}$", "live\\.[a-z]{2,4}$", "msn\\.[a-z]{2,4}$",
				"^gabriel\\.cc$"));
		return client;
	}

	@Test
	public void testSend() throws Exception {
		new MockTest() {

			private PatternMailClient _client;

			@Override
			protected void setup(EasyMockContext context) throws Exception {
				_client = client();
				_client.setAlternativeClient(context.create(IMailClient.class));

				context.get(IMailClient.class).send(EasyMock.anyObject(Message.class),
						EasyMock.anyObject(PushChannelPreferences.class));

			}

			@Override
			protected void test(EasyMockContext context) throws Exception {
				PushChannelPreferences cPrefs = new PushChannelPreferences();
				MailChannel.setRecipient(cPrefs, "mugel@gabriel.cc");
				_client.send(new Message("subject", "message", Type.TEXT), cPrefs);
			}

		}.run();

	}

	@Test
	public void testGetClient() {
		PatternMailClient client = client();
		assertSame(client.getDefaultClient(), client.getClient(""));
		assertSame(client.getDefaultClient(), client.getClient("gmail.com"));
		assertSame(client.getAlternativeClient(), client.getClient("mail2.hotmail.com"));
		assertSame(client.getAlternativeClient(), client.getClient("gabriel.cc"));
	}

	@Test
	public void testGetServer() {
		PatternMailClient client = client();

		assertEquals("molindo.at", client.getServer("stf@molindo.at"));
		assertEquals("", client.getServer("root"));
		assertEquals("", client.getServer(null));
	}
}
