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

import java.io.Serializable;

import org.junit.Test;

public class BeanParamsTest {

	@Test
	public void test() {
		BeanParams<MyBean> params = new BeanParams<MyBean>(new MyBean());

		Param<String> pFoo = Param.pString("foo");
		Param<Object> pBar = Param.pSerializable("bar");
		Param<Long> pBaz = Param.pLong("bar.baz");
		Param<Integer> pQux = Param.pInteger("qux");

		params.set(pFoo, "hello world!");
		params.set(pBar, new Bar());
		params.set(pBaz, 4711L);
		params.set(pQux, 42);

		assertEquals("hello world!", params.get(pFoo));
		assertEquals((Long) 4711L, params.get(pBaz));
		assertEquals((Integer) 42, params.get(pQux));

		params.set(Param.pBoolean("foo"), true);
		params.set(Param.pString("qux"), "43");
		params.set(Param.pString("bar.baz"), "4712");

		assertEquals("true", params.get(pFoo));
		assertEquals(true, params.get(Param.pBoolean("foo")));
		assertEquals((Long) 4712L, params.get(pBaz));
		assertEquals((Integer) 43, params.get(pQux));
	}

	@Test
	public void testUnknown() {
		BeanParams<MyBean> params = new BeanParams<MyBean>(new MyBean());
		// simply ignore
		params.set(Param.pBoolean("doesNotExist"), true);
	}

	public static class MyBean {

		private String _foo;
		private Bar _bar;
		private int _qux;

		public String getFoo() {
			return _foo;
		}

		public void setFoo(String foo) {
			_foo = foo;
		}

		public Bar getBar() {
			return _bar;
		}

		public void setBar(Bar bar) {
			_bar = bar;
		}

		public int getQux() {
			return _qux;
		}

		public void setQux(int qux) {
			_qux = qux;
		}

	}

	public static class Bar implements Serializable {
		private static final long serialVersionUID = 1L;
		private Long _baz;

		public Long getBaz() {
			return _baz;
		}

		public void setBaz(Long baz) {
			_baz = baz;
		}

	}
}
