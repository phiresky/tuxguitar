package org.herac.tuxguitar.gui.system.plugins.base;

import org.eclipse.swt.events.TypedEvent;
import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.actions.Action;
import org.herac.tuxguitar.gui.tools.custom.TGCustomTool;
import org.herac.tuxguitar.gui.tools.custom.TGCustomToolManager;

public abstract class TGToolItemPlugin extends TGPluginAdapter{
	
	private boolean loaded;
	private TGCustomTool tool;
	private TGCustomToolAction toolAction;
	
	protected abstract String getItemName();
	
	protected abstract void doAction();
	
	public void init(){
		String name = getItemName();
		this.tool = new TGCustomTool(name,name);
		this.toolAction = new TGCustomToolAction(this.tool.getName());
	}
	
	public void close(){
		this.removePlugin();
	}
	
	protected void addPlugin(){
		if(!this.loaded){
			TuxGuitar.instance().getActionManager().addAction(this.toolAction);
			TGCustomToolManager.instance().addCustomTool(this.tool);
			TuxGuitar.instance().getItemManager().createMenu();
			this.loaded = true;
		}
	}
	
	protected void removePlugin(){
		if(this.loaded){
			TGCustomToolManager.instance().removeCustomTool(this.tool);
			TuxGuitar.instance().getActionManager().removeAction(this.tool.getAction());
			TuxGuitar.instance().getItemManager().createMenu();
			this.loaded = false;
		}
	}
	
	public void setEnabled(boolean enabled) {
		if(enabled){
			addPlugin();
		}else{
			removePlugin();
		}
	}
	
	protected class TGCustomToolAction extends Action{
		
		public TGCustomToolAction(String name) {
			super(name, AUTO_LOCK | AUTO_UNLOCK | AUTO_UPDATE | KEY_BINDING_AVAILABLE);
		}
		
		protected int execute(TypedEvent e) {
			doAction();
			return 0;
		}
	}
}