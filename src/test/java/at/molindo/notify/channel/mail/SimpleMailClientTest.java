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

import static org.junit.Assert.assertTrue;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;

import org.junit.Test;

import at.molindo.notify.channel.mail.IMailClient.MailException;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.PushChannelPreferences;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.IRenderService.Type;
import at.molindo.utils.net.DnsUtils;

public class SimpleMailClientTest {

	private static final boolean SEND = false;

	@Test
	public void testSend() throws AddressException, NamingException,
			RenderException, MailException {

		final boolean[] sent = { false };

		SimpleMailClient client = (SimpleMailClient) new SimpleMailClient() {
			@Override
			protected void send(MimeMessage mm) throws MessagingException {
				String message = MailUtils.toString(mm);
				// System.out.println(message);

				assertTrue(message
						.contains("From: SimpleMailClientTest <test@test.molindo.at>"));
				assertTrue(message
						.contains("Sender: SimpleMailClientTest <test@test.molindo.at>"));
				assertTrue(message
						.contains("To: sfussenegger <stf+test@molindo.at>"));
				assertTrue(message.contains("Subject: Test"));
				assertTrue(message
						.contains("Content-Type: multipart/alternative;"));
				assertTrue(message.contains("X-Mailer: molindo-notify"));
				assertTrue(message
						.contains("Content-Type: text/plain; charset=UTF-8"));
				assertTrue(message
						.contains("Content-Type: text/html; charset=UTF-8"));

				if (SEND) {
					super.send(mm);
				}

				sent[0] = true;
			}
		}.setServer(DnsUtils.lookupMailHosts("molindo.at")[0])
				.setFrom("test@test.molindo.at",
						SimpleMailClientTest.class.getSimpleName()).init();

		Message message = Message.parse(
				"Subject: Test\n\nThis is a <strong>test</strong>", Type.HTML);

		PushChannelPreferences cPrefs = new PushChannelPreferences();
		MailChannel.setRecipient(cPrefs, "stf+test@molindo.at");
		MailChannel.setRecipientName(cPrefs, "sfussenegger");

		client.send(message, cPrefs);

		// poor man's assertion
		assertTrue(sent[0]);
	}

}
