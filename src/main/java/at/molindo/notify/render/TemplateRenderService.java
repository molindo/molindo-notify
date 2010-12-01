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

package at.molindo.notify.render;

import org.springframework.beans.factory.InitializingBean;

import at.molindo.notify.dao.ITemplateDAO;
import at.molindo.notify.model.IParams;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Template;
import at.molindo.notify.util.NotifyUtils;

public class TemplateRenderService implements IRenderService, InitializingBean {

	private ITemplateDAO _templateDAO;
	private ITemplateRenderer _renderer;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (_renderer == null) {
			throw new Exception("renderer not configured");
		}
		if (_templateDAO == null) {
			throw new Exception("templateDAO not configured");
		}
	}

	@Override
	public Message render(String key, Version version, IParams params) throws RenderException {

		Template template = NotifyUtils.choose(_templateDAO.findTemplates(key), version);

		if (template == null) {
			throw new RenderException("no template available for '" + key + "' (" + version + ")");
		}

		return Message.parse(_renderer.render(template, params), template.getType());
	}

	ITemplateDAO getTemplateDAO() {
		return _templateDAO;
	}

	public void setTemplateDAO(ITemplateDAO templateDAO) {
		_templateDAO = templateDAO;
	}

	ITemplateRenderer getRenderer() {
		return _renderer;
	}

	public void setRenderer(ITemplateRenderer renderer) {
		_renderer = renderer;
	}

}
