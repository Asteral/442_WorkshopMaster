/*
  A Processing MOD/S3M/XM replayer library.
  
  PortaMod 0.1 (c) 2009 Brendan Ratliff ## echolevel@gmail.com ## http://crayolon.net
  
  ibxm alpha 51 (c)2008 mumart@gmail.com
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package crayolon.portamod;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.*;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;


import crayolon.portamod.CellContent;
import crayolon.portamod.Channel;
import crayolon.portamod.CurrentPattern;
import crayolon.portamod.DataCell;
import crayolon.portamod.FastTracker2;
import crayolon.portamod.IBXM;
import crayolon.portamod.NoteData;
import crayolon.portamod.Player;
import crayolon.portamod.ProTracker;
import crayolon.portamod.ScreamTracker3;

import java.lang.reflect.Method;

import processing.core.PApplet;


/**
 * PortaMod - a MOD/XM/S3M replayer library for Processing by Brendan Ratliff (aka Crayolon, aka Syphus of UpRough)
 * 
 * Based on IBXM by Martin Cameron (� 2008)
 * 
 * Fundamentally, PortaMod is a Processing implementation of IBXM so that the Processing community can benefit from 
 * the advantages that oldschool 'tracker' formats offer. 
 * 
 * Compared to WAV/MP3/OGG, these advantages include small file-size,
 * the ability to synchronise visuals and other triggered events to events in the music (e.g. synching a screenflash to a
 * particular note, at a particular time and volume) and the possibility for allowing extensive user interaction with the
 * music itself.
 * 
 * Compared to MIDI, tracked formats use instruments based on samples which sound the same on any replayer system, rather 
 * than being dependent on varying MIDI-synths. Though oldschool 4-channel chiptunes can be as small as 3 kilobytes, complex and high-
 * quality music can be arranged in an XM of up to 32 channels, often matching mp3 quality but with a reduced filesize.
 * 
 * PortaMod builds quite heavily on IBXM in the control features it offers, while retaining IBXM's efficiency and reliable
 * handling of these formats and their idiosyncrasies. Its diverse range of methods is intended to give the Processing community
 * a very granular relationship with tracked audio data, which can either be found amongst the gigabytes of free modules available
 * online or created with trackers such as ProTracker (Amiga), FastTracker (MS-DOS), ScreamTracker (MS-DOS), or the modern-day
 * and very cross-platform MilkyTracker - www.milkytracker.org
 * 
 * Feedback always appreciated - echolevel@gmail.com
 * 
 * Special thanks to Peter Quayle (parapete), Paul Carpenter (Vampire^TZT) and Anders Carlsson (goto80), for help and bugtesting; to 
 * Joey Scully (dataprole) and Jamie Allen for advice; to Martin Cameron (of course!) for IBXM :D
 * 
 *  
 * @author Brendan Ratliff 
 * 
 */
public class PortaMod {

	PApplet p;

	GZIPInputStream zipin;
	Method noteArrived;
	Method noteArrivedB;
	String modpath = "";
	boolean keydown = false;
	boolean overridetempo = false;
	public String filepath;
	public int sequencecounter = 0;
	public int interpolation = 0;
	int[][][] current_row_notes;
	int currentrowcount;
	CurrentPattern temppattern;
	int pausedseq, pausedrow, specheight, tempovalue, seconds, buffersize; //seconds: progress of main tune in seconds
	int pausedpos = 0; //contains the progress, in number-of-samples, at which we paused
	int playpos = 0; //we'll use this in case we need to change the play-position...
	int volvalue = 127;
	public int globvol = 64;
	public boolean paused = false;
	int posvalue = 0;
	public int loadSuccess = 0;
	int modseparation;
	int s3mpanning;
	public byte[] mod_header;
	public byte[] localmod_header;
	boolean fastforwardrow = false;
	boolean rewindrow = false;
	//boolean[] channeldelayswitch = new boolean[31];
	public int delayedchannel = -1;
	public boolean looping = false;
	public int transpose = 0;
	float volumelocal = 0.f;
	int channels = 0;
	int currentsong = 0;
	int totalrows;
	int endcount = 0;
	float spectrumBars = 32f;
	float loopcount;
	public float minVol = -39.9f;
	public float maxVol = 6.019f;
	public boolean playing = false;
	public boolean muted = false; 
	public long songStart, songLength; //songStart in milliseconds, songLength in seconds
	public long songPosition;
	String tunepath, title;
	public int numchannels = 0;
	public int numinstruments = 0;
	public int bpmvalue = 0;
	float panning = 0;
	public int initialtempo;
	public int numpatterns = 0;
	public String infotext[];
	FileInputStream file_input_stream;
	public FloatControl volCtrl;
	public Player player;
	public Player delayplayerA;
	InputStream mymod;
	DataCell[] cells;
	BooleanControl muteCtrl;
	public NoteData localnotes;
	public NoteData thisrow;
	ArrayList<CurrentPattern> positions = new ArrayList(); //this will be cleared and filled every pattern
	public ArrayList<CurrentPattern>[] currentpatternrows;
	public ArrayList<Instrument> oldsamples = new ArrayList();
	public ArrayList<CustomNote> delayednotes = new ArrayList();
	public CustomNote[] delaynotes;
	public int chantranspose[];
	public int origloopstart[];
	public int origlooplength[];
	public int loopstart[];
	public int looplength[];
	public int sampledatalength[];
	public CellContent[] cellcontent;
	public CellContent[] content;
	public String modtype;
	public int jamcounter = 0;
	int[] jamnote;
	int[] jaminst;
	
