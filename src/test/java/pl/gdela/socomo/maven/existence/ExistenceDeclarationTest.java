package pl.gdela.socomo.maven.existence;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExistenceDeclarationTest {

	@Test
	public void testIsAllowed() {
		ExistenceDeclaration declaration = new ExistenceDeclaration();
		// order of deny/allow statements is important, a later rule may change earlier's rule decision
		declaration.deny("*");
		declaration.allow("pl.gdela.*.alfa.*");
		declaration.allow("pl.gdela.*.beta.*");
		declaration.deny ("pl.gdela.*.beta.wyjatek.*");
		declaration.deny ("*.enu.*");
		
		assertFalse(declaration.isAllowed("pl.Main"));
		assertTrue (declaration.isAllowed("pl.gdela.foo.alfa.Main"));
		assertTrue (declaration.isAllowed("pl.gdela.foo.beta.Main"));
		assertFalse(declaration.isAllowed("pl.gdela.foo.gama.Main"));
		assertFalse(declaration.isAllowed("pl.gdela.foo.beta.wyjatek.Main"));
		assertFalse(declaration.isAllowed("pl.gdela.foo.alfa.enu.Main"));
	}

}
