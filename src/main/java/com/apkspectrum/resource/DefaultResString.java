package com.apkspectrum.resource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apkspectrum.util.Log;
import com.apkspectrum.util.XmlPath;
import com.apkspectrum.util.ZipFileUtil;

public class DefaultResString implements ResString<String>
{
	public static final String DEFAULT_XML_NAME = "strings";

	static {
		_RProp.LANGUAGE.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setLanguage((String) evt.getNewValue());
			}
		});
	}

	private static List<LanguageChangeListener> listeners = new ArrayList<>();
	private static String lang = null;
	
	private static Map<String, XmlPath[]> xmlPaths = new HashMap<>();

	private String defaultXmlName;
	private String value;

	public DefaultResString(String value) {
		this(DEFAULT_XML_NAME, value);
	}

	public DefaultResString(String defaultXmlName, String value) {
		this.defaultXmlName = defaultXmlName;
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String get() {
		return getString();
	}

	@Override
	public String toString() {
		return getString();
	}

	@Override
	public String getString() {
		String id = getValue();
		String value = null;

		if(!id.startsWith("@")) return id;
		id = id.substring(1);

		XmlPath[] stringXmlPath = xmlPaths.get(defaultXmlName);

		if(stringXmlPath == null) {
			stringXmlPath = makeStringXmlPath(defaultXmlName);
			xmlPaths.put(defaultXmlName, stringXmlPath);
		}

		for(XmlPath xPath: stringXmlPath) {
			XmlPath node = xPath.getNode(
					"/resources/string[@name='" + id + "']");
			if(node != null) {
				value = node.getTextContent();
				if(value != null) break;
			}
		}

		return value != null ? value.replaceAll("\\\\n", "\n")
				.replaceAll("\\\\t", "\t") : null;
	}

	public static void setLanguage(String l) {
		if(lang == l) return;
		String old = lang;
		lang = l;
		xmlPaths.clear();

		fireLanguageChange(old, lang);
	}

	public static String getLanguage() {
		return lang;
	}

	public static void addLanguageChangeListener(
			LanguageChangeListener listener) {
		if(listener == null) return;
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public static void removeLanguageChangeListener(
			LanguageChangeListener listener) {
		if(listener == null) return;
		if(listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public static LanguageChangeListener[] getLanguageChangeListener() {
		return listeners.toArray(new LanguageChangeListener[listeners.size()]);
	}

	private static void fireLanguageChange(String oldLanguage,
			String newLanguage) {
		for(LanguageChangeListener l: listeners) {
			l.languageChange(oldLanguage, newLanguage);
		}
	}

	private static XmlPath[] makeStringXmlPath(String xmlName) {
		ArrayList<XmlPath> xmlList = new ArrayList<XmlPath>();

		if(lang != null && !lang.isEmpty()) {
			String fileName = xmlName + "-" + lang.toLowerCase() + ".xml";

			File extFile = new DefaultResFile(
					ResFile.Type.DATA, fileName).get();
			if(extFile.exists()) {
				xmlList.add(new XmlPath(extFile));
			}

			ResFile<?> resXml = new DefaultResFile(
					ResFile.Type.RES_VALUE, fileName);
			try(InputStream xml = resXml.getResourceAsStream()) {
				if(xml != null) {
					xmlList.add(new XmlPath(xml));
				}
			} catch(IOException e) { }
		}

		String fileName = xmlName + ".xml";
		File extFile = new DefaultResFile(ResFile.Type.DATA, fileName).get();
		if(extFile.exists()) {
			xmlList.add(new XmlPath(extFile));
		}

		ResFile<?> resXml = new DefaultResFile(ResFile.Type.RES_VALUE, fileName);
		try(InputStream xml = resXml.getResourceAsStream()) {
			if(xml != null) {
				xmlList.add(new XmlPath(xml));
			}
		} catch(IOException e) { }

		return xmlList.toArray(new XmlPath[0]);
	}

	public static String[] getSupportedLanguages() {
		return getSupportedLanguages(DEFAULT_XML_NAME);
	}

	public static String[] getSupportedLanguages(String xmlName) {
		ArrayList<String> languages = new ArrayList<String>();

		File valueDir = new DefaultResFile(ResFile.Type.DATA, "").get();
		if(valueDir != null && valueDir.isDirectory()) {
			String prefix = xmlName + "-";
			for(String name: valueDir.list()) {
				if(name.startsWith(prefix) && name.endsWith(".xml")) {
					name = name.substring(prefix.length(), name.length() - 4);
					if(!languages.contains(name)) {
						languages.add(name);
					}
				}
			}
		}

		URL resource = new DefaultResFile(ResFile.Type.RES_VALUE, "").getURL();
		String resFilePath = resource.getFile();
		try {
			resFilePath = URLDecoder.decode(resFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if("jar".equals(resource.getProtocol())) {
			String[] jarPath = resFilePath.split("!");
			if(jarPath != null && jarPath.length == 2) {
				String[] list = ZipFileUtil.findFiles(jarPath[0].substring(5),
						".xml", "^"+jarPath[1].substring(1) + "/.*");
				String prefix = "values/" + xmlName + "-";
				for(String name : list) {
					if(name.startsWith(prefix)
							&& name.endsWith(".xml")) {
						name = name.substring(prefix.length(), name.length()-4);
						if(!languages.contains(name)) {
							languages.add(name);
						}
					}
				}
			}
		} else if("file".equals(resource.getProtocol())) {
			valueDir = new File(resFilePath);
			if(valueDir != null && valueDir.isDirectory()) {
				String prefix = xmlName + "-";
				for(String name: valueDir.list()) {
					if(name.startsWith(prefix) && name.endsWith(".xml")) {
						name = name.substring(prefix.length(), name.length()-4);
						if(!languages.contains(name)) {
							languages.add(name);
						}
					}
				}
			}
		} else {
			Log.e("Unknown protocol " + resource);
		}

		Collections.sort(languages);
		languages.add(0, "");

		return languages.toArray(new String[languages.size()]);
	}
}
