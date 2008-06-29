package org.herac.tuxguitar.song.managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.herac.tuxguitar.song.factory.TGFactory;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.song.models.TGChord;
import org.herac.tuxguitar.song.models.TGDuration;
import org.herac.tuxguitar.song.models.TGMeasure;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.song.models.TGString;
import org.herac.tuxguitar.song.models.TGText;
import org.herac.tuxguitar.song.models.effects.TGEffectBend;
import org.herac.tuxguitar.song.models.effects.TGEffectGrace;
import org.herac.tuxguitar.song.models.effects.TGEffectHarmonic;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloBar;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloPicking;
import org.herac.tuxguitar.song.models.effects.TGEffectTrill;

public class TGMeasureManager {
	private TGSongManager songManager;
	
	public TGMeasureManager(TGSongManager songManager){
		this.songManager = songManager;
	}
	
	public TGSongManager getSongManager(){
		return this.songManager;
	}
	
	public void orderBeats(TGMeasure measure){
		for(int i = 0;i < measure.countBeats();i++){
			TGBeat minBeat = null;
			for(int j = i;j < measure.countBeats();j++){
				TGBeat beat = measure.getBeat(j);
				if(minBeat == null || beat.getStart() < minBeat.getStart()){
					minBeat = beat;
				}
			}
			measure.moveBeat(i, minBeat);
		}
	}	
	
	/**
	 * Agrega un beat al compas
	 */
	public void addBeat(TGMeasure measure,TGBeat beat){
		//Verifico si entra en el compas
		if(validateDuration(measure,beat,false,false)){
			
			//Agrego el beat
			measure.addBeat(beat);
		}
	}
	
	public void removeBeat(TGBeat beat){
		beat.getMeasure().removeBeat(beat);
	}
	
