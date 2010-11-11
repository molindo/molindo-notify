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


public class ParamValue {
	private ParamType _type;
	private String _name;
	private Object _value;
	private String _stringValue;

	public ParamValue() {
	}

	public <T> ParamValue(Param<T> param, T value) {
		setType(param.type());
		setName(param.getName());
		setValue(value);
	}

	public ParamType getType() {
		return _type;
	}

	public void setType(ParamType type) {
		if (type == null) {
			throw new NullPointerException("type");
		}
		_type = type;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		_name = name;
	}

	public Object getValue() {
		if (_value == null && _stringValue != null) {
			_value = param().toObject(_stringValue);
		}

		return _value;
	}

	public void setValue(Object value) {
		if (value == null) {
			throw new NullPointerException("value");
		}
		_value = value;
	}

	public String getStringValue() {
		return param().toString(getValue());
	}

	public void setStringValue(String stringValue) {
		_value = null;
		_stringValue = stringValue;
	}

	protected Param<?> param() {
		ParamType t = getType();
		String n = getName();
		if (t == null || n == null) {
			throw new IllegalStateException("type (was " + t + ") and name (was " + n + ") must be set");
		}
		return t.p(n);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (_name == null ? 0 : _name.hashCode());
		result = prime * result + (_type == null ? 0 : _type.hashCode());
		result = prime * result + (_value == null ? 0 : _value.hashCode());
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
		if (!(obj instanceof ParamValue)) {
			return false;
		}
		ParamValue other = (ParamValue) obj;
		if (_name == null) {
			if (other._name != null) {
				return false;
			}
		} else if (!_name.equals(other._name)) {
			return false;
		}
		if (_type != other._type) {
			return false;
		}
		if (_value == null) {
			if (other._value != null) {
				return false;
			}
		} else if (!_value.equals(other._value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Value [type=" + _type + ", name=" + _name + ", value=" + _value + "]";
	}

}