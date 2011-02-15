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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * simplistic version of {@link DelegatingFilterProxy}
 * 
 * @author stf
 */
public class NotifyFilter implements Filter {

	private NotifyFilterBean _notifyFilterBean;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_notifyFilterBean = findNotifyFilterBean(findWebApplicationContext(filterConfig.getServletContext()));
		if (_notifyFilterBean == null) {
			throw new ServletException("couldn't find NotifyFilterBean");
		}
	}

	protected NotifyFilterBean findNotifyFilterBean(WebApplicationContext webApplicationContext) {
		return webApplicationContext.getBean(NotifyFilterBean.class);
	}

	protected WebApplicationContext findWebApplicationContext(ServletContext servletContext) {
		return WebApplicationContextUtils.getWebApplicationContext(servletContext);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		_notifyFilterBean.doFilter(request, response, chain);
	}

	@Override
	public void destroy() {
	}
}
