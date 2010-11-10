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
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

public class ParamTest {

	@Test
	public void testP() throws MalformedURLException {
		assertConvert(Param.pBoolean("test"), Boolean.TRUE);
		assertConvert(Param.pCharacter("test"), 'C');
		assertConvert(Param.pDouble("test"), 47.11);
		assertConvert(Param.pFloat("test"), 47.11F);
		assertConvert(Param.pInteger("test"), 42);
		assertConvert(Param.pLong("test"), 42L);
		assertConvert(Param.pSerializable("test"), Arrays.asList("foo", "bar", "baz"));
		assertConvert(Param.pString("test"), "test");
		assertConvert(Param.pURL("test"), new URL("http://www.example.com/"));
	}

	@Test
	public void testPNull() throws MalformedURLException {
		for (Param.Type t : Param.Type.values()) {
			assertNull(t.p("test").toObject(null));
			assertNull(t.p("test").toString(null));
		}
	}

	@Test(expected = RuntimeException.class)
	public void testPObject() {
		assertConvert(Param.pObject("test"), Boolean.TRUE);
	}

	private <T> void assertConvert(Param<T> param, T object) {
		assertEquals(object, convert(param, object));
	}

	private <T> T convert(Param<T> param, T object) {
		return param.toObject(param.toString(object));
	}
}
