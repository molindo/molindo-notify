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

package at.molindo.notify.render.velocity;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class SLF4JLogChute implements LogChute {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VelocityTemplateRenderer.class);

	public void init(RuntimeServices rs) throws Exception {
	}

	public void log(int level, String message) {
		switch (level) {
		case LogChute.WARN_ID:
			log.warn(message);
			break;
		case LogChute.INFO_ID:
			log.info(message);
			break;
		case LogChute.TRACE_ID:
			log.trace(message);
			break;
		case LogChute.ERROR_ID:
			log.error(message);
			break;
		case LogChute.DEBUG_ID:
		default:
			log.debug(message);
			break;
		}
	}

	public void log(int level, String message, Throwable t) {
		switch (level) {
		case LogChute.WARN_ID:
			log.warn(message, t);
			break;
		case LogChute.INFO_ID:
			log.info(message, t);
			break;
		case LogChute.TRACE_ID:
			log.trace(message, t);
			break;
		case LogChute.ERROR_ID:
			log.error(message, t);
			break;
		case LogChute.DEBUG_ID:
		default:
			log.debug(message, t);
			break;
		}
	}

	public boolean isLevelEnabled(int level) {
		switch (level) {
		case LogChute.DEBUG_ID:
			return log.isDebugEnabled();
		case LogChute.INFO_ID:
			return log.isInfoEnabled();
		case LogChute.TRACE_ID:
			return log.isTraceEnabled();
		case LogChute.WARN_ID:
			return log.isWarnEnabled();
		case LogChute.ERROR_ID:
			return log.isErrorEnabled();
		default:
			return true;
		}
	}
}