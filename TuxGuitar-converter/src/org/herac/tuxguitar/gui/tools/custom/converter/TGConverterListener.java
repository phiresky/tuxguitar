package org.herac.tuxguitar.gui.tools.custom.converter;

public interface TGConverterListener {
	
	public void notifyStart();
	
	public void notifyFinish();
	
	public void notifyFileProcess( String filename );
	
	public void notifyFileResult( String filename, int errorCode );
	
}
