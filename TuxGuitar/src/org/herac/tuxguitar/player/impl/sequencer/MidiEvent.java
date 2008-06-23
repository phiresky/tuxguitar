package org.herac.tuxguitar.player.impl.sequencer;

public class MidiEvent {
	
	public static final int ALL_TRACKS = -1;
	
	public static final int MIDI_SYSTEM_EVENT = 1;
	public static final int MIDI_EVENT_NOTEON = 2;
	public static final int MIDI_EVENT_NOTEOFF = 3;
	public static final int MIDI_EVENT_PROGRAM_CHANGE = 4;
	public static final int MIDI_EVENT_CONTROL_CHANGE = 5;
	public static final int MIDI_EVENT_PITCH_BEND = 6;
	
	private long tick;
	private int type;
	private int track;
	private byte[] data;
	
	public MidiEvent(long tick,int type,byte[] data){
		this(tick,type,ALL_TRACKS,data);
	}
	
	public MidiEvent(long tick,int type,int track,byte[] data){
		this.tick = tick;
		this.type = type;
		this.track = track;
		this.data = data;
	}
	
	public long getTick() {
		return this.tick;
	}
	
	public int getType() {
		return this.type;
	}
	
	public int getTrack() {
		return this.track;
	}
	
	public byte[] getData() {
		return this.data;
	}
	
	public static MidiEvent systemReset(final long tick){
		return new MidiEvent(tick,MIDI_SYSTEM_EVENT,ALL_TRACKS,null);
	}
	
	public static MidiEvent noteOn(final long tick,final int track,final int channel,final int key,final int velocity){
		return new MidiEvent(tick,MIDI_EVENT_NOTEON,track,new byte[]{(byte)channel,(byte)key,(byte)velocity});
	}
	
	public static MidiEvent noteOff(final long tick,final int track,final int channel,final int key,final int velocity){
		return new MidiEvent(tick,MIDI_EVENT_NOTEOFF,track,new byte[]{(byte)channel,(byte)key,(byte)velocity});
	}
	
	public static MidiEvent controlChange(final long tick,final int track,final int channel,final int controller,final int value){
		return new MidiEvent(tick,MIDI_EVENT_CONTROL_CHANGE,track,new byte[]{(byte)channel,(byte)controller,(byte)value});
	}
	
	public static MidiEvent programChange(final long tick,final int track,final int channel,final int value){
		return new MidiEvent(tick,MIDI_EVENT_PROGRAM_CHANGE,track,new byte[]{(byte)channel,(byte)value});
	}
	
	public static MidiEvent pitchBend(final long tick,final int track,final int channel,final int value){
		return new MidiEvent(tick,MIDI_EVENT_PITCH_BEND,track,new byte[]{(byte)channel,(byte)value});
	}
	
	public static MidiEvent tempoInUSQ(final long tick,final int usq){
		return new MidiEvent(tick,MIDI_SYSTEM_EVENT,new byte[]{0x51,(byte) (usq & 0xff),(byte) ((usq >> 8) & 0xff),(byte) ((usq >> 16) & 0xff)});
	}
}
