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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class GridTest extends TestCase {
    
	// TODO: refactor for better readability
	
    public void testListAllowedDependencies() {
        // Grid definitions:
        // ' ' is for next cell
        // '/' is for next row
        // 'foo('..')' is for subgrid in cell foo
    	
        assertEquals(
                toset("top-bottom"),
                flatten(grid("top / bottom").getAllowedLinks()));
        assertEquals(
                toset("top-bottom bottom-top"),
                flatten(grid("top / bottom").getAllLinks()));
        
        assertEquals(
                toset("top-middle top-bottom middle-bottom"),
                flatten(grid("top / middle / bottom").getAllowedLinks()));
        assertEquals(
                toset("top-middle top-bottom middle-top middle-bottom bottom-top bottom-middle"),
                flatten(grid("top / middle / bottom").getAllLinks()));
        
        assertEquals(
                Collections.EMPTY_SET,
                flatten(grid("left right").getAllowedLinks()));
        
        assertEquals(
                toset("top-midleft top-midright top-bottom midleft-bottom midright-bottom"),
                flatten(grid("top / midleft midright / bottom").getAllowedLinks()));
        
        assertEquals(
                toset("top-midleft top-midright top-bottom midleft-bottom midright-bottom top-mid mid-bottom mid-midleft midleft-mid mid-midright midright-mid"),
                flatten(grid("top / mid(midleft midright) / bottom").getAllowedLinks()));
        
        assertEquals(
                toset("midtop-midbottom mid-midtop midtop-mid mid-midbottom midbottom-mid"),
                flatten(grid("left mid(midtop / midbottom) right").getAllowedLinks()));
        
        assertEquals(
                toset("nw-sw nw-se ne-sw ne-se"),
                flatten(grid("nw ne / sw se").getAllowedLinks()));
        
        assertEquals(
                toset("nw-sw ne-se w-nw nw-w w-sw sw-w e-ne ne-e e-se se-e"),
                flatten(grid("w(nw / sw) e(ne / se)").getAllowedLinks()));
        
        assertEquals(
                toset("xb-base xt-xb xt-base yb-base yt-yb yt-base yt-xt yt-xb yt-x zb-base zt-zb zt-base zt-yt zt-yb zt-y zt-xt zt-xb zt-x x-base y-base y-yt yt-y y-yb yb-y x-xt xt-x x-xb xb-x y-x x-y y-xt xt-y y-xb xb-y"),
                flatten(grid("zt / zb y(yt / yb x(xt / xb)) / base").getAllowedLinks()));
    }
    
    public void testListAllowedDependenciesWithOverturns() {
    	Grid grid = grid("top / middle / bottom");
    	assertEquals(
                toset("top-middle top-bottom middle-bottom"),
                flatten(grid.getAllowedLinks()));
    	
    	grid.overturns.add(new Overturn(grid.getCell("top"), grid.getCell("bottom"), false));
		assertEquals(
                toset("top-middle middle-bottom"),
                flatten(grid.getAllowedLinks()));
		
		grid.overturns.add(new Overturn(grid.getCell("middle"), grid.getCell("top"), true));
		assertEquals(
                toset("top-middle middle-bottom middle-top"),
                flatten(grid.getAllowedLinks()));
    }
    
    private Set<String> flatten(Set<Link> links) {
    	HashSet<String> result = new HashSet<String>();
    	for (Link link : links) {
    		result.add(link.getFrom() + "-" + link.getTo());
    	}
    	return result;
	}

	private Grid grid(String definition) {
        return grid(new StringBuffer(definition));
    }

    private Grid grid(StringBuffer definition) {
        Grid grid = new Grid(null);
        Row row = new Row();
        grid.rows.add(row);
        String name = "";
        while (definition.length() > 0) {
            char c = definition.charAt(0);
            definition.deleteCharAt(0);
            if (Character.isLetterOrDigit(c)) {
                name += c;
            }
            else if (Character.isWhitespace(c)) {
                if (!name.isEmpty()) {
                    Cell cell = new Cell(name);
                    row.cells.add(cell);
                    name = "";
                }
            }
            else if (c == '/') {
                row = new Row();
                grid.rows.add(row);
            }
            else if (c == '(') {
                Cell cell = new Cell(name, grid(definition));
                row.cells.add(cell);
                name = "";
            }
            else if (c == ')') {
                break;
            }
            else {
                throw new IllegalArgumentException("unexpected character in the definition: " + definition);
            }
        }
        if (!name.isEmpty()) {
            Cell cell = new Cell(name);
            row.cells.add(cell);
            name = "";
        }
        return grid;
    }

    private Set<String> toset(String string) {
        return new HashSet<String>(Arrays.asList(string.split("\\s+")));
    }
}
