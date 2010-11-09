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

import java.util.Date;
import java.util.List;

import at.molindo.notify.channel.IPullChannel;
import at.molindo.notify.dao.INotificationDAO;
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
import at.molindo.utils.data.StringUtils;

import com.google.common.collect.Lists;

public abstract class AbstractPullChannel implements IPullChannel {

	private IRenderService _renderService;
	private INotificationDAO _notificationDAO;
	private IPreferencesDAO _preferencesDAO;

	public static final Param<String> SECRET = Param.pString("secret");
	public static final Param<Integer> AMOUNT = Param.pInteger("amount");

	public static final int MAX_AMOUNT = 100;
	public static final int DEFAULT_AMOUNT = 25;

	private Integer _defaultAmount = DEFAULT_AMOUNT;

	@Override
	public boolean isAuthorized(String userId, ChannelPreferences cPrefs) {
		Preferences p = _preferencesDAO.getPreferences(userId);
		if (p == null) {
			return false;
		}
		String prefSecret = p.getParams().get(SECRET);
		String reqSecret = cPrefs.getParams().get(SECRET);

		return StringUtils.equals(prefSecret, reqSecret);
	}

	@Override
	public boolean isConfigured(String userId, ChannelPreferences prefs) {
		return prefs.getParams().containsAll(AbstractFeedChannel.AMOUNT);
	}

	@Override
	public final String pull(String userId, ChannelPreferences cPrefs) throws PullException {

		Preferences prefs = _preferencesDAO.getPreferences(userId);

		int amount = cPrefs.getParams().get(AMOUNT);

		List<Notification> notifications = _notificationDAO.getRecent(userId, getNotificationTypes(), 0, amount);
		if (notifications.size() == 0) {
			throw new PullException("no notifications found");
		}

		Date lastModified = notifications.get(0).getDate();
		List<Message> messages = Lists.newArrayListWithCapacity(notifications.size());
		for (Notification notification : notifications) {
			try {
				messages.add(render(notification, prefs, cPrefs));
			} catch (RenderException e) {
				throw new PullException("failed to render notification: " + notification, e);
			}
		}

		return pull(messages, lastModified, cPrefs, prefs);

	}

	@Override
	public ChannelPreferences newDefaultPreferences() {
		ChannelPreferences prefs = new FeedChannelPreferences(new Params().set(AMOUNT, _defaultAmount));
		prefs.setVersion(Version.LONG);
		return prefs;
	}

	protected Message render(final Notification notification, Preferences prefs, ChannelPreferences cPrefs)
			throws RenderException {
		return NotifyUtils.render(_renderService, notification, prefs, cPrefs);
	}

	public Integer getDefaultAmount() {
		return _defaultAmount;
	}

	public void setDefaultAmount(Integer defaultAmount) {
		_defaultAmount = defaultAmount;
	}

	public void setNotificationDAO(INotificationDAO notificationDAO) {
		_notificationDAO = notificationDAO;
	}

	public void setPreferencesDAO(IPreferencesDAO preferencesDAO) {
		_preferencesDAO = preferencesDAO;
	}

	public void setRenderService(IRenderService renderService) {
		_renderService = renderService;
	}

	protected abstract String pull(List<Message> messages, Date lastModified, ChannelPreferences cPrefs,
			Preferences prefs) throws PullException;

}
