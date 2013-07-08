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

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

public class DiagramReaderTest extends TestCase {

    public void testRead() throws IOException {
        DiagramReader reader = new DiagramReader(new File("src/test/resources/example.java.hsp"));
        Grid primitive = reader.read("Primitive");
        System.out.println("--- primitive ---\n" + primitive);
        Cell anOnlyCell = primitive.getCell("an only cell");
        assertEquals("example.*", anOnlyCell.pattern);
        
        Grid complex = reader.read("Complex");
        System.out.println("--- complex ---\n" + complex);
        assertEquals("unwanted.*", complex.getExcludes());
        assertEquals(6, complex.getAllCells().size());
        assertNotNull(complex.getCell("ALFA").grid);
        assertEquals(4, complex.getCell("ALFA").grid.getAllCells().size());
        
        assertEquals(3, complex.overturns.size());
        assertEquals("top left #> bottom", complex.overturns.get(0).toString());
        assertEquals("bottom -> top right", complex.overturns.get(1).toString());
        assertEquals("BETA -> ALFA", complex.overturns.get(2).toString());
    }
    
    public void testReadAll() throws IOException {
    	DiagramReader reader = new DiagramReader(new File("src/test/resources/example.java.hsp"));
    	List<Grid> grids = reader.readAll();
    	assertEquals(3, grids.size());
    }

}
