/**
 * Copyright 2012 Wojciech Gdela
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

/**
 * Manual override of a general grid rule. Class is named <code>Overturn</code>
 * to not conflict with <code>java.lang.Override</code>.
 */
public class Overturn {
	/**
	 * Source is the "from" end of a link. 
	 */
	final Cell source;
	
	/**
	 * Target is the "to" end of a link.
	 */
	final Cell target;
	
	/**
	 * Whether we override to explicitly allow this link (true) or to explicitly
	 * disallow this link (false).
	 */
	final boolean allowed;
	
	public Overturn(Cell source, Cell target, boolean allowed) {
		this.source = source;
		this.target = target;
		this.allowed = allowed;
	}

	@Override
	public String toString() {
		String symbol = allowed ? " -> " : " #> ";
		return source + symbol + target;
	}
	
	
}
