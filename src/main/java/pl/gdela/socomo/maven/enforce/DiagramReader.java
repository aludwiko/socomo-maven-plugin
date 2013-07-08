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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import pl.gdela.socomo.maven.LogRuntimeException;

public class DiagramReader {
    private File projectFile;
    
    private Document document;

    public DiagramReader(File projectFile) {
        this.projectFile = projectFile;
    }
    
    private void readDocument() throws JDOMException, IOException {
        document = new SAXBuilder().build(projectFile);   
    }
    
    /**
     * Reads all diagrams from file.
     * @return
     * @throws IOException
     */
	public List<Grid> readAll() throws IOException {
		try {
			readDocument();
			ArrayList<Grid> grids = new ArrayList<Grid>();
			List<Element> gridEls = XPath.selectNodes(document, "/local-project/grid-set/grid");
			for (Iterator<Element> gridElIt = gridEls.iterator(); gridElIt.hasNext(); ) {
				grids.add(parseGrid(gridElIt.next()));
			}
			return grids;
		} catch (JDOMException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Reads specified diagram from file.
	 * @param name
	 * @return diagram or <code>null</code> if there's no such diagram
	 * @throws IOException
	 */
    public Grid read(String name) throws IOException {
    	try {
	        readDocument();
	        Element gridEl = (Element) XPath.selectSingleNode(document, "/local-project/grid-set/grid[@name='" + name + "']");
	        if (gridEl != null) {
	        	return parseGrid(gridEl);
	        } else {
	        	return null;
	        }
	    } catch (JDOMException e) {
			throw new IOException(e);
		}
    }

	private Grid parseGrid(Element gridEl) {
		Grid grid = new Grid(gridEl.getAttributeValue("name"));
		grid.setExcludes(gridEl.getAttributeValue("excludes"));
		for (Iterator<Element> rowElIt = gridEl.getChildren("row").iterator(); rowElIt.hasNext();) {
        	Element rowEl = rowElIt.next();
        	grid.rows.add(parseRow(rowEl));
        }
		for (Iterator<Element> overturnElIt = gridEl.getChildren("override").iterator(); overturnElIt.hasNext();) {
        	Element overturnEl = overturnElIt.next();
        	grid.overturns.add(parseOverturn(overturnEl, grid));
        }
		return grid;
	}

	private Row parseRow(Element rowEl) {
		Row row = new Row();
		for (Iterator<Element> cellElIt = rowEl.getChildren("cell").iterator(); cellElIt.hasNext();) {
        	Element cellEl = cellElIt.next();
        	row.cells.add(parseCell(cellEl));
        }
		return row;
	}

	private Cell parseCell(Element cellEl) {
		Cell cell = new Cell(cellEl.getAttributeValue("name"), cellEl.getAttributeValue("pattern"));
		Element gridEl = cellEl.getChild("grid");
		if (gridEl != null) {
			cell.grid = parseGrid(gridEl);
		}
		return cell;
	}
	
	private Overturn parseOverturn(Element overturnEl, Grid grid) {
		Cell source = findCell(grid, overturnEl.getAttributeValue("source"));
		Cell target = findCell(grid, overturnEl.getAttributeValue("target"));
		boolean allowed = Boolean.parseBoolean(overturnEl.getAttributeValue("allowed"));
		return new Overturn(source, target, allowed);
	}

	/**
	 * Returns cell located at position specified in S101 notation.
	 * @param grid the grid at which we start looking
	 * @param location location specification, i.e. $GP1.0$GP2.3
	 */
	private Cell findCell(Grid grid, String location) {
		Pattern pattern = Pattern.compile("\\$GP([0-9]+)\\.([0-9]+)(.*)");
		Matcher matcher = pattern.matcher(location);
		if (matcher.matches()) {
			int rowNr = Integer.parseInt(matcher.group(1));
			int cellNr = Integer.parseInt(matcher.group(2));
			String remainingLocation = matcher.group(3);
			Cell cell = grid.getCell(rowNr, cellNr);
			if (remainingLocation.isEmpty()) {
				return cell;
			} else {
				return findCell(cell.grid, remainingLocation);
			}
		} else {
			throw new LogRuntimeException("invalid location specified: " + location);
		}
	}
}
