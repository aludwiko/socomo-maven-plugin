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

import org.junit.Test;

public class TangleTest {

	@Test
	public void testOverlaps() {
		assertTrue(tangle("a,b,c").overlaps(tangle("b,c,d")));
		assertFalse(tangle("a,b,c").overlaps(tangle("x,y,z")));
	}

	@Test
	public void testMergeWith() {
		assertEquals(tangle("a,b,c"), tangle("a,b").mergeWith(tangle("b,c")));
	}

}
