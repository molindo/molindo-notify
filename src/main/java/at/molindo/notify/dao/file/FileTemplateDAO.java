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

package at.molindo.notify.dao.file;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.molindo.notify.dao.ITemplateDAO;
import at.molindo.notify.model.Template;
import at.molindo.notify.render.IRenderService.Type;
import at.molindo.notify.render.IRenderService.Version;
import at.molindo.utils.io.Compression;
import at.molindo.utils.io.FileUtils;
import at.molindo.utils.io.StreamUtils;

import com.google.common.collect.Lists;

public class FileTemplateDAO implements ITemplateDAO {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(FileTemplateDAO.class);

	/**
	 * 
	 * requires file names being long.en.txt
	 */
	private static final Pattern PATTERN;

	static {
		StringBuilder buf = new StringBuilder();

		// version
		buf.append("^(");
		for (Version version : Version.values()) {
			buf.append(version.name().toLowerCase()).append("|");
		}
		buf.setCharAt(buf.length() - 1, ')');

		// optinal locale
		buf.append("\\.");
		buf.append("([a-z]{2})?");

		// type as suffix
		buf.append("\\.");
		buf.append("(");
		for (Type type : Type.values()) {
			buf.append(type.name().toLowerCase()).append("|");
		}
		buf.setCharAt(buf.length() - 1, ')');
		buf.append("$");

		String pattern = buf.toString();

		log.debug("file name pattern: " + pattern);

		PATTERN = Pattern.compile(pattern);
	}

	private File _baseDir;

	@Override
	public List<Template> findTemplates(String key) {
		List<Template> templates = Lists.newArrayList();
		;

		File keyDir = new File(_baseDir, key);
		if (!keyDir.isDirectory()) {
			return templates;
		}

		for (File template : keyDir.listFiles()) {
			Matcher m = PATTERN.matcher(template.getName());
			if (m.matches()) {
				try {
					templates.add(new Template()
							.setKey(key)
							.setVersion(
									Version.valueOf(m.group(1).toUpperCase()))
							.setLocale(
									m.group(2) == null ? null : new Locale(m
											.group(2)))
							.setType(Type.valueOf(m.group(3).toUpperCase()))
							.setLastModified(new Date(template.lastModified()))
							.setContent(
									StreamUtils.string(FileUtils.in(template,
											Compression.NONE))));
				} catch (IOException e) {
					log.warn("failed to read template from " + template, e);
				}
			}
		}

		return templates;
	}

	public File getBaseDir() {
		return _baseDir;
	}

	public void setBaseDir(File baseDir) {
		if (baseDir != null && !baseDir.isDirectory()) {
			throw new IllegalArgumentException("baseDir not a directory: "
					+ baseDir);
		}
		_baseDir = baseDir;
	}

}
