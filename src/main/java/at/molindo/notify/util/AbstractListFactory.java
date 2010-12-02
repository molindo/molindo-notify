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

package at.molindo.notify.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.FactoryBean;

abstract class AbstractListFactory<T> implements FactoryBean<List<T>> {

	private final List<T> _list = new CopyOnWriteArrayList<T>();

	protected void set(List<T> collection) {
		_list.clear();
		_list.addAll(collection);
	}

	@Override
	public final List<T> getObject() throws Exception {
		return _list;
	}

	@Override
	public final Class<?> getObjectType() {
		return List.class;
	}

	@Override
	public final boolean isSingleton() {
		return true;
	}

}