	public void removeBeat(TGMeasure measure,long start,boolean moveNextComponents){
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			removeBeat(beat, moveNextComponents);
		}
	}
	
	/**
	 * Elimina un silencio del compas.
	 * si se asigna moveNextComponents = true. los componentes que le siguen
	 * se moveran para completar el espacio vacio que dejo el silencio
	 */
	public void removeBeat(TGBeat beat,boolean moveNextBeats){
		TGMeasure measure = beat.getMeasure();
		
		removeBeat(beat);
		if(moveNextBeats){
			long start = beat.getStart();
			long length = beat.getDuration().getTime();
			TGBeat next = getNextBeat(measure.getBeats(),beat);
			if(next != null){
				length = next.getStart() - start;
			}
			moveBeats(beat.getMeasure(),start + length,-length,  beat.getDuration());
		}
	}
	
	public void removeBeatsBeforeEnd(TGMeasure measure,long fromStart){
		List beats = getBeatsBeforeEnd( measure.getBeats() , fromStart);
		Iterator it = beats.iterator();
		while(it.hasNext()){
			TGBeat beat = (TGBeat) it.next();
			removeBeat(beat);
		}
	}
	
	public void addNote(TGMeasure measure,long start, TGNote note, TGDuration duration){
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			addNote(beat, note, duration);
		}
	}
	
	public void addNote(TGBeat beat, TGNote note, TGDuration duration){
		addNote(beat, note, duration, beat.getStart());
	}
	
	public void addNote(TGBeat beat, TGNote note, TGDuration duration, long start){
		//Verifico si entra en el compas
		if(validateDuration(beat.getMeasure(),beat, duration,true,true)){
			//Borro lo que haya en la misma posicion
			removeNote(beat.getMeasure(),beat.getStart(),note.getString());
			
			duration.copy(beat.getDuration());
			
			//trato de agregar un silencio similar al lado
			tryChangeSilenceAfter(beat.getMeasure(),beat);
			
			// Despues de cambiar la duracion, verifico si hay un beat mejor para agregar la nota.
			TGBeat realBeat = beat;
			if(realBeat.getStart() != start){
				TGBeat beatIn = getBeatIn(realBeat.getMeasure(), start);
				if( beatIn != null ) {
					realBeat = beatIn;
				}
			}
			realBeat.addNote(note);
		}
	}
	
	public void removeNote(TGNote note){
		note.getBeat().removeNote(note);
	}
	
	/**
	 * Elimina los Componentes que empiecen en Start y esten en la misma cuerda
	 * Si hay un Silencio lo borra sin importar la cuerda
	 */
	public void removeNote(TGMeasure measure,long start,int string){
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			for( int i = 0; i < beat.countNotes(); i ++){
				TGNote note = beat.getNote(i);
				if(note.getString() == string){
					removeNote(note);
					
					//si era el unico componente agrego un silencio
					if(beat.isRestBeat()){
						//Borro un posible acorde
						removeChord(measure, beat.getStart());
					}
					return;
				}
			}
		}
	}
	
	public void removeNotesAfterString(TGMeasure measure,int string){
		List notesToRemove = new ArrayList();
		
		Iterator beats = measure.getBeats().iterator();
		while(beats.hasNext()){
			TGBeat beat = (TGBeat)beats.next();
			Iterator notes = beat.getNotes().iterator();
			while(notes.hasNext()){
				TGNote note = (TGNote)notes.next();
				if(note.getString() > string){
					notesToRemove.add(note);
				}
			}
		}
		Iterator it = notesToRemove.iterator();
		while(it.hasNext()){
			TGNote note = (TGNote)it.next();
			removeNote(note);
		}
	}
	
	/**
	 * Retorna Todas las Notas en la posicion Start
	 */
	public List getNotes(TGMeasure measure,long start){
		List notes = new ArrayList();
		
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			Iterator it = beat.getNotes().iterator();
			while(it.hasNext()){
				TGNote note = (TGNote)it.next();
				notes.add(note);
			}
		}
		return notes;
	}
	
	/**
	 * Retorna la Nota en la posicion y cuerda
	 */
	public TGNote getNote(TGMeasure measure,long start,int string) {
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			return getNote(beat, string);
		}
		return null;
	}
	
	/**
	 * Retorna la Nota en la cuerda
	 */
	public TGNote getNote(TGBeat beat,int string) {
		Iterator it = beat.getNotes().iterator();
		while(it.hasNext()){
			TGNote note = (TGNote)it.next();
			if (note.getString() == string) {
				return note;
			}
		}
		return null;
	}
	
	public TGNote getPreviousNote(TGMeasure measure,long start, int string) {
		TGBeat beat = getBeat(measure, start);
		if( beat != null ){
			TGBeat previous = getPreviousBeat(measure.getBeats(),beat);
			while(previous != null){
				for (int i = 0; i < previous.countNotes(); i++) {
					TGNote current = previous.getNote(i);
					if (current.getString() == string) {
						return current;
					}
				}
				previous = getPreviousBeat(measure.getBeats(),previous);
			}
		}
		return null;
	}
	
	public TGNote getNextNote(TGMeasure measure,long start, int string) {
		TGBeat beat = getBeat(measure, start);
		if( beat != null ){
			TGBeat next = getNextBeat(measure.getBeats(),beat);
			while(next != null){
				for (int i = 0; i < next.countNotes(); i++) {
					TGNote current = next.getNote(i);
					if (current.getString() == string) {
						return current;
					}
				}
				next = getNextBeat(measure.getBeats(),next);
			}
		}
		return null;
	}
	
	/**
	 * Retorna las Nota en la posicion y cuerda
	 */
	public TGBeat getBeat(TGMeasure measure,long start) {
		Iterator it = measure.getBeats().iterator();
		while(it.hasNext()){
			TGBeat beat = (TGBeat)it.next();
			if (beat.getStart() == start) {
				return beat;
			}
		}
		return null;
	}
	
	/**
	 * Retorna las Nota en la posicion y cuerda
	 */
	public TGBeat getBeatIn(TGMeasure measure,long start) {
		Iterator it = measure.getBeats().iterator();
		while(it.hasNext()){
			TGBeat beat = (TGBeat)it.next();
			if (beat.getStart() <= start && (beat.getStart() + beat.getDuration().getTime() > start)) {
				return beat;
			}
		}
		return null;
	}
	
	/**
	 * Retorna el Siguiente Componente
	 */
	public TGBeat getNextBeat(List beats,TGBeat beat) {
		TGBeat next = null;
		for (int i = 0; i < beats.size(); i++) {
			TGBeat current = (TGBeat) beats.get(i);
			if (current.getStart() > beat.getStart()) {
				if (next == null) {
					next = current;
				} else if (current.getStart() < next.getStart()) {
					next = current;
				} else if (current.getStart() == next.getStart() && current.getDuration().getTime() <= next.getDuration().getTime()) {
					next = current;
				}
			}
		}
		return next;
	}
	
	/**
	 * Retorna el Componente Anterior
	 */
	public TGBeat getPreviousBeat(List beats,TGBeat beat) {
		TGBeat previous = null;
		for (int i = 0; i < beats.size(); i++) {
			TGBeat current = (TGBeat) beats.get(i);
			if (current.getStart() < beat.getStart()) {
				if (previous == null) {
					previous = current;
				} else if (current.getStart() > previous.getStart()) {
					previous = current;
				} else if (current.getStart() == previous.getStart() && current.getDuration().getTime() <= previous.getDuration().getTime()) {
					previous = current;
				}
			}
		}
		return previous;
	}
	
	/**
	 * Retorna el Primer Componente
	 */
	public TGBeat getFirstBeat(List components) {
		TGBeat first = null;
		for (int i = 0; i < components.size(); i++) {
			TGBeat component = (TGBeat) components.get(i);
			if (first == null || component.getStart() < first.getStart()) {
				first = component;
			}
		}
		return first;
	}
	
	/**
	 * Retorna el Ultimo Componente
	 */
	public TGBeat getLastBeat(List components) {
		TGBeat last = null;
		for (int i = 0; i < components.size(); i++) {
			TGBeat component = (TGBeat) components.get(i);
			if (last == null || last.getStart() < component.getStart()) {
				last = component;
			}
		}
		return last;
	}
	
	/**
	 * Retorna el Siguiente Componente
	 */
	public TGBeat getNextRestBeat(List beats,TGBeat component) {
		TGBeat next = getNextBeat(beats, component);
		while(next != null && !next.isRestBeat()){
			next = getNextBeat(beats, next);
		}
		return next;
	}
	
	
	/**
	 * Retorna Todos los desde Start hasta el final del compas
	 */
	public List getBeatsBeforeEnd(List beats,long fromStart) {
		List list = new ArrayList();
		Iterator it = beats.iterator();
		while(it.hasNext()){
			TGBeat current = (TGBeat)it.next();
			if (current.getStart() >= fromStart) {
				list.add(current);
			}
		}
		return list;
	}
	
	public void moveAllBeats(TGMeasure measure,long theMove){
		moveBeats(measure.getBeats(),theMove);
	}
	
	public boolean moveBeats(TGMeasure measure,long start,long theMove, TGDuration fillDuration){
		if( theMove == 0 ){
			return false;
		}
		boolean success = true;
		long measureStart = measure.getStart();
		long measureEnd =  (measureStart + measure.getLength());
		
		// Muevo los componentes
		List beatsToMove = getBeatsBeforeEnd(measure.getBeats(),start);
		moveBeats(beatsToMove,theMove);
		
		if(success){
			List beatsToRemove = new ArrayList();
			List beats = new ArrayList(measure.getBeats());
			
			// Verifica los silencios a eliminar al principio del compas
			TGBeat first = getFirstBeat( beats );
			while(first != null && first.isRestBeat() && !first.isTextBeat() && first.getStart() < measureStart){
				beats.remove(first);
				beatsToRemove.add(first);
				first = getNextBeat( beats,first);
			}
			
			// Verifica los silencios a eliminar al final del compas
			TGBeat last = getLastBeat(beats);
			while(last != null && last.isRestBeat() && !last.isTextBeat()  && (last.getStart() + last.getDuration().getTime() ) > measureEnd  ){
				beats.remove(last);
				beatsToRemove.add(last);
				last = getPreviousBeat(beats,last);
			}
			
			// Si el primer o ultimo componente, quedan fuera del compas, entonces el movimiento no es satisfactorio
			if(first != null && last != null){
				if(first.getStart() < measureStart || (last.getStart() + last.getDuration().getTime()) > measureEnd){
					success = false;
				}
			}
			
			if(success){
				// Elimino los silencios que quedaron fuera del compas.
				Iterator it = beatsToRemove.iterator();
				while( it.hasNext() ){
					TGBeat beat = (TGBeat)it.next();
					removeBeat(beat);
				}
				
				// Se crean silencios en los espacios vacios, si la duracion fue especificada.
				if( fillDuration != null ){
					if( theMove < 0 ){
						last = getLastBeat(measure.getBeats());
						TGBeat beat = getSongManager().getFactory().newBeat();
						beat.setStart( (last != null ? last.getStart()  + last.getDuration().getTime() : start  )  );
						fillDuration.copy( beat.getDuration() );
						if( (beat.getStart() + beat.getDuration().getTime()) <= measureEnd ){
							addBeat(measure, beat );
						}
					}
					else{
						first = getFirstBeat(getBeatsBeforeEnd(measure.getBeats(),start));
						TGBeat beat = getSongManager().getFactory().newBeat();
						beat.setStart( start );
						fillDuration.copy( beat.getDuration() );
						if( (beat.getStart() + beat.getDuration().getTime()) <= (first != null ?first.getStart() : measureEnd ) ){
							addBeat(measure, beat );
						}
					}
				}
			}
		}
		
		// Si el movimiento no es satisfactorio, regreso todo como estaba
		if(! success ){
			moveBeats(beatsToMove,-theMove);
		}
		
		return success;
	}
	
	/**
	 * Mueve los componentes
	 */
	private void moveBeats(List beats,long theMove){
		Iterator it = beats.iterator();
		while(it.hasNext()){
			TGBeat beat = (TGBeat)it.next();
			moveBeat(beat,theMove);
		}
	}
	
	/**
	 * Mueve el componente
	 */
	private void moveBeat(TGBeat beat,long theMove){
		//obtengo el start viejo
		long start = beat.getStart();
		
		//asigno el nuevo start
		beat.setStart(start + theMove);
	}
	
	public void cleanBeat(TGBeat beat){
		if( beat.getText() != null ){
			beat.removeChord();
		}
		if( beat.getChord() != null){
			beat.removeText();
		}
		
		this.cleanBeatNotes(beat);
	}
	
	public void cleanBeatNotes(TGBeat beat){
		while(beat.countNotes() > 0 ){
			TGNote note = beat.getNote(0);
			removeNote(note);
		}
	}
	
	public void cleanBeatNotes(TGMeasure measure, long start){
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			cleanBeatNotes(beat);
		}
	}
	
	/**
	 * Agrega el acorde al compas
	 */
	public void addChord(TGMeasure measure,long start, TGChord chord){
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			addChord(beat, chord);
		}
	}
	
	/**
	 * Agrega el acorde al compas
	 */
	public void addChord(TGBeat beat,TGChord chord){
		beat.removeChord();
		beat.setChord(chord);
	}
	
	/**
	 * Retorna el acorde en la position
	 */
	public TGChord getChord(TGMeasure measure,long start) {
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			return beat.getChord();
		}
		return null;
	}
	
	/**
	 * Borra el acorde en la position
	 */
	public void removeChord(TGMeasure measure,long start) {
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			beat.removeChord();
		}
	}
	
	/**
	 * Agrega el texto al compas
	 */
	public void addText(TGMeasure measure,long start, TGText text){
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			addText(beat, text);
		}
	}
	
	/**
	 * Agrega el texto al compas
	 */
	public void addText(TGBeat beat,TGText text){
		beat.removeText();
		if(!text.isEmpty()){
			beat.setText(text);
		}
	}
	
	/**
	 * Retorna el texto en la position
	 */
	public TGText getText(TGMeasure measure,long start) {
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			return beat.getText();
		}
		return null;
	}
	
	/**
	 * Borra el texto en el pulso
	 */
	public void removeText(TGBeat beat){
		beat.removeText();
	}
	
	/**
	 * Borra el texto en la position
	 */
	public boolean removeText(TGMeasure measure,long start) {
		TGBeat beat = getBeat(measure, start);
		if(beat != null){
			removeText(beat);
			return true;
		}
		return false;
	}
	
	public void cleanMeasure(TGMeasure measure){
		while( measure.countBeats() > 0){
			removeBeat( measure.getBeat(0));
		}
	}
	
	/**
	 * Mueve la nota a la cuerda de arriba
	 */
	public int shiftNoteUp(TGMeasure measure,long start,int string){
		return shiftNote(measure, start, string,-1);
	}
	
	/**
	 * Mueve la nota a la cuerda de abajo
	 */
	public int shiftNoteDown(TGMeasure measure,long start,int string){
		return shiftNote(measure, start, string,1);
	}
	
	/**
	 * Mueve la nota a la siguiente cuerda
	 */
	private int shiftNote(TGMeasure measure,long start,int string,int move){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			int nextStringNumber = (note.getString() + move);
			while(getNote(measure,start,nextStringNumber) != null){
				nextStringNumber += move;
			}
			if(nextStringNumber >= 1 && nextStringNumber <= measure.getTrack().stringCount()){
				TGString currentString = measure.getTrack().getString(note.getString());
				TGString nextString = measure.getTrack().getString(nextStringNumber);
				int noteValue = (note.getValue() + currentString.getValue());
				if(noteValue >= nextString.getValue() && ((nextString.getValue() + 30 > noteValue) || measure.getTrack().isPercussionTrack()) ){
					note.setValue(noteValue - nextString.getValue());
					note.setString(nextString.getNumber());
					return note.getString();
				}
			}
		}
		return 0;
	}
	
	/**
	 * Mueve la nota 1 semitono arriba
	 */
	public boolean moveSemitoneUp(TGMeasure measure,long start,int string){
		return moveSemitone(measure, start, string,1);
	}
	
	/**
	 * Mueve la nota 1 semitono abajo
	 */
	public boolean moveSemitoneDown(TGMeasure measure,long start,int string){
		return moveSemitone(measure, start, string,-1);
	}
	
	/**
	 * Mueve la nota los semitonos indicados
	 */
	private boolean moveSemitone(TGMeasure measure,long start,int string,int semitones){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			int newValue = (note.getValue() + semitones);
			if(newValue >= 0 && (newValue < 30 || measure.getTrack().isPercussionTrack()) ){
				note.setValue(newValue);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Verifica si el componente se puede insertar en el compas.
	 * si no puede, con la opcion removeSilences, verifica si el motivo por el
	 * cual no entra es que lo siguen silencios. de ser asi los borra. 
	 */
	public boolean validateDuration(TGMeasure measure,TGBeat beat,boolean moveNextComponents, boolean setCurrentDuration){
		return validateDuration(measure, beat, beat.getDuration(),moveNextComponents, setCurrentDuration);
	}
	
	
	public boolean validateDuration(TGMeasure measure,TGBeat beat,TGDuration duration,boolean moveNextBeats, boolean setCurrentDuration){
		int errorMargin = 10;
		this.orderBeats(measure);
		long measureStart = measure.getStart();
		long measureEnd =  (measureStart + measure.getLength());
		long beatStart = beat.getStart();
		long beatLength = duration.getTime();
		long beatEnd = (beatStart + beatLength);
		List beats = measure.getBeats();
		
		//Verifico si hay un beat en el mismo lugar, y comparo las duraciones.
		TGBeat currentBeat = getBeat(measure,beatStart);
		if(currentBeat != null && beatLength <= currentBeat.getDuration().getTime()){
			/*if(componentAtBeat instanceof TGSilence){
				removeSilence((TGSilence)componentAtBeat, false);
			}*/
			return true;
		}
		
		//Verifico si hay lugar para meter el beat
		TGBeat nextComponent = getNextBeat(beats,beat);
		if(currentBeat == null){
			if(nextComponent == null && beatEnd < (measureEnd + errorMargin)){
				return true;
			}
			if(nextComponent != null && beatEnd < (nextComponent.getStart() + errorMargin)){
				return true;
			}
		}
		
		// Busca si hay espacio disponible de silencios entre el componente y el el que le sigue.. si encuentra lo borra
		if(nextComponent != null && nextComponent.isRestBeat()){
			//Verifico si lo que sigue es un silencio. y lo borro
			long nextBeatEnd = 0;
			List nextBeats = new ArrayList();
			while(nextComponent != null && nextComponent.isRestBeat() && !nextComponent.isTextBeat()){
				nextBeats.add(nextComponent);
				nextBeatEnd = nextComponent.getStart() + nextComponent.getDuration().getTime();
				nextComponent = getNextBeat(beats,nextComponent);
			}
			if(nextComponent == null){
				nextBeatEnd = measureEnd;
			}else if(!nextComponent.isRestBeat() || nextComponent.isTextBeat()){
				nextBeatEnd = nextComponent.getStart();
			}
			if(beatEnd <= (nextBeatEnd + errorMargin)){
				while(!nextBeats.isEmpty()){
					TGBeat currBeat = (TGBeat)nextBeats.get(0);
					nextBeats.remove(currBeat);
					removeBeat(currBeat, false);
				}
				return true;
			}
		}
		
		// Busca si hay espacio disponible de silencios entre el componente y el final.. si encuentra mueve todo
		if(moveNextBeats){
			nextComponent = getNextBeat(beats,beat);
			if(nextComponent != null){
				long requiredLength = (beatLength  - (nextComponent.getStart() - beatStart));
				long nextSilenceLength = 0;
				TGBeat nextRestBeat = getNextRestBeat(beats, beat);
				while(nextRestBeat != null && !nextRestBeat.isTextBeat()){ 
					nextSilenceLength += nextRestBeat.getDuration().getTime();
					nextRestBeat = getNextRestBeat(beats, nextRestBeat);
				}
				
				if(requiredLength <= (nextSilenceLength + errorMargin)){
					beats = getBeatsBeforeEnd(measure.getBeats(),nextComponent.getStart());
					while(!beats.isEmpty()){
						TGBeat current = (TGBeat)beats.get(0);
						if(current.isRestBeat() && !current.isTextBeat()){
							requiredLength -= current.getDuration().getTime();
							removeBeat(current, false);
						}else if(requiredLength > 0){
							moveBeat(current,requiredLength);
						}
						beats.remove(0);
					}
					return true;
				}
			}
		}
		
		// como ultimo intento, asigno la duracion de cualquier componente existente en el lugar.
		if(setCurrentDuration && currentBeat != null){
			/*if(componentAtBeat instanceof TGSilence){
				removeSilence((TGSilence)componentAtBeat, false);
			}*/
			currentBeat.getDuration().copy( duration );
			return true;
		}
		return false;
	}
	
	/**
	 * Cambia la Duracion del pulso.
	 */
	public void changeDuration(TGMeasure measure,TGBeat beat,TGDuration duration,boolean tryMove){
		//obtengo la duracion vieja
		TGDuration oldDuration = beat.getDuration().clone(getSongManager().getFactory());
		
		//si no entra vuelvo a dejar la vieja
		if(validateDuration(measure,beat, duration,tryMove,false)){
			//se lo agrego a todas las notas en la posicion
			beat.setDuration(duration.clone(getSongManager().getFactory()));
			
			//trato de agregar un silencio similar al lado
			tryChangeSilenceAfter(measure,beat);
		}else{
			oldDuration.copy( beat.getDuration() );
		}
	}
	
	public void tryChangeSilenceAfter(TGMeasure measure,TGBeat beat){
		autoCompleteSilences(measure);
		TGBeat nextBeat = getNextBeat(measure.getBeats(),beat);
		
		long beatEnd = (beat.getStart() + beat.getDuration().getTime());
		long measureEnd = (measure.getStart() + measure.getLength());
		if(nextBeat != null && nextBeat.isRestBeat() && beatEnd <= measureEnd){
			long theMove = (getRealStart(measure,beatEnd)) - getRealStart(measure,nextBeat.getStart());
			if((nextBeat.getStart() + theMove) < measureEnd && (nextBeat.getStart() + nextBeat.getDuration().getTime() + theMove) <= measureEnd){
				moveBeat(nextBeat,theMove);
				changeDuration(measure,nextBeat,beat.getDuration().clone(getSongManager().getFactory()),false);
			}
		}
	}
	
	/**
	 * Calcula si hay espacios libres. y crea nuevos silencios
	 */
	public void autoCompleteSilences(TGMeasure measure){
		long start = measure.getStart();
		long end = 0;
		long diff = 0;
		List components = measure.getBeats();
		TGBeat component = getFirstBeat(components);
		
		while (component != null) {
			end = component.getStart() + component.getDuration().getTime();
			if(component.getStart() > start){
				diff = component.getStart() - start;
				if(diff > 0){
					createSilences(measure,start,diff);
				}
			}
			start = end;
			component = getNextBeat(components,component);
		}
		end = measure.getStart() + measure.getLength();
		diff = end - start;
		if(diff > 0){
			createSilences(measure,start,diff);
		}
	}
	
	/**
	 * Crea Silencios temporarios en base a length
	 */
	private void createSilences(TGMeasure measure,long start,long length){
		long nextStart = start;
		List durations = createDurations(getSongManager().getFactory(),length);
		Iterator it = durations.iterator();
		while(it.hasNext()){
			TGDuration duration = (TGDuration)it.next();
			TGBeat beat = getSongManager().getFactory().newBeat();
			beat.setStart( getRealStart(measure, nextStart) );
			duration.copy(beat.getDuration());
			addBeat(measure,beat);
			nextStart += duration.getTime();
		}
	}
	
	public long getRealStart(TGMeasure measure,long currStart){
		long beatLength = TGSongManager.getDivisionLength(measure.getHeader());
		long start = currStart;
		boolean startBeat = (start % beatLength == 0);
		if(!startBeat){
			TGDuration minDuration = getSongManager().getFactory().newDuration();
			minDuration.setValue(TGDuration.SIXTY_FOURTH);
			minDuration.getTupleto().setEnters(3);
			minDuration.getTupleto().setTimes(2);
			for(int i = 0;i < minDuration.getTime();i++){
				start ++;
				startBeat = (start % beatLength == 0);
				if(startBeat){
				   break;
				}
			}
			if(!startBeat){
				start = currStart;
			}
		}
		return start;
	}
	
	/** 
	 * Liga la nota
	 */
	public void changeTieNote(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			changeTieNote(note);
		}
	}
	
	/** 
	 * Liga la nota
	 */
	public void changeTieNote(TGNote note){
		note.setTiedNote(!note.isTiedNote());
		note.getEffect().setDeadNote(false);
	}
	
	/** 
	 * Agrega un vibrato
	 */
	public void changeVibratoNote(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setVibrato(!note.getEffect().isVibrato());
		}
	}
	
	/** 
	 * Agrega una nota muerta
	 */
	public void changeDeadNote(TGNote note){
		note.getEffect().setDeadNote(!note.getEffect().isDeadNote());
		note.setTiedNote(false);
	}
	
	/** 
	 * Agrega un slide
	 */
	public void changeSlideNote(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setSlide(!note.getEffect().isSlide());
		}
	}
	
	/** 
	 * Agrega un hammer
	 */
	public void changeHammerNote(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setHammer(!note.getEffect().isHammer());
		}
	}
	
	/** 
	 * Agrega un palm-mute
	 */
	public void changePalmMute(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setPalmMute(!note.getEffect().isPalmMute());
		}
	}
	
	/** 
	 * Agrega un staccato
	 */
	public void changeStaccato(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setStaccato(!note.getEffect().isStaccato());
		}
	}
	
	/** 
	 * Agrega un tapping
	 */
	public void changeTapping(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setTapping(!note.getEffect().isTapping());
		}
	}
	
	/** 
	 * Agrega un slapping
	 */
	public void changeSlapping(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setSlapping(!note.getEffect().isSlapping());
		}
	}
	
	/** 
	 * Agrega un popping
	 */
	public void changePopping(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setPopping(!note.getEffect().isPopping());
		}
	}
	
	/** 
	 * Agrega un bend
	 */
	public void changeBendNote(TGMeasure measure,long start,int string,TGEffectBend bend){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setBend(bend);
		}
	}
	
	/** 
	 * Agrega un tremoloBar
	 */
	public void changeTremoloBar(TGMeasure measure,long start,int string,TGEffectTremoloBar tremoloBar){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setTremoloBar(tremoloBar);
		}
	}
	
	/** 
	 * Agrega un GhostNote
	 */
	public void changeGhostNote(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){ 
			note.getEffect().setGhostNote(!note.getEffect().isGhostNote());
		}
	}
	
	/** 
	 * Agrega un AccentuatedNote
	 */
	public void changeAccentuatedNote(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){ 
			note.getEffect().setAccentuatedNote(!note.getEffect().isAccentuatedNote());
		}
	}
	
	/** 
	 * Agrega un GhostNote
	 */
	public void changeHeavyAccentuatedNote(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setHeavyAccentuatedNote(!note.getEffect().isHeavyAccentuatedNote());
		}
	}
	
	/** 
	 * Agrega un harmonic
	 */
	public void changeHarmonicNote(TGMeasure measure,long start,int string,TGEffectHarmonic harmonic){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setHarmonic(harmonic);
		}
	}
	
	/** 
	 * Agrega un grace
	 */
	public void changeGraceNote(TGMeasure measure,long start,int string,TGEffectGrace grace){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setGrace(grace);
		}
	}
	
	/** 
	 * Agrega un trill
	 */
	public void changeTrillNote(TGMeasure measure,long start,int string,TGEffectTrill trill){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setTrill(trill);
		}
	}
	
	/** 
	 * Agrega un tremolo picking
	 */
	public void changeTremoloPicking(TGMeasure measure,long start,int string,TGEffectTremoloPicking tremoloPicking){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setTremoloPicking(tremoloPicking);
		}
	}
	
	/** 
	 * Agrega un fadeIn
	 */
	public void changeFadeIn(TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.getEffect().setFadeIn(!note.getEffect().isFadeIn());
		}
	}
	
	/** 
	 * Cambia el Velocity
	 */
	public void changeVelocity(int velocity,TGMeasure measure,long start,int string){
		TGNote note = getNote(measure,start,string);
		if(note != null){
			note.setVelocity(velocity);
		}
	}
	/*
	public static List createDurations(TGFactory factory,long time){
		List durations = new ArrayList();
		TGDuration tempDuration = factory.newDuration();
		tempDuration.setValue(TGDuration.WHOLE);
		tempDuration.setDotted(true);
		long tempTime = time;
		boolean finish = false;
		while(!finish){
			long currentDurationTime = tempDuration.getTime();
			if(currentDurationTime <= tempTime){
				durations.add(tempDuration.clone(factory));
				tempTime -= currentDurationTime;
			}else{
				if(tempDuration.isDotted()){
					tempDuration.setDotted(false);
				}else{
					tempDuration.setValue(tempDuration.getValue() * 2);
					tempDuration.setDotted(true);
				}
			}
			if(tempDuration.getValue() > TGDuration.SIXTY_FOURTH){
				finish = true;
			}
		}
		return durations;
	}
	*/
	
	public static List createDurations(TGFactory factory,long time){
		List durations = new ArrayList();
		TGDuration minimum = factory.newDuration();
		minimum.setValue(TGDuration.SIXTY_FOURTH);
		minimum.setDotted(false);
		minimum.setDoubleDotted(false);
		minimum.getTupleto().setEnters(3);
		minimum.getTupleto().setTimes(2);
		
		long missingTime = time;
		while( missingTime > minimum.getTime() ){
			TGDuration duration = TGDuration.fromTime(factory, missingTime, minimum ,  10);
			durations.add( duration.clone(factory) );
			missingTime -= duration.getTime();
		}
		return durations;
	}
	
}
