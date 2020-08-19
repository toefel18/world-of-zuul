package nl.toefel.game.sound;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Plays sound files using the java applet.AudioClip, can be used to reapeatedly play a soudn
 * @author Christophe
 *
 */
public class AudioPlayer extends Applet implements AudioClip {

	private AudioClip ac;
	
	public AudioPlayer(String filename){
		File file = new File(filename);
		try{
			ac = Applet.newAudioClip(file.toURI().toURL());
		}catch(MalformedURLException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void loop() {
		// TODO Auto-generated method stub
		ac.loop();
	}

	@Override
	public void play() {
		// TODO Auto-generated method stub
		ac.play();
	}
	
	public void stop(){
		ac.stop();
		super.stop();
	}
}

