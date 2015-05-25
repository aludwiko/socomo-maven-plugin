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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.objectweb.asm.ClassReader;
import pl.gdela.socomo.maven.check.visitor.DependencyClassVisitor;
import pl.gdela.socomo.maven.check.visitor.VisitorDataCollector;

/**
 * Checks source code modularity.
 * 
 * @XXXgoal check TODO: temporarily this mojo is off (for site generation purposes)
 */
public class CheckMojo extends AbstractMojo {
    /**
     * The package to check.
     * @parameter expression="${socomo.rootPackage}"
     * @required
     */
    protected String rootPackage;
    
    /**
     * Directory containing *.class files to check.
     * @parameter expression="${socomo.classesDirectory}" default-value="${project.build.outputDirectory}"
     */
    protected File classesDirectory;

    /** @parameter default-value="${project}" */
    protected MavenProject mavenProject;

    public void execute() throws MojoExecutionException {
		VisitorDataCollector visitorDataCollector = new VisitorDataCollector();
    	Iterator<File> it = FileUtils.iterateFiles(classesDirectory, new String[] { "class" }, true);
    	while (it.hasNext()) {
    		try {
				new ClassReader(new FileInputStream(it.next())).accept(new DependencyClassVisitor(visitorDataCollector), 0);
			} catch (IOException e) {
				throw new MojoExecutionException("could not read file", e);
			}
    	}
    	getLog().info("pakiet to " + rootPackage + ", katalog to " + classesDirectory);
    	try {
			DependencyTracker.drawDiagram(visitorDataCollector);
			// TODO: przekazywa� rootpackage do filtrowania
			DependencyGraph g = JGraphTDemo.visitorToGraph(visitorDataCollector);
			Collection<Tangle> tangles = new TanglesDetector(g).detect();
			if (!tangles.isEmpty()) {
			    getLog().warn("your code is tangled, please fix those!");
			}
			for (Tangle tangle : tangles) {
			    // TODO: beautify tangle list: order from smallest to biggest, show only x biggest ones, make sure one tangle fit to one line
			    getLog().warn(tangle.toString());
			}
		} catch (IOException e) {
			throw new MojoExecutionException("could not draw diagram", e);
		}
    }
}