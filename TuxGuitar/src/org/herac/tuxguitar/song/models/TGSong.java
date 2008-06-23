/*
 * Created on 23-nov-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.herac.tuxguitar.song.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.herac.tuxguitar.song.factory.TGFactory;

/**
 * @author julian
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class TGSong {
	
	private String name;
	private String artist;
	private String album;
	private String author;
	private List tracks;
	private List measureHeaders;
	
	public TGSong() {
		this.name = new String();
		this.artist = new String();
		this.album = new String();
		this.author = new String();
		this.tracks = new ArrayList();
		this.measureHeaders = new ArrayList();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAlbum() {
		return this.album;
	}
	
	public void setAlbum(String album) {
		this.album = album;
	}
	
	public String getAuthor() {
		return this.author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getArtist() {
		return this.artist;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	public int countMeasureHeaders(){
		return this.measureHeaders.size();
	}
	
	public void addMeasureHeader(TGMeasureHeader measureHeader){
		this.addMeasureHeader(countMeasureHeaders(),measureHeader);
	}
	
	public void addMeasureHeader(int index,TGMeasureHeader measureHeader){
		measureHeader.setSong(this);
		this.measureHeaders.add(index,measureHeader);
	}
	
	public void removeMeasureHeader(int index){
		this.measureHeaders.remove(index);
	}
	
	public void removeMeasureHeader(TGMeasureHeader measureHeader){
		this.measureHeaders.remove(measureHeader);
	}
	
	public TGMeasureHeader getMeasureHeader(int index){
		return (TGMeasureHeader)this.measureHeaders.get(index);
	}
	
	public Iterator getMeasureHeaders() {
		return this.measureHeaders.iterator();
	}
	
	public int countTracks(){
		return this.tracks.size();
	}
	
	public void addTrack(TGTrack track){
		this.addTrack(countTracks(),track);
	}
	
	public void addTrack(int index,TGTrack track){
		track.setSong(this);
		this.tracks.add(index,track);
	}
	
	public void moveTrack(int index,TGTrack track){
		this.tracks.remove(track);
		this.tracks.add(index,track);
	}
	
	public void removeTrack(TGTrack track){
		this.tracks.remove(track);
		track.clear();
	}
	
	public TGTrack getTrack(int index){
		return (TGTrack)this.tracks.get(index);
	}
	
	public Iterator getTracks() {
		return this.tracks.iterator();
	}
	
	public boolean isEmpty(){
		return (countMeasureHeaders() == 0 || countTracks() == 0);
	}
	
	public void clear(){
		Iterator tracks = getTracks();
		while(tracks.hasNext()){
			TGTrack track = (TGTrack)tracks.next();
			track.clear();
		}
	}
	
	public TGSong clone(TGFactory factory){
		TGSong song = factory.newSong();
		copy(factory,song);
		return song;
	}
	
	public void copy(TGFactory factory,TGSong song){
		song.setName(getName());
		song.setArtist(getArtist());
		song.setAlbum(getAlbum());
		song.setAuthor(getAuthor());
		Iterator headers = getMeasureHeaders();
		while(headers.hasNext()){
			TGMeasureHeader header = (TGMeasureHeader)headers.next();
			song.addMeasureHeader(header.clone(factory));
		}
		Iterator tracks = getTracks();
		while(tracks.hasNext()){
			TGTrack track = (TGTrack)tracks.next();
			song.addTrack(track.clone(factory, song));
		}
	}
}
