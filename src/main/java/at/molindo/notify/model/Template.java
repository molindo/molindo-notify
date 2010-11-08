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
import java.util.Locale;

import at.molindo.notify.render.IRenderService.Type;
import at.molindo.notify.render.IRenderService.Version;

public class Template implements Cloneable {

	private String _key;
	private Type _type = Type.HTML;
	private Version _version = Version.LONG;
	private Date _lastModified;
	private Locale _locale;
	private String _content;

	public Template() {
		
	}
	
	public String getKey() {
		return _key;
	}

	public Template setKey(String key) {
		_key = key;
		return this;
	}

	public Type getType() {
		return _type;
	}

	public Template setType(Type type) {
		if (type == null) {
			throw new NullPointerException("type");
		}
		_type = type;
		return this;
	}

	public Version getVersion() {
		return _version;
	}

	public Template setVersion(Version version) {
		if (version == null) {
			throw new NullPointerException("version");
		}
		_version = version;
		return this;
	}

	public String getContent() {
		return _content;
	}

	public Template setContent(String content) {
		_content = content;
		return this;
	}

	public Date getLastModified() {
		return _lastModified;
	}

	public Template setLastModified(Date lastModified) {
		_lastModified = lastModified;
		return this;
	}

	public Key key() {
		return getKey() == null || getVersion() == null ? null : new Key(
				getKey(), getVersion());
	}

	public Locale getLocale() {
		return _locale;
	}

	public Template setLocale(Locale locale) {
		_locale = locale;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getKey() == null) ? 0 : getKey().hashCode());
		result = prime * result + getVersion().hashCode();
		result = prime * result + getType().hashCode();
		result = prime
				* result
				+ ((getLastModified() == null) ? 0 : getLastModified()
						.hashCode());
		result = prime * result
				+ ((getLocale() == null) ? 0 : getLocale().hashCode());
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
		if (getKey() == null) {
			if (other.getKey() != null)
				return false;
		} else if (!getKey().equals(other.getKey()))
			return false;
		if (getType() != other.getType())
			return false;
		if (getVersion() != other.getVersion())
			return false;
		if (getLastModified() == null) {
			if (other.getLastModified() != null)
				return false;
		} else if (!getLastModified().equals(other.getLastModified()))
			return false;
		if (getLocale() == null) {
			if (other.getLocale() != null)
				return false;
		} else if (!getLocale().equals(other.getLocale()))
			return false;
		if (getContent() == null) {
			if (other.getContent() != null)
				return false;
		} else if (!getContent().equals(other.getContent()))
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

	@Override
	public Template clone() {
		try {
			Template t = (Template) super.clone();
			if (_lastModified != null)
				t._lastModified = new Date(_lastModified.getTime());
			if (_locale != null)
				t._locale = (Locale) _locale.clone();
			return t;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("clone object not supported?", e);
		}
	}

}
