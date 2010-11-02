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

import java.util.Date;

import at.molindo.notify.render.IRenderService.Version;

public class Template {

	private String _key;
	private Version _version = Version.LONG;
	private Date _lastModified;
	private String _content;

	public String getKey() {
		return _key;
	}

	public void setKey(String key) {
		_key = key;
	}

	public Version getVersion() {
		return _version;
	}

	public void setVersion(Version version) {
		_version = version;
	}

	public String getContent() {
		return _content;
	}

	public void setContent(String content) {
		_content = content;
	}

	public Date getLastModified() {
		return _lastModified;
	}

	public void setLastModified(Date lastModified) {
		_lastModified = lastModified;
	}

	public Key key() {
		return getKey() == null || getVersion() == null ? null : new Key(
				getKey(), getVersion());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getContent() == null) ? 0 : getContent().hashCode());
		result = prime * result
				+ ((getKey() == null) ? 0 : getKey().hashCode());
		result = prime
				* result
				+ ((getLastModified() == null) ? 0 : getLastModified()
						.hashCode());
		result = prime * result
				+ ((getVersion() == null) ? 0 : getVersion().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Template == false)
			return false;
		Template other = (Template) obj;
		if (getContent() == null) {
			if (other.getContent() != null)
				return false;
		} else if (!getContent().equals(other.getContent()))
			return false;
		if (getKey() == null) {
			if (other.getKey() != null)
				return false;
		} else if (!getKey().equals(other.getKey()))
			return false;
		if (getLastModified() == null) {
			if (other.getLastModified() != null)
				return false;
		} else if (!getLastModified().equals(other.getLastModified()))
			return false;
		if (getVersion() != other.getVersion())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Template [_key=" + _key + ", _version=" + _version
				+ ", _lastModified=" + _lastModified + "]";
	}

	public static final class Key {

		private final String _key;
		private final Version _version;

		private Key(String key, Version version) {
			if (key == null) {
				throw new NullPointerException("key");
			}
			if (version == null) {
				throw new NullPointerException("version");
			}
			_key = key;
			_version = version;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + _key.hashCode();
			result = prime * result + _version.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (obj instanceof Key == false)
				return false;
			Key other = (Key) obj;

			if (!_key.equals(other._key))
				return false;
			if (_version != other._version)
				return false;
			return true;
		}
	}
}