	public final String VERSION = "0.2.0";

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example portaTest
	 * @param p
	 */
	public PortaMod(PApplet p) {
		this.p = p;
		IBXM.SetMC(this);
	    try {
	        noteArrived = p.getClass().getMethod("grabNewdata", new Class[] { PortaMod.class });
	    } catch (Exception e) {
	        PApplet.println(e.getMessage());
	    }
	    try {
	        noteArrivedB = p.getClass().getMethod("grabNewdataB", new Class[] { PortaMod.class });
	    } catch (Exception e) {
	        PApplet.println(e.getMessage());
	    }

	}
	
	private void makeEvent(IBXM instance) {
		if (loadSuccess > 0) {
			if (noteArrived != null && instance == this.player.ibxm) {
				try {
					noteArrived.invoke(p, new Object[] { this });
				} catch (Exception e) {
					System.err.println("Disabling noteArrived for "
							+ " because of an error.");
					e.printStackTrace();
					noteArrived = null;
				}
			}
			if (noteArrivedB != null && instance == this.player.ibxm) {
				try {
					noteArrivedB.invoke(p, new Object[] { this });
				} catch (Exception e) {
					System.err.println("Disabling noteArrivedB for "
							+ " because of an error.");
					e.printStackTrace();
					noteArrived = null;
				}
			}
		}
	}
 

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public String version() {
		return VERSION;
	}
	
	/**
	 * getTitle() returns a string containing the module's title.
	 * @return
	 */
	public String getTitle() {
		String temptitle = new String();
		if (loadSuccess > 0) {
			temptitle = title;
		}
		return temptitle;
	}
	
	/**
	 * 	Start playback, or resume playback after pause
	 * 
	 */
	public void play() {
		if (paused) {
			this.player.play();
			paused = false;
		} else {
			this.player.play();
			songStart = p.millis();
		}
	}
	
	/**
	 * 	Pause playback
	 * 
	 */
	public void pause() {
		if (!paused) {
			this.player.stop();
			paused = true;
		}
	}
	
	/**
	 * getVol() - returns the current overall volume as a float of between -40.0f and 6.020f
	 */
	public float getVol(){
		return volCtrl.getValue();
	}
	
	/**
	 * 	setVol(float) adjusts overall volume. Checks for min/max limits to avoid crashes.
	 * 
	 */
	public void setVol(float newvol){
		if (newvol > -40.0f && newvol < 6.020f) {
			try {
				volCtrl.setValue(newvol);
			} catch (Exception e) {
				PApplet.println(e.getMessage());
			}
		} else {
			PApplet.println("Error - volume '" + newvol + "' is out of range. Must be between -40.0f and 6.020f!");
		}
	}
	
	/**
	 * 	Mute audio (playback continues)
	 * 
	 */
	public void mute() {
		if (!muted) {
			try {
				muteCtrl.setValue(true);
				muted = true;
			} catch (Exception e) {
				PApplet.println(e.getMessage());
			}
		} else {
			try {
				muteCtrl.setValue(false);
				muted = false;
			} catch (Exception e) {
				PApplet.println(e.getMessage());
			}

		}
	}
	
	/**
	 * 	setTranspose(int, int) shifts playback key up or down by 12 semitones, while retaining tempo. If chan is set to -1, all channels are transposed.
	 * 
	 */
	public void setTranspose(int chan, int trans) {
		if (chan < 0) {
			if (trans >= -12 && trans <= 12) {

				for (int i=0; i<player.get_num_channels(); i++) {
					player.ibxm.channels[i].transposer = trans;
					
				}
				transpose = trans;
			}
		} else {
			if (trans >= -12 && trans <= 12) {
					player.ibxm.channels[chan].transposer = trans;
					
					chantranspose[chan] = trans;
			}
		}
	}
	
	/**
	 * getTempo() returns an int of between 32 and 255 indicating the playing module's current BPM tempo. 
	 */
	public int getTempo() {
		return bpmvalue;
	}
	
	/**
	 * setTempo(int) adjusts tempo in beats-per-minute when given an int value between 32 and 255
	 * 
	 */
	public void setTempo(int tempo) {
		if (tempo > 32 && tempo < 255) {
			player.set_tempo(tempo);
			bpmvalue = tempo;
		}
	}
	
	/**
	 * getPanning() returns the current panning value for this instance as a flot of between -1.0f and 1.0f, where 0 is the centre-position.
	 */
	public float getPanning(){
		return panning;
	}
	
