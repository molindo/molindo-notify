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

package at.molindo.notify.test.util;

import java.util.Map;

import org.easymock.EasyMock;

import com.google.common.collect.Maps;

public class EasyMockContext {
	private Map<Class<?>, Object> _mocks = Maps.newHashMap();
	
	public <T> T create(Class<T> cls) {
		T mock = EasyMock.createMock(cls);
		if (_mocks.put(cls, mock) != null) {
			throw new IllegalArgumentException("duplicate mock class " + cls);
		}
		return mock;
	}
	
	public <T> T get(Class<T> cls) {
		T mock = cls.cast(_mocks.get(cls));
		if (mock == null) {
			throw new IllegalArgumentException("no mock for class " + cls);
		}
		return mock;
	}
	
	public void replay() {
		EasyMock.replay(_mocks.values().toArray());
	}
	
	public void verify() {
		EasyMock.verify(_mocks.values().toArray());
	}
}
