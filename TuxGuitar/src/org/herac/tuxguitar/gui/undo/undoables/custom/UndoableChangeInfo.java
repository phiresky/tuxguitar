package org.herac.tuxguitar.gui.undo.undoables.custom;

import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.undo.CannotRedoException;
import org.herac.tuxguitar.gui.undo.CannotUndoException;
import org.herac.tuxguitar.gui.undo.UndoableEdit;
import org.herac.tuxguitar.gui.undo.undoables.UndoableCaretHelper;
import org.herac.tuxguitar.song.models.TGSong;

public class UndoableChangeInfo implements UndoableEdit{
	private int doAction;
	private UndoableCaretHelper undoCaret;
	private UndoableCaretHelper redoCaret;
	private String undoName;
	private String undoArtist;
	private String undoAlbum;
	private String undoAuthor;
	private String redoName;
	private String redoArtist;
	private String redoAlbum;
	private String redoAuthor;
	
	private UndoableChangeInfo(){
		super();
	}
	
	public void redo() throws CannotRedoException {
		if(!canRedo()){
			throw new CannotRedoException();
		}
		TuxGuitar.instance().getSongManager().setProperties(this.redoName,this.redoArtist,this.redoAlbum,this.redoAuthor);
		TuxGuitar.instance().showTitle();
		this.redoCaret.update();
		
		this.doAction = UNDO_ACTION;
	}
	
	public void undo() throws CannotUndoException {
		if(!canUndo()){
			throw new CannotUndoException();
		}
		TuxGuitar.instance().getSongManager().setProperties(this.undoName,this.undoArtist,this.undoAlbum,this.undoAuthor);
		TuxGuitar.instance().showTitle();
		this.undoCaret.update();
		
		this.doAction = REDO_ACTION;
	}
	
	public boolean canRedo() {
		return (this.doAction == REDO_ACTION);
	}
	
	public boolean canUndo() {
		return (this.doAction == UNDO_ACTION);
	}
	
	public static UndoableChangeInfo startUndo(){
		TGSong song = TuxGuitar.instance().getSongManager().getSong();
		UndoableChangeInfo undoable = new UndoableChangeInfo();
		undoable.doAction = UNDO_ACTION;
		undoable.undoCaret = new UndoableCaretHelper();
		undoable.undoName = song.getName();
		undoable.undoArtist = song.getArtist();
		undoable.undoAlbum = song.getAlbum();
		undoable.undoAuthor = song.getAuthor();
		return undoable;
	}
	
	public UndoableChangeInfo endUndo(){
		TGSong song = TuxGuitar.instance().getSongManager().getSong();
		this.redoCaret = new UndoableCaretHelper();
		this.redoName = song.getName();
		this.redoArtist = song.getArtist();
		this.redoAlbum = song.getAlbum();
		this.redoAuthor = song.getAuthor();
		return this;
	}
}
