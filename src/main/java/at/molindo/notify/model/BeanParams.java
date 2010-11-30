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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import at.molindo.notify.INotifyService.NotifyRuntimeException;

public class BeanParams<B> implements IParams {

	private final B _bean;

	public static <B> BeanParams<B> params(B bean) {
		return new BeanParams<B>(bean);
	}

	public BeanParams(B bean) {
		if (bean == null) {
			throw new NullPointerException("bean");
		}
		_bean = bean;

	}

	public B getBean() {
		return _bean;
	}

	@Override
	public <T> IParams set(Param<T> param, T value) {
		setProperty(param, value);
		return this;
	}

	@Override
	public <T> T get(Param<T> param) {
		Object o = getProperty(param.getName());
		if (o == null) {
			return null;
		} else if (param.getType().isAssignableFrom(o.getClass())) {
			return param.getType().cast(o);
		} else if (o instanceof String) {
			return param.toObject((String) o);
		} else {
			return param.toObject(param.toString(o));
		}
	}

	@Override
	public boolean isSet(Param<?> param) {
		return getProperty(param.getName()) != null;
	}

	@Override
	public Iterator<ParamValue> iterator() {
		return new Iterator<ParamValue>() {

			private final Iterator<PropertyDescriptor> _iter = getDescriptorsIter();
			private ParamValue _next = findNext();

			private ParamValue findNext() {
				while (_iter.hasNext()) {
					PropertyDescriptor pd = _iter.next();
					if (pd.getWriteMethod() == null || pd.getReadMethod() == null) {
						continue;
					}
					Object value = invoke(pd.getReadMethod());
					if (value == null) {
						continue;
					}
					return Param.p(pd.getPropertyType(), pd.getName()).paramValue(value);
				}
				return null;
			}

			@Override
			public boolean hasNext() {
				return _next != null;
			}

			@Override
			public ParamValue next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				ParamValue next = _next;
				_next = findNext();
				return next;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean containsAll(Param<?>... params) {
		return IParams.Util.containsAll(this, params);
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

	private PropertyDescriptor getDescriptor(String propertyName) {
		try {
			PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(_bean, propertyName);
			return pd != null && pd.getWriteMethod() != null && pd.getReadMethod() != null ? pd : null;
		} catch (IllegalAccessException e) {
			throw new NotifyRuntimeException("failed to get PropertyDescriptor for property " + propertyName
					+ " from bean " + _bean, e);
		} catch (InvocationTargetException e) {
			throw new NotifyRuntimeException("failed to get PropertyDescriptor for property " + propertyName
					+ " from bean " + _bean, e);
		} catch (NoSuchMethodException e) {
			// ignore
			return null;
		}
	}

	private Iterator<PropertyDescriptor> getDescriptorsIter() {
		return Arrays.asList(PropertyUtils.getPropertyDescriptors(_bean)).iterator();
	}

	private Object getProperty(String name) {
		try {
			return BeanUtils.getProperty(_bean, name);
		} catch (IllegalAccessException e) {
			throw new NotifyRuntimeException("failed to get property " + name + " from bean " + _bean, e);
		} catch (InvocationTargetException e) {
			throw new NotifyRuntimeException("failed to get property " + name + " from bean " + _bean, e);
		} catch (NoSuchMethodException e) {
			// ignore
			return null;
		}
	}

	private Object invoke(Method m, Object... args) {
		try {
			return m.invoke(_bean, args);
		} catch (IllegalArgumentException e) {
			throw new NotifyRuntimeException("failed to invoke method " + m + " on " + _bean, e);
		} catch (IllegalAccessException e) {
			throw new NotifyRuntimeException("failed to invoke method " + m + " on " + _bean, e);
		} catch (InvocationTargetException e) {
			throw new NotifyRuntimeException("failed to invoke method " + m + " on " + _bean, e);
		}
	}

	private void setProperty(Param<?> param, Object value) {
		PropertyDescriptor pd = getDescriptor(param.getName());
		if (pd == null) {
			// TODO simply ignore?
			return;
		}

		Object converted;
		if (value == null || pd.getPropertyType().isAssignableFrom(value.getClass())) {
			converted = value;
		} else {
			converted = Param.p(pd.getPropertyType(), pd.getName()).toObject(param.toString(value));
		}

		try {
			PropertyUtils.setProperty(_bean, param.getName(), converted);
		} catch (NoSuchMethodException e) {
			throw new NotifyRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new NotifyRuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new NotifyRuntimeException(e);
		}
	}

}
