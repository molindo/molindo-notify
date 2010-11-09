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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import at.molindo.notify.channel.mail.IMailClient.MailException;
import at.molindo.utils.data.StringUtils;
import at.molindo.utils.io.CharsetUtils;

public class MailUtils {
	private MailUtils() {
	}

	public static String domainFromAddress(String address) throws MailException {
		String[] parts = new String[2];
		try {
			if (StringUtils.split(address, "@", parts) == 2) {
				return parts[1];
			} else {
				throw new MailException("illegal address: " + address, false);
			}
		} catch (IndexOutOfBoundsException e) {
			throw new MailException("illegal address: " + address, e, false);
		}
	}

	public static String toString(MimeMessage mm) throws MessagingException {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			mm.writeTo(os);

			String message = new String(os.toByteArray(), CharsetUtils.US_ASCII);
			return message;
		} catch (IOException e) {
			throw new RuntimeException("can't write to ByteArrayOutputStream", e);
		}
	}
}
