/*
 * Created on 13-dic-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.herac.tuxguitar.player.base;

import java.util.List;

import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.song.models.TGChannel;
import org.herac.tuxguitar.song.models.TGDuration;
import org.herac.tuxguitar.song.models.TGMeasure;
import org.herac.tuxguitar.song.models.TGMeasureHeader;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.song.models.TGString;
import org.herac.tuxguitar.song.models.TGTrack;
import org.herac.tuxguitar.song.models.TGVelocities;
import org.herac.tuxguitar.song.models.effects.TGEffectBend;
import org.herac.tuxguitar.song.models.effects.TGEffectHarmonic;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloBar;

/**
 * @author julian
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class MidiSequenceParser {
	
	private static final int DEFAULT_METRONOME_KEY = 37;
	
	private static final int DEFAULT_DEAD_NOTE_DURATION = 50;
	
	private static final int DEFAULT_BEND = 64;
	
	private static final float DEFAULT_BEND_SEMI_TONE = 2.75f;
	
	/**
	 * flag para agregar los controles por defecto, 
	 * no se recomienda usar este flag si el reproductor asigna estos controles en tiempo real.
	 */
	public static final int ADD_DEFAULT_CONTROLS = 0x01;
	/**
	 * flag para agregar los valores del mixer (volumen, balance, instrumento),
	 * no se recomienda usar este flag si el reproductor asigna estos valores en tiempo real.
	 */
	public static final int ADD_MIXER_MESSAGES = 0x02;
	/**
	 * flag para agregar la pista del metronomo,
	 * en casos como la exportacion de midi, este flag no sera necesario
	 */
	public static final int ADD_METRONOME = 0x04;
	/**
	 * tuxguitar usa como primer tick el valor de la constante Duration.QUARTER_TIME
	 * asignando este flag, es posible crear el primer tick en cero.
	 */
	public static final int ADD_FIRST_TICK_MOVE = 0x08;
	
	public static final int DEFAULT_PLAY_FLAGS = (ADD_METRONOME);
	
	public static final int DEFAULT_EXPORT_FLAGS = (ADD_FIRST_TICK_MOVE | ADD_DEFAULT_CONTROLS | ADD_MIXER_MESSAGES);
	
	/**
	 * Song Manager
	 */
	private TGSongManager manager;
	/**
	 * flags
	 */
	private int flags;
	/**
	 * Index of info track
	 */
	private int infoTrack;
	/**
	 * Index of metronome track
	 */
	private int metronomeTrack;
	
	private int fisrtTickMove;
	
	private int tempoPercent;
	
	private int transpose;
	
	public MidiSequenceParser(TGSongManager manager,int flags,int tempoPercent,int transpose) {
		this.manager = manager;
		this.flags = flags;
		this.transpose = transpose;
		this.tempoPercent = tempoPercent;
		this.fisrtTickMove = (int)(((flags & ADD_FIRST_TICK_MOVE) != 0)?(-TGDuration.QUARTER_TIME):0);
	}
	
	public MidiSequenceParser(TGSongManager manager,int flags) {
		this(manager,flags,100,0);
	}
	
	/**
	 * Crea la cancion
	 */
	public void parse(MidiSequenceHandler sequence) {
		this.infoTrack = sequence.getInfoTrack();
		this.metronomeTrack = sequence.getMetronomeTrack();
		addDefaultMessages(sequence);
		for (int i = 0; i < this.manager.getSong().countTracks(); i++) {
			TGTrack songTrack = this.manager.getSong().getTrack(i);
			createTrack(sequence,songTrack);
		}
		sequence.notifyFinish();
	}
	
	private int infoIndex(){
		return this.infoTrack;
	}
	
	private int metronomeIndex(){
		return this.metronomeTrack;
	}
	
	private long getTick(long tick){
		return (tick + this.fisrtTickMove);
	}
	
	/**
	 * Crea las pistas de la cancion
	 */
	private void createTrack(MidiSequenceHandler sequence,TGTrack track) {
		TGMeasure previous = null;
		MidiRepeatController controller = new MidiRepeatController(track.getSong());
		
		addBend(sequence,track.getNumber(),TGDuration.QUARTER_TIME,DEFAULT_BEND,track.getChannel().getChannel());
		makeChannel(sequence, track.getChannel(), track.getNumber());
		while(!controller.finished()){
			TGMeasure measure = track.getMeasure(controller.getIndex());
			int index = controller.getIndex();
			long move = controller.getRepeatMove();
			controller.process();
			if(controller.shouldPlay()){
				if(track.getNumber() == 1){
					addTimeSignature(sequence,measure, previous,move);
					addTempo(sequence,measure, previous,move);
					addMetronome(sequence,measure.getHeader(),move);
				}
				//agrego los pulsos
				makeBeats(sequence,track.getNumber(), track, measure,index, move);
			}
			previous = measure;
		}
	}
	
	private void makeBeats(MidiSequenceHandler sequence,int track, TGTrack songTrack, TGMeasure measure, int measureIdx, long startMove) {
		for (int bIndex = 0; bIndex < measure.countBeats(); bIndex++) {
			TGBeat beat = measure.getBeat(bIndex);
			makeNotes(sequence, track, songTrack, beat, measureIdx, bIndex, startMove);
		}
	}
	
	/**
	 * Crea las notas del compas
	 */
	private void makeNotes(MidiSequenceHandler sequence,int track, TGTrack songTrack, TGBeat beat, int measureIdx,int bIndex, long startMove) {
		BeatData data = checkTripletFeel(beat,bIndex);
		for (int noteIdx = 0; noteIdx < beat.countNotes(); noteIdx++) {
			TGNote note = beat.getNote(noteIdx);
			if (!note.isTiedNote()) {
				int key = (this.transpose + songTrack.getOffset() + note.getValue() + ((TGString)songTrack.getStrings().get(note.getString() - 1)).getValue());
				
				
				long start = data.getStart() + startMove;
				long duration = getRealNoteDuration(note,data.getDuration(), songTrack, measureIdx,bIndex);
				int velocity = getRealVelocity(note, songTrack, measureIdx, bIndex);
				int channel = songTrack.getChannel().getChannel();
				int effectChannel = songTrack.getChannel().getEffectChannel();
				
				boolean percussionTrack = songTrack.isPercussionTrack();
				//---Fade In---
				if(note.getEffect().isFadeIn()){
					channel = effectChannel;
					makeFadeIn(sequence,track, start, duration, songTrack.getChannel().getVolume(), channel);
				}
				//---Grace---
				if(note.getEffect().isGrace() && effectChannel >= 0 && !percussionTrack ){
					channel = effectChannel;
					int graceKey = songTrack.getOffset() + note.getEffect().getGrace().getFret() + ((TGString)songTrack.getStrings().get(note.getString() - 1)).getValue();
					int graceLength = note.getEffect().getGrace().getDurationTime();
					
					int graceDuration = ((note.getEffect().getGrace().isDead())?DEFAULT_DEAD_NOTE_DURATION:graceLength);
					int graceVelocity = note.getEffect().getGrace().getDynamic();
					
					if(note.getEffect().getGrace().isOnBeat() || (start - graceLength) < TGDuration.QUARTER_TIME){
						start += graceLength;
						duration -= graceLength;
					}
					makeNote(sequence,track, graceKey,start - graceLength,graceDuration,graceVelocity,channel);
					
				}
				//---Trill---
				if(note.getEffect().isTrill() && effectChannel >= 0 && !percussionTrack ){
					int trillKey = songTrack.getOffset() + note.getEffect().getTrill().getFret() + ((TGString)songTrack.getStrings().get(note.getString() - 1)).getValue();
					long trillLength = note.getEffect().getTrill().getDuration().getTime();
					
					boolean realKey = true;
					long tick = start;
					while(true){
						if(tick + 10 >= (start + duration)){
							break ;
						}else if( (tick + trillLength) >= (start + duration)){
							trillLength = (((start + duration) - tick) - 1);
						}
						makeNote(sequence,track,((realKey)?key:trillKey),tick,trillLength,velocity,channel);
						realKey = (!realKey);
						tick += trillLength;
					}
					continue;
				}
				//---Tremolo Picking---
				if(note.getEffect().isTremoloPicking() && effectChannel >= 0){
					long tpLength = note.getEffect().getTremoloPicking().getDuration().getTime();
					long tick = start;
					while(true){
						if(tick + 10 >= (start + duration)){
							break ;
						}else if( (tick + tpLength) >= (start + duration)){
							tpLength = (((start + duration) - tick) - 1);
						}
						makeNote(sequence,track,key,tick,tpLength,velocity,channel);
						tick += tpLength;
					}
					continue;
				}
				
				//---Bend---
				if(note.getEffect().isBend() && effectChannel >= 0 && !percussionTrack ){
					channel = effectChannel;
					makeBend(sequence,track,start,duration,note.getEffect().getBend(),channel);
				}
				//---TremoloBar---
				else if(note.getEffect().isTremoloBar() && effectChannel >= 0 && !percussionTrack ){
					channel = effectChannel;
					makeTremoloBar(sequence,track,start,duration,note.getEffect().getTremoloBar(),channel);
				}
				//---Slide---
				else if(note.getEffect().isSlide() && effectChannel >= 0 && !percussionTrack){
					channel = effectChannel;
					TGNote nextNote = getNextNote(note,songTrack,measureIdx,bIndex);
					makeSlide(sequence,track,note,nextNote,startMove,channel);
				}
				//---Vibrato---
				else if(note.getEffect().isVibrato() && effectChannel >= 0 && !percussionTrack){
					channel = effectChannel;
					makeVibrato(sequence,track,start,duration,channel);
				}
				//---Harmonic---
				if(note.getEffect().isHarmonic() && !percussionTrack){
					int orig = key;
					
					//Natural
					if(note.getEffect().getHarmonic().isNatural()){
						for(int i = 0;i < TGEffectHarmonic.NATURAL_FREQUENCIES.length;i ++){
							if((note.getValue() % 12) ==  (TGEffectHarmonic.NATURAL_FREQUENCIES[i][0] % 12) ){
								key = ((orig + TGEffectHarmonic.NATURAL_FREQUENCIES[i][1]) - note.getValue());
								break;
							}
						}
					}
					//Artifical/Tapped/Pinch/Semi
					else{
						if(note.getEffect().getHarmonic().isSemi() && !percussionTrack){
							makeNote(sequence,track,Math.min(127,orig), start, duration,Math.max(TGVelocities.MIN_VELOCITY,velocity - (TGVelocities.VELOCITY_INCREMENT * 3)),channel);
						}
						key = (orig + TGEffectHarmonic.NATURAL_FREQUENCIES[note.getEffect().getHarmonic().getData()][1]);
						
					}
					if( (key - 12) > 0 ){
						int hVelocity = Math.max(TGVelocities.MIN_VELOCITY,velocity - (TGVelocities.VELOCITY_INCREMENT * 4));
						makeNote(sequence,track,(key - 12), start, duration,hVelocity,channel);
					}
				}
				
				//---Normal Note---
				makeNote(sequence,track, Math.min(127,key), start, duration, velocity,channel);
			}
		}
	}
	
	/**
	 * Crea una nota en la posicion start
	 */
	private void makeNote(MidiSequenceHandler sequence,int track, int key, long start, long duration, int velocity, int channel) {
		sequence.addNoteOn(getTick(start),track,channel,key,velocity);
		sequence.addNoteOff(getTick(start + duration),track,channel,key,velocity);
	}
	
	private void makeChannel(MidiSequenceHandler sequence,TGChannel channel,int track) {
		if( (this.flags & ADD_MIXER_MESSAGES) != 0){
			makeChannel(sequence, channel, track,true);
			if(channel.getChannel() != channel.getEffectChannel()){
				makeChannel(sequence, channel, track,false);
			}
		}
	}
	
	private void makeChannel(MidiSequenceHandler sequence,TGChannel channel,int track,boolean primary) {
		int number = (primary?channel.getChannel():channel.getEffectChannel());
		sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),track,number,MidiControllers.VOLUME,channel.getVolume());
		sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),track,number,MidiControllers.BALANCE,channel.getBalance());
		sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),track,number,MidiControllers.CHORUS,channel.getChorus());
		sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),track,number,MidiControllers.REVERB,channel.getReverb());
		sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),track,number,MidiControllers.PHASER,channel.getPhaser());
		sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),track,number,MidiControllers.TREMOLO,channel.getTremolo());
		sequence.addProgramChange(getTick(TGDuration.QUARTER_TIME),track,number,channel.getInstrument());
	}
	/**
	 * Agrega un Time Signature si es distinto al anterior
	 */
	private void addTimeSignature(MidiSequenceHandler sequence,TGMeasure currMeasure, TGMeasure prevMeasure,long startMove){
		boolean addTimeSignature = false;
		if (prevMeasure == null) {
			addTimeSignature = true;
		} else {
			int currNumerator = currMeasure.getTimeSignature().getNumerator();
			int currValue = currMeasure.getTimeSignature().getDenominator().getValue();
			int prevNumerator = prevMeasure.getTimeSignature().getNumerator();
			int prevValue = prevMeasure.getTimeSignature().getDenominator().getValue();
			if (currNumerator != prevNumerator || currValue != prevValue) {
				addTimeSignature = true;
			}
		}
		if (addTimeSignature) {
			sequence.addTimeSignature(getTick(currMeasure.getStart() + startMove), infoIndex(), currMeasure.getTimeSignature());
		}
	}
	
	/**
	 * Agrega un Tempo si es distinto al anterior
	 */
	private void addTempo(MidiSequenceHandler sequence,TGMeasure currMeasure, TGMeasure prevMeasure,long startMove){
		boolean addTempo = false;
		if (prevMeasure == null) {
			addTempo = true;
		} else {
			if (currMeasure.getTempo().getInUSQ() != prevMeasure.getTempo().getInUSQ()) {
				addTempo = true;
			}
		}
		if (addTempo) {
			int usq = (int)(currMeasure.getTempo().getInUSQ() * 100.00 / this.tempoPercent );
			sequence.addTempoInUSQ(getTick(currMeasure.getStart() + startMove), infoIndex(), usq);
		}
	}
	
	/**
	 * Retorna la Duracion real de una nota, verificando si tiene otras ligadas
	 */
	private long getRealNoteDuration(TGNote note,long duration, TGTrack track, int mIndex, int bIndex) {
		if(note.getEffect().isDeadNote()){
			return DEFAULT_DEAD_NOTE_DURATION;
		}
		long lastEnd = (note.getBeat().getStart() + note.getBeat().getDuration().getTime());
		long realDuration = duration;
		int nextBIndex = (bIndex + 1);
		int measureCount = track.countMeasures();
		for (int m = mIndex; m < measureCount; m++) {
			TGMeasure measure = track.getMeasure( m );
			int beatCount = measure.countBeats();
			for (int b = nextBIndex; b < beatCount; b++) {
				TGBeat beat = measure.getBeat(b);
				if(beat.isRestBeat()){
					return applyDurationEffects(note,realDuration);
				}
				int noteCount = beat.countNotes();
				for (int n = 0; n < noteCount; n++) {
					TGNote nextNote = beat.getNote( n );
					if (!nextNote.equals(note)) {
						if (nextNote.getString() == note.getString()) {
							if (nextNote.isTiedNote()) {
								realDuration += (beat.getStart() - lastEnd) + (nextNote.getBeat().getDuration().getTime());
								lastEnd = (beat.getStart() + beat.getDuration().getTime());
							} else {
								return applyDurationEffects(note,realDuration);
							}
						}
					}
				}
			}
			nextBIndex = 0;
		}
		return applyDurationEffects(note,realDuration);
	}
	
	private long applyDurationEffects(TGNote note,long duration){
		//palm mute
		if(note.getEffect().isPalmMute()){
			return (long)(duration * 75.00 / 100.00);
		}
		//staccato
		if(note.getEffect().isStaccato()){
			return (long)(duration * 50.00 / 100.00);
		}
		return duration;
	}
	
	private int getRealVelocity(TGNote note, TGTrack songTrack, int mIndex,int bIndex){
		int velocity = note.getVelocity();
		
		//Check for Hammer effect
		if(!songTrack.isPercussionTrack()){
			TGNote prevNote = getPreviousNote(note,songTrack,mIndex,bIndex);
			if(prevNote != null && prevNote.getEffect().isHammer()){
				velocity = Math.max(TGVelocities.MIN_VELOCITY,(velocity - 25));
			}
		}
		
		//Check for GhostNote effect
		if(note.getEffect().isGhostNote()){
			velocity = Math.max(TGVelocities.MIN_VELOCITY,(velocity - TGVelocities.VELOCITY_INCREMENT));
		}else if(note.getEffect().isAccentuatedNote()){
			velocity = Math.max(TGVelocities.MIN_VELOCITY,(velocity + TGVelocities.VELOCITY_INCREMENT));
		}else if(note.getEffect().isHeavyAccentuatedNote()){
			velocity = Math.max(TGVelocities.MIN_VELOCITY,(velocity + (TGVelocities.VELOCITY_INCREMENT * 2)));
		}
		
		return ((velocity > 127)?127:velocity);
	}
	
	public void addMetronome(MidiSequenceHandler sequence,TGMeasureHeader header, long startMove){
		if( (this.flags & ADD_METRONOME) != 0) {
		
			long start = (startMove + header.getStart());
			long length = header.getTimeSignature().getDenominator().getTime();
			for(int i = 1; i <= header.getTimeSignature().getNumerator();i ++){
				makeNote(sequence,metronomeIndex(),DEFAULT_METRONOME_KEY,start,length,TGVelocities.DEFAULT,9);
				start += length;
			}
		}
	}
	
	public void addDefaultMessages(MidiSequenceHandler sequence) {
		if( (this.flags & ADD_DEFAULT_CONTROLS) != 0) {
			for(int i = 0; i < 16; i ++){
				sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),infoIndex(),i,MidiControllers.RPN_MSB,0);
				sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),infoIndex(),i,MidiControllers.RPN_LSB,0);
				sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),infoIndex(),i,MidiControllers.DATA_ENTRY_MSB,12);
				sequence.addControlChange(getTick(TGDuration.QUARTER_TIME),infoIndex(),i,MidiControllers.DATA_ENTRY_LSB, 0);
			}
		}
	}
	
	private void addBend(MidiSequenceHandler sequence,int track, long tick,int bend, int channel) {
		sequence.addPitchBend(getTick(tick),track,channel,bend);
	}
	
	public void makeVibrato(MidiSequenceHandler sequence,int track,long start, long duration,int channel){
		long nextStart = start;
		long end = nextStart + duration;
		
		while(nextStart < end){
			nextStart = ((nextStart + 160 > end)?end:nextStart + 160);
			addBend(sequence,track,nextStart,DEFAULT_BEND,channel);
			nextStart = ((nextStart + 160 > end)?end:nextStart + 160);
			addBend(sequence,track,nextStart,DEFAULT_BEND + (int)(DEFAULT_BEND_SEMI_TONE / 2.0f),channel);
		}
		addBend(sequence,track,nextStart,DEFAULT_BEND,channel);
	}
	
	public void makeBend(MidiSequenceHandler sequence,int track,long start, long duration, TGEffectBend bend, int channel){
		List points = bend.getPoints();
		for(int i=0;i<points.size();i++){
			TGEffectBend.BendPoint point = (TGEffectBend.BendPoint)points.get(i);
			long bendStart = start + point.getTime(duration);
			int value = DEFAULT_BEND + (int)(point.getValue() * DEFAULT_BEND_SEMI_TONE / TGEffectBend.SEMITONE_LENGTH);
			value = ((value <= 127)?value:127);
			value = ((value >= 0)?value:0);
			addBend(sequence,track,bendStart,value,channel);
			
			if(points.size() > i + 1){
				TGEffectBend.BendPoint nextPoint = (TGEffectBend.BendPoint)points.get(i + 1);
				int nextValue = DEFAULT_BEND + (int)(nextPoint.getValue() * DEFAULT_BEND_SEMI_TONE / TGEffectBend.SEMITONE_LENGTH);
				long nextBendStart = start + nextPoint.getTime(duration);
				if(nextValue != value){
					double width = ( (nextBendStart - bendStart) / Math.abs(  (nextValue - value) ) );
					//ascendente
					if(value < nextValue){
						while(value < nextValue){
							value ++;
							bendStart +=width;
							addBend(sequence,track,bendStart,((value <= 127)?value:127),channel);
						}
						//descendente
					}else if(value > nextValue){
						while(value > nextValue){
							value --;
							bendStart +=width;
							addBend(sequence,track,bendStart,((value >= 0)?value:0),channel);
						}
					}
				}
			}
		}
		addBend(sequence,track,start + duration,DEFAULT_BEND,channel);
	}
	
	public void makeTremoloBar(MidiSequenceHandler sequence,int track,long start, long duration, TGEffectTremoloBar effect, int channel){
		List points = effect.getPoints();
		for(int i=0;i<points.size();i++){
			TGEffectTremoloBar.TremoloBarPoint point = (TGEffectTremoloBar.TremoloBarPoint)points.get(i);
			long pointStart = start + point.getTime(duration);
			int value = DEFAULT_BEND + (int)(point.getValue() * (DEFAULT_BEND_SEMI_TONE * 2) );
			value = ((value <= 127)?value:127);
			value = ((value >= 0)?value:0);
			addBend(sequence,track,pointStart,value,channel);
			if(points.size() > i + 1){
				TGEffectTremoloBar.TremoloBarPoint nextPoint = (TGEffectTremoloBar.TremoloBarPoint)points.get(i + 1);
				int nextValue = DEFAULT_BEND + (int)(nextPoint.getValue() * (DEFAULT_BEND_SEMI_TONE * 2));
				long nextPointStart = start + nextPoint.getTime(duration);
				if(nextValue != value){
					double width = ( (nextPointStart - pointStart) / Math.abs(  (nextValue - value) ) );
					//ascendente
					if(value < nextValue){
						while(value < nextValue){
							value ++;
							pointStart +=width;
							addBend(sequence,track,pointStart,((value <= 127)?value:127),channel);
						}
					//descendente
					}else if(value > nextValue){
						while(value > nextValue){
							value --;
							pointStart += width;
							addBend(sequence,track,pointStart,((value >= 0)?value:0),channel);
						}
					}
				}
			}
		}
		addBend(sequence,track,start + duration,DEFAULT_BEND,channel);
	}
	
	public void makeSlide(MidiSequenceHandler sequence,int track,TGNote note,TGNote nextNote,long startMove,int channel){
		if(nextNote != null){
			makeSlide(sequence,track,note.getBeat().getStart()+startMove,note.getValue(),nextNote.getBeat().getStart() + startMove,nextNote.getValue(),channel);
			addBend(sequence,track,nextNote.getBeat().getStart() + startMove,DEFAULT_BEND,channel);
		}
	}
	
	public void makeSlide(MidiSequenceHandler sequence,int track,long tick1,int value1,long tick2,int value2,int channel){
		long distance = (value2 - value1);
		long length = (tick2 - tick1);
		int points = (int)(length / (TGDuration.QUARTER_TIME / 8));
		for(int i = 1;i <= points; i ++){
			float tone = ((((length / points) * (float)i) * distance) / length);
			int bend = (DEFAULT_BEND + (int)(tone * (DEFAULT_BEND_SEMI_TONE * 2)));
			addBend(sequence,track,tick1 + ( (length / points) * i),bend,channel);
		}
	}
	
	private void makeFadeIn(MidiSequenceHandler sequence,int track,long start,long duration,int volume,int channel){
		int newVolume = 40;
		if(volume > newVolume){
			int interval = (int)(duration / 4);
			int times = (int)(duration / interval);
			int increment = ((volume - newVolume) / times);
			for(long tick = start;tick < (start + duration);tick += interval){
				sequence.addControlChange(getTick(tick),track,channel,MidiControllers.VOLUME, newVolume);
				newVolume += increment;
			}
			sequence.addControlChange(getTick((start + duration)),track,channel, MidiControllers.VOLUME,volume);
		}
	}
	
	private BeatData checkTripletFeel(TGBeat beat,int bIndex){
		//long bStart = fixStart(beat.getStart());
		long bStart = beat.getStart();
		long bDuration =  beat.getDuration().getTime();
		if(beat.getMeasure().getTripletFeel() == TGMeasureHeader.TRIPLET_FEEL_EIGHTH){
			if(beat.getDuration().isEqual(newDuration(TGDuration.EIGHTH))){
				//first time
				if( (bStart % TGDuration.QUARTER_TIME) == 0){
					TGBeat b = getNextBeat(beat,bIndex);
					//if(b == null || (fixStart(b.getStart()) > (bStart + beat.getDuration().getTime()) || b.getDuration().isEqual(newDuration(TGDuration.EIGHTH)))  ){
					if(b == null || ( b.getStart() > (bStart + beat.getDuration().getTime()) || b.getDuration().isEqual(newDuration(TGDuration.EIGHTH)))  ){
						TGDuration duration = newDuration(TGDuration.EIGHTH);
						duration.getTupleto().setEnters(3);
						duration.getTupleto().setTimes(2);
						bDuration = (duration.getTime() * 2);
					}
				}
				//second time
				else if( (bStart % (TGDuration.QUARTER_TIME / 2)) == 0){
					TGBeat b = getPreviousBeat(beat,bIndex);
					//if(b == null || (fixStart(b.getStart()) < (bStart - beat.getDuration().getTime())  || b.getDuration().isEqual(newDuration(TGDuration.EIGHTH)) )){
					if(b == null || ( b.getStart() < (bStart - beat.getDuration().getTime())  || b.getDuration().isEqual(newDuration(TGDuration.EIGHTH)) )){
						TGDuration duration = newDuration(TGDuration.EIGHTH);
						duration.getTupleto().setEnters(3);
						duration.getTupleto().setTimes(2);
						bStart = ( (bStart - beat.getDuration().getTime()) + (duration.getTime() * 2));
						bDuration = duration.getTime();
					}
				}
			}
		}else if(beat.getMeasure().getTripletFeel() == TGMeasureHeader.TRIPLET_FEEL_SIXTEENTH){
			if(beat.getDuration().isEqual(newDuration(TGDuration.SIXTEENTH))){
				//first time
				if( (bStart % (TGDuration.QUARTER_TIME / 2)) == 0){
					TGBeat b = getNextBeat(beat,bIndex);
					//if(b == null || (fixStart(b.getStart()) > (bStart + beat.getDuration().getTime()) || b.getDuration().isEqual(newDuration(TGDuration.SIXTEENTH)))  ){
					if(b == null || ( b.getStart() > (bStart + beat.getDuration().getTime()) || b.getDuration().isEqual(newDuration(TGDuration.SIXTEENTH)))  ){
						TGDuration duration = newDuration(TGDuration.SIXTEENTH);
						duration.getTupleto().setEnters(3);
						duration.getTupleto().setTimes(2);
						bDuration = (duration.getTime() * 2);
					}
				}
				//second time
				else if( (bStart % (TGDuration.QUARTER_TIME / 4)) == 0){
					TGBeat b = getPreviousBeat(beat,bIndex);
					//if(b == null || (fixStart(b.getStart()) < (bStart - beat.getDuration().getTime())  || b.getDuration().isEqual(newDuration(TGDuration.SIXTEENTH)) )){
					if(b == null || ( b.getStart() < (bStart - beat.getDuration().getTime())  || b.getDuration().isEqual(newDuration(TGDuration.SIXTEENTH)) )){
						TGDuration duration = newDuration(TGDuration.SIXTEENTH);
						duration.getTupleto().setEnters(3);
						duration.getTupleto().setTimes(2);
						bStart = ( (bStart - beat.getDuration().getTime()) + (duration.getTime() * 2));
						bDuration = duration.getTime();
					}
				}
			}
		}
		return new BeatData(bStart, bDuration);
	}
	
	private TGDuration newDuration(int value){
		TGDuration duration = this.manager.getFactory().newDuration();
		duration.setValue(value);
		return duration;
	}
	
	private TGBeat getPreviousBeat(TGBeat beat,int bIndex){
		TGBeat previous = null;
		for (int b = bIndex - 1; b >= 0; b--) {
			TGBeat current = beat.getMeasure().getBeat( b );
			if(current.getStart() < beat.getStart()){
				if(previous == null || current.getStart() > previous.getStart()){
					previous = current;
				}
			}
		}
		return previous;
	}
	
	private TGBeat getNextBeat(TGBeat beat,int bIndex){
		TGBeat next = null;
		for (int b = bIndex + 1; b < beat.getMeasure().countBeats(); b++) {
			TGBeat current = beat.getMeasure().getBeat( b );
			if(current.getStart() > beat.getStart()){
				if(next == null || current.getStart() < next.getStart()){
					next = current;
				}
			}
		}
		return next;
	}
	
	private TGNote getNextNote(TGNote note,TGTrack track, int mIndex, int bIndex){ 
		int nextBIndex = (bIndex + 1);
		int measureCount = track.countMeasures();
		for (int m = mIndex; m < measureCount; m++) {
			TGMeasure measure = track.getMeasure( m );
			int beatCount = measure.countBeats();
			for (int b = nextBIndex; b < beatCount; b++) {
				TGBeat beat = measure.getBeat( b );
				int noteCount = beat.countNotes();
				for (int n = 0; n < noteCount; n++) {
					TGNote currNote = beat.getNote( n );
					if(currNote.getString() == note.getString()){
						return currNote;
					}
				}
				return null;
			}
			nextBIndex = 0;
		}
		return null;
	}
	
	private TGNote getPreviousNote(TGNote note,TGTrack track, int mIndex, int bIndex){
		int nextBIndex = bIndex;
		for (int m = mIndex; m >= 0; m--) {
			TGMeasure measure = track.getMeasure( m );
			nextBIndex = (nextBIndex < 0 ? measure.countBeats() : nextBIndex);
			for (int b = (nextBIndex - 1); b >= 0; b--) {
				TGBeat beat = measure.getBeat( b );
				for (int n = 0; n < beat.countNotes(); n ++) {
					TGNote current = beat.getNote( n );
					if(current.getString() == note.getString()){
						return current;
					}
				}
			}
			nextBIndex = -1;
		}
		return null;
	}
	/*
	public long fixStart(long value){
		return (((value % (TGDuration.QUARTER_TIME / 2)) + 10  > (TGDuration.QUARTER_TIME / 2))?(value + ((TGDuration.QUARTER_TIME / 2) - (value % (TGDuration.QUARTER_TIME / 2)))):value);
	}
	*/
	private class BeatData{
		private long start;
		private long duration;
		
		public BeatData(long start,long duration){
			this.start = start;
			this.duration = duration;
		}
		
		public long getDuration() {
			return this.duration;
		}
		
		public long getStart() {
			return this.start;
		}
	}
}