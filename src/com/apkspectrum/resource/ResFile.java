package com.apkspectrum.resource;

import java.net.URL;

public interface ResFile<T> extends ResValue<T>
{
	public String getPath();
	public URL getURL();

	public enum Type {
		BIN("tool"),
		DATA("data"),
		LIB("lib"),
		PLUGIN("plugin"),
		SECURITY("security"),
		ETC(""),
		RES_VALUE(null),
		RES_ROOT(null);

		private String value;
		Type(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
