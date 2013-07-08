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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 This class is a demonstration program for creating a depencency chart,
 directed graph, then locating and outputting any implicit loops,
 cycles.
 */
public class JGraphTDemo {
	
    public static DependencyGraph visitorToGraph(DependencyVisitor v) {
    	DependencyGraph g = new DependencyGraph();
		
		for (String fromPackage : v.getDependencies().keySet()) {
			if (!fromPackage.startsWith("pl")) {
				continue;
			}
			String f = StringUtils.substringAfterLast(fromPackage, "/");
			g.addVertex(f);
			Map<String, Integer> toPackageAndCount = v.getDependencies().get(fromPackage);
			for (String toPackage : toPackageAndCount.keySet()) {
				if (!toPackage.startsWith("pl")) {
					continue;
				}
				String t = StringUtils.substringAfterLast(toPackage, "/");
				g.addVertex(t);
				if (!f.equals(t)) {
					g.addEdge(f, t);
				}
				// i niema zo zrobi� z sizem: toPackageAndCount.get(toPackage)
			}
		}
		System.out.println(g);
        return g;
    }

   /**
    Test creating a directed graph, checking it for cycles and either
    outputting cycle information or ordering.

    @param createCycles true - create a directed graph which contains
    cycles.  false - create a directed graph which does not contain
    any cycles.
    */
   public static void test(boolean createCycles) {
      DefaultDirectedGraph<String, DefaultEdge> g;

      g = new DefaultDirectedGraph<String, DefaultEdge>(
         DefaultEdge.class);

      // Add vertices, e.g. equations.
      g.addVertex("a");
      g.addVertex("b");
      g.addVertex("c");
      g.addVertex("d");
      g.addVertex("e");

      // Add edges, e.g. dependencies.
      // 2 cycles,
      //    a = f(b)
      //    b = f(c)
      //    c = f(a)
      // and
      //    d = f(e)
      //    e = f(d)
      g.addEdge("b", "a");
      g.addEdge("c", "b");
      if (createCycles) {
         g.addEdge("a", "c");
         g.addEdge("a", "d");
      }
      g.addEdge("e", "d");
      if (createCycles) {
         g.addEdge("d", "e");
      }

      System.out.println(g.toString());

      displayCyclesInfo(g);
   }

   public static void displayCyclesInfo(
		DefaultDirectedGraph<String, DefaultEdge> g) {
	// Are there cycles in the dependencies.
      CycleDetector<String, DefaultEdge> cycleDetector;
      cycleDetector = new CycleDetector<String, DefaultEdge>(g);
      // Cycle(s) detected.
      if (cycleDetector.detectCycles()) {
         Iterator<String> iterator;
         Set<String> cycleVertices;
         Set<String> subCycle;
         String cycle;

         System.out.println("Cycles detected.");

         // Get all vertices involved in cycles.
         cycleVertices = new HashSet<String>(cycleDetector.findCycles());
         System.out.println("cv: " + cycleVertices);
         
         for (String x1 : cycleVertices) {
        	 System.out.println("SUBCYC: " + x1 + "\n" + cycleDetector.findCyclesContainingVertex(x1));
         }
         
         if (true) {
        	 return;
         }

         // Loop through vertices trying to find disjoint cycles.
         while (! cycleVertices.isEmpty()) {
            System.out.println("Cycle:");

            // Get a vertex involved in a cycle.
            iterator = cycleVertices.iterator();
            cycle = iterator.next();

            // Get all vertices involved with this vertex.
            subCycle = cycleDetector.findCyclesContainingVertex(cycle);
            for (String sub : subCycle) {
               System.out.println("   " + sub);
               // Remove vertex so that this cycle is not encountered
               // again.
               cycleVertices.remove(sub);
            }
         }
      }

      // No cycles.  Just output properly ordered vertices.
      else {
         String v;
         TopologicalOrderIterator<String, DefaultEdge> orderIterator;

         orderIterator =
            new TopologicalOrderIterator<String, DefaultEdge>(g);
         System.out.println("\nOrdering:");
         while (orderIterator.hasNext()) {
            v = orderIterator.next();
            System.out.println(v);
         }
      }
}

   /**
    Generate two cases, one with cycles, this is depencencies and one
    without.

    @param args Ignored.
    */
   public static void main(String [] args) {
//      System.out.println("\nCase 1: There are cycles.");
//      test(true);
//
//      System.out.println("\nCase 2: There are no cycles.");
//      test(false);
//
//      System.out.println("\nAll done");
//      System.exit(0);
	   
	   DefaultDirectedGraph<String, DefaultEdge> g;

	      g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

	      // Add vertices, e.g. equations.
	      g.addVertex("a");
	      g.addVertex("b");
	      g.addVertex("c");
	      g.addVertex("d");
	      
	      g.addVertex("x");
	      g.addVertex("y");
	      g.addVertex("z");

	      g.addEdge("a", "b");
	      g.addEdge("b", "c");
	      g.addEdge("c", "d");
	      g.addEdge("d", "a");
	      
	      g.addEdge("a", "x");
	      g.addEdge("x", "y");
	      g.addEdge("y", "z");
	      g.addEdge("z", "a");

	      System.out.println(g.toString());

	      displayCyclesInfo(g);
	   
   }
}