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

import org.springframework.context.SmartLifecycle;

public abstract class AbstractSmartLifecycle implements SmartLifecycle {

	private final Object _monitor = new Object();

	@Override
	public final void start() {
		synchronized (_monitor) {
			if (!isRunning()) {
				doStart();
			}
		}

	}

	@Override
	public final void stop() {
		synchronized (_monitor) {
			if (isRunning()) {
				doStop();
			}
		}
	}

	@Override
	public final void stop(final Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE; // at the very end
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public abstract boolean isRunning();

	protected abstract void doStart();

	protected abstract void doStop();
}
