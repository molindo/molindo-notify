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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParamsTest {

	@Test
	public void testGetSet() {
		Param<String> pString = Param.pString("foo");
		Param<Integer> pInt = Param.pInteger("foo");

		Params params = new Params();

		params.set(pString, "42");

		assertEquals("42", params.get(pString));
		assertEquals((Integer) 42, params.get(pInt));
		assertEquals("42", params.newMap().get("foo"));

		params.set(pInt, 42);

		assertEquals("42", params.get(pString));
		assertEquals((Integer) 42, params.get(pInt));
		assertEquals(42, params.newMap().get("foo"));
	}
}
