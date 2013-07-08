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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static pl.gdela.socomo.maven.check.SyntacticSugar.*;

import org.junit.Test;

import pl.gdela.socomo.maven.check.DependencyGraph;

public class DependencyGraphTest {

	@Test
	public void testParseGraph() {
	    DependencyGraph graph = graph("a-b-c, a-d, x");
	    assertThat(graph.vertexSet(), is(vertexSet("a,b,c,d,x")));
	    assertTrue(graph.containsEdge("a", "b"));
	    assertTrue(graph.containsEdge("b", "c"));
	    assertTrue(graph.containsEdge("a", "d"));
	    assertEquals(3, graph.edgeSet().size());
	}
	
	@Test
	public void testParseGraphMoreSpaces() {
	    DependencyGraph graph = graph("a-b , b-a ");
	    assertThat(graph.vertexSet(), is(vertexSet("a,b")));
	    assertTrue(graph.containsEdge("a", "b"));
	    assertTrue(graph.containsEdge("b", "a"));
	    assertEquals(2, graph.edgeSet().size());
	}

}
