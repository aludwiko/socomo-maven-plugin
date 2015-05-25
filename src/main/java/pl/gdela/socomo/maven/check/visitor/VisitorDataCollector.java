package pl.gdela.socomo.maven.check.visitor;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class VisitorDataCollector {

	Set<String> packages = new HashSet<String>();

	// map of dependencies from package (String) to package(String) strength (Integer)
	Map<String, Map<String, Integer>> dependencies = new HashMap<String, Map<String, Integer>>();
	private Map<String, Integer> current;

	void addDesc(final String desc) {
		addType(Type.getType(desc));
	}

	void addSignature(final String signature) {
		if (signature != null) {
			new SignatureReader(signature).accept(new DependencySignatureVisitor(this));
		}
	}

	void addInternalName(final String name) {
		addType(Type.getObjectType(name));
	}

	void addType(final Type t) {
		switch (t.getSort()) {
			case Type.ARRAY:
				addType(t.getElementType());
				break;
			case Type.OBJECT:
				addName(t.getInternalName());
				break;
		}
	}

	private void addName(final String name) {
		if (name == null) {
			return;
		}
		String p = getGroupKey(name);
		if (current.containsKey(p)) {
			current.put(p, current.get(p) + 1);
		} else {
			current.put(p, 1);
		}
	}

	void addInternalNames(final String[] names) {
		for (int i = 0; names != null && i < names.length; i++) {
			addInternalName(names[i]);
		}
	}

	void addTypeSignature(final String signature) {
		if (signature != null) {
			new SignatureReader(signature).acceptType(new DependencySignatureVisitor(this));
		}
	}

	void addMethodDesc(final String desc) {
		addType(Type.getReturnType(desc));
		Type[] types = Type.getArgumentTypes(desc);
		for (int i = 0; i < types.length; i++) {
			addType(types[i]);
		}
	}

	String getGroupKey(String name) {
		int n = name.lastIndexOf('/');
		if (n > -1) {
			name = name.substring(0, n);
		}
		packages.add(name);
		return name;
	}

	private Map<String, Integer> getDependenciesByGroupKey(String name) {
		String p = getGroupKey(name);
		Map<String, Integer> current = dependencies.get(p);
		if (current == null) {
			current = new HashMap<String, Integer>();
			dependencies.put(p, current);
		}
		return current;
	}

	public void collect(String name, String superName, String[] interfaces, String signature) {
		current = getDependenciesByGroupKey(name);
		if (signature == null) {
			addInternalName(superName);
			addInternalNames(interfaces);
		} else {
			addSignature(signature);
		}
	}

	public Map<String, Map<String, Integer>> getDependencies() {
		Map<String, Map<String, Integer>> result = new TreeMap<String, Map<String, Integer>>();
		for (String fromPackage : dependencies.keySet()) {
			Map<String, Integer> toPackageAndCount = dependencies.get(fromPackage);
			TreeMap<String,Integer> inner = new TreeMap<String,Integer>();
			result.put(fromPackage.replace('/', '.'), inner);
			for (String toPackage : toPackageAndCount.keySet()) {
				Integer count = toPackageAndCount.get(toPackage);
				inner.put(toPackage.replace('/', '.'), count);
			}
		}
		return result;
	}

	public Set<String> getPackages() {
		HashSet<String> result = new HashSet<String>();
		for (String pkg : packages) {
			result.add(pkg.replace('/', '.'));
		}
		return result;
	}
}
