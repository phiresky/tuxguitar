package org.herac.tuxguitar.player.impl.sequencer;

import org.herac.tuxguitar.player.base.MidiPlayerException;
import org.herac.tuxguitar.player.base.MidiPort;
import org.herac.tuxguitar.player.base.MidiPortEmpty;
import org.herac.tuxguitar.player.base.MidiSequenceHandler;
import org.herac.tuxguitar.player.base.MidiSequencer;

public class MidiSequencerImpl implements MidiSequencer{
	
	private boolean reset;
	private boolean running;
	private MidiPort midiPort;
	private MidiTickPlayer midiTickPlayer;
	private MidiEventPlayer midiEventPlayer;
	private MidiEventDispacher midiEventDispacher;
	private MidiTrackController midiTrackController;
	
	public MidiSequencerImpl(){
		this.running = false;
		this.midiTickPlayer = new MidiTickPlayer();
		this.midiEventPlayer = new MidiEventPlayer(this);
		this.midiEventDispacher = new MidiEventDispacher(this);
		this.midiTrackController = new MidiTrackController(this);
	}
	
	public synchronized MidiTrackController getMidiTrackController(){
		return this.midiTrackController;
	}
	
	public synchronized void setTempo(int tempo){
		this.midiTickPlayer.setTempo(tempo);
	}
	
	public synchronized long getTickPosition(){
		return this.midiTickPlayer.getTick();
	}
	
	public synchronized void setTickPosition(long tickPosition){
		this.reset = true;
		this.midiTickPlayer.setTick(tickPosition);
	}
	
	public synchronized long getTickLength(){
		return this.midiTickPlayer.getTickLength();
	}
	
	public synchronized void sendEvent(MidiEvent event) throws MidiPlayerException{
		if(!this.reset){
			this.midiEventDispacher.dispatch(event);
		}
	}
	
	public synchronized void addEvent(MidiEvent event){
		this.midiEventPlayer.addEvent(event);
		this.midiTickPlayer.notifyTick(event.getTick());
	}
	
	public synchronized boolean isRunning() {
		return this.running;
	}
	
	public synchronized void setRunning(boolean running) {
		this.running = running;
		if(this.running){
			this.setTempo(120);
			this.setTickPosition( this.getTickPosition() );
			new MidiTimer(this).start();
		}
	}
	
	public synchronized void stop() throws MidiPlayerException{
		this.setRunning(false);
	}
	
	public synchronized void start() throws MidiPlayerException{
		this.setRunning(true);
	}
	
	public synchronized void reset(boolean systemReset)  throws MidiPlayerException{
		this.getMidiPort().out().sendAllNotesOff();
		for(int channel = 0; channel < 16;channel ++){
			this.getMidiPort().out().sendPitchBend(channel, 64);
		}
		if( systemReset ){
			this.getMidiPort().out().sendSystemReset();
		}
	}
	
	protected synchronized boolean process() throws MidiPlayerException{
		boolean running = this.isRunning();
		if(running){
			if(this.reset){
				this.reset( false );
				this.reset = false;
				this.midiEventPlayer.reset();
			}
			this.midiTickPlayer.process();
			this.midiEventPlayer.process();
			if(this.getTickPosition() > this.getTickLength()){
				this.stop();
			}
		}
		else{
			this.midiEventPlayer.clearEvents();
			this.midiTickPlayer.clearTick();
			this.reset( true );
		}
		return running;
	}
	
	public synchronized MidiPort getMidiPort() {
		if(this.midiPort == null){
			this.midiPort = new MidiPortEmpty();
		}
		return this.midiPort;
	}
	
	public synchronized void setMidiPort(MidiPort midiPort) {
		this.midiPort = midiPort;
	}
	
	public synchronized void open() {
		//not implemented
	}
	
	public synchronized void close() throws MidiPlayerException {
		if(isRunning()){
			this.stop();
		}
	}
	
	public synchronized MidiSequenceHandler createSequence(int tracks){
		return new MidiSequenceHandlerImpl(this,tracks);
	}
	
	public synchronized void setSolo(int index,boolean solo) throws MidiPlayerException{
		this.getMidiTrackController().setSolo(index, solo);
	}
	
	public synchronized void setMute(int index,boolean mute) throws MidiPlayerException{
		this.getMidiTrackController().setMute(index, mute);
	}
	
	public String getKey() {
		return "tuxguitar.sequencer";
	}
	
	public String getName() {
		return "TuxGuitar Sequencer";
	}
	
	private class MidiTimer extends Thread{
		
		private static final int TIMER_DELAY = 15;
		
		private MidiSequencerImpl sequencer;
		
		public MidiTimer(MidiSequencerImpl sequencer){
			this.sequencer = sequencer;
		}
		
		public void run() {
			try {
				synchronized(this.sequencer) {
					while( this.sequencer.process() ){
						this.sequencer.wait( TIMER_DELAY );
					}
				}
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}
	}
}
