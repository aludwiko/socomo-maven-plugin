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
package pl.gdela.socomo.maven.check;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import pl.gdela.socomo.maven.check.DependencyGraph;
import pl.gdela.socomo.maven.check.Path;
import pl.gdela.socomo.maven.check.Tangle;

public class SyntacticSugar {
	
	public static Path path(String definition) {
		return Path.parsePath(definition);
	}
	
	public static DependencyGraph graph(String definition) {
		return DependencyGraph.parseGraph(definition);
	}
	
	public static Tangle tangle(String definition) {
		return new Tangle(vertexSet(definition));
	}
	
	/**
	 * Creates set of vertices from comma separated string definition of this
	 * set. For example <code>"a,b,c"</code> means set of vertices named
	 * <i>a</i>, <i>b</i> and <i>c</i>.
	 * 
	 * @param definition
	 *            definition of set
	 * @return vertex set, preserving order in original definition
	 */
	public static Set<String> vertexSet(String definition) {
		definition = definition.replaceAll("\\s+", "");
        return new LinkedHashSet<String>(Arrays.asList(definition.split(",")));
    }
}
