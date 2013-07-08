package pl.gdela.socomo.maven.acyclity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Declaration that a package should be acyclic with some minor exceptions.
 */
public class AcyclityDeclaration {
	
	/**
	 * The name of package that this declaration aplies to. I.e. <code>pl.gdela.car</code>.
	 */
	private String packageName;
	
	/**
	 * Set of subpackage names that are excluded from acyclity checks. I.e. <code>engine</code>,
	 * <code>controls</code>
	 */
	private Set<String> excludes = new HashSet<String>();

	public AcyclityDeclaration(String packageName, String... excludes) {
		this.packageName = packageName;
        this.excludes.addAll(Arrays.asList(excludes));
	}

	public String getPackageName() {
		return packageName;
	}

	public Set<String> getExcludes() {
		return Collections.unmodifiableSet(excludes);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + excludes.hashCode();
		result = prime * result + packageName.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AcyclityDeclaration other = (AcyclityDeclaration) obj;
		return packageName.equals(other.packageName) && excludes.equals(other.excludes);
	}

	@Override
	public String toString() {
		return packageName + (excludes.isEmpty() ? "" : " excluding " + excludes);
	}
	
	
}
