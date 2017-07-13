package com.iiqtools.jdp.annotation;

/**
 * 
 * @author Serge Voina
 *
 */
public enum EOL {
	Target {
		@Override
		public String lineSeparator() {
			return null;
		}
	},
	System {
		@Override
		public String lineSeparator() {
			return java.lang.System.lineSeparator();
		}
	},
	Unix {
		@Override
		public String lineSeparator() {
			return "\n";
		}
	},
	Windows {
		@Override
		public String lineSeparator() {
			return "\r\n";
		}
	};

	public abstract String lineSeparator();
}
