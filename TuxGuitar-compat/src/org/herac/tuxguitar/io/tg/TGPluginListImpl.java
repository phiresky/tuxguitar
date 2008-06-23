package org.herac.tuxguitar.io.tg;

import java.util.ArrayList;
import java.util.List;

import org.herac.tuxguitar.gui.system.plugins.base.TGInputStreamPlugin;
import org.herac.tuxguitar.gui.system.plugins.base.TGPluginList;
import org.herac.tuxguitar.io.base.TGInputStreamBase;

public class TGPluginListImpl extends TGPluginList{
	
	protected List getPlugins() {
		List plugins = new ArrayList();
		plugins.add(new TGInputStreamPlugin() {
			protected TGInputStreamBase getInputStream() {
				return new org.herac.tuxguitar.io.tg.v09.TGInputStream();
			}
		});
		plugins.add(new TGInputStreamPlugin() {
			protected TGInputStreamBase getInputStream() {
				return new org.herac.tuxguitar.io.tg.v08.TGInputStream();
			}
		});
		plugins.add(new TGInputStreamPlugin() {
			protected TGInputStreamBase getInputStream() {
				return new org.herac.tuxguitar.io.tg.v07.TGInputStream();
			}
		});
		return plugins;
	}
	
	public String getAuthor() {
		return "Julian Casadesus <julian@casadesus.com.ar>";
	}
	
	public String getName() {
		return "TuxGuitar file format compatibility";
	}
	
	public String getDescription() {
		return "This plugin, provides support for other tuxguitar file format versions.";
	}
	
	public String getVersion() {
		return "1.0";
	}
}
