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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;

/**
 * Path is a list of vertices. 
 * @author Wojtek
 */
public class Path {
	
	private ArrayDeque<String> vertices = new ArrayDeque<String>();
	
	public static Path parsePath(String definition) {
		definition = definition.replaceAll("\\s+", "");
		Path path = new Path();
		for (String vertex : definition.split("-")) {
			path.addNextStep(vertex);
		}
		return path;
	}
	
	public List<String> verticesList() {
		return new ArrayList<String>(vertices);
	}

	public boolean contains(String vertex) {
		return vertices.contains(vertex);
	}

	/**
	 * Adds another vertex at the end of the path.
	 * @param vertex
	 */
	public void addNextStep(String vertex) {
		vertices.addLast(vertex);
	}

	/**
	 * Removes vertex from the end of the path.
	 * @return
	 */
	public String removeLastStep() {
		return vertices.removeLast();
	}
	
	/**
	 * Returns subpath from given vertex to the end of this path.
	 * @param vertex the beginning of subpath
	 * @return subpath
	 * @throws NoSuchElementException if path does not contain given vertex
	 */
	public Path subpathFrom(String vertex) throws NoSuchElementException {
		return subpath(vertex, null);
	}
	
	/**
	 * Returns subpath from the beginning of this path to the given vertex.
	 * @param vertex the beginning of subpath
	 * @return subpath
	 * @throws NoSuchElementException if path does not contain given vertex
	 */
	public Path subpathTo(String vertex) {
		return subpath(null, vertex);
	}
	
	/**
	 * Returns subpath of this path from and to given vertices.
	 * @param from
	 * @param to
	 * @return
	 * @throws NoSuchElementException if path does not contain given vertices
	 */
	public Path subpath(String from, String to) {
		if (from == null) {
			from = vertices.getFirst();
		}
		if (to == null) {
			to = vertices.getLast();
		}
		if (from.equals(to) && vertices.contains(from)) {
			Path subpath = new Path();
			subpath.addNextStep(from);
			return subpath;
		}
		
		Iterator<String> verticesIt = vertices.iterator();
		for (;;) {
			if (!verticesIt.hasNext()) {
				throw new NoSuchElementException("path " + this + " does not contains subpath from " + from);
			}
			if (verticesIt.next().equals(from)) {
				break;
			}
		}
		
		
		Path subpath = new Path();
		subpath.addNextStep(from);
		for (;;) {
			if (!verticesIt.hasNext()) {
				if (!vertices.contains(to)) {
					throw new NoSuchElementException("path " + this + " does not contains subpath to " + to);	
				} else {
					throw new NoSuchElementException("path " + this + " does not contains subpath from " + from + " to " + to);
				}
			}
			String vertex = verticesIt.next();
			subpath.addNextStep(vertex);
			if (vertex.equals(to)) {
				break;
			}
		}
		
		return subpath;
	}
	
	@Override
	public int hashCode() {
		return new ArrayList<String>(vertices).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Path other = (Path) obj;
		return new ArrayList<String>(vertices).equals(
				new ArrayList<String>(other.vertices));
	}

	@Override
	public String toString() {
		return StringUtils.join(vertices, "-");
	}
}
