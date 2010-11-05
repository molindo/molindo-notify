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

public abstract class MockTest {

	abstract protected void setup(EasyMockContext context) throws Exception;

	abstract protected void test(EasyMockContext context) throws Exception;

	public final void run() throws Exception {
		EasyMockContext context = new EasyMockContext();

		setup(context);

		context.replay();

		test(context);

		context.verify();
	}
}
