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

package at.molindo.notify.servlet;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import at.molindo.notify.model.Confirmation;
import at.molindo.notify.model.IParams;

public class DefaultNotifyUrlFactory implements INotifyUrlFactory, ServletContextAware {

	private final Object _mutex = new Object();

	private INotifyUrlFactory _wrappedFactory;

	private ServletContext _servletContext;
	private NotifyFilter _filter;

	@Override
	public void setServletContext(ServletContext servletContext) {
		synchronized (_mutex) {
			_servletContext = servletContext;
		}
	}

	protected INotifyUrlFactory getWrappedFactory() {
		synchronized (_mutex) {
			if (_wrappedFactory != null) {
				return _wrappedFactory;
			} else if (_filter != null) {
				return _filter;
			} else if (_servletContext != null) {
				return _filter = NotifyFilter.getFilter(_servletContext);
			} else {
				throw new IllegalStateException("no wrapped INotifyUrlFactory available");
			}
		}
	}

	public void setWrappedFactory(INotifyUrlFactory wrappedFactory) {
		_wrappedFactory = wrappedFactory;
	}

	@Override
	public String toPullPath(String channelId, String userId, IParams params) {
		return getWrappedFactory().toPullPath(channelId, userId, params);
	}

	@Override
	public String toConfirmPath(Confirmation confirmation) {
		return getWrappedFactory().toConfirmPath(confirmation);
	}

}
