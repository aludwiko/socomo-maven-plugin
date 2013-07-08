package pl.gdela.socomo.maven.existence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ExistenceDeclaration {
	
	private static enum Resolution {
		DENY, ALLOW;
	}
	
	static class Rule {
		private String pattern;
		private Pattern regex;
		private Resolution resolution;
		public Rule(String pattern, Resolution resolution) {
			this.pattern = pattern;
			this.regex = compile(pattern);
			this.resolution = resolution;
		}
		private Pattern compile(String pattern) {
			String regex = pattern;
			if (regex.endsWith(".*")) {
				// FIXME: dopoki matchujemy pakiety a nie klasy, to musimy na koncu zaniedbac kropke
				regex = regex.substring(0, regex.length() - 2) + "*";
			}
			regex = regex.replace(".", Pattern.quote("."));
			regex = regex.replace("*", ".*");
			return Pattern.compile(regex);
		}
		String getPattern() {
			return pattern;
		}
		boolean matches(String name) {
			return regex.matcher(name).matches();
		}
		boolean denies(String name) {
			return resolution == Resolution.DENY && matches(name);
		}
		boolean allows(String name) {
			return resolution == Resolution.ALLOW && matches(name);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + pattern.hashCode();
			result = prime * result + resolution.hashCode();
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
			Rule other = (Rule) obj;
			return pattern.equals(other.pattern) && resolution == other.resolution;
		}
	}
	
	private List<Rule> rules = new ArrayList<Rule>();
	
	public List<Rule> getRules() {
		return rules;
	}

	public void deny(String pattern) {
		rules.add(new Rule(pattern, Resolution.DENY));
	}

	public void allow(String pattern) {
		rules.add(new Rule(pattern, Resolution.ALLOW));
	}

	public boolean isAllowed(String name) {
		return getDenyingRule(name) == null;
	}
	
	/**
	 * Returns the rule which denies existence of this name.
	 * 
	 * @param name
	 *            full class name
	 * @return denying rule or <code>null</code> if no rule denies this name or
	 *         denial was cancelled by an allow rule
	 */
	public Rule getDenyingRule(String name) {
		Rule denyingRule = null;
		for (Rule rule : rules) {
			if (denyingRule == null && rule.denies(name)) {
				denyingRule = rule;
			}
			if (denyingRule != null && rule.allows(name)) {
				denyingRule = null;
			}
		}
		return denyingRule;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rules == null) ? 0 : rules.hashCode());
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
		ExistenceDeclaration other = (ExistenceDeclaration) obj;
		return rules.equals(other.rules);
	}
	
	
}
