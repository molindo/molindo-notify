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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import at.molindo.notify.model.ChannelPreferences;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.render.IRenderService.RenderException;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.io.FeedException;

public abstract class AbstractFeedChannel extends AbstractPullChannel {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(AbstractFeedChannel.class);

	private String _authorName;

	@Override
	protected String pull(List<Message> messages, Date lastModified,
			ChannelPreferences cPrefs, Preferences prefs) throws PullException {
		
		try {
			return FeedUtils.toFeedXml(toFeed(messages, lastModified, prefs, cPrefs));
		} catch (FeedException e) {
			throw new PullException("failed to serialize feed", e);
		} catch (RenderException e) {
			log.info("failed to render feed", e);
			return null;
		}
	}

	public WireFeed toFeed(List<Message> messages, Date lastModified, Preferences prefs,
			ChannelPreferences cPrefs) throws RenderException {
		final Feed f = new Feed("atom_1.0");

		f.setTitle("TODO title");
		f.setUpdated(lastModified == null ? new Date() : lastModified);

		Person author = new Person();
		author.setName(_authorName);

		f.setAuthors(Arrays.asList(author));

		final List<Entry> entries = Lists.newArrayList();

		for (final Message msg : messages) {
			final Entry e = new Entry();
			final Content c = new Content();

			c.setType("text/html");
			c.setValue(msg.getHtml());

			e.setTitle(msg.getSubject());
			e.setContents(Arrays.asList(c));
			entries.add(e);
		}

		f.setEntries(entries);
		return f;
	}

	@Override
	public boolean isConfigured(ChannelPreferences prefs) {
		// TODO validation
		return super.isConfigured(prefs);
	}

	public String getAuthorName() {
		return _authorName;
	}

	public void setAuthorName(String authorName) {
		_authorName = authorName;
	}

}
