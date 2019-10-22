//package org.wikijava.sound.playWave;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound {

    private InputStream waveStream;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
    private SourceDataLine dataLine;
    private int audioPosition;
    private Clip dataClip = null;
    private boolean flag = true;
    private String filename;

    /**
     * CONSTRUCTOR
     */
    public PlaySound(String Filename, int audioPosition) {
    	this.filename = Filename;
    	this.audioPosition = audioPosition;
    }

    public void play() throws PlayWaveException {
    	

    	try {
    		this.waveStream = new FileInputStream(this.filename);
    	} catch (FileNotFoundException e2) {
    		// TODO Auto-generated catch block
    		e2.printStackTrace();
    	}
    	AudioInputStream audioInputStream = null;
    	try {
    		//add buffer for mark/reset support, modified by Jian
    		InputStream bufferedIn = new BufferedInputStream(this.waveStream);
    	    audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
    		
    	} catch (UnsupportedAudioFileException e1) {
    	    throw new PlayWaveException(e1);
    	} catch (IOException e1) {
    	    throw new PlayWaveException(e1);
    	}

    	try {
    		dataClip = AudioSystem.getClip();
    	} catch (LineUnavailableException e1) {
    	    throw new PlayWaveException(e1);
    	}

    	try {
    		// Starts the music :P
    		dataClip.open(audioInputStream);
    		dataClip.setFramePosition(audioPosition);  // Must always rewind!
    		dataClip.loop(audioPosition);
    		dataClip.start();
    	} catch (LineUnavailableException | IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}

	/*	// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);

		// opens the audio channel
		dataLine = null;
		try {
		    dataLine = (SourceDataLine) AudioSystem.getLine(info);
		    dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
		} catch (LineUnavailableException e1) {
		    throw new PlayWaveException(e1);
		}

		// Starts the music :P
		
		dataLine.start();
	
		int readBytes = 0;
		byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

		try {
			
		    while (readBytes != -1) {
		    	
			readBytes = audioInputStream.read(audioBuffer, 0,
				audioBuffer.length);
			if (readBytes >= 0){
			    dataLine.write(audioBuffer, 0, readBytes);
			}

		    }
		} catch (IOException e1) {
		    throw new PlayWaveException(e1);
		} finally {
		    // plays what's left and and closes the audioChannel
		    dataLine.drain();
		    dataLine.close();
		}*/

    } 
    
   /* public void pause() {
    	flag = false;
    	audioPosition = dataLine.getFramePosition();
    	dataLine.stop();
    	dataLine.flush();
    }

    public void stop(){
    	flag = false;
    	audioPosition = 0;
    	dataLine.stop();
    	dataLine.flush();
    }*/
    public void pause() {
    	audioPosition = dataClip.getFramePosition();
    	dataClip.stop();
    }
    
    public void loop(){
    	dataClip.loop(Clip.LOOP_CONTINUOUSLY);
    }
    
    public void stop(){
    	audioPosition = 0;
    	dataClip.stop();
    }
}