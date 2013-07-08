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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class TanglesDetector {
	
	// TODO: log4j, trace execution instead of sysout
	
	private DependencyGraph graph;
	
	private Path path;

    private HashSet<Path> cycles;

	private Set<String> verticesToCheck;
	
	public TanglesDetector(DependencyGraph graph) {
		this.graph = graph;
	}

	public Collection<Tangle> detect() {
		cycles = new HashSet<Path>();
	    path = new Path();
		verticesToCheck = new LinkedHashSet<String>(graph.vertexSet());
		
		while (!verticesToCheck.isEmpty()) {
			String uncheckedVertex = verticesToCheck.iterator().next();
			goThroughVertex(uncheckedVertex);
		}
		
		return mergeCycles(cycles);
	}

	private void goThroughVertex(String vertex) {
		//System.out.println("path: " + path + "-" + vertex);
		if (path.contains(vertex)) {
			// encountered vertex that we've been already to while walking this path
			Path cycle = path.subpathFrom(vertex);
			//System.out.println("- cycle detected: " + cycle);
			cycles.add(cycle);			
			return;
		}
//		if (!verticesToCheck.contains(vertex)) {
//			// been there, done that
//			return;
//		}
		verticesToCheck.remove(vertex);
		path.addNextStep(vertex);
		Set<DefaultEdge> edges = graph.outgoingEdgesOf(vertex);
		for (DefaultEdge edge : edges) {
			goThroughVertex(graph.getEdgeTarget(edge));
		}
		path.removeLastStep();
	}
	
	static Collection<Tangle> mergeCycles(Collection<Path> cycles) {
		HashSet<Tangle> tangles = new HashSet<Tangle>();
		for (Path cycle : cycles) {
			tangles.add(new Tangle(cycle.verticesList()));
		}
		boolean overlappingTanglesFound;
		do {
			overlappingTanglesFound = false;
			overlappingTanglesSearch:
			for (Tangle tangleA : tangles) {
				for (Tangle tangleB : tangles) {
					if (tangleA != tangleB && tangleA.overlaps(tangleB)) {
						tangles.remove(tangleA);
						tangles.remove(tangleB);
						tangles.add(tangleA.mergeWith(tangleB));
						overlappingTanglesFound = true;
						break overlappingTanglesSearch;
					}
				}
			}
		} while (overlappingTanglesFound);
		return tangles;
	}
}
