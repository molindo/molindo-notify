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

package at.molindo.notify.channel.feed;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.ServletContextAware;

import at.molindo.notify.servlet.NotifyFilter;


public abstract class AbstractServletPullChannel extends AbstractPullChannel implements ServletContextAware, DisposableBean {

	private ServletContext _servletContext;
	
	public void setServletContext(ServletContext servletContext) {
		if (servletContext == _servletContext) {
			return;
		}
		
		if (_servletContext != null) {
			NotifyFilter.removeChannel(this, _servletContext);
		} 
		
		if (servletContext != null){
			NotifyFilter.addChannel(this, servletContext);
		}
		
		_servletContext = servletContext;
	}

	@Override
	public void destroy() throws Exception {
		setServletContext(null);
	}

	
}
