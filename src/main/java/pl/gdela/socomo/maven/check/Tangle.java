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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tangle is a set of vertices that are circular dependent of each other.
 * 
 * @author wgdela
 */
public class Tangle {
    
    private TreeSet<String> vertices = new TreeSet<String>();
    
    public Tangle() {
    	// noop
    }
    
    public Tangle(Collection<String> vertices) {
    	addVertices(vertices);
	}
    
	public void addVertices(Collection<String> vertices) {
		for (String vertex : vertices) {
			addVertex(vertex);
		}
	}
	
	public void addVertex(String vertex) {
        this.vertices.add(vertex);
    }

    public Set<String> vertexSet() {
        return Collections.unmodifiableSet(vertices);
    }
    
	public boolean overlaps(Tangle other) {
		for (String vertex : vertices) {
			if (other.vertices.contains(vertex)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns new tangle containing vertices from given tangle and this tangle.
	 * @param other
	 * @return
	 */
	public Tangle mergeWith(Tangle other) {
		Tangle merged = new Tangle();
		merged.addVertices(this.vertices);
		merged.addVertices(other.vertices);
		return merged;
	}
    
	@Override
	public int hashCode() {
		return vertices.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tangle other = (Tangle) obj;
		return vertices.equals(other.vertices);
	}

	@Override
	public String toString() {
		return vertices.toString();
	}
}
