package org.herac.tuxguitar.player.impl.midiport.alsa;

import org.herac.tuxguitar.player.base.MidiControllers;
import org.herac.tuxguitar.player.base.MidiOut;

public class MidiOutImpl implements MidiOut{
	
	private boolean connected;
	private MidiPortImpl midiPort;
	private MidiSystem midiSystem;
	
	public MidiOutImpl(MidiPortImpl midiPort, MidiSystem midiSystem){
		this.midiPort = midiPort;
		this.midiSystem = midiSystem;
		this.connected = false;
	}
	
	public boolean isConnected(){
		return this.connected;
	}
	
	public void connect(){
		if(!isConnected()){
			this.midiSystem.openPort(this.midiPort.getClient(),this.midiPort.getPort());
			this.connected = true;
		}
	}
	
	public void disconnect() {
		if(isConnected()){
			this.midiSystem.closePort();
			this.connected = false;
		}
	}
	
	public void sendAllNotesOff() {
		for(int i = 0; i < 16; i ++){
			this.sendControlChange(i,MidiControllers.ALL_NOTES_OFF,0);
		}
	}
	
	public void sendControlChange(int channel, int controller, int value) {
		if(isConnected()){
			this.midiSystem.controlChange(channel, controller, value);
		}
	}
	
	public void sendNoteOff(int channel, int key, int velocity) {
		if(isConnected()){
			this.midiSystem.noteOff(channel, key, velocity);
		}
	}
	
	public void sendNoteOn(int channel, int key, int velocity) {
		if(isConnected()){
			this.midiSystem.noteOn(channel, key, velocity);
		}
	}
	
	public void sendPitchBend(int channel, int value) {
		if(isConnected()){
			this.midiSystem.pitchBend(channel, value);
		}
	}
	
	public void sendProgramChange(int channel, int value) {
		if(isConnected()){
			this.midiSystem.programChange(channel, value);
		}
	}
	
	public void sendSystemReset() {
		//not implemented
	}
}
