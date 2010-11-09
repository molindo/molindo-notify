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

package at.molindo.notify.model;

import java.util.Map;

import com.google.common.collect.Maps;

public class Preferences implements Cloneable {

	private String _userId;

	private Params _params;

	private Map<String, PushChannelPreferences> _channelPrefs = Maps.newHashMap();

	public String getUserId() {
		return _userId;
	}

	public Preferences setUserId(String userId) {
		_userId = userId;
		return this;
	}

	public Map<String, PushChannelPreferences> getChannelPrefs() {
		return _channelPrefs;
	}

	public Preferences setChannelPrefs(Map<String, PushChannelPreferences> channelPrefs) {
		_channelPrefs.clear();
		_channelPrefs.putAll(channelPrefs);
		return this;
	}

	public Preferences addChannelPrefs(String channelId, PushChannelPreferences cPrefs) {
		_channelPrefs.put(channelId, cPrefs);
		return this;
	}

	public Params getParams() {
		return _params;
	}

	public Preferences setParams(Params params) {
		_params = params;
		return this;
	}

	@Override
	public Preferences clone() {

		try {
			Preferences p = (Preferences) super.clone();
			p._params = _params.clone();
			p._channelPrefs = Maps.newHashMap();
			for (Map.Entry<String, PushChannelPreferences> e : _channelPrefs.entrySet()) {
				p._channelPrefs.put(e.getKey(), e.getValue().clone());
			}

			return p;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("clone object not supported?", e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getChannelPrefs() == null) ? 0 : getChannelPrefs().size());
		result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Preferences)) {
			return false;
		}
		Preferences other = (Preferences) obj;

		if (getUserId() == null) {
			if (other.getUserId() != null) {
				return false;
			}
		} else if (!getUserId().equals(other.getUserId())) {
			return false;
		}
		if (getParams() == null) {
			if (other.getParams() != null) {
				return false;
			}
		} else if (!getParams().equals(other.getParams())) {
			return false;
		}
		if (getChannelPrefs() == null) {
			if (other.getChannelPrefs() != null) {
				return false;
			}
		} else if (!getChannelPrefs().equals(other.getChannelPrefs())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Preferences [userId=" + getUserId() + "]";
	}

}
