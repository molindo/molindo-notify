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

import at.molindo.notify.model.IParams;
import at.molindo.notify.model.Message;
import at.molindo.notify.model.Param;
import at.molindo.notify.model.Params;
import at.molindo.utils.data.StringUtils;

public class MasterRenderService implements IRenderService {

	public static final String DEFAULT_TEMPLATE_KEY = "masterTemplate";
	public static final String DEFAULT_TEMPLATE_CONTENT = "message";

	private IRenderService _renderService;
	private String _masterTemplateKey = DEFAULT_TEMPLATE_KEY;
	private String _masterTemplateMessage = DEFAULT_TEMPLATE_CONTENT;
	private IParams _masterParams;

	@Override
	public Message render(String key, Version version, IParams params) throws RenderException {

		Message mRaw = _renderService.render(key, version, params);

		Params masterParams = new Params();
		if (_masterParams != null) {
			masterParams.setAll(_masterParams);
		}
		masterParams.set(getMasterTemplateMessageParam(), mRaw);

		Message m = _renderService.render(_masterTemplateKey, version, masterParams);
		if (StringUtils.empty(m.getSubject()) && !StringUtils.empty(mRaw.getSubject())) {
			m.setSubject(mRaw.getSubject());
		}

		return m;
	}

	public void setRenderService(IRenderService renderService) {
		_renderService = renderService;
	}

	public String getMasterTemplateKey() {
		return _masterTemplateKey;
	}

	public void setMasterTemplateKey(String masterTemplateKey) {
		if (StringUtils.empty(masterTemplateKey)) {
			throw new IllegalArgumentException("masterTemplateKey must not be empty");
		}
		_masterTemplateKey = masterTemplateKey;
	}

	public Param<Object> getMasterTemplateMessageParam() {
		return Param.pObject(_masterTemplateMessage);
	}

	public void setMasterTemplateMessage(String masterTemplateMessage) {
		_masterTemplateMessage = masterTemplateMessage;
	}

	public IParams getMasterParams() {
		return _masterParams;
	}

	public void setMasterParams(IParams masterParams) {
		_masterParams = masterParams;
	}

}
