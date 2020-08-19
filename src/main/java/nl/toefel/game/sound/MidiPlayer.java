package nl.toefel.game.sound;

import javax.sound.midi.*;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.*;

/**
 * 	Plays a midi file.
 * 
 * 	@author http://blog.taragana.com/index.php/archive/how-to-play-a-midi-file-from-a-java-application/nl/
 **/
public class MidiPlayer implements AudioClip {
	private Sequencer sequencer = null;
	
    public MidiPlayer(String filename)
    {
    	File midiFile = new File(filename);
        if(!midiFile.exists() || midiFile.isDirectory() || !midiFile.canRead()) {
            System.out.println("Soundfile not found.");
        }
        
        try {
			sequencer = MidiSystem.getSequencer();
			sequencer.setSequence(MidiSystem.getSequence(midiFile));
	        sequencer.open();
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }  

	@Override
	public void loop() {
		// TODO Auto-generated method stub
		sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
		
		play();
	}

	@Override
	public void play() {
        sequencer.start();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		sequencer.stop();
		sequencer.close();
	}
}
