package pl.gdela.socomo.maven.acyclity;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;

import pl.gdela.socomo.maven.check.DependencyGraph;
import pl.gdela.socomo.maven.check.Tangle;
import pl.gdela.socomo.maven.check.TanglesDetector;

public class AcyclityChecker {
	
	private Log log;
	
	/**
	 * Raw dependencies list as analyzed by {@link pl.gdela.socomo.maven.check.visitor.DependencyClassVisitor}.
	 */
	private Map<String, Map<String, Integer>> dependencies;
	
	public AcyclityChecker(Log log, Map<String, Map<String, Integer>> dependencies) {
		this.log = log;
		this.dependencies = dependencies;
	}
	
	private void logdebug(String msg) {
	    if (log != null) {
	        log.debug(msg);
	    }
	}

	/**
	 * Returns <code>true</code> when declared packages are acyclically composed.
	 * @param declarations
	 * @param output additional comments about acyclic composition is directed to this writer
	 * @return
	 */
	public boolean check(Collection<AcyclityDeclaration> declarations, PrintWriter output) {
		boolean allAcyclic = true;
		for (AcyclityDeclaration declaration : declarations) {
			allAcyclic &= check(declaration, output);
		}
		return allAcyclic;
	}

	private boolean check(AcyclityDeclaration declaration, PrintWriter output) {
	    logdebug("checking " + declaration.getPackageName());
		Collection<Tangle> tangles = new TanglesDetector(constructGraphFor(declaration)).detect();
		if (tangles.isEmpty()) {
		    logdebug(declaration.getPackageName() + " is acyclic");
			return true;
		} else {
			output.println("check acyclicCompositionOf " + declaration.getPackageName());
			output.println("  Found cycle consisting of:");
			for (String vertex : longestTangle(tangles).vertexSet()) {
				output.println("    -> " + vertex);
			}
			return false;
		}
	}

	private static Tangle longestTangle(Collection<Tangle> tangles) {
		Iterator<Tangle> tanglesIt = tangles.iterator();
		Tangle longestTangle = null;
		if (tanglesIt.hasNext()) {
			longestTangle = tanglesIt.next();
		}
		while (tanglesIt.hasNext()) {
			Tangle tangle = tanglesIt.next();
			if (tangle.vertexSet().size() > longestTangle.vertexSet().size()) {
				longestTangle = tangle;
			}
		}
		return longestTangle;
	}
	
	private DependencyGraph constructGraphFor(AcyclityDeclaration declaration) {
        Dependencies distilledDependencies = flatten(dependencies, declaration.getPackageName());
        distilledDependencies = exclude(distilledDependencies, declaration.getExcludes());
		DependencyGraph graph = dependenciesToGraph(distilledDependencies);
		return graph;
	}

	/**
	 * Aggregate subsubpackages into subpackage of given packageName. Filter out dependencies
	 * outside of packageName.
	 * 
	 * @param dependencies
	 * @param packageName
	 * @return
	 */
	static Dependencies flatten(Map<String, Map<String, Integer>> dependencies, String packageName) {
		String prefix = packageName + '.';
		Dependencies result = new Dependencies();
		for (String fromPackage : dependencies.keySet()) {
			if (!fromPackage.startsWith(prefix)) {
				continue;
			}
			Map<String, Integer> toPackageAndCount = dependencies.get(fromPackage);
			for (String toPackage : toPackageAndCount.keySet()) {
				if (!toPackage.startsWith(prefix)) {
					continue;
				}
				Integer count = toPackageAndCount.get(toPackage);
				result.add(directSubPackage(prefix, fromPackage), directSubPackage(prefix, toPackage), count);
			}
		}
		return result;
	}
	
	/**
	 * Excludes given subpackages from dependencies map. 
	 * 
	 * @param dependencies
	 * @param excludes list of subpackages (just the last part, no dots)
	 * @return
	 */
	public static Dependencies exclude(Dependencies dependencies, Set<String> excludes) {
		Dependencies result = new Dependencies();
		for (String fromPackage : dependencies.keySet()) {
			String fromPackageLastPart = StringUtils.substringAfterLast(fromPackage, ".");
			if (excludes.contains(fromPackageLastPart)) {
				continue;
			}
			Map<String, Integer> toPackageAndCount = dependencies.get(fromPackage);
			for (String toPackage : toPackageAndCount.keySet()) {
				String toPackageLastPart = StringUtils.substringAfterLast(toPackage, ".");
				if (excludes.contains(toPackageLastPart)) {
					continue;
				}
				Integer count = toPackageAndCount.get(toPackage);
				result.add(fromPackage, toPackage, count);
			}
		}
		return result;
	}
	
	private static String directSubPackage(String prefix, String packageName) {
		if (!packageName.startsWith(prefix)) {
			throw new IllegalArgumentException(packageName + " does not start with " + prefix);
		}
		String suffix = packageName.substring(prefix.length());
		return prefix + StringUtils.substringBefore(suffix, ".");
	}

	/**
	 * Converts list of dependencies to graph.
	 * @param dependencies
	 * @return
	 */
	static DependencyGraph dependenciesToGraph(Dependencies dependencies) {
		DependencyGraph graph = new DependencyGraph();
		for (String fromPackage : dependencies.keySet()) {
			graph.addVertex(fromPackage);
			Map<String, Integer> toPackageAndCount = dependencies.get(fromPackage);
			for (String toPackage : toPackageAndCount.keySet()) {
				graph.addVertex(toPackage);
				if (!fromPackage.equals(toPackage)) {
					graph.addEdge(fromPackage, toPackage);
				}
			}
		}
		return graph;
	}
	
	static class Dependencies extends TreeMap<String, Map<String, Integer>> {
		public void add(String from, String to, int strength) {
			Map<String, Integer> inner = get(from);
			if (inner == null) {
				inner = new TreeMap<String, Integer>();
				put(from, inner);
			}
			Integer currentStrength = inner.get(to);
			if (currentStrength == null) {
				currentStrength = 0;
			}
			inner.put(to, currentStrength + strength);
		}
	}
}
