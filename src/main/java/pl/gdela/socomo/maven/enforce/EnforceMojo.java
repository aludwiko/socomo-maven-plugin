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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import pl.gdela.socomo.maven.acyclity.AcyclityChecker;
import pl.gdela.socomo.maven.check.Dependencies;
import pl.gdela.socomo.maven.existence.ExistenceChecker;

import classycle.Analyser;
import classycle.dependency.DefaultResultRenderer;
import classycle.dependency.DependencyChecker;
import classycle.dependency.ResultRenderer;
import classycle.util.AndStringPattern;
import classycle.util.NotStringPattern;
import classycle.util.StringPattern;
import classycle.util.WildCardPattern;

/**
 * Checks if class dependencies conforms to provided definitions.
 * 
 * @goal enforce
 * @phase compile
 * @author Wojciech Gdela
 */
public class EnforceMojo extends AbstractMojo {
	/**
	 * Generated definition file name.
	 */
	private static final String DEFINITION_FILE_NAME = "target/collected-dependency-rules.ddf";
	
	/**
	 * Whether to skip socomo step altogether. Useful as a temporary way to build your project i.e.
	 * during refactoring.
	 * 
	 * @parameter expression="${socomo.skip}" default-value="false"
	 */
    protected boolean skip;

	/**
     * Directory containing *.class files to check.
     * @parameter expression="${socomo.classesDirectory}" default-value="${project.build.outputDirectory}"
     */
    protected File classesDirectory;
    
	/**
	 * Comma or space separated list of wild-card patterns of fully-qualified
	 * class name which are included in the analysis. Only '*' are recognized as
	 * wild-card character. By default all classes defined in the file set are
	 * included.
	 * 
	 * @parameter expression="${socomo.includingClasses}"
	 */
    protected String includingClasses;
    
	/**
	 * Comma or space separated list of wild-card patterns of fully-qualified
	 * class name which are excluded from the analysis. Only '*' are recognized
	 * as wild-card character. By default no class defined in the file set is
	 * excluded.
	 * 
	 * @parameter expression="${socomo.excludingClasses}"
	 */
    protected String excludingClasses;
    
	/**
	 * Comma or space separated list of wild-card patterns of fully-qualified
	 * class name. Only '*' are recognized as wild-card character.
	 * 
	 * If in the code of a class an ordinary string constant matches one of
	 * these patterns and if this string constant has a valid syntax for a
	 * fully-qualified class name this constant will be treated as a class
	 * reference.
	 * 
	 * By default ordinary string constants are not treated as class references.
	 * 
	 * @parameter expression="${socomo.reflectionPattern}"
	 */
    protected String reflectionPattern;
    
	/**
	 * Dependency definition commands. See <a
	 * href="http://gdela.pl/socomo/usage.html">syntax documentation</a>. Use this only for
	 * simple definitions, prefered way is to store them in *.scm file. Mojo will automatically
	 * detect those files. See also <code>files</code> parameter.
	 * 
	 * @parameter expression="${socomo.definition}"
	 */
	protected String definition;

	/**
	 * Paths of the dependency definition file. It is either absolute or relative to the base
	 * directory. The filename must end with <code>*.scm</code> extension.
	 * 
	 * @parameter
	 */
    protected List<String> files;
    
	/**
	 * Path of the dependency definition file. You can use this instead of
	 * <code>files</code> parameter if you have only one file
	 * 
	 * @parameter expression="${socomo.file}"
	 */
    protected String file;
	
