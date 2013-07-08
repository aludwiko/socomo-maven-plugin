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

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DependencyGraph extends DefaultDirectedGraph<String, DefaultEdge> {

    public DependencyGraph() {
        super(DefaultEdge.class);
    }
    
    /**
	 * Creates graph from string definition of it. For example
	 * <code>"a-b-c, b-d"</code> means graph with edges from <i>a</i> to
	 * <i>b</i>, from <i>b</i> to <i>c</i> and from <i>b</i> to <i>d</i>.
	 * 
	 * @param definition
	 *            definition of graph
	 * @return graph object
	 */
    public static DependencyGraph parseGraph(String definition) {
    	definition = definition.replaceAll("\\s+", "");
	    DependencyGraph graph = new DependencyGraph();
	    for (String path: definition.split(",")) {
	        String[] vertices = path.split("-");
	        for (String vertex : vertices) {
	            graph.addVertex(vertex);
	        }
	        for (int i = 0; i < vertices.length - 1; i++) {
	            graph.addEdge(vertices[i], vertices[i+1]);
	        }
	    }
        return graph;
    }

}
