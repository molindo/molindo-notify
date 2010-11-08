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

import java.net.URL;

import at.molindo.utils.data.StringUtils;

public class Param<T> {

	private String _name;
	private Class<T> _type;

	public static Param<String> pString(String name) {
		return p(name, String.class);
	}
	
	public static Param<Integer> pInteger(String name) {
		return p(name, Integer.class);
	}
	
	public static Param<Long> pLong(String name) {
		return p(name, Long.class);
	}
	
	public static Param<Double> pDouble(String name) {
		return p(name, Double.class);
	}
	
	public static Param<Float> pFloat(String name) {
		return p(name, Float.class);
	}
	
	public static Param<Boolean> pBoolean(String name) {
		return p(name, Boolean.class);
	}
	
	public static Param<Character> pCharacter(String name) {
		return p(name, Character.class);
	}
	
	public static Param<URL> pURL(String name) {
		return p(name, URL.class);
	}
	
	public static <T> Param<T> p(String name, Class<T> type) {
		return new Param<T>(name, type);
	}

	protected Param() {

	}

	public Param(String name, Class<T> type) {
		setName(name);
		setType(type);
	}

	public String getName() {
		return _name;
	}

	protected void setName(String name) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		if (StringUtils.empty(name)) {
			throw new IllegalArgumentException("name must not be empty");
		}
		_name = name;
	}

	public Class<T> getType() {
		return _type;
	}

	protected void setType(Class<T> type) {
		if (type == null) {
			throw new NullPointerException("type");
		}
		_type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result
				+ ((getType() == null) ? 0 : getType().getName().hashCode());
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
		if (!(obj instanceof Param)) {
			return false;
		}
		Param<?> other = (Param<?>) obj;
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		if (getType() == null) {
			if (other.getType() != null) {
				return false;
			}
		} else if (!getType().getName().equals(other.getType().getName())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Param [name=" + _name + ", type=" + _type == null ? null
				: _type.getSimpleName() + "]";
	}

}
