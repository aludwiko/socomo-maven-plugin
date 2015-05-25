package pl.gdela.socomo.maven.existence;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.maven.plugin.logging.Log;

import pl.gdela.socomo.maven.existence.ExistenceDeclaration.Rule;

public class ExistenceChecker {
	
	private Log log;
	
	/**
	 * Raw dependencies list as analyzed by {@link pl.gdela.socomo.maven.check.visitor.DependencyClassVisitor}.
	 */
	private Map<String, Map<String, Integer>> dependencies;
	
	public ExistenceChecker(Log log, Map<String, Map<String, Integer>> dependencies) {
		this.log = log;
		this.dependencies = dependencies;
	}
	
	private void logdebug(String msg) {
	    if (log != null) {
	        log.debug(msg);
	    }
	}
	
	/**
	 * Returns <code>true</code> when no class is denied it's existence.
	 * @param declarations
	 * @param output additional comments about existence is directed to this writer
	 * @return
	 */
	public boolean check(Collection<ExistenceDeclaration> declarations, PrintWriter output) {
		boolean allValid = true;
		for (ExistenceDeclaration declaration : declarations) {
			allValid &= check(declaration, output);
		}
		return allValid;
	}

	private boolean check(ExistenceDeclaration declaration, PrintWriter output) {
	    logdebug("checking existence");
	    
	    LinkedHashMap<Rule, TreeSet<String>> denials = new LinkedHashMap<Rule, TreeSet<String>>();
	    for (Rule rule : declaration.getRules()) {
	    	denials.put(rule, new TreeSet<String>());
	    }
	    
		for (String className : dependencies.keySet()) { // FIXME: to wcale nie są nazwy klas, tylko nazwy pakietów
	    	ExistenceDeclaration.Rule rule = declaration.getDenyingRule(className);
	    	if (rule != null) {
	    		denials.get(rule).add(className);
	    	}
	    }
	    
	    boolean foundDenied = false;
	    for (Rule rule : denials.keySet()) {
	    	TreeSet<String> denied = denials.get(rule);
	    	if (!denied.isEmpty()) {
		    	output.println("check deny " + rule.getPattern());
				output.println("  Found denied packages:");
				for (String className : denied) {
					output.println("    -> " + className);
				}
				foundDenied = true;
	    	}
	    }
	    
	    return !foundDenied;
	}
}
