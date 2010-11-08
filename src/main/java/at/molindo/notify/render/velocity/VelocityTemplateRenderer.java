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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.springframework.beans.factory.InitializingBean;

import at.molindo.notify.model.Params;
import at.molindo.notify.model.Template;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.ITemplateRenderer;

import com.google.common.collect.MapMaker;

public class VelocityTemplateRenderer implements ITemplateRenderer, InitializingBean {

	private Map<Template.Key, org.apache.velocity.Template> _templateCache = new MapMaker()
			.expiration(1, TimeUnit.HOURS).concurrencyLevel(4).makeMap();

	private RuntimeServices _runtime = new RuntimeInstance();

	public VelocityTemplateRenderer () {

	}

	@Override
	public final void afterPropertiesSet() throws Exception {
		init();
	}
	
	public VelocityTemplateRenderer init() throws Exception {
		_runtime = new RuntimeInstance();
		_runtime.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new SLF4JLogChute());
		_runtime.init();
		return this;
	}

	@Override
	public String render(Template template, Params params)
			throws RenderException {
		
		try {
			StringWriter writer = new StringWriter();
			getVelocityTemplate(template).merge(buildContext(params), writer);
			return writer.toString();
		} catch (ResourceNotFoundException e) {
			throw new RenderException("failed to render template " + template,
					e);
		} catch (ParseErrorException e) {
			throw new RenderException("failed to render template " + template,
					e);
		} catch (MethodInvocationException e) {
			throw new RenderException("failed to render template " + template,
					e);
		} catch (IOException e) {
			throw new RenderException("failed to render template " + template,
					e);
		}
	}

	private Context buildContext(Params params) {
		return new VelocityContext(params.newMap());
	}

	private org.apache.velocity.Template getVelocityTemplate(Template template)
			throws RenderException {
		Template.Key key = template.key();

		org.apache.velocity.Template vt = _templateCache.get(key);
		if (vt == null) {
			_templateCache.put(key, vt = toVelocityTemplate(template));
		}

		return vt;
	}

	private org.apache.velocity.Template toVelocityTemplate(Template template)
			throws RenderException {
		try {
			StringReader reader = new StringReader(template.getContent());
			SimpleNode node = _runtime.parse(reader, template.getKey() + "."
					+ template.getVersion());
			org.apache.velocity.Template vt = new org.apache.velocity.Template();
			vt.setRuntimeServices(_runtime);
			vt.setData(node);
			vt.initDocument();
			return vt;
		} catch (ParseException e) {
			throw new RenderException("failed to parse template", e);
		}
	}

}
