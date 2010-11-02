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

import java.io.StringReader;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.WireFeedOutput;

public class FeedUtils {

	private FeedUtils() {

	}

	/**
	 * 
	 * @param feed
	 * @return
	 * @throws FeedException
	 * @throws IllegalArgumentException
	 *             if title or updated aren't set
	 */
	public static String toFeedXml(final WireFeed feed) throws FeedException {
		return toFeedXml(feed, false);
	}

	/**
	 * 
	 * @param feed
	 * @param debug
	 *            pretty printed if true
	 * @return
	 * @throws FeedException
	 * @throws IllegalArgumentException
	 *             if title or updated aren't set
	 */
	public static String toFeedXml(final WireFeed feed, final boolean debug) throws FeedException {
		
		if (feed instanceof Feed) {
			if (((Feed)feed).getTitle() == null) {
				throw new IllegalArgumentException("feed title must be set");
			}
			if (((Feed)feed).getUpdated() == null) {
				throw new IllegalArgumentException("feed updated must be set");
			}
		} else if (feed instanceof Channel) {
			if (((Channel)feed).getTitle() == null) {
				throw new IllegalArgumentException("feed title must be set");
			}
		}
		
		final Document doc = new WireFeedOutput().outputJDom(feed);

		final Format format = debug ? Format.getPrettyFormat() : Format.getRawFormat();
		format.setOmitDeclaration(true);
		format.setOmitEncoding(true);

		final XMLOutputter outputter = new XMLOutputter(format);
		return outputter.outputString(doc);
	}

	public static Feed fromFeedXml(final String feedXml) throws FeedException {
		return (Feed) new WireFeedInput(false).build(new StringReader(feedXml));
	}
}
