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

import at.molindo.notify.render.IRenderService.Version;

public class ChannelPreferences implements Cloneable {

	private Version _version = Version.LONG;
	private Params _params = new Params();

	public ChannelPreferences() {
		this(null);
	}

	public ChannelPreferences(Params defaults) {
		_params = new Params(defaults);
	}

	public Version getVersion() {
		return _version;
	}

	public void setVersion(Version version) {
		if (version == null) {
			throw new NullPointerException("version");
		}
		_version = version;
	}

	protected void setParams(Params params) {
		if (params == null) {
			throw new NullPointerException("params");
		}
		_params = params;
	}

	public Params getParams() {
		return _params;
	}

	@Override
	public ChannelPreferences clone() {
		try {
			ChannelPreferences p = (ChannelPreferences) super.clone();
			p._params = _params.clone();
			return p;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("clone object not supported?", e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getVersion() == null ? 0 : getVersion().hashCode());
		result = prime * result + (getParams() == null ? 0 : getParams().hashCode());
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
		if (!(obj instanceof ChannelPreferences)) {
			return false;
		}
		ChannelPreferences other = (ChannelPreferences) obj;
		if (getVersion() == null) {
			if (other.getVersion() != null) {
				return false;
			}
		} else if (!getVersion().equals(other.getVersion())) {
			return false;
		}
		if (getParams() == null) {
			if (other.getParams() != null) {
				return false;
			}
		} else if (!getParams().equals(other.getParams())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ChannelPreferences [params=" + getParams() + "]";
	}
}
