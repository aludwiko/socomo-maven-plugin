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

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class DefinitionBuilder {
	
	private static int lastInstanceNr = 0;
	private String suffix;
    private String excludes;
	
	public DefinitionBuilder() {
		// a little hack to prevent names collision
		this("_" + lastInstanceNr++);
	}
	
    public DefinitionBuilder(String suffix) {
    	this.suffix = suffix;
	}
    
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

	public String build(Set<Link> disallowedLinks) {
        String result = "";
        result += buildSets(disallowedLinks) + "\n";
        result += buildChecks(disallowedLinks) + "\n";
        return result;
    }

    /**
     * Builds set definitions. Sets are definied according to s101 rules: <blockquote>An important
     * flexibility is that a physical entity maps to the cell with the most specific expression. For
     * example if you include 2 cells, one with expression a.b.* and the other with the expression
     * a.b.c.*, then the entity a.b.c.D will map to the latter (a.b.c.* is more specific than
     * a.b.*). The effects of this can be at the same time subtle and powerful. For example you
     * could move the cell that maps to a.b.c.* into a completely different parent cell (e.g.
     * x.y.*).</blockquote>
     * @param disallowedLinks
     * @return
     */
    private String buildSets(Set<Link> disallowedLinks) {
        TreeSet<Cell> allCells = new TreeSet<Cell>();
        for (Link link : disallowedLinks) {
            allCells.add(link.getFrom());
            allCells.add(link.getTo());
        }
        
        TreeSet<SetDefinition> sets = new TreeSet<SetDefinition>();
        for (Cell cell : allCells) {
            sets.add(convert(cell));
        }
        
        for (SetDefinition set : sets) {
            for (SetDefinition other : sets) {
                if (other != set) {
                    for (String pattern : set.including) {
                        for (String otherPattern : other.including) {
                            if (contains(pattern, otherPattern)) {
                                set.excluding.add(otherPattern);
                            }
                        }
                    }
                }
            }
        }
        
        return StringUtils.join(sets, "\n");
    }

    /**
     * Returns <code>true</code> if bigger pattern contains smaller pattern. Only '*' is considered
     * a wildcard character, it matches everything.
     * @param smaller
     * @param bigger
     * @return
     */
    static boolean contains(String bigger, String smaller) {
        while (bigger.contains(".*.*")) {
            bigger = bigger.replace(".*.*", ".*");
        }
        while (smaller.contains(".*.*")) {
            smaller = smaller.replace(".*.*", ".*");
        }
        for (int i = 0; i < Math.min(bigger.length(), smaller.length()); i++) {
            if (smaller.charAt(i) == '*' && smaller.length() == i+1) {
                return false;
            }
            if (bigger.charAt(i) == '*' && bigger.length() == i+1) {
                return true;
            }
            if (smaller.charAt(i) == '*' && bigger.contains("*")) {
                return true;
            }
            if (bigger.charAt(i) == '*' && smaller.contains("*")) {
                return true;
            }
            if (bigger.charAt(i) != smaller.charAt(i)) {
                return false;
            }
        }
        return false;
    }

    private String buildChecks(Set<Link> disallowedLinks) {
        TreeSet<String> checks = new TreeSet<String>();
        for (Link link : disallowedLinks) {
            checks.add("check [" + normalizeName(link.getFrom()) + "] directlyIndependentOf [" + normalizeName(link.getTo()) + "]");
        }
        return StringUtils.join(checks, '\n');
    }
	
    /**
     * Converts s101 Cell to ddf SetDefinition. Normalizes name and converts s101 pattern, according
     * to following rules: <blockquote>A "*" maps to any string and a "?" maps to any class in the
     * preceding package (see Transformations for more details on pattern-matching). A pattern can
     * contain multiple expressions separated by a ",".</blockquote>
     * @param cell
     * @return
     */
	SetDefinition convert(Cell cell) {
	    SetDefinition set = new SetDefinition(normalizeName(cell));
	    if (cell.pattern != null) {
    	    String[] patterns = cell.pattern.split("\\s*,\\s*");
            for (String pattern : patterns) {
                addPatternToSetDefinition(pattern, set);
            }
	    }
	    if (excludes != null) {
    	    String[] patterns = excludes.split("\\s*,\\s*");
            for (String pattern : patterns) {
                // TODO: support '?' patterns
                set.excluding.add(pattern);
            }
	    }
	    return set;
    }
	
	String normalizeName(Cell cell) {
        // TODO: name sets uniquely in whole DefinitionCollector
        return StringUtils.replaceChars(cell.name, ' ', '_') + suffix;
    }
	
    private static void addPatternToSetDefinition(String pattern, SetDefinition set) {
        if (pattern.endsWith("?")) {
            String tmp = pattern.replace('?', '*');
            set.including.add(tmp);
            set.excluding.add(tmp + ".*");
        } else {
            set.including.add(pattern);
        }
    }
}