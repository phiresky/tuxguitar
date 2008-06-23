package org.herac.tuxguitar.gui.system.language;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

public class TGResourceBundle {
	
	private Locale locale;
	private Properties properties;
	
	public TGResourceBundle(Locale locale, Properties properties){
		this.locale = locale;
		this.properties = properties;
	}
	
	public Locale getLocale() {
		return this.locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public String getString(String key) {
		return this.properties.getProperty(key);
	}
	
	public static TGResourceBundle getBundle(String baseName, Locale locale,ClassLoader loader){
		Properties properties = new Properties();
		
		String bundleName = baseName.replace('.','/');
		String bundleExtension = ".properties";
		
		// load default
		TGResourceBundle.loadResources( (bundleName + bundleExtension ), properties, loader);
		
		// load language
		bundleName += "_";
		if(locale.getLanguage() != null && locale.getLanguage().length() > 0){
			bundleName += locale.getLanguage();
			TGResourceBundle.loadResources( (bundleName + bundleExtension ), properties, loader);
		}
		
		// load country
		bundleName += "_";
		if(locale.getCountry() != null && locale.getCountry().length() > 0){
			bundleName += locale.getCountry();
			TGResourceBundle.loadResources( (bundleName + bundleExtension ), properties, loader);
		}
		
		// load variant
		bundleName += "_";
		if(locale.getVariant() != null && locale.getVariant().length() > 0){
			bundleName += locale.getVariant();
			TGResourceBundle.loadResources( (bundleName + bundleExtension ), properties, loader);
		}
		
		return new TGResourceBundle(locale, properties);
	}
	
	private static void loadResources(String name, Properties p, ClassLoader loader){
		try {
			Enumeration enumeration = loader.getResources(name);
			while (enumeration.hasMoreElements()) {
				URL url = (URL) enumeration.nextElement();
				Properties properties = new Properties();
				properties.load( url.openStream() );
				p.putAll(properties);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
