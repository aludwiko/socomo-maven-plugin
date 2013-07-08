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

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import pl.gdela.socomo.maven.acyclity.AcyclityDeclaration;
import pl.gdela.socomo.maven.existence.ExistenceDeclaration;

public class ScmToDdfParserTest {

    @Test
    public void testParse() throws IOException {
        String data = IOUtils.toString(getClass().getResourceAsStream("ScmToDdfParserTest.txt"));
        data = data.replaceAll("^>>>\\s*", "").replaceAll("\\s*<<<$", "").replaceAll("\r", "");
        String[] cases = data.split("\\s*<<<\\s*>>>\\s*");
        for (String acase : cases) {
            String[] tmp = acase.split("\\s*---\\s*");
            String in = tmp[0];
            String out = tmp[1];
            ScmToDdfParser parser = new ScmToDdfParser(null) {
                @Override
                String loadDiagrams(String fileName, Collection<String> enforcedDiagrams, Collection<String> exemptedDiagrams) {
                    String result = "";
                    if (enforcedDiagrams != null) {
	                    for (String name : enforcedDiagrams) {
	                        result += fileName + ":" + name + "\n";
	                    }
                    } else if (exemptedDiagrams != null) {
                    	for (String name : exemptedDiagrams) {
	                        result += fileName + ":not " + name + "\n";
	                    }
                    } else {
                    	result += fileName + ":all\n";
                    }
                    return StringUtils.chomp(result);
                }
            };
            assertEquals(out, parser.parse(in));
        }
    }

    @Test
    public void testParseNames() {
        assertEquals(set("Foo"), ScmToDdfParser.parseNames("Foo"));
        assertEquals(set("Foo", "Bla"), ScmToDdfParser.parseNames("Foo Bla"));
        assertEquals(set("Foo Bla", "Gre"), ScmToDdfParser.parseNames("\"Foo Bla\" Gre"));
    }
    
    @Test
    public void testParseAcyclicPackages() {
    	String scm = "";
    	scm += "check acyclicCompositionOf pl.gdela.raz\n";
    	scm += "check something different pl.gdela.raz\n";
    	scm += "check acyclicCompositionOf pl.gdela.dwa excluding alfa beta\n";
    	List<AcyclityDeclaration> expected = new ArrayList<AcyclityDeclaration>(); 
    	expected.add(new AcyclityDeclaration("pl.gdela.raz"));
    	expected.add(new AcyclityDeclaration("pl.gdela.dwa", "alfa", "beta"));
		assertEquals(expected, new ScmToDdfParser(null).parseAcyclicPackages(scm));
    }
    
    @Test
    public void testParseExistenceRules() {
    	String scm = "";
    	scm += "check deny *\n";
    	scm += "check allow pl.gdela.alfa.*\n";
    	scm += "check allow pl.gdela.beta.*\n";
    	ExistenceDeclaration expected = new ExistenceDeclaration(); 
    	expected.deny("*");
    	expected.allow("pl.gdela.alfa.*");
    	expected.allow("pl.gdela.beta.*");
		assertEquals(list(expected), new ScmToDdfParser(null).parseExistenceRules(scm));
    }

    private <T> Set<T> set(T... objects) {
        HashSet<T> set = new HashSet<T>(objects.length);
        set.addAll(Arrays.asList(objects));
        return set;
    }
    
    private <T> List<T> list(T... objects) {
        return Arrays.asList(objects);
    }
}
