package pl.gdela.socomo.maven.acyclity;

import static org.junit.Assert.*;
import static pl.gdela.socomo.maven.check.SyntacticSugar.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import pl.gdela.socomo.maven.check.DependencyGraph;

public class AcyclityCheckerTest {

	@Test
	public void testFlatten() {
		AcyclityChecker.Dependencies dependencies = new AcyclityChecker.Dependencies();
		dependencies.add("pl.gdela.alfa.one", "pl.gdela.beta", 1);
		dependencies.add("pl.gdela.alfa.two", "pl.gdela.beta", 2);
		dependencies.add("pl.gdela.gama", "pl.gdela.alfa.one", 4);
		dependencies.add("pl.gdela.gama", "pl.gdela.alfa.two", 8);
		dependencies.add("pl.gdela.gama", "pl.gdela.beta", 16);
		dependencies.add("pl.other", "pl.gdela.beta", 32);
		AcyclityChecker.Dependencies expected = new AcyclityChecker.Dependencies();
		expected.add("pl.gdela.alfa", "pl.gdela.beta", 3);
		expected.add("pl.gdela.gama", "pl.gdela.alfa", 12);
		expected.add("pl.gdela.gama", "pl.gdela.beta", 16);
		assertEquals(expected, AcyclityChecker.flatten(dependencies, "pl.gdela"));
	}
	
	@Test
	public void testExclude() {
		AcyclityChecker.Dependencies dependencies = new AcyclityChecker.Dependencies();
		dependencies.add("pl.gdela.alfa", "pl.gdela.beta", 3);
		dependencies.add("pl.gdela.gama", "pl.gdela.alfa", 12);
		dependencies.add("pl.gdela.gama", "pl.gdela.beta", 16);
		Set<String> excludes = new HashSet<String>();
		excludes.add("gama");
		AcyclityChecker.Dependencies expected = new AcyclityChecker.Dependencies();
		expected.add("pl.gdela.alfa", "pl.gdela.beta", 3);
		assertEquals(expected, AcyclityChecker.exclude(dependencies, excludes));
	}

	@Test
	public void testDependenciesToGraph() {
		AcyclityChecker.Dependencies dependencies = new AcyclityChecker.Dependencies();
		dependencies.add("alfa", "beta", 3);
		dependencies.add("gama", "alfa", 12);
		dependencies.add("gama", "beta", 16);
		DependencyGraph expected = graph("alfa-beta, gama-alfa, gama-beta");
		assertEquals(expected.toString(), AcyclityChecker.dependenciesToGraph(dependencies).toString());
	}
}
