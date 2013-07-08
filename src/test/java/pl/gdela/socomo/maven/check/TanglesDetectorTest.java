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

import static org.junit.Assert.*;
import static pl.gdela.socomo.maven.check.SyntacticSugar.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import pl.gdela.socomo.maven.check.DependencyGraph;
import pl.gdela.socomo.maven.check.Path;
import pl.gdela.socomo.maven.check.Tangle;
import pl.gdela.socomo.maven.check.TanglesDetector;

public class TanglesDetectorTest {
    
    @Test
    public void testDetectorOnCycles() {
        test("a-b-a => a,b");
        test("a-b-c-a => a,b,c");
        test("a-b-c-b => b,c");
        test("a-b-a, c-d-c => a,b; c,d");
        test("a-b-c-d, c-b => b,c");
    }
    
    @Test
    public void testDetectorOnTangles() {
    	// xf = vertex attached only to x
    	test("a-b, b-a, a-c-b, af-a, b-bf, c-cf => a,b,c");
    	test("a-b-c, c-d-a, c-e-a, a-af, bf-b, cf-c, d-df, e-ef => a,b,c,d,e");
    	test("a-x-b-a, c-x-d-c => a,b,c,d,x");
    	test("a-b-c-d, b-x-a, d-y-c => a,b,x; c,d,y");
    	test("a-b-c-d, b-x-a, d-y-c, d-a => a,b,c,d,x,y");
    }
    
	/**
	 * Tests if tangle detector correctly detects on defined graph listed
	 * tangles. Definition example: <code>"a-b-a => a,b"</code>.
	 * 
	 * @param definition
	 *            test definition
	 */
    private void test(String definition) {
        DependencyGraph graph = graph(StringUtils.substringBefore(definition, "=>"));
        String[] expectedTangles = StringUtils.substringAfter(definition, "=>").split("\\s*;\\s");
        Collection<Tangle> detectedTangles = new TanglesDetector(graph).detect();
        HashSet<Tangle> superfluousTangles = new HashSet<Tangle>();
        superfluousTangles.addAll(detectedTangles);
        for (String expectedTangle : expectedTangles) {
            boolean found = false;
            for (Tangle detectedTangle : detectedTangles) {
                if (vertexSet(expectedTangle).equals(detectedTangle.vertexSet())) {
                    found = true;
                    superfluousTangles.remove(detectedTangle);
                    break;
                }
            }
            if (!found) {
                String msg = "tangle " + expectedTangle + " not detected for graph " + graph + "\n";
                msg += "detected only: " + detectedTangles;
                fail(msg);
            }
        }
        if (superfluousTangles.size() != 0) {
        	String msg = "too many tangles detected for graph " + graph + "\n";
        	msg += "superfluous tangles: " + superfluousTangles;
        }
    }
	
    @Test
    public void testMergeCycles() {
    	LinkedHashSet<Path> cycles = new LinkedHashSet<Path>();
    	cycles.add(path("a-b-c"));
    	cycles.add(path("b-c-d"));
    	cycles.add(path("x-y-z"));
    	cycles.add(path("z"));
    	Collection<Tangle> tangles = TanglesDetector.mergeCycles(cycles);
    	assertThat(tangles, hasItem(tangle("a,b,c,d")));
    	assertThat(tangles, hasItem(tangle("x,y,z")));
    	assertEquals(2, tangles.size());
    }
    
    @Test
    public void testMergeCyclesAnotherCase() {
    	LinkedHashSet<Path> cycles = new LinkedHashSet<Path>();
    	cycles.add(path("a-b-x"));
    	cycles.add(path("c-d-y"));
    	cycles.add(path("a-b-c-d"));
    	Collection<Tangle> tangles = TanglesDetector.mergeCycles(cycles);
    	assertThat(tangles, hasItem(tangle("a,b,c,d,x,y")));
    	assertEquals(1, tangles.size());
    }
}
