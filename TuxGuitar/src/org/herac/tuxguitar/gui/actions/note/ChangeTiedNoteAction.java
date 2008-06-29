/*
 * Created on 17-dic-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.herac.tuxguitar.gui.actions.note;

import java.util.Iterator;

import org.eclipse.swt.events.TypedEvent;
import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.actions.Action;
import org.herac.tuxguitar.gui.editors.tab.Caret;
import org.herac.tuxguitar.gui.undo.undoables.measure.UndoableMeasureGeneric;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.song.models.TGDuration;
import org.herac.tuxguitar.song.models.TGMeasure;
import org.herac.tuxguitar.song.models.TGNote;

/**
 * @author julian
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ChangeTiedNoteAction extends Action{
	public static final String NAME = "action.note.general.tied";
	
	public ChangeTiedNoteAction() {
		super(NAME, AUTO_LOCK | AUTO_UNLOCK | AUTO_UPDATE | DISABLE_ON_PLAYING | KEY_BINDING_AVAILABLE);
	}
	
	protected int execute(TypedEvent e){
		Caret caret = getEditor().getTablature().getCaret();
		if(caret.getSelectedNote() != null){
			//comienza el undoable
			UndoableMeasureGeneric undoable = UndoableMeasureGeneric.startUndo();
			
			getSongManager().getMeasureManager().changeTieNote(caret.getSelectedNote());
			
			//termia el undoable
			addUndoableEdit(undoable.endUndo());
		}else{
			TGNote note = getSongManager().getFactory().newNote();
			note.setValue(0);
			note.setVelocity(caret.getVelocity());
			note.setString(caret.getSelectedString().getNumber());
			note.setTiedNote(true);
			
			TGDuration duration = getSongManager().getFactory().newDuration();
			caret.getDuration().copy(duration);
			
			setTiedNoteValue(note,caret);
			
			//comienza el undoable
			UndoableMeasureGeneric undoable = UndoableMeasureGeneric.startUndo();
			
			getSongManager().getMeasureManager().addNote(caret.getSelectedBeat(),note,duration);
			
			//termia el undoable
			addUndoableEdit(undoable.endUndo());
		}
		TuxGuitar.instance().getFileHistory().setUnsavedFile();
		updateTablature();
		return 0;
	}
	
	private void setTiedNoteValue(TGNote note,Caret caret){
		TGMeasure measure = caret.getMeasure();
		TGBeat beat = getSongManager().getMeasureManager().getPreviousBeat( measure.getBeats(), caret.getSelectedBeat());
		while( measure != null){
			while( beat != null ){
				if(beat.isRestBeat()){
					note.setValue(0);
					return;
				}
				// Check if is there any note at same string.
				Iterator it = beat.getNotes().iterator();
				while( it.hasNext() ){
					TGNote current = (TGNote) it.next();
					if(current.getString() == note.getString()){
						note.setValue( current.getValue() );
						return;
					}
				}
				beat = getSongManager().getMeasureManager().getPreviousBeat( measure.getBeats(), beat);
			}
			measure = getSongManager().getTrackManager().getPrevMeasure(measure);
			if( measure != null ){
				beat = getSongManager().getMeasureManager().getLastBeat( measure.getBeats() );
			}
		}
	}
	/*
	private void setTiedNoteValue(TGNote note,Caret caret){
		TGMeasure measure = caret.getMeasure();
		TGNote previous = null;
		while(measure != null){
			previous = getSongManager().getMeasureManager().getPreviousNote(measure,caret.getPosition(),caret.getSelectedString().getNumber());
			if(previous != null){
				note.setValue(previous.getValue());
				break;
			}
			measure = getSongManager().getTrackManager().getPrevMeasure(measure);
		}
	}
	*/
	
	public void updateTablature() {
		fireUpdate(getEditor().getTablature().getCaret().getMeasure().getNumber());
	}
}
