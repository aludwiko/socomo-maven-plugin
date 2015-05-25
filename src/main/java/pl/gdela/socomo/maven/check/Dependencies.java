package pl.gdela.socomo.maven.check;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.objectweb.asm.ClassReader;

import pl.gdela.socomo.maven.LogRuntimeException;
import pl.gdela.socomo.maven.check.visitor.DependencyClassVisitor;
import pl.gdela.socomo.maven.check.visitor.VisitorDataCollector;

/**
 * Utilities for constructing raw dependencies information. 
 * 
 * @author Wojtek
 */
public class Dependencies {
	public static Map<String, Map<String, Integer>> analyze(Collection<File> classFiles) {
		VisitorDataCollector visitorDataCollector = new VisitorDataCollector();
		for (File file : classFiles) {
    		try {
    		    if (file.getName().equals("package-info.class")) {
    		        // package info does not consitutes any dependency or class existence
    		        continue;
    		    }
				FileInputStream input = new FileInputStream(file);
                new ClassReader(input).accept(new DependencyClassVisitor(visitorDataCollector), 0);
                input.close();
			} catch (IOException e) {
				throw new LogRuntimeException(e);
			}
    	}
		return visitorDataCollector.getDependencies();
	}
}
