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

package at.molindo.notify.dispatch;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import at.molindo.notify.INotifyService.IParamsFactory;
import at.molindo.notify.INotifyService.NotifyException;
import at.molindo.notify.model.Dispatch;
import at.molindo.notify.model.IChannelPreferences;
import at.molindo.notify.model.IPreferences;
import at.molindo.notify.model.Notification;
import at.molindo.notify.model.Params;
import at.molindo.notify.render.IRenderService;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.IRenderService.Version;

public class DispatchService implements IDispatchService {

	private IRenderService _renderService;
	private final List<IParamsFactory> _paramsFactories = new CopyOnWriteArrayList<IParamsFactory>();

	@Override
	public Dispatch create(Notification notification, IPreferences prefs, IChannelPreferences cPrefs)
			throws RenderException {

		Params params = new Params();
		params.setAll(prefs.getParams());
		params.setAll(cPrefs.getParams());
		params.setAll(notification.getParams());

		for (IParamsFactory factory : _paramsFactories) {
			try {
				factory.params(params);
			} catch (NotifyException e) {
				throw new RenderException("params unavailable", e);
			}
		}

		String key = notification.getKey();
		Version version = cPrefs.getVersion();

		return new Dispatch(_renderService.render(key, version, params), params);
	}

	public void setRenderService(IRenderService renderService) {
		_renderService = renderService;
	}

	@Override
	public void addParamsFactory(IParamsFactory factory) {
		_paramsFactories.add(factory);
	}

	@Override
	public void removeParamsFactory(IParamsFactory factory) {
		_paramsFactories.removeAll(Arrays.asList(factory));
	}

	public void setParamsFactories(List<IParamsFactory> factories) {
		_paramsFactories.clear();
		_paramsFactories.addAll(factories);
	}
}
