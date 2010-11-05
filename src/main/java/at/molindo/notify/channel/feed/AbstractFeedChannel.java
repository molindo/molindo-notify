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

import com.google.common.collect.Lists;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.io.FeedException;

import at.molindo.notify.channel.IPullChannel;
import at.molindo.notify.dao.INotificationsDAO;
import at.molindo.notify.dao.IPreferencesDAO;
import at.molindo.notify.model.ChannelPreferences;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.Preferences;
import at.molindo.notify.render.IRenderService;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.IRenderService.Version;
import at.molindo.notify.util.NotifyUtils;

public abstract class AbstractFeedChannel implements IPullChannel {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(AbstractFeedChannel.class);
	
	public static final Param<Integer> AMOUNT =  Param.p("amount", Integer.class);
	
	public static final int MAX_AMOUNT = 100;
	public static final int DEFAULT_AMOUNT = 25;

	private Integer _defaultAmount = DEFAULT_AMOUNT;
	
	private INotificationsDAO _notificationsDAO;
	private IPreferencesDAO _preferencesDAO;
	private IRenderService _renderService;
	private String _authorName;

	@Override
	public String pull(String userId, ChannelPreferences cPrefs)
			throws PullException {

		Preferences prefs = _preferencesDAO.getPreferences(userId);

		int amount = cPrefs.getParams().get(AMOUNT);

		
		List<Notification> notifications = _notificationsDAO.getRecent(userId,
				getNotificationTypes(), 0, amount);
		if (notifications.size() == 0) {
			throw new PullException("no notifications found");
		}
		
		try {
			WireFeed feed = toFeed(notifications, prefs, cPrefs);
			return FeedUtils.toFeedXml(feed);
		} catch (FeedException e) {
			throw new PullException("failed to serialize feed", e);
		} catch (RenderException e) {
			log.info("failed to render feed", e);
			return null;
		}
	
	}

	public WireFeed toFeed(List<Notification> notifications, Preferences prefs, ChannelPreferences cPrefs) throws RenderException {
		final Feed f = new Feed("atom_1.0");
		f.setTitle("TODO title");

		final Date lastModified = notifications.iterator().next().getDate();
		f.setUpdated(lastModified == null ? new Date() : lastModified);

		Person author = new Person();
		author.setName(_authorName);

		f.setAuthors(Arrays.asList(author));

		final List<Entry> entries = Lists.newArrayList();

		for (final Notification notification : notifications) {
			final Entry e = new Entry();
			final Content c = new Content();

			Message msg = NotifyUtils.render(_renderService, notification, prefs, cPrefs);
			
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
	public ChannelPreferences newDefaultPreferences() {
		ChannelPreferences prefs = new FeedChannelPreferences(new Params().set(AMOUNT, _defaultAmount));
		prefs.setVersion(Version.LONG);
		return prefs;
	}

	@Override
	public boolean isConfigured(ChannelPreferences prefs) {
		// TODO validation
		return prefs.getParams().containsAll(AbstractFeedChannel.AMOUNT);
	}

	public Integer getDefaultAmount() {
		return _defaultAmount;
	}

	public void setDefaultAmount(Integer defaultAmount) {
		_defaultAmount = defaultAmount;
	}

	public String getAuthorName() {
		return _authorName;
	}

	public void setAuthorName(String authorName) {
		_authorName = authorName;
	}

	public void setNotificationsDAO(INotificationsDAO notificationsDAO) {
		_notificationsDAO = notificationsDAO;
	}

	public void setPreferencesDAO(IPreferencesDAO preferencesDAO) {
		_preferencesDAO = preferencesDAO;
	}

	public void setRenderService(IRenderService renderService) {
		_renderService = renderService;
	}
	
}
