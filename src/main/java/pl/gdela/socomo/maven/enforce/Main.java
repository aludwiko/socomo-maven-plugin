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
import java.util.Set;

import org.jdom.JDOMException;

public class Main {
    public static void main(String[] args) throws IOException {
        DefinitionCollector collector = null;
        Grid grid = new DiagramReader(new File("yyy.java.hsp")).read("xxx");
        Set<Link> disallowedLinks = grid.getDisallowedLinks();
        // TODO: maybe add intermediate step of transforming s101 model into ddf model
        String definition = new DefinitionBuilder().build(disallowedLinks);
        String description = "diagram XXX from file YYY";
        collector.collect(definition, description, new File("."));
    }
}
