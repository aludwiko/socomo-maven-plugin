/**
 * Copyright 2011 Wojciech Gdela
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
package pl.gdela.socomo.ant.enforce;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import pl.gdela.socomo.maven.acyclity.AcyclityChecker;
import pl.gdela.socomo.maven.check.Dependencies;
import pl.gdela.socomo.maven.enforce.DefinitionCollector;
import pl.gdela.socomo.maven.existence.ExistenceChecker;
import classycle.Analyser;
import classycle.dependency.DefaultResultRenderer;
import classycle.dependency.DependencyChecker;
import classycle.dependency.ResultRenderer;
import classycle.util.AndStringPattern;
import classycle.util.NotStringPattern;
import classycle.util.StringPattern;
import classycle.util.TrueStringPattern;
import classycle.util.WildCardPattern;

/**
 * Checks if class dependencies conforms to provided definitions.
 * 
 * @author Wojciech Gdela
 */
public class EnforceTask extends Task {

    private StringPattern includingClasses = new TrueStringPattern();

    private StringPattern excludingClasses = new TrueStringPattern();

    private StringPattern reflectionPattern;

    private LinkedList<FileSet> fileSets = new LinkedList<FileSet>();

    private File file;

    private String definition;

    public void setIncludingClasses(String patternList) {
        includingClasses = WildCardPattern.createFromsPatterns(patternList, ", ");
    }

    public void setExcludingClasses(String patternList) {
        excludingClasses = new NotStringPattern(WildCardPattern.createFromsPatterns(patternList, ", "));
    }

    public void setReflectionPattern(String patternList) {
        if ("".equals(patternList)) {
            reflectionPattern = new TrueStringPattern();
        } else {
            reflectionPattern = WildCardPattern.createFromsPatterns(patternList, ", ");
        }
    }

    public void addConfiguredFileset(FileSet set) {
        fileSets.add(set);
    }

    protected String[] getClassFileNames() {
        ArrayList<String> fileNames = new ArrayList<String>();
        String fileSeparator = System.getProperty("file.separator");
        for (Iterator<FileSet> i = fileSets.iterator(); i.hasNext();) {
            FileSet set = (FileSet) i.next();
            DirectoryScanner scanner = set.getDirectoryScanner(getProject());
            String path = scanner.getBasedir().getAbsolutePath();
            String[] localFiles = scanner.getIncludedFiles();
            for (int j = 0; j < localFiles.length; j++) {
                fileNames.add(path + fileSeparator + localFiles[j]);
            }
        }
        String[] classFiles = new String[fileNames.size()];
        return (String[]) fileNames.toArray(classFiles);
    }
    
    private Collection<File> getClassFiles() {
        ArrayList<File> classFiles = new ArrayList<File>();
        for (String classFileName : getClassFileNames()) {
            classFiles.add(new File(classFileName));
        }
        return classFiles;
    }

    protected StringPattern getPattern() {
        AndStringPattern pattern = new AndStringPattern();
        pattern.appendPattern(includingClasses);
        pattern.appendPattern(excludingClasses);
        return pattern;
    }

    protected StringPattern getReflectionPattern() {
        return reflectionPattern;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void addText(String text) {
        this.definition = text.trim();
    }

    public void execute() throws BuildException {
        super.execute();
        
        if (fileSets.size() == 0) {
            throw new BuildException("at least one file set is required");
        }
        
        if ("true".equals(System.getProperty("socomo.skip"))) {
            log("skipping socomo checks");
            return;
        }
        boolean mergeInnerClasses = true;
        Analyser analyser = new Analyser(getClassFileNames(), getPattern(), getReflectionPattern(), mergeInnerClasses);
        DefinitionCollector definitionCollector = getDefinitionCollector();
        if (!definitionCollector.hasResult()) {
            log("no depedency definitions found");
        }
        
        log("enforcing source code structure");
        PrintWriter output = new PrintWriter(System.out);
        
        boolean structureValid = true;
        
        // classycle
        try {
            ResultRenderer renderer = new DefaultResultRenderer();
            DependencyChecker dependencyChecker = new DependencyChecker(analyser, definitionCollector.getResult(), System.getProperties(), renderer);
            structureValid &= dependencyChecker.check(output);
            output.flush();
        } catch (IllegalArgumentException e) {
            String msg = "Syntax error " + StringUtils.uncapitalize(e.getMessage());
            throw new BuildException(msg);
        }

        // TODO: a conceptual model of  raw dependencies
        Map<String, Map<String, Integer>> dependencies = Dependencies.analyze(getClassFiles());
        
        // acyclity
		AcyclityChecker acyclityChecker = new AcyclityChecker(null, dependencies);
        structureValid &= acyclityChecker.check(definitionCollector.getAcyclicPackages(), output);
        output.flush();
        
        // existence
        ExistenceChecker checker = new ExistenceChecker(null, dependencies);
        structureValid &= checker.check(definitionCollector.getExistenceRules(), output);
        output.flush();
        
        if (structureValid) {
            log("source code structured correctly");
        } else {
            throw new BuildException("Unwanted dependencies found. See output for details.");
        }

//        TAK BYLO
//        boolean ok = false;
//        try {
//            boolean mergeInnerClasses = true;
//            Analyser analyser = new Analyser(getClassFileNames(), getPattern(), getReflectionPattern(),
//                    mergeInnerClasses);
//            Map properties = file == null ? getProject().getProperties() : System.getProperties();
//            DependencyChecker dependencyChecker = new DependencyChecker(analyser, getDependencyDefinitions(),
//                    properties, getRenderer());
//            PrintWriter printWriter = new PrintWriter(System.out);
//            ok = dependencyChecker.check(printWriter);
//            printWriter.flush();
//        } catch (BuildException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new BuildException(e);
//        }
//        if (ok == false) {
//            throw new BuildException("Unwanted dependencies found. See output for details.");
//        }
    }
    
    private DefinitionCollector getDefinitionCollector() {
        DefinitionCollector collector = new DefinitionCollector();
        if (definition != null) {
            collector.collect(definition, "pom.xml fragment or system property", new File("."));
        }
        if (file != null) {
            collector.collect(file);
        }
        return collector;
    }
}
