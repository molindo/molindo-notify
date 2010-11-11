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

package at.molindo.notify.dao.memory;

import java.util.List;
import java.util.ListIterator;

import at.molindo.notify.dao.ITemplateDAO;
import at.molindo.notify.model.Template;
import at.molindo.utils.collections.ListMap;

import com.google.common.collect.Lists;

public class MemoryTemplateDAO implements ITemplateDAO {

	private final ListMap<String, Template> _templates = ListMap.newListMap();

	@Override
	public List<Template> findTemplates(String key) {
		synchronized (_templates) {
			List<Template> templates = _templates.get(key);
			if (templates == null) {
				return Lists.newArrayListWithCapacity(0);
			} else {
				templates = Lists.newArrayList(templates);
				ListIterator<Template> iter = templates.listIterator();
				while (iter.hasNext()) {
					iter.set(iter.next().clone());
				}
				return templates;
			}
		}
	}

	public void setTemplates(List<Template> templates) {
		synchronized (_templates) {
			_templates.clear();
			for (Template template : templates) {
				_templates.put(template.getKey(), template);
			}
		}
	}
}
