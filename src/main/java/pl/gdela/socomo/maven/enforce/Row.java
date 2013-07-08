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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Row {
    final List<Cell> cells = new ArrayList<Cell>();

    /**
     * Returns all cells contained in this row and all it's subgrids.
     * @return
     */
    public Set<Cell> getAllCells() {
        HashSet<Cell> result = new HashSet<Cell>();
        result.addAll(cells);
        for (Cell cell : cells) {
            result.addAll(cell.getAllSubCells());
        }
        return result;
    }
    
    @Override
    public String toString() {
        String result = "";
        for (Cell cell : cells) {
            result += cell + " ";
        }
        return result.trim();
    }
}