    public void execute() throws MojoExecutionException, MojoFailureException {
    	if (skip) {
    		getLog().warn("skipping socomo checks");
    		return;
    	}
    	boolean mergeInnerClasses = true;
        Analyser analyser = new Analyser(getClassFileNames(), getPattern(), getReflectionPattern(), mergeInnerClasses);
        DefinitionCollector definitionCollector = getDefinitionCollector();
        try {
			FileUtils.writeStringToFile(new File(DEFINITION_FILE_NAME), definitionCollector.getResult(), "UTF-8");
		} catch (IOException e) { /* ignore */ }
        if (!definitionCollector.hasResult()) {
        	getLog().warn("no depedency definitions found");
        }
        
        getLog().info("enforcing source code structure");
        PrintWriter output = new PrintWriter(System.out);
        
        boolean structureValid = true;
        
        // classycle
    	try {
    		ResultRenderer renderer = new DefaultResultRenderer();
			DependencyChecker dependencyChecker = new DependencyChecker(analyser, definitionCollector.getResult(), getProperties(), renderer);
			structureValid &= dependencyChecker.check(output);
	        output.flush();
    	} catch (IllegalArgumentException e) {
    		String msg = DEFINITION_FILE_NAME + " syntax " + StringUtils.uncapitalize(e.getMessage());
			throw new MojoExecutionException(msg);
    	}
    	
    	// TODO: a conceptual model of  raw dependencies
        Map<String, Map<String, Integer>> dependencies = Dependencies.analyze(getClassFiles());
    	
    	// acyclity
    	AcyclityChecker acyclityChecker = new AcyclityChecker(getLog(), dependencies);
    	structureValid &= acyclityChecker.check(definitionCollector.getAcyclicPackages(), output);
    	output.flush();
    	
    	// existence
        ExistenceChecker checker = new ExistenceChecker(getLog(), dependencies);
        structureValid &= checker.check(definitionCollector.getExistenceRules(), output);
        output.flush();
    	
    	if (structureValid) {
    		getLog().info("source code structured correctly");
    	} else {
        	throw new MojoFailureException("Unwanted dependencies found. See output for details.");
        }
    }

	private String[] getClassFileNames() {
		Collection<File> files = getClassFiles();
        String[] paths = new String[files.size()];
        int i = 0;
        for (Iterator<File> filesIt = files.iterator(); filesIt.hasNext();) {
        	File file = filesIt.next();
        	paths[i++] = file.getAbsolutePath();
        }
        return paths;
	}
	
	private Collection<File> getClassFiles() {
	    if (classesDirectory.isDirectory()) { 
            IOFileFilter fileFilter = new SuffixFileFilter(".class");
            IOFileFilter dirFilter = new NotFileFilter(new WildcardFileFilter("*-INF")); // META-INF, APP-INF, etc.
	        return FileUtils.listFiles(classesDirectory, fileFilter, dirFilter);
        } else {
            return Collections.EMPTY_LIST;
        }
	}

	private StringPattern getPattern() {
		AndStringPattern pattern = new AndStringPattern();
		if (StringUtils.isNotBlank(includingClasses)) {
			pattern.appendPattern(WildCardPattern.createFromsPatterns(includingClasses, ", "));
		}
		if (StringUtils.isNotBlank(excludingClasses)) {
			pattern.appendPattern(new NotStringPattern(WildCardPattern.createFromsPatterns(excludingClasses, ", ")));
		}
	    return pattern;
	}

	private StringPattern getReflectionPattern() {
		if (StringUtils.isNotBlank(reflectionPattern)) {
			return WildCardPattern.createFromsPatterns(reflectionPattern, ", ");
		} else {
			return null;
		}
	}
	
	private DefinitionCollector getDefinitionCollector() throws MojoFailureException, MojoExecutionException {
		DefinitionCollector collector = new DefinitionCollector();
		if (definition != null) {
			collector.collect(definition, "pom.xml fragment or system property", new File("."));
		}
		if (file != null) {
			collector.collect(new File(file));
		}
		if (files != null) {
			for (String file : files) {
				collector.collect(new File(file));
			}
		}
		if (definition == null && file == null && files == null) {
			// autodetect
			File dir = new File(".").getAbsoluteFile();
			while (true) {
				collector.collectFromDir(dir);
				if (collector.hasResult()) {
					break;
				}
				dir = dir.getParentFile();
				if (!new File(dir, "pom.xml").exists()) {
					break;
				}
			}
		}
		return collector;
	}

	private Map getProperties() {
		return System.getProperties();
	}
}
