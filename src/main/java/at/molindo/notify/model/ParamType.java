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

public enum ParamType {
	STRING {

		@Override
		Param<String> p(String name) {
			return Param.pString(name);
		}

	},
	INTEGER {

		@Override
		Param<Integer> p(String name) {
			return Param.pInteger(name);
		}

	},
	LONG {

		@Override
		Param<Long> p(String name) {
			return Param.pLong(name);
		}

	},
	DOUBLE {

		@Override
		Param<Double> p(String name) {
			return Param.pDouble(name);
		}

	},
	FLOAT {

		@Override
		Param<Float> p(String name) {
			return Param.pFloat(name);
		}

	},
	BOOLEAN {

		@Override
		Param<Boolean> p(String name) {
			return Param.pBoolean(name);
		}

	},
	CHARACTER {

		@Override
		Param<Character> p(String name) {
			return Param.pCharacter(name);
		}

	},
	URL {

		@Override
		Param<URL> p(String name) {
			return Param.pURL(name);
		}

	},
	SERIALIZABLE {

		@Override
		Param<Object> p(String name) {
			return Param.pSerializable(name);
		}
	},
	OBJECT {
		@Override
		Param<Object> p(String name) {
			return Param.pObject(name);
		}
	};

	abstract Param<?> p(String name);
}