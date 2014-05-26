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

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;

import at.molindo.notify.INotifyService.NotifyRuntimeException;
import at.molindo.notify.model.IParams;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.velocity.SLF4JLogChute;

public class VelocityUtils {

	private VelocityUtils() {
	}

	public static RuntimeServices newRuntime() {
		try {
			RuntimeInstance runtime = new RuntimeInstance();
			runtime.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new SLF4JLogChute());
			runtime.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true);
			runtime.init();
			return runtime;
		} catch (Exception e) {
			throw new NotifyRuntimeException("failed to init velocity runtime", e);
		}
	}

	public static String merge(RuntimeServices runtime, String content, String templateName, IParams params)
			throws RenderException {
		return merge(newTemplate(runtime, content, templateName), params);
	}

	public static String merge(Template template, IParams params) throws RenderException {
		return merge(template, params, null);
	}

	public static String merge(Template template, IParams params, Context nestedContext) throws RenderException {
		try {
			StringWriter writer = new StringWriter();
			template.merge(new VelocityContext(params.newMap(), nestedContext), writer);
			return writer.toString();
		} catch (ResourceNotFoundException e) {
			throw new RenderException("failed to render template " + template, e);
		} catch (ParseErrorException e) {
			throw new RenderException("failed to render template " + template, e);
		} catch (MethodInvocationException e) {
			throw new RenderException("failed to render template " + template, e);
		}
	}

	public static Template newTemplate(RuntimeServices runtime, String content, String templateName)
			throws RenderException {

		try {
			Template vt = new Template();
			vt.setRuntimeServices(runtime);
			vt.setData(runtime.parse(new StringReader(content), templateName));
			vt.initDocument();
			return vt;

		} catch (ParseException e) {
			throw new RenderException("failed to parse template", e);
		}
	}
}
