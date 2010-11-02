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

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

public class Params implements Cloneable {

	private Map<String, Object> _params = Maps.newHashMap();

	private final Params _defaults;
	
	public Params() {
		this(null);
	}

	public Params(Params defaults) {
		if (defaults != null) {
			_defaults = defaults.clone();
			_params.putAll(_defaults._params);
		} else {
			_defaults = null;
		}
	}
	
	public <T> Params set(Param<T> param, T value) {
		if (value == null) {
			if (_params.remove(param.getName()) != null && _defaults != null) {
				T def = _defaults.get(param);
				if (def != null) {
					_params.put(param.getName(), def);
				}
			}
		} else {
			_params.put(param.getName(), value);
		}
		return this;
	}

	public <T> T get(Param<T> param) {
		Object v = _params.get(param.getName());
		Class<T> t = param.getType();
		return t.isInstance(v) ? t.cast(v) : null;
	}

	public boolean containsAll(Param<?>... params) {
		for (Param<?> p : params) {
			if (!_params.containsKey(p.getName())) {
				return false;
			}
		}
		return true;
	}

	public Params setAll(Params params) {
		if (params != null) {
			_params.putAll(params._params);
		}
		return this;
	}

	public Map<String, Object> newMap() {
		Map<String, Object> map = Maps.newHashMap();
		map.putAll(_params);
		return map;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_params == null) ? 0 : _params.hashCode());
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
		if (!(obj instanceof Params)) {
			return false;
		}
		Params other = (Params) obj;
		if (_params == null) {
			if (other._params != null) {
				return false;
			}
		} else if (!_params.equals(other._params)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Params [params=" + _params + "]";
	}

	@Override
	protected Params clone() {
		Params p;
		try {
			p = (Params) super.clone();
			p._params = new HashMap<String, Object>(_params);
			return p;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("can't clone object?", e);
		}
		
	}
	
	
}
