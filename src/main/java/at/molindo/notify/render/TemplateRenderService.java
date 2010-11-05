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

import java.util.List;

import at.molindo.notify.dao.ITemplateDAO;
import at.molindo.notify.model.Params;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Template;

public class TemplateRenderService implements IRenderService {
	
	private ITemplateDAO _templateDAO;
	private ITemplateRenderer _renderer;
	
	@Override
	public Message render(String key, Version version, Params params) throws RenderException {
		
		List<Template> templates = _templateDAO.findTemplates(key);
		
		Template template = choose(templates, version);
		
		if (template == null) {
			throw new RenderException("no template available for '" + key + "' (" + version + ")");
		}
		
		return Message.parse(_renderer.render(template, params), template.getType());
	}

	private Template choose(List<Template> templates, Version version) {
		// TODO use fallbacks
		for (Template t : templates) {
			if (version.equals(t.getVersion())) {
				return t;
			}
		}
		return null;
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
