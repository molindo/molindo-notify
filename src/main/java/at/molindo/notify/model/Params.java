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

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

public class Params implements IParams {

	private Map<String, ParamValue> _params = Maps.newHashMap();

	public Params() {

	}

	public Params(IParams... params) {
		for (IParams p : params) {
			setAll(p);
		}
	}

	public <T> IParams setString(Param<T> param, String value) {
		return set(param, param.toObject(value));
	}

	@Override
	public <T> IParams set(Param<T> param, T value) {
		if (value == null) {
			_params.remove(param.getName());
		} else {
			_params.put(param.getName(), param.value(value));
		}
		return this;
	}

	@Override
	public <T> T get(Param<T> param) {
		ParamValue v = _params.get(param.getName());
		if (v == null || v.getValue() == null) {
			return null;
		}

		if (param.getType().isAssignableFrom(v.getValue().getClass())) {
			// compatible types
			return param.getType().cast(v.getValue());
		} else {
			// convert to string and back to object
			Param<?> valueParam = v.getType().p(param.getName());
			String valueStr = valueParam.toString(v.getValue());
			return param.toObject(valueStr);
		}
	}

	@Override
	public boolean isSet(Param<?> param) {
		return _params.get(param.getName()) != null;
	}

	@Override
	public boolean containsAll(Param<?>... params) {
		for (Param<?> p : params) {
			if (!_params.containsKey(p.getName())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IParams setAll(IParams params) {
		IParams.Util.setAll(this, params);
		return this;
	}

	@Override
	public Map<String, Object> newMap() {
		return IParams.Util.newMap(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (_params == null ? 0 : _params.hashCode());
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
			p._params = Maps.newHashMap(_params);
			return p;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("can't clone object?", e);
		}
	}

	protected Map<String, ParamValue> getValues() {
		return _params;
	}

	protected void setValues(Map<String, ParamValue> params) {
		if (params == null) {
			throw new NullPointerException("params");
		}
		_params = params;
	}

	@Override
	public Iterator<ParamValue> iterator() {
		return _params.values().iterator();
	}

}
