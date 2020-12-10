package com.apkspectrum.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;

public class DefaultResFile implements ResFile<File>
{
	private String value;
	private Type type;
	private String config;

	public DefaultResFile(Type type, String value) {
		this(type, value, null);
	}

	public DefaultResFile(Type type, String value, String config) {
		this.type = type;
		this.value = value;
		this.config = config;
	}

	public DefaultResFile(Type type, ResFile<?>[] cfgResources) {
		if(cfgResources == null | cfgResources.length == 0) {
			throw new IllegalArgumentException();
		}

		this.type = type;
		for(ResFile<?> r: cfgResources) {
			if(SystemUtil.OS.contains(r.getConfiguration())) {
				this.value = r.getValue();
				this.config = r.getConfiguration();
				break;
			}
		}
		if(this.value == null || config == null) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getConfiguration() {
		return config;
	}

	@Override
	public String toString() {
		return getPath();
	}

	@Override
	public String getPath() {
		if(type == Type.RES_VALUE || type == Type.RES_ROOT) {
			return getURL().toExternalForm();
		}
		return getUTF8Path(value);
	}

	@Override
	public URL getURL() {
		switch(type){
		case RES_ROOT:
			return getClass().getResource("/" + value);
		case RES_VALUE:
			if(value == null || value.isEmpty())
				return getClass().getResource("/values");
			else
				return getClass().getResource("/values/" + value);
		default:
			try {
				return new File(getPath()).toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public File get() {
		try {
			return new File(getURL().toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getString() {
		try (InputStream is= getURL().openStream();
			 InputStreamReader ir = new InputStreamReader(is);
			 BufferedReader br = new BufferedReader(ir)) {
	        StringBuilder out = new StringBuilder();
	        String line;
	        while ((line = br.readLine()) != null) {
	            out.append(line);
	        }
	        return out.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public URL getResource() {
		return getURL();
	}

	@Override
	public InputStream getResourceAsStream() {
		switch(type){
		case RES_VALUE:
			return getClass().getResourceAsStream("/values/" + value);
		default:
			try {
				return getURL().openStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String getUTF8Path(String relativePath) {
		File bin = new File(_RFile.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath());
		String rootPath = bin.getParentFile().getPath();

		String[] modulePaths = { rootPath };
		if(bin.isDirectory()) {
			modulePaths = getModulePaths(rootPath);
		}

		String utf8Path = null;
		for(String module: modulePaths) {
			module += File.separator + type.getValue() + File.separator;
			try {
				module = URLDecoder.decode(module, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			module += relativePath;
			if(new File(module).exists()) {
				utf8Path = module;
				break;
			} else if(utf8Path == null) {
				utf8Path = module;
			}
		}
		return utf8Path;
	}

	private static String[] cachedModulePaths = null;
	private String[] getModulePaths(String rootPath) {
		if(cachedModulePaths != null) return cachedModulePaths;
		List<String> paths = new ArrayList<>();
		paths.add(rootPath);

		File gitmodules = new File(rootPath + File.separator + ".gitmodules");
		if(gitmodules.exists() && gitmodules.canRead()) {
			// https://stackoverflow.com/questions/190629/what-is-the-easiest-way-to-parse-an-ini-file-in-java
			Pattern _section  = Pattern.compile( "\\s*\\[([^]]*)\\]\\s*" );
			Pattern _keyValue = Pattern.compile( "\\s*([^=]*)=(.*)" );
			try(FileReader fr = new FileReader(gitmodules);
				BufferedReader br = new BufferedReader(fr)) {
				String line;
				String section = null;
				while((line = br.readLine()) != null ) {
					Matcher m = _section.matcher(line);
					if( m.matches()) {
						section = m.group(1).trim();
					} else if( section != null ) {
						m = _keyValue.matcher(line);
						if( m.matches()) {
							String key   = m.group(1).trim();
							String value = m.group(2).trim();
							if("path".equals(key)) {
								Log.v("found a module : " + section +
										", path : " + value);
								paths.add(rootPath + File.separator + value);
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cachedModulePaths = paths.toArray(new String[paths.size()]);
	}
}
