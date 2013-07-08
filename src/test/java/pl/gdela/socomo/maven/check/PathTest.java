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

import java.util.NoSuchElementException;

import org.junit.Test;

public class PathTest {
	
	@Test
	public void testSubpathFrom() {
		assertEquals(path("a-b-c").subpathFrom("a"), path("a-b-c"));
		assertEquals(path("a-b-c").subpathFrom("b"), path("b-c"));
		assertEquals(path("a-b-c").subpathFrom("c"), path("c"));
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testSubpathFromNoSuchElement() {
		path("a-b-c").subpathFrom("d");	
	}

	@Test
	public void testSubpathTo() {
		assertEquals(path("a-b-c").subpathTo("a"), path("a"));
		assertEquals(path("a-b-c").subpathTo("b"), path("a-b"));
		assertEquals(path("a-b-c").subpathTo("c"), path("a-b-c"));
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testSubpathToNoSuchElement() {
		path("a-b-c").subpathTo("d");	
	}

	@Test
	public void testSubpath() {
		assertEquals(path("a-b-c-d").subpath("b", "c"), path("b-c"));
		assertEquals(path("a-b-c-d").subpath("a", "c"), path("a-b-c"));
		assertEquals(path("a-b-c-d").subpath("b", "d"), path("b-c-d"));
		assertEquals(path("a-b-c-d").subpath("a", "d"), path("a-b-c-d"));
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testSubpathNoSuchElement() {
		path("a-b-c-d").subpath("c", "b");	
	}
}
