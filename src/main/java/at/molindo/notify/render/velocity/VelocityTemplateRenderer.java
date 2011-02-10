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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.springframework.beans.factory.InitializingBean;

import at.molindo.notify.model.IParams;
import at.molindo.notify.model.Template;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.ITemplateRenderer;
import at.molindo.notify.util.VelocityUtils;

import com.google.common.collect.MapMaker;

public class VelocityTemplateRenderer implements ITemplateRenderer, InitializingBean {

	private final Map<Template.Key, org.apache.velocity.Template> _templateCache = new MapMaker()
			.expiration(1, TimeUnit.HOURS).concurrencyLevel(4).makeMap();

	private RuntimeServices _runtime = new RuntimeInstance();

	public VelocityTemplateRenderer() {

	}

	@Override
	public final void afterPropertiesSet() throws Exception {
		init();
	}

	public VelocityTemplateRenderer init() {
		_runtime = VelocityUtils.newRuntime();
		return this;
	}

	@Override
	public String render(Template template, IParams params) throws RenderException {
		return VelocityUtils.merge(getVelocityTemplate(template), params);
	}

	private org.apache.velocity.Template getVelocityTemplate(Template template) throws RenderException {
		Template.Key key = template.key();

		org.apache.velocity.Template vt = _templateCache.get(key);
		if (vt == null) {
			_templateCache.put(key, vt = toVelocityTemplate(template));
		}

		return vt;
	}

	private org.apache.velocity.Template toVelocityTemplate(Template template) throws RenderException {
		return VelocityUtils.newTemplate(_runtime, template.getContent(),
				template.getKey() + "." + template.getVersion());
	}

}
