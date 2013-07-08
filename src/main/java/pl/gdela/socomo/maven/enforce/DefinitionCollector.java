/**
 * Copyright 2010 Wojciech Gdela
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.gdela.socomo.maven.enforce;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import pl.gdela.socomo.maven.acyclity.AcyclityDeclaration;
import pl.gdela.socomo.maven.existence.ExistenceDeclaration;

/**
 * Collects various sources to build one merged definition. 
 */
public class DefinitionCollector {
	
	private String result = "";
	
	private ArrayList<AcyclityDeclaration> acyclicPackages = new ArrayList<AcyclityDeclaration>();
	
	private ArrayList<ExistenceDeclaration> existenceRules = new ArrayList<ExistenceDeclaration>();
	
	private void writeLabel(String label) {
		this.result += "\n### " + label + "\n";
	}
	
	private void write(String text) {
		this.result += text;
	}

	/**
	 * Adds text to definition.
	 * @param text text with source definition
	 * @param description description of the text source
	 * @param baseDir base directory on which relative file paths are resolved 
	 */
	public void collect(String text, String description, File baseDir) {
		Validate.notNull(text, "text cannot be null");
		writeLabel(description);
		ScmToDdfParser scmToDdfParser = new ScmToDdfParser(baseDir);
        write(scmToDdfParser.parse(text));
		if (!text.endsWith("\n")) {
			write("\n");
		}
		acyclicPackages.addAll(scmToDdfParser.parseAcyclicPackages(text));
		existenceRules.addAll(scmToDdfParser.parseExistenceRules(text));
	}

	/**
	 * Adds contents of a file to definition.
	 * @param file
	 */
	public void collect(File file) {
		try {
			Validate.notNull(file, "file cannot be null");
			if (file.getName().endsWith("scm") || file.getName().endsWith("ddf")) {
				String definition = FileUtils.readFileToString(file, "UTF-8");
				String description = file.getPath();
				collect(definition, description, file.getParentFile());
			} else {
				throw new IllegalArgumentException("unrecognized file type: " + file);
			}
		} catch (IOException e) {
			// TODO: throw something when file cannot be read
			e.printStackTrace();
		}
	}

	/**
	 * Detects known definition files and collects their content.
	 * @param dir
	 */
	public void collectFromDir(File dir) {
		Collection files = FileUtils.listFiles(dir, new String[] { "scm" }, false);
		for (Iterator filesIt = files.iterator(); filesIt.hasNext();) {
			collect((File) filesIt.next());
		}
	}
	
	/**
	 * Returns <code>true</code> if has any result yet.
	 * @return
	 */
	public boolean hasResult() {
		return StringUtils.isNotBlank(result);
	}
	
	/**
	 * Returns collected definition.
	 * @return
	 */
	public String getResult() {
		return result;
	}

	public Collection<AcyclityDeclaration> getAcyclicPackages() {
		return acyclicPackages;
	}

	public Collection<ExistenceDeclaration> getExistenceRules() {
		return existenceRules;
	}
}
