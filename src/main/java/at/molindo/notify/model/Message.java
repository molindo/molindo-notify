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

package at.molindo.notify.model;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import at.molindo.notify.render.IRenderService;
import at.molindo.notify.render.IRenderService.RenderException;
import at.molindo.notify.render.IRenderService.Type;
import at.molindo.notify.util.NotifyUtils;
import at.molindo.utils.data.StringUtils;

public class Message {

	private static final String FIELD_SUBJECT = "Subject";
	
	private static final ImmutableSet<String> REQUIRED_FIELDS = ImmutableSet.of(FIELD_SUBJECT);
	private static final ImmutableSet<String> SUPPORTED_FIELDS = ImmutableSet.of(FIELD_SUBJECT);
	
	private Type _type = Type.HTML;
	private String _subject;
	private String _message;
	
	public static Message parse(String str, IRenderService.Type type) throws RenderException {
		
		Set<String> required = Sets.newHashSet(REQUIRED_FIELDS);
		Map<String, String> fieldValues = Maps.newHashMap();
		
		Message rendered = new Message();
		rendered.setType(type);
		
		StringBuilder body = null;
		for (String line : StringUtils.split(str, "\n")) {
			if (body == null) {
				if (StringUtils.empty(StringUtils.trim(line))) {
					body = new StringBuilder();
				} else {
					int split = line.indexOf(':');
					if (split < 0) {
						
					}
					String fieldName = line.substring(0, split).trim();
					String fieldValue = line.substring(split + 1).trim();
	
					if (!SUPPORTED_FIELDS.contains(fieldName)) {
						throw new RenderException("unknown field: " + fieldName);
					}
					
					fieldValues.put(fieldName, fieldValue);
				}
			} else if (body.length() > 0 || !StringUtils.empty(StringUtils.trim(line))) {
				body.append(line).append("\n");
			}
		}
		
		required.removeAll(fieldValues.keySet());
		if (required.size() > 0) {
			throw new RenderException("missing fields: " + required);
		}
		
		if (body == null || body.length() == 0) {
			throw new RenderException("empty body");
		}
		// remove trailing \n
		body.setLength(body.length() - 1);
		
		rendered.setSubject(fieldValues.get(FIELD_SUBJECT));
		rendered.setMessage(body.toString());
		
		return rendered;
	}
	
	public Message() {
		
	}
	
	public Message(String subject, String message, Type type) {
		setType(type);
		setSubject(subject);
		setMessage(message);
	}
	
	public Type getType() {
		return _type;
	}
	public void setType(Type type) {
		if (type == null) {
			throw new NullPointerException("type");
		}
		_type = type;
	}
	public String getSubject() {
		return _subject;
	}
	public void setSubject(String subject) {
		_subject = subject;
	}
	public String getMessage() {
		return _message;
	}
	public void setMessage(String message) {
		_message = message;
	}
	
	public String getText() {
		if (getType() == Type.HTML) {
			return NotifyUtils.html2text(getMessage());
		} else {
			return getMessage();
		}
	}
	
	public String getHtml() {
		if (getType() == Type.TEXT) {
			return NotifyUtils.text2html(getMessage());
		} else {
			return getMessage();
		}
	}
	
}
