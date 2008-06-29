/*
 * Created on 17-dic-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.herac.tuxguitar.gui.actions.file;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.actions.Action;
import org.herac.tuxguitar.gui.actions.ActionLock;
import org.herac.tuxguitar.gui.helper.SyncThread;
import org.herac.tuxguitar.gui.util.ConfirmDialog;
import org.herac.tuxguitar.gui.util.FileChooser;
import org.herac.tuxguitar.io.base.TGSongImporter;

/**
 * @author julian
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class ImportSongAction extends Action {
	public static final String NAME = "action.file.import";
	
	public ImportSongAction() {
		super(NAME, AUTO_LOCK | AUTO_UPDATE);
	}
	
	protected int execute(final TypedEvent event){
		TuxGuitar.instance().getPlayer().reset();
		
		if(TuxGuitar.instance().getFileHistory().isUnsavedFile()){
			ConfirmDialog confirm = new ConfirmDialog(TuxGuitar.getProperty("file.save-changes-question"));
			confirm.setDefaultStatus( ConfirmDialog.STATUS_CANCEL );
			int status = confirm.confirm(ConfirmDialog.BUTTON_YES | ConfirmDialog.BUTTON_NO | ConfirmDialog.BUTTON_CANCEL, ConfirmDialog.BUTTON_YES);
			if(status == ConfirmDialog.STATUS_CANCEL){
				return AUTO_UNLOCK;
			}
			if(status == ConfirmDialog.STATUS_YES){
				final String fileName = FileActionUtils.getFileName();
				if(fileName == null){
					return AUTO_UNLOCK;
				}
				TuxGuitar.instance().loadCursor(SWT.CURSOR_WAIT);
				new Thread(new Runnable() {
					public void run() {
						if(!TuxGuitar.isDisposed()){
							FileActionUtils.save(fileName);
							new SyncThread(new Runnable() {
								public void run() {
									if(!TuxGuitar.isDisposed()){
										TuxGuitar.instance().loadCursor(SWT.CURSOR_ARROW);
										importFile(event.widget.getData());
									}
								}
							}).start();
						}
					}
				}).start();
				return 0;
			}
		}
		importFile(event.widget.getData());
		
		return 0;
	}
	
	protected void importFile(Object data){
		final TGSongImporter importer = (TGSongImporter)data;
		final String path = FileChooser.instance().open(TuxGuitar.instance().getShell(),importer.getFileFormat());
		if(path == null || !importer.configure(false)){
			ActionLock.unlock();
			return;
		}
		
		TuxGuitar.instance().loadCursor(SWT.CURSOR_WAIT);
		new Thread(new Runnable() {
			public void run() {
				if(!TuxGuitar.isDisposed()){
					FileActionUtils.importSong(importer, path);
					TuxGuitar.instance().loadCursor(SWT.CURSOR_ARROW);
					ActionLock.unlock();
				}
			}
		}).start();
	}
}
