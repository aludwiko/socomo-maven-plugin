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

import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * Classycle set definition. 
 * @author wgdela
 */
public class SetDefinition implements Comparable<SetDefinition> {
    private final String name;
    final TreeSet<String> including = new TreeSet<String>();
    final TreeSet<String> excluding = new TreeSet<String>();
    
    public SetDefinition(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public int compareTo(SetDefinition other) {
        return new CompareToBuilder()
                .append(this.name, other.name)
                .toComparison();
    }

    @Override
    public String toString() {
        String result = "[" + name + "] = ";
        if (!including.isEmpty()) {
            result += StringUtils.join(including, " ");
        } else {
            result += "_empty_";
        }
        if (!excluding.isEmpty()) {
            result += " excluding " + StringUtils.join(excluding, " ");
        }
        return result;
    }
}
