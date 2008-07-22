package org.herac.tuxguitar.player.impl.midiport.coreaudio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.herac.tuxguitar.player.base.MidiControllers;
import org.herac.tuxguitar.player.base.MidiOut;
import org.herac.tuxguitar.player.impl.midiport.coreaudio.MidiPortImpl;

public class MidiOutImpl extends MidiReceiverJNI implements MidiOut{
	private boolean open; // unncessary
    private boolean connected;	
	private List ports;
	
	public MidiOutImpl(){
		this.ports = new ArrayList();	
        this.connected = false;
	}

	public void open(){
		super.open();
		this.open = true;
	}

	public void close(){
		if(this.isOpen()){
			this.disconnect();
			super.close();
			this.open = false;
		}
	}	
		
	public boolean isOpen(){
		return (this.open);
	}	
			
	public boolean isConnected(){
		return (this.isOpen() && this.connected);
	}	
			
    public void connect(){
        if(isOpen()){
            if(!isConnected()){             
                this.connected = true;
                this.openDevice();
            }
        }
    }

	public void disconnect() {
		if(isConnected()){
			this.closeDevice();
			this.connected = false;
		}
	}		
		
	public List listPorts(){
		if(isOpen()){
			this.ports.clear();
			this.ports.add(new MidiPortImpl(this, "Core Audio midi playback" , "coreaudio" ));
			return this.ports;
		}
		return Collections.EMPTY_LIST;
	}		

	public void sendSystemReset() {
		if(isOpen()){
			//not implemented
		}
	}
	
	public void sendAllNotesOff() {

		for(int i = 0; i < 16; i ++){
			sendControlChange(i,MidiControllers.ALL_NOTES_OFF,0);		
		}	
/*
		for(int i = 0; i < 16; i ++){
			sendControlChange(i,120 ,0);		
		}	
 */
	}

	public void sendControlChange(int channel, int controller, int value) {
		if(isOpen()){
			super.controlChange(channel, controller, value);
		}
	}

	public void sendNoteOff(int channel, int key, int velocity) {
		if(isOpen()){
			super.noteOff(channel, key, velocity);
		}
	}

	public void sendNoteOn(int channel, int key, int velocity) {
		if(isOpen()){
			super.noteOn(channel, key, velocity);
		}
	}

	public void sendPitchBend(int channel, int value) {
		if(isOpen()){
			super.pitchBend(channel, value);
		}
	}

	public void sendProgramChange(int channel, int value) {
		if(isOpen()){
			super.programChange(channel, value);
		}
	}
}
