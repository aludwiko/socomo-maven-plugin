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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

// TODO: maybe we can make this package protected 
public class Grid {
    private final String name;
	private String excludes;
	final List<Row> rows = new ArrayList<Row>();
	final List<Overturn> overturns = new ArrayList<Overturn>();
    
	public Grid(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
    public String getExcludes() {
        return excludes;
    }
    
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }
    
    public Set<Link> getAllLinks() {
    	return cartesian(getAllCells(), getAllCells());
    }

    /**
     * Returns set indicating allowed cells usage. If returned set contains link from A to B, it
     * means that cell A can use cell B.
     * 
     * <p>
     * Allowed links are calculated according to Structure101 specification: <blockquote>Layering is
     * indicated by the top-down arrangement of cells. Cells should only be used by cells in higher
     * layers (unless specifically allowed or disallowed by a layering override). You can specify
     * that strict layering applies to a diagram - in this case cells should only depend on cells in
     * the next layer, not on cells below that.</blockquote>
     * @return
     */
    public Set<Link> getAllowedLinks() {
        Set<Link> result = new HashSet<Link>();
        // connect on this level
        for (int nrFrom = 0; nrFrom < rows.size(); nrFrom++) {
            Row fromRow = rows.get(nrFrom);
            for (int nrTo = nrFrom + 1; nrTo < rows.size(); nrTo++) {
                Row toRow = rows.get(nrTo);
                result.addAll(cartesian(fromRow.getAllCells(), toRow.getAllCells()));
            }
        }
        // recursively connect on lower levels
        for (Row row : rows) {
        	for (Cell cell : row.cells) {
	            if (cell.grid != null) {
	                result.addAll(cell.grid.getAllowedLinks());
	            }
        	}
        }
        // consider when one cell contains another
        for (Row row : rows) {
        	for (Cell cell : row.cells) {
	            if (cell.grid != null) {
	                result.addAll(cartesian(cell, cell.grid.getAllCells()));
	                result.addAll(cartesian(cell.grid.getAllCells(), cell));
	            }
        	}
        }
        // TODO: consider strict layering
        // consider manual overrides of general rules
        for (Overturn overturn : overturns) {
        	Link link = new Link(overturn.source, overturn.target);
			if (overturn.allowed) {
        		result.add(link); // override rules, this link is allowed
        	}
        	if (!overturn.allowed) {
        		result.remove(link); // override rules, this link is disallowed
        	}
        }
        return result;
    }
    
	public Set<Link> getDisallowedLinks() {
    	Set<Link> result = new HashSet<Link>();
    	result.addAll(getAllLinks());
    	result.removeAll(getAllowedLinks());
    	return result;
    }

    /**
     * Returns cartesian product of two sets. Resulting set excludes pairs from diagonal relation (loops). 
     */
    private Set<Link> cartesian(Collection<Cell> fromCells, Collection<Cell> toCells) {
        HashSet<Link> result = new HashSet<Link>();
        for (Cell from : fromCells) {
            for (Cell to : toCells) {
            	if (!from.equals(to)) {
            		result.add(new Link(from, to));
            	}
            }
        }
        return result;
    }
    
    private Set<Link> cartesian(Cell fromCell, Collection<Cell> toCells) {
    	HashSet<Cell> fromCells = new HashSet<Cell>(1);
    	fromCells.add(fromCell);
		return cartesian(fromCells, toCells);
	}
    
    private Set<Link> cartesian(Collection<Cell> fromCells, Cell toCell) {
    	HashSet<Cell> toCells = new HashSet<Cell>(1);
    	toCells.add(toCell);
		return cartesian(fromCells, toCells);
	}

    /**
     * Returns all cells contained in this grid and all it's subgrids. 
     * @return
     */
    public Collection<Cell> getAllCells() {
        ArrayList<Cell> result = new ArrayList<Cell>();
        for (Row row : rows) {
            result.addAll(row.getAllCells());
        }
        return result;
    }
    
	/**
	 * Returns cell with given name.
	 * 
	 * @param name
	 * @return cell or <code>null</code> if no such cell found
	 * @throws IllegalArgumentException
	 *             if there's more than one cell with that name
	 */
	public Cell getCell(String name) throws IllegalArgumentException {
		Cell result = null;
		for (Cell cell : getAllCells()) {
			if (StringUtils.equals(cell.name, name)) {
				if (result == null) {
					result = cell;
				} else {
					throw new IllegalArgumentException("there's more than one cell named '" + name + "'");
				}
			}
		}
		return result;
	}

	/**
	 * Returns cell at the specified location.
	 */
	public Cell getCell(int rowNr, int cellNr) {
		return rows.get(rowNr).cells.get(cellNr);
	}
    
    @Override
    public String toString() {
    	return toString("");
    }

    public String toString(String prefix) {
        String result = "";
        for (Row row : rows) {
        	result += prefix;
            for (Cell cell : row.cells) {
            	result += cell.name;
            	if (cell.grid != null) {
            		result += "(\n" + cell.grid.toString(prefix + "  ") + "\n" + prefix + ")";
            	}
            	result += " ";
            }
            result += "\n";
        }
        return StringUtils.chomp(result);
    }
}
