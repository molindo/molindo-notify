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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import at.molindo.notify.model.Message;
import at.molindo.notify.model.PushChannelPreferences;

import com.google.common.collect.Iterators;

public class RoundRobinMailClient implements IMailClient {

	private CopyOnWriteArrayList<IMailClient> _clients = new CopyOnWriteArrayList<IMailClient>();

	private Object _mutex = new Object();
	private Iterator<IMailClient> _iter = newIterator();

	@Override
	public void send(Message message, PushChannelPreferences cPrefs) throws MailException {
		getNext().send(message, cPrefs);
	}

	private IMailClient getNext() throws MailException {
		synchronized (_mutex) {
			try {
				return _iter.next();
			} catch (NoSuchElementException e) {
				throw new MailException("no client available", e, true);
			}
		}
	}

	public void setClients(Collection<IMailClient> clients) {
		synchronized (_mutex) {
			_clients.clear();
			_clients.addAll(clients);
			_iter = newIterator();
		}
	}

	private Iterator<IMailClient> newIterator() {
		return Iterators.cycle(_clients);
	}

}
