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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import pl.gdela.socomo.maven.acyclity.AcyclityDeclaration;
import pl.gdela.socomo.maven.existence.ExistenceDeclaration;

public class ScmToDdfParser {
    
    private File baseDir;
    
    /**
     * @param baseDir base directory on which relative file paths are resolved
     */
    public ScmToDdfParser(File baseDir) {
        this.baseDir = baseDir;
    }
    
    public String parse(String scm) {
        String ddf = "";
        String[] lines = scm.replaceAll("\r", "").split("\n");
        for (String line : lines) {
            ddf += convertLine(line) + "\n";
        }
        return StringUtils.chomp(ddf);
    }

    private final static Pattern ACYCLIC = Pattern.compile("\\s*check\\s+acyclicCompositionOf\\s+(.+?)(\\s+excluding\\s+(.+))?");
    
    private final static Pattern EXISTENCE_DENY = Pattern.compile("\\s*check\\s+deny\\s+(.+)");
    
    private final static Pattern EXISTENCE_ALLOW = Pattern.compile("\\s*check\\s+allow\\s+(.+)");

    private final static Pattern DIAGRAM = Pattern.compile("\\s*check\\s+diagram\\s+(.+)from\\s+(.+.java.hsp)");
    
    private final static Pattern ALL_DIAGRAMS = Pattern.compile("\\s*check\\s+all\\s+diagrams\\s+from\\s+(.+.java.hsp)");
    
    private final static Pattern ALL_DIAGRAMS_EXCEPT = Pattern.compile("\\s*check\\s+all\\s+diagrams\\s+except\\s+(.+)\\s+from\\s+(.+.java.hsp)");
    
    private String convertLine(String line) {
        Matcher matcher;
        matcher = ACYCLIC.matcher(line);
        if (matcher.matches()) {
        	// TODO: this check is done by AcyclityChecker
        	return "# " + matcher.group();
        }
        matcher = EXISTENCE_DENY.matcher(line);
        if (matcher.matches()) {
        	// TODO: this check is done by ExistenceChecker
            return "# " + matcher.group();
        }
        matcher = EXISTENCE_ALLOW.matcher(line);
        if (matcher.matches()) {
        	// TODO: this check is done by ExistenceChecker
        	return "# " + matcher.group();
        }
        matcher = DIAGRAM.matcher(line);
        if (matcher.matches()) {
            Collection<String> names = parseNames(matcher.group(1));
            String fileName = matcher.group(2);
            return loadDiagrams(fileName, names, null);
        }
        matcher = ALL_DIAGRAMS.matcher(line);
        if (matcher.matches()) {
            String fileName = matcher.group(1);
            return loadDiagrams(fileName, null, null);
        }
        matcher = ALL_DIAGRAMS_EXCEPT.matcher(line);
        if (matcher.matches()) {
        	Collection<String> names = parseNames(matcher.group(1));
            String fileName = matcher.group(2);
            return loadDiagrams(fileName, null, names);
        }
        return line;
    }

    String loadDiagrams(String fileName, Collection<String> enforcedDiagrams, Collection<String> exemptedDiagrams) {
        try {
            DiagramReader reader = new DiagramReader(new File(baseDir, fileName));
            String result = "";
            
            if (enforcedDiagrams == null) {
				// TODO: in case of empty diagrams list recognize from xml file which diagrams are enforced
				for (Grid grid : reader.readAll()) {
					if (exemptedDiagrams == null || !exemptedDiagrams.contains(grid.getName())) {
						DefinitionBuilder builder = new DefinitionBuilder();
						builder.setExcludes(grid.getExcludes());
						String definition = builder.build(grid.getDisallowedLinks());
						String description = fileName + ": " + grid.getName();
						result += "#" + description + "\n" + definition + "\n";
					}
				}
			} else {
				for (Iterator<String> wantedDiagramsIt = enforcedDiagrams.iterator(); wantedDiagramsIt.hasNext();) {
					String name = wantedDiagramsIt.next();
					Grid grid = reader.read(name);
					if (grid != null) {
						DefinitionBuilder builder = new DefinitionBuilder();
						builder.setExcludes(grid.getExcludes());
						String definition = builder.build(grid.getDisallowedLinks());
						String description = fileName + ": " + grid.getName();
						result += "#" + description + "\n" + definition + "\n";
					} else {
						// TODO: should be error
						result += "# warn diagram " + name + " not found in " + fileName;
					}
				}
			}
            
            return result;
        } catch (IOException e) {
            return "could not read diagrams from " + fileName; 
        }
    }

    static Collection<String> parseNames(String namesExpression) {
        HashSet<String> names = new HashSet<String>();
        StringBuilder buffer = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < namesExpression.length(); i++) {
            char ch = namesExpression.charAt(i);
            if (ch == '"' && !inQuotes) {
                inQuotes = true;
            } else if (ch == '"' && inQuotes) {
                inQuotes = false;
                if (buffer.length() > 0) {
                    names.add(buffer.toString());
                }
                buffer.delete(0, buffer.length());
            } else if (ch == ' ' && !inQuotes) {
                if (buffer.length() > 0) {
                    names.add(buffer.toString());
                }
                buffer.delete(0, buffer.length());
            } else {
                buffer.append(ch);
            }
        }
        if (buffer.length() > 0) {
            names.add(buffer.toString());
        }
        return names;
    }

	public Collection<AcyclityDeclaration> parseAcyclicPackages(String scm) {
		ArrayList<AcyclityDeclaration> acyclicPackages = new ArrayList<AcyclityDeclaration>();
        String[] lines = scm.replaceAll("\r", "").split("\n");
        for (String line : lines) {
        	Matcher matcher = ACYCLIC.matcher(line);
        	if (matcher.matches()) {
        		String packageName = matcher.group(1);
        		String[] excludes;
        		if (matcher.group(3) != null) {
        			excludes = matcher.group(3).split("\\s+");
        		} else {
        			excludes = new String[] {};
        		}
				acyclicPackages.add(new AcyclityDeclaration(packageName, excludes));
        	}
        }
		return acyclicPackages;
	}

	public Collection<ExistenceDeclaration> parseExistenceRules(String scm) {
		ExistenceDeclaration declaration = new ExistenceDeclaration();
		String[] lines = scm.replaceAll("\r", "").split("\n");
        for (String line : lines) {
        	Matcher matcher;
        	matcher = EXISTENCE_DENY.matcher(line);
        	if (matcher.matches()) {
        		String classNamePattern = matcher.group(1);
        		declaration.deny(classNamePattern);
        	}
        	matcher = EXISTENCE_ALLOW.matcher(line);
        	if (matcher.matches()) {
        		String classNamePattern = matcher.group(1);
        		declaration.allow(classNamePattern);
        	}
        }
		return Arrays.asList(declaration);
	}
}