	/**
	 *  setPanning(float) takes a float from -1.0f to 1.0f to pan the overall audio output across the stereo spectrum. The centre-point is 0.
	 * 
	 */
	public void setPanning(float panval){
		if (panval > -1.0f && panval < 1.0f) {
			try {
				FloatControl panctrl = (FloatControl)player.output_line.getControl(FloatControl.Type.BALANCE);
				panctrl.setValue(panval);
				panning = panval;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
	}
	
	/**
	 * getChanmute(int) returns the mute status of a channel as a boolean - true for muted, false for unmuted.
	 */
	public boolean getChanmute(int chan){
		return player.ibxm.chanmute[chan];
	}
	
	/**
	 * setChanmute(int, boolean) takes a channel-number as an int and and a boolean to mute or unmute that channel. Check the mute status of the channel first with getChanmute(channel). 
	 * 
	 */
	public void setChanmute(int chan, boolean flip) {
		if (chan <player.get_num_channels()) {
			if (flip) {
				player.ibxm.chanmute[chan] = true;
			} else {
				player.ibxm.chanmute[chan] = false;
			}
		}		
	}
	
	/**
	 * loopSong() switches song-looping on or off. 
	 * mymod.looping shows the current status.
	 */
	public void setSongloop(boolean flip) {
		player.loop = flip;
		System.out.println("Song looping is " + flip);
	}
	
	/**
	 * getChanvol(int) takes a channel as its argument and returns that channel's current volume as an int between 0 and 64.
	 * This is the final volume given to the mixer engine after volume command/tremolo/envelope/global levels have been calculated. 
	 * 
	 * @param chan
	 * @return int
	 */
	public int getChanvol(int chan) {
		int chanvol = player.ibxm.channels[chan].chanvolfinal;
		//return (int)p.map(chanvol, 0, 8064, 0, 64);
		return (int)p.map(chanvol, 0, 12288, 0, 64);
	}
	
	/**
	 *  setChanvol(int, int) takes the channel number and the desired channel volume - it will override the channel's mixing volume, possibly with some unexpected
	 *  consequences (such as more 'clipped'-sounding volume slides), and may be overridden by some varieties of volume change command in the module's patterndata. 
	 * 
	 * 
	 */
	public void setChanvol(int chan, int vol){
		player.ibxm.channels[chan].chanvol_override = vol;
	}
	
	/**
	 * setGlobvol(int) 
	 * 
	 */
	public void setGlobvol(int vol){
		if (vol >= 0 && vol <= 64) {
			/*for (int i=0; i < numchannels; i++){
				globvol = vol;
				player.ibxm.channels[i].globvol_override = vol;
			}*/
			globvol = vol;
			player.ibxm.global_volume[0] = vol;
		} else {
			System.out.println("Global volume must be between 0 and 64");
		}
	}
	
	/**
	 *  getGlobvol()
	 */
	public int getGlobvol() {
		return globvol;
	}
	
	
	/**
	 * setStereosep(int) takes a percentage for desired MOD stereo spread. 100 gives full Amiga hard-panning while 0 centres all channels. This only works on
	 * 4 or 8 channel files that are verified as being MODs. Panning arrangement is 1:L 2:R 3:R 4:L(5:L 6:R 7:R 8:L).
	 * Just to be clear, this has no effect on XM or S3M files! 
	 * 
	 */
	public void setStereosep(int percentage) {
		int spread = (int)PApplet.map(percentage, 0, 100, 0, 128);
		if (modtype == "MOD") {
			player.ibxm.channels[0].set_panning(128-spread);
			player.ibxm.channels[1].set_panning(128+spread);
			player.ibxm.channels[2].set_panning(128+spread);
			player.ibxm.channels[3].set_panning(128-spread);
			if (player.get_num_channels() > 4){
				player.ibxm.channels[4].set_panning(128-spread);
				player.ibxm.channels[5].set_panning(128+spread);
				player.ibxm.channels[6].set_panning(128+spread);
				player.ibxm.channels[7].set_panning(128-spread);
			}


		}
	}
	
	/**
	 * setInterpolation(int) sets the mixing interpolation to 'none' (0), 'linear' (1) or 'sinc' (2). Default is 0.
	 * @param interp
	 */
	public void setInterpolation(int interp) {
		interpolation = interp;
	}
	
	/**
	 * getSeek() returns the current position of the song in milliseconds as an int.
	 * @return songSeek
	 */
	public int getSeek() {
		if (playing) {
			try {
				songPosition = player.play_position/48;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return (int)songPosition;
		} else {
			System.out.println("Couldn't get song position");
			return 0;
		}
	}
	
	/**
	 * setSeek(int) sets the current position of the song in milliseconds, from an int parameter.
	 * 
	 */
	public void setSeek(int newpos) {
		player.seek(newpos*48);
	}
	
	
	/**
	 * setOverridetempo() switches tempo override on or off. Many modules have tempo commands scattered throughout which can reset the BPM - sometimes it's desirable
	 * to avoid this when trying to run an entire module at a custom BPM. Use this in conjunction with bpm() 
	 */
	public void setOverridetempo(boolean flip) {
		
		player.ibxm.overridetempo = flip;
		System.out.println("Tempo override is " + flip);		
	}
	
	
	/** sampleSwap(int, byte[]) takes a MOD sample number and the path to a new sample, then replaces the extant sampledata with the new.
	 *  Expect this to be highly buggy and prone to exceptions - seems safe with samples below 25kb, as long as they're RAW or IFF and 8bit/mono/22050
	 * 
	 * @param idx
	 * @param data_input
	 */
	public void sampleSwap(int idx, String inputpath, int offset) {
		

		
		byte[] sampbytes = p.loadBytes(inputpath);
		Instrument tempinst;
		try {
			tempinst = this.replace_instrument(sampbytes, idx, sampbytes, offset);
			this.set_instrument(idx, tempinst);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * sampleRestore(int) takes the index of a sample you've replaced and restores the original sample.
	 *  
	 * @param idx
	 */
	public void sampleRestore(int idx) {
		Instrument tempinst_old;
		try {
			if (oldsamples.get(idx-1) != null){


				tempinst_old = (Instrument)oldsamples.get(idx-1);


				this.set_instrument(idx, tempinst_old);
			}else {
				System.out.println("Sample hasn't been replaced, so can't be restored!");
			}
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Sample hasn't been replaced, so can't be restored!");
		}

	}
	
	
	
	/**
	 * Load the MOD/XM/S3M module by filepath string. If the second parameter is false, the module will not automatically start to play. The third parameter is 
	 * the starting volume as a float value between -40.0f and 6.020f. Start at the bottom if you want to fade a module in.
	 * 
	 * @return int
	 */
	public int doModLoad(String tune, boolean autostart, int startVol) {
		filepath = tune;
		modpath = tune;
		headerCheck(tune);
		PApplet.println("Header checked: "+ modtype);
		
		if (loadSuccess > 0){
			loadSuccess = 0;
		}
		
		InputStream file_input_stream = p.createInput(tune);

			try {
				if (playing == true) {
					player.stop();
					PApplet.println("stopped");
			}
			player = new Player(interpolation);

			player.set_module(Player.load_module(file_input_stream));
			file_input_stream.close();
			player.set_loop(true);
			player.receivebuffer(buffersize);
			if (autostart) {
				player.play();
				songStart = p.millis();				
			}
			this.setGlobvol(startVol);
			PApplet.println(player.get_title());
			PApplet.println(player.song_duration);

			songLength = player.song_duration/48000;
			infotext = new String[player.get_num_instruments()];
			
			for (int i = 0; i < (player.get_num_instruments()); i++) {
				infotext[i] = "";
				//store copies of all the instruments for changeSample 
				Instrument tempinst_old = player.module.instruments[i];
				oldsamples.add(i, tempinst_old);
				if (player.ins_name(i) != null) {
					PApplet.println(player.ins_name(i));
					infotext[i] = player.ins_name(i);
				}
				

			}
			
			chantranspose = new int[player.get_num_channels()];
			for (int c=0;c<player.get_num_channels();c++){
				chantranspose[c] = 0;
			}
			loopstart = new int[player.get_num_instruments()];
			looplength = new int[player.get_num_instruments()];
			origloopstart = new int[player.get_num_instruments()];
			origlooplength = new int[player.get_num_instruments()];
			sampledatalength = new int[player.get_num_instruments()];
			for (int ins = 0; ins < player.get_num_instruments(); ins++) {
				//initialise loopinfo arrays
				origloopstart[ins] = 0;
				origlooplength[ins] = 0;
				sampledatalength[ins] = player.module.instruments[ins].samples[0].sample_data_length;
				//store initial loop info for loopReset
				//System.out.println("Orig start: " + player.module.instruments[ins].samples[0].loop_start);
				//System.out.println("Orig length: " +player.module.instruments[ins].samples[0].loop_length);
				origloopstart[ins] = player.module.instruments[ins].samples[0].loop_start;
				origlooplength[ins] = player.module.instruments[ins].samples[0].loop_length;
			}
			
			PApplet.println("Channels:" + player.get_num_channels());
			//PApplet.println(player.output_line.getControls());

			try {
				//volCtrl = (FloatControl) player.output_line.getControl(FloatControl.Type.MASTER_GAIN);
				//volCtrl.setValue(startVol);
			} catch (Exception e) {
				PApplet.println("Mystery javasound failure lucky dip! This week's prize: " + e.getMessage());
			}

			try {
				muteCtrl = (BooleanControl) player.output_line.getControl(BooleanControl.Type.MUTE);
			} catch (Exception e) {
				PApplet.println(e.getMessage());
			}
/*			for (int i=0; i < numchannels; i++){
				globvol = startVol;
				try {
					player.ibxm.channels[i].chanvol_override = startVol;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
		
		title = player.get_title();
		numchannels = player.get_num_channels();
		jamnote = new int[player.get_num_channels()];
		jaminst = new int[player.get_num_channels()];
		for (int c=0; c<player.get_num_channels();c++ ) {
			jamnote[c] = 0;
			jaminst[c] = 0;			
		}
		numinstruments = player.get_num_instruments();
		numpatterns = player.ibxm.module.get_sequence_length();
		playing = true;
		loadSuccess = 1;
		initialtempo = player.get_bpm();
		bpmvalue = player.get_bpm();
		currentrowcount = player.ibxm.total_rows;
		endcount = 0;
//		if (player.get_num_channels() > 0) {
//			sequencecounter = 0;
//			refreshpattern();
//			displayCurrentpattern();
//		}
	}
		catch (Exception e) {
			PApplet.println(e.getMessage());
			PApplet.println("Printing stack trace... ");
			e.printStackTrace();
			
		}
		return loadSuccess;		
	}
	
	
	// This is fucking stupid as hell; if this method makes it into a release, somebody shoot me in the head.
	public void delayTap1(int channel, int volume) {

		try {
			InputStream file_input_stream = p.createInput(modpath);
				delayplayerA = new Player(interpolation);

				delayplayerA.set_module(Player.load_module(file_input_stream));
				file_input_stream.close();
				delayplayerA.set_loop(true);
				delayplayerA.receivebuffer(buffersize);
				
				for (int i=0; i < delayplayerA.get_num_channels(); i++) {
					if (i != channel) {
						delayplayerA.ibxm.chanmute[i] = true;
					}
				}
				delayplayerA.ibxm.channels[channel].chanvol_override = volume;
				
				delayplayerA.play();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Callback from the replayer engine. This fires an event notifier so that the contents of the current NoteData object at any given time
	 * can be accessed from a sketch. One suggested use is to set a boolean switch when some notedata fulfills a condition so that, for instance,
	 * a visual effect in the sketch can be triggered in sync. 
	 * 
	 */
	public void doIt(IBXM instance, NoteData notedata) {
		localnotes = notedata;
		try {
			makeEvent(instance);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sequencecounter = localnotes.currentseq;
//		if(localnotes.channel == player.get_num_channels()-1 && localnotes.currentseq == player.get_sequence_length()-1 && (localnotes.currentrow/player.get_num_channels()) == localnotes.seqlength-1){
//			endcount +=1;
//			songStart = p.millis();
//		}
		
 		
		

		if(delayedchannel >= 0) {
			if(localnotes.channel == 0) {
				//delaykeyDown(localnotes.note, localnotes.inst, localnotes.vol, localnotes.effect,localnotes.effparam);
			}
		}

		
		
			
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	ArrayList<CurrentPattern>[] refreshpattern() {
		currentpatternrows = player.ibxm.all_rows();
		return currentpatternrows;
	}
	
	void displayCurrentpattern(){
		for (int thisrow=0; thisrow < currentpatternrows[0].size(); thisrow++){
			for (int channelcount = 0; channelcount < player.get_num_channels(); channelcount++) {
				try {
					CurrentPattern tempdata = (CurrentPattern)currentpatternrows[channelcount].get(thisrow); //channelcount works, thisrow is often out of bounds
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}

	
	
	/**
	 * Returns a formatted tracker-style representation of a given decimal note along with its octave and either an accidental or a hyphen, e.g. D#3 or C-2
	 * 
	 * @return String
	*/
	public String noteConvert(int noteval) {
		String note = "";
		switch(noteval) {
		case 0:
			//no note should be 0...
			note = "- -";
			break;
		case 1:
			note = "C-0";
			break;
		case 2:
			note = "C#0";
			break;
		case 3:
			note = "D-0";
			break;
		case 4:
			note = "D#0";
			break;
		case 5:
			note = "E-0";
			break;
		case 6:
			note = "F-0";
			break;
		case 7:
			note = "F#0";
			break;
		case 8:
			note = "G-0";
			break;
		case 9:
			note = "G#0";
			break;
		case 10:
			note = "A-0";
			break;
		case 11:
			note = "A#0";
			break;
		case 12:
			note = "B-0";
			break;
		case 13:
			note = "C-1";
			break;
		case 14:
			note = "C#1";
			break;
		case 15:
			note = "D-1";
			break;
		case 16:
			note = "D#1";
			break;
		case 17:
			note = "E-1";
			break;
		case 18:
			note = "F-1";
			break;
		case 19:
			note = "F#1";
			break;
		case 20:
			note = "G-1";
			break;
		case 21:
			note = "G#1";
			break;
		case 22:
			note = "A-1";
			break;
		case 23:
			note = "A#1";
			break;
		case 24:
			note = "B-1";
			break;
		case 25:
			note = "C-2";
			break;
		case 26:
			note = "C#2";
			break;
		case 27:
			note = "D-2";
			break;
		case 28:
			note = "D#2";
			break;
		case 29:
			note = "E-2";
			break;
		case 30:
			note = "F-2";
			break;
		case 31:
			note = "F#2";
			break;
		case 32:
			note = "G-2";
			break;
		case 33:
			note = "G#2";
			break;
		case 34:
			note = "A-2";
			break;
		case 35:
			note = "A#2";
			break;
		case 36:
			note = "B-2";
			break;
		case 37:
			note = "C-3";
			break;
		case 38:
			note = "C#3";
			break;
		case 39:
			note = "D-3";
			break;
		case 40:
			note = "D#3";
			break;
		case 41:
			note = "E-3";
			break;
		case 42:
			note = "E#3";
			break;
		case 43:
			note = "F-3";
			break;
		case 44:
			note = "G-3";
			break;
		case 45:
			note = "G#3";
			break;
		case 46:
			note = "A-3";
			break;
		case 47:
			note = "A#3";
			break;
		case 48:
			note = "B-3";
			break;
		case 49:
			note = "C-4";
			break;
		case 50:
			note = "C#4";
			break;
		case 51:
			note = "D-4";
			break;
		case 52:
			note = "D#4";
			break;
		case 53:
			note = "E-4";
			break;
		case 54:
			note = "F-4";
			break;
		case 55:
			note = "F#4";
			break;
		case 56:
			note = "G-4";
			break;
		case 57:
			note = "G#4";
			break;
		case 58:
			note = "A-4";
			break;
		case 59:
			note = "A#4";
			break;
		case 60:
			note = "B-4";
			break;
		case 61:
			note = "C-5";
			break;
		case 62:
			note = "C#5";
			break;
		case 63:
			note = "D-5";
			break;
		case 64:
			note = "D#5";
			break;
		case 65:
			note = "E-5";
			break;
		case 66:
			note = "F-5";
			break;
		case 67:
			note = "F#5";
			break;
		case 68:
			note = "G-5";
			break;
		case 69:
			note = "G#5";
			break;
		case 70:
			note = "A-5";
			break;
		case 71:
			note = "A#5";
			break;
		case 72:
			note = "B-5";
			break;
		case 73:
			note = "C-6";
			break;
		case 74:
			note = "C#6";
			break;
		case 75:
			note = "D-6";
			break;
		case 76:
			note = "D#6";
			break;
		case 77:
			note = "E-6";
			break;
		case 78:
			note = "F-6";
			break;
		case 79:
			note = "F#6";
			break;
		case 80:
			note = "G-6";
			break;
		case 81:
			note = "G#6";
			break;
		case 82:
			note = "A-6";
			break;
		case 83:
			note = "A#6";
			break;
		case 84:
			note = "B-6";
			break;
		case 85:
			note = "C-7";
			break;
		case 86:
			note = "C#7";
			break;
		case 87:
			note = "D-7";
			break;
		case 88:
			note = "D#7";
			break;
		case 89:
			note = "E-7";
			break;
		case 90:
			note = "F-7";
			break;
		case 91:
			note = "F#7";
			break;
		case 92:
			note = "G-7";
			break;
		case 93:
			note = "G#7";
			break;
		case 94:
			note = "A-7";
			break;
		case 95:
			note = "A#7";
			break;
		case 96:
			note = "B-7";
			break;
			//This one's a note-off (XM)	
		case 97:
			note = "=";
			break;
		}


		return note;
	}

	/**
	 * headerCheck(String) takes the file path of a module, returns a boolean (true if the module is a valid MOD/S3M/XM file) and updates 'modtype'. 
	 * 
	 * @return boolean
	 */
	public boolean headerCheck (String tune){
		boolean valid = false;
		
		if (p.createInput(tune) == null) {
			System.out.println("PortaMod says: couldn't find the file when performing a header check. Please check the filename and path.");
		} else {
			InputStream file_input_stream = p.createInput(tune);

		String headercheck = "unknown";
		
		DataInputStream data_input_stream = new DataInputStream( file_input_stream );
		byte[] xm_header = new byte[ 60 ];
		try {
			data_input_stream.readFully( xm_header );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String xm_identifier;
		xm_identifier = FastTracker2.ascii_text( xm_header, 0, 17 );
		if (xm_identifier.equals( "Extended Module: " )){
			headercheck = "xm";
			modtype = "XM";
			//println("Is it an XM? " +headercheck);
		}

		//Check if data is in ScreamTracker 3 format.	
		byte[] s3m_header = new byte[ 96 ];
		System.arraycopy( xm_header, 0, s3m_header, 0, 60 );
		try {
			data_input_stream.readFully( s3m_header, 60, 36 );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( ScreamTracker3.is_s3m( s3m_header ) == true){
			headercheck = "s3m";
			modtype="S3M";
		}

		//Check if data is in ProTracker format.
		mod_header = new byte[ 1084 ];
		System.arraycopy( s3m_header, 0, mod_header, 0, 96 );
		try {
			data_input_stream.readFully( mod_header, 96, 988 );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		localmod_header = mod_header;
		if (ProTracker.is_mod(mod_header) == true) {
			headercheck = "mod";
			modtype = "MOD";
		}

		if (headercheck == "xm" || headercheck == "s3m" || headercheck == "mod"){
			valid = true;
		} else {
			valid = false;
		}
		}
		return valid;
	}
	
	Instrument replace_instrument(byte[] mod_header, int idx, byte[] data_input, int replacetranspose ) throws IOException  {
		int header_offset, sample_data_length;
		int loop_start, loop_length, sample_idx, fine_tune;
		Instrument instrument;
		Sample sample;
		//byte[] raw_sample_data;
		short[] sample_data;
		header_offset = ( idx - 1 ) * 30 + 20;
		
		instrument = new Instrument();
		sample = new Sample();
		//sample_data_length = unsigned_short_be( mod_header, header_offset + 22 ) << 1;
		sample_data_length = data_input.length;
		System.out.println("Sampledata bytes: " + sample_data_length);
		fine_tune = mod_header[ header_offset + 24 ] & 0x0F;
		if( fine_tune > 7 ) {
			fine_tune -= 16;
		}
		sample.transpose = ( fine_tune + (replacetranspose*10) << IBXM.FP_SHIFT ) / 96;
		sample.volume = mod_header[ header_offset + 25 ] & 0x7F;
		loop_start = unsigned_short_be( mod_header, header_offset + 26 ) << 1;
		loop_length = unsigned_short_be( mod_header, header_offset + 28 ) << 1;
		if( loop_length < 4 ) {
			loop_length = 0;
		}
		//raw_sample_data = new byte[ sample_data_length ];
		sample_data = new short[ sample_data_length ];
		
		for( sample_idx = 0; sample_idx < data_input.length; sample_idx++ ) {
			sample_data[ sample_idx ] = ( short ) ( data_input[ sample_idx ] << 8 );
		}
		sample.set_sample_data( sample_data, loop_start, loop_length, false );
		instrument.set_num_samples( 1 );
		instrument.set_sample( 0, sample );
		return instrument;
		
	}
	
	


	/**
	 * customkeyDown(int, int, String, String, String) takes note, instrument, volume, effect and effect-parameter and uses them to override playing
	 * notes in the selected channel. inst, effect and effparam should be set to '0' if unused, while volume must always be set (between 0 and 40), as must channel.  
	 * 
	 */
	public void customkeyDown(int note, int inst, String vol, String effect, String effparam){
		
		if (jamcounter >= jamnote.length) {
			jamcounter = 0;
		}
		
		CustomNote temp = new CustomNote();
		temp.channel = jamcounter;
		temp.note = note;
		temp.inst = inst;
		temp.vol = Integer.parseInt(vol,16);
		temp.effect = Integer.parseInt(effect,16);
		temp.effparam = Integer.parseInt(effparam,16);
		player.ibxm.customnote = temp;
		// If E9X, do a retrigger on selected channel
		
		if (!keydown) {
			player.ibxm.keydown[temp.channel] = true;
		}
		jamnote[jamcounter] = note;
		jaminst[jamcounter] = inst;
		jamcounter ++;
		
	}
	
	 /**
	  *  delaykeyDown(int, int, int, int, int) takes note, instrument, volume, effect and effect-parameter and uses them to override playing
	  */
		public void delaykeyDown(int note, int inst, int vol, int effect, int effparam){
			
			// Currently DOES NOT WORK.
			
			if (jamcounter >= jamnote.length) {
				jamcounter = 0;
			}
			
			CustomNote temp = new CustomNote();
			temp.channel = jamcounter;
			temp.note = note;
			temp.inst = inst;
			temp.vol = vol;
			temp.effect = effect;
			temp.effparam = effparam;
			player.ibxm.customnote = temp;
			// If E9X, do a retrigger on selected channel
			
			if (!keydown) {
				player.ibxm.keydown[temp.channel] = true;
			}
			jamnote[jamcounter] = note;
			jaminst[jamcounter] = inst;
			jamcounter ++;
			
		}
	
	/**
	 * channelDelay(int, int, int) - OMG this'll be so awesome if it works. The idea is to use one or more jam channels to do a classic tracker multipattern delay
	 * on the notes in the specified channel. This could be fucking dubtastic. If this shit works for 1x feedback, I want to do it for more...
	 * 
	 * Takes the target channel, the time in rows by which the delayed notes should be delayed and the desired mix volume of the delayed notes. 
	 * 
	 * Hmm. Every row...I'll need to read the current target note and store it in an array. I'll also play current-n note in a jamchannel at half volume.
	 * If the array is empty, do nothing.
	 * 
	 * Should delay status be checked for on every row, during all playback? I could just check backwards by <number of rows> and play what's there. If
	 * it's at the start of the song, there won't be anything to play. Call the function every time a row callback comes in IF delay is enabled. Cos this
	 * arraylist stuff is clearly bullshit...
	 *  
	 * @param chan
	 * @param rowlength
	 */
	public void channelDelay(int chan, int delaytime, int delaysend) {
		
		// Currently DOES NOT WORK
		
		//channeldelayswitch[chan] = true;
		delayedchannel = chan;
		
	}
	
	/**
	 * effector(int, String, String) - quite experimental for now. Supports E9X retrigger, AXX volume slide, 0XX arpeggio, 2XX porta down and 1XX porta up, with
	 * extremely varying degrees of success. Other effect commands simply are simply ignored for the time being. Some effects will be impossible/impractical to support,
	 * while many would be pointless in this context.
	 * 
	 */
	public void effector(int chan, String effect, String effparam) {
		CustomNote temp = new CustomNote();
		temp.channel = chan;
		temp.note = 0;
		temp.inst = 0;
		temp.vol = 40;
		temp.effect = Integer.parseInt(effect,16);
		temp.effparam = Integer.parseInt(effparam,16);
		player.ibxm.effector[chan] = true;
		player.ibxm.customnote = temp;
		if (!keydown) {
			player.ibxm.keydown[temp.channel] = true;
		}
	}
	
	/**
	 * customkeyUp(int, int) takes note and instrument numbers and flips the relevant keydown switch to off. Use -1 as the parameter to disable keydowns for all channels.   
	 * 
	 */
	public void customkeyUp(int note, int inst){
		if (note > 0) {
			for (int i=0 ; i < jamnote.length ; i++ ) {
				if (jamnote[i] == note && jaminst[i] == inst) {
					player.ibxm.keydown[i] = false;
					player.ibxm.jamchans[ i].reset();
				}
			}
		} else {
			for (int i=0 ; i < jamnote.length ; i++ ) {
					player.ibxm.keydown[i] = false;
					player.ibxm.jamchans[ i].reset();
			}
		}
		keydown = false;
	}
	
	/**
	 * getLoopstart(inst) returns an int value of the current loop start-point for the chosen instrument 
	 */
//	public int getLoopstart(int inst){
//		return  
//	}
	
	/**
	 * loopAdjust(inst, loopstart, looplength) takes an instrument number and two percentage values. loopstart sets the looping start-point to the desired percentage offset
	 * of the sample, while looplength sets the end-point to a percentage of the remaining sampleduration (from loopstart to the end of the sample).
	 * If looplength is -1, it's taken to be the full length of the sample.
	 * 
	 *     Warning: this is FLAKY. I might disable loopstart altogether, since it's causing real problems...more logic needed.
	 */

	
//	public void loopAdjust(int inst, int newloopstart, int newlooplength) {
//		
//		//Maybe I should do a full-on set_sample_data call, then it can take care of the overflow logic, plus I can set ping-pongs...
//		
//		int temp_loopstart = player.module.instruments[inst].samples[0].sample_data_length/100*newloopstart;
//		if (temp_loopstart < player.module.instruments[inst].samples[0].sample_data_length && temp_loopstart < loopstart[inst]+looplength[inst]) {
//			player.module.instruments[inst].samples[0].loop_start = temp_loopstart;
//			loopstart[inst] = temp_loopstart;
//		}
//		
//		if (newlooplength > 0 && newlooplength < player.module.instruments[inst].samples[0].sample_data_length) {
//			player.module.instruments[inst].samples[0].loop_length = (player.module.instruments[inst].samples[0].sample_data_length - loopstart[inst])/100*newlooplength;
//
//			looplength[inst] = (player.module.instruments[inst].samples[0].sample_data_length - loopstart[inst])/100*newlooplength;
//		} else {
//			//player.module.instruments[inst].samples[0].loop_length = (player.module.instruments[inst].samples[0].sample_data_length);
//			player.module.instruments[inst].samples[0].loop_length = (player.module.instruments[inst].samples[0].sample_data_length);
//			looplength[inst] = player.module.instruments[inst].samples[0].sample_data_length;
//		}
//				
//	}
	
	public void loopStart(int inst, int newloopstart) {	
		
		int samplelength = player.module.instruments[inst].samples[0].sample_data_length;
		//int availabledata = samplelength - 4 - loopstart[inst];
		int temp_loopstart = samplelength / 100 * newloopstart;
		
		if (temp_loopstart < samplelength) {
			if (temp_loopstart < samplelength ) {
				player.module.instruments[inst].samples[0].loop_start = temp_loopstart;
				
//				if (player.module.instruments[inst].samples[0].loop_length - player.module.instruments[inst].samples[0].loop_start >= samplelength) {
//					player.module.instruments[inst].samples[0].loop_length = samplelength;
//					looplength[inst] = samplelength;
//				}
				
				loopstart[inst] = temp_loopstart;
			} else {
				player.module.instruments[inst].samples[0].loop_length = temp_loopstart;
				looplength[inst] = temp_loopstart;
			}
		}
		
	}
	
	public void loopLength(int inst, int newlooplength) {
		int temp_looplength = 0;
		int samplelength = player.module.instruments[inst].samples[0].sample_data_length;
		
		int availabledata = samplelength - 4 - loopstart[inst];
		
		temp_looplength = availabledata/100 * newlooplength;
		
		
		
		//if (temp_looplength > loopstart[inst] ) {
			player.module.instruments[inst].samples[0].loop_length = temp_looplength;
			looplength[inst] = temp_looplength;
		//}
				
	}
	
	/**
	 * loopReset(inst) takes the chosen instrument and resets the loop start/end points to the originals.
	 * @param inst
	 */
	public void loopReset(int inst) {

		player.module.instruments[inst].samples[0].loop_start = origloopstart[inst];
		player.module.instruments[inst].samples[0].loop_length = origlooplength[inst];
		loopstart[inst] = origloopstart[inst];
		looplength[inst] = origlooplength[inst];
	}
	
	
	public void sampleDump(){
		if (loadSuccess > 0) {
			try {
				for (int i = 0; i < (player.get_num_instruments() - 1); i++) {
					Sample sampshort = new Sample();
					sampshort = player.module.instruments[i].get_sample(i);
					byte [] samp = new byte[sampshort.sample_data.length];
					for (int j=0; j < sampshort.sample_data.length; j++ ) {
												samp[j] = (byte)((sampshort.sample_data[j] >> 8) & 0xff);
					}

					p.saveBytes(player.ins_name(i)+i+".raw", samp);
					System.out.println("Sample " + i + " written to disk.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	void set_instrument( int instrument_index, Instrument instrument ) {
		if( instrument_index > 0 && instrument_index <= player.module.instruments.length) {
			player.module.instruments[ instrument_index - 1 ] = instrument;
		}
	}
	
	private static int unsigned_short_be( byte[] buf, int offset ) {
		int value;
		value = ( buf[ offset ] & 0xFF ) << 8;
		value = value | ( buf[ offset + 1 ] & 0xFF );
		return value;
	}


	// SONG TRANSPORT/NAVIGATION
	
	/**
	 * getCurrent_sequence_index
	 */	
	public int getCurrent_sequence_index() {
		return player.ibxm.current_sequence_index;
	}


	/**
	 * getCurrent_row() returns the current row at the current sequence position as an int
	 */
	public int getCurrent_row() {
		return player.ibxm.current_row;
	}
	
	/**
	 * getNext_sequence_index() returns the sequence number of the next pattern due to be played
	 * @return
	 */
	public int getNext_sequence_index() {
		return player.ibxm.next_sequence_index;
	}

	/**
	 * getNext_row() returns the sequence number of the next pattern due to be played
	 * @return
	 */
	public int getNext_row() {
		return player.ibxm.next_row;
	}

	/**
	 * setNext_sequence_index(newpos, behaviour) takes the desired new sequence position and an int 0 or 1 to choose between 'continuous' pattern-skip behaviour 
	 * (where the row in the new pattern follows smoothly from the row in the old pattern) and 'play from start' behaviour, where the new pattern plays from row 0. 
	 * Do the logic in your sketch using, for instance, getCurrent_sequence_index, to determine how to skip back or forward in your chosen increments.
	 */
	public void setNext_sequence_index(int newpos, int behaviour){
			if (behaviour == 0) {
				player.ibxm.next_sequence_index = newpos;
			}
			if (behaviour == 1) {
				player.ibxm.next_sequence_index = newpos;
				player.ibxm.next_row = 0;
			}
	}

	/**
	 * setNext_row(int) takes the number of the desired next row in the current pattern. 
	 * 
	 */
	public void setNext_row(int newrow){
		if (newrow < player.ibxm.total_rows && newrow >= 0){
			player.ibxm.next_row = newrow;
		}
	}
	
	public void stop() {
		player.stop();
	}

}

