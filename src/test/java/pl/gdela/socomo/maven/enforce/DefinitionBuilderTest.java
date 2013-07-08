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

import java.util.HashSet;
import pl.gdela.socomo.maven.enforce.DefinitionBuilder;

import junit.framework.TestCase;

public class DefinitionBuilderTest extends TestCase {
	
	private final DefinitionBuilder builder = new DefinitionBuilder("");

    public void testCellName() {
        assertEquals("alfa", builder.normalizeName(new Cell("alfa")));
        assertEquals("bla_foo", builder.normalizeName(new Cell("bla foo")));
    }
    
    public void testConvertToSetDefinition() {
        assertEquals("[alfa] = alfa.*", builder.convert(new Cell("alfa", "alfa.*")).toString());
        assertEquals("[foo_bla] = foobla", builder.convert(new Cell("foo bla", "foobla")).toString());
        assertEquals("[nopattern] = _empty_", builder.convert(new Cell("nopattern")).toString());
        assertEquals("[bf] = bla.* foo.*", builder.convert(new Cell("bf", "bla.*, foo.*")).toString());
        assertEquals("[alfa] = alfa.* excluding alfa.*.*", builder.convert(new Cell("alfa", "alfa.?")).toString());
        assertEquals("[bf] = bla.* foo.* excluding bla.*.*", builder.convert(new Cell("bf", "bla.?, foo.*")).toString());
        assertEquals("[bf] = bla.* foo.* excluding foo.*.*", builder.convert(new Cell("bf", "bla.*, foo.?")).toString());
        assertEquals("[bf] = bla.* foo.* excluding bla.*.* foo.*.*", builder.convert(new Cell("bf", "bla.?, foo.?")).toString());
    }
    
    public void testConvertWithExcluding() {
        builder.setExcludes("foo.*, bla.*");
        assertEquals("[alfa] = alfa.* excluding bla.* foo.*", builder.convert(new Cell("alfa", "alfa.*")).toString());
        builder.setExcludes(null);
    }

    public void testBuild() {
        Cell alfa = new Cell("alfa", "example.alfa.*");
        Cell beta = new Cell("beta", "example.beta.*");
        Cell gama = new Cell("gama", "example.gama.*");
        HashSet<Link> disallowedLinks = new HashSet<Link>();
        disallowedLinks.add(new Link(alfa, beta));
        disallowedLinks.add(new Link(gama, beta));
        String expected = "";
        expected += "[alfa] = example.alfa.*\n";
        expected += "[beta] = example.beta.*\n";
        expected += "[gama] = example.gama.*\n";
        expected += "check [alfa] directlyIndependentOf [beta]\n";
        expected += "check [gama] directlyIndependentOf [beta]\n";
        assertEquals(expected, new DefinitionBuilder("").build(disallowedLinks));
    }
    
    public void testContains() {
        assertFalse(DefinitionBuilder.contains("foo.*", "foo.*"));
        
        assertTrue(DefinitionBuilder.contains("foo.*", "foo.bla.*"));
        assertTrue(DefinitionBuilder.contains("foo.*.*", "foo.bla.*"));
        assertFalse(DefinitionBuilder.contains("foo.bla.*", "foo.*"));
        assertFalse(DefinitionBuilder.contains("foo.bla.*", "foo.*.*"));
        
        assertTrue(DefinitionBuilder.contains("alfa.beta.*", "alfa.*.gamma.*"));
        assertTrue(DefinitionBuilder.contains("alfa.*.gamma.*", "alfa.beta.*"));
        
        assertTrue(DefinitionBuilder.contains("pl.gdela.klient.*", "pl.gdela.*.logika.*"));
        assertTrue(DefinitionBuilder.contains("pl.gdela.*.logika.*", "pl.*.klient.*"));
    }
    
    public void testBuildWithOverlappingPatterns() {
        Cell building = new Cell("building", "building.*");
        Cell roof = new Cell("roof", "building.roof.?");
        Cell chimney = new Cell("chimney", "building.roof.chimney.*");
        Cell parking = new Cell("parking", "parking.*");
        HashSet<Link> disallowedLinks = new HashSet<Link>();
        disallowedLinks.add(new Link(building, parking));
        disallowedLinks.add(new Link(roof, parking));
        disallowedLinks.add(new Link(chimney, parking));
        String expected = "";
        expected += "[building] = building.* excluding building.roof.* building.roof.chimney.*\n";
        expected += "[chimney] = building.roof.chimney.*\n";
        expected += "[parking] = parking.*\n";
        expected += "[roof] = building.roof.* excluding building.roof.*.* building.roof.chimney.*\n";
        expected += "check [building] directlyIndependentOf [parking]\n";
        expected += "check [chimney] directlyIndependentOf [parking]\n";
        expected += "check [roof] directlyIndependentOf [parking]\n";
        assertEquals(expected, new DefinitionBuilder("").build(disallowedLinks));
    }
}
