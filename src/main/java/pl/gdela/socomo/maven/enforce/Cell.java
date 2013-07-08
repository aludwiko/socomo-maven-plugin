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

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * Cell in Structure101 diagram.
 * Note: this class has a natural ordering that is inconsistent with equals.
 * @author wgdela
 */
class Cell implements Comparable<Cell> {
    final String name;
    final String pattern;
    Grid grid;

    public Cell(String name) {
        this(name, (Grid) null);
    }

    public Cell(String name, Grid grid) {
        this(name, null, grid);
    }

    public Cell(String name, String pattern) {
        this(name, pattern, null);
    }
    
    private Cell(String name, String pattern, Grid grid) {
        this.name = name;
        this.pattern = pattern;
        this.grid = grid;
    }

    /**
     * Returns all cells contained in this cell (in subgrids) not including this cell.
     * @return
     */
    public Collection<Cell> getAllSubCells() {
        if (grid != null) {
            return grid.getAllCells();
        } else {
            return Collections.emptyList();
        }
    }
    
    public int compareTo(Cell other) {
        return new CompareToBuilder().append(this.name, other.name).append(this.pattern, other.pattern).toComparison();
    }

    @Override
    public String toString() {
        return StringUtils.isNotBlank(name) ? name : "_anonymous_";
    }
}
