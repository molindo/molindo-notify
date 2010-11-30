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

public interface IParams extends Cloneable, Iterable<ParamValue> {

	<T> IParams set(Param<T> param, T value);

	<T> T get(Param<T> param);

	boolean isSet(Param<?> param);

	boolean containsAll(Param<?>... params);

	/**
	 * set all params from passed object, overwriting current mappings
	 * 
	 * @param params
	 * @return
	 */
	IParams setAll(IParams params);

	Map<String, Object> newMap();

	@Override
	Iterator<ParamValue> iterator();

	static class Util {
		protected static boolean containsAll(IParams target, Param<?>... params) {
			for (Param<?> param : params) {
				if (!target.isSet(param)) {
					return false;
				}
			}
			return true;
		}

		protected static void setAll(IParams target, IParams source) {
			for (ParamValue v : source) {
				v.set(target);
			}
		}

		protected static Map<String, Object> newMap(IParams target) {
			Map<String, Object> map = Maps.newHashMap();
			for (ParamValue v : target) {
				map.put(v.getName(), v.getValue());
			}
			return map;
		}

	}

}