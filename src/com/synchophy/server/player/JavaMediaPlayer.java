package com.synchophy.server.player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer.Info;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

import com.synchophy.server.PlayerManager;

public class JavaMediaPlayer implements IMediaPlayer {

	private AudioDevice audio;
	private Decoder decoder;
	private Bitstream bitstream;

	private Bitstream loadBitStream(String filename) {

		if (filename == null) {
			return null;
		}

		try {

			return new Bitstream(new BufferedInputStream(new FileInputStream(
					filename), 2048));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.player.IMediaPlayer#play(java.lang.String)
	 */
	public boolean notifyPlay(String filename) throws IOException {

		System.err.println("Starting play with JavaMediaPlayer.");

		startOutput();

		bitstream = loadBitStream(filename);
		if (bitstream == null) {
			System.err.println("Bitstream is null");
			return false;
		}
		int currentPosition = PlayerManager.getInstance().getPosition()
				.intValue();

		int frames = Integer.MAX_VALUE;
		while (Boolean.TRUE.equals(PlayerManager.getInstance().isPlaying())) {
			try {
				int position = PlayerManager.getInstance().getPosition()
						.intValue();
				if (currentPosition != position) {
					// song has changed, break and get the next song
					System.err.println(currentPosition + ":" + position);
					return true;
				}

				boolean done = false;
				while (frames-- > 0
						&& Boolean.TRUE.equals(PlayerManager.getInstance()
								.isPlaying())) {
					if (decodeFrame() == false) {
						done = true;
						break;
					}
				}
				if (done) {
					// only advance if we actually finish the song
					System.err
							.println("Advancing because we finished the song.");
					PlayerManager.getInstance().setPosition(position++);
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		try {
			bitstream.close();
		} catch (BitstreamException e) {
			e.printStackTrace();
		}
		System.err.println("Advancing because we feel out of the loop.");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.synchophy.server.player.IMediaPlayer#startOutput(javax.sound.sampled
	 * .AudioFormat)
	 */
	private void startOutput() {

		Info[] info = AudioSystem.getMixerInfo();
		System.err.println("Found " + info.length + " mixer implementations.");
		for (int i = 0; i < info.length; i++) {
			Info mixer = info[i];
			System.err.println(mixer.getName());
		}

		decoder = new Decoder();

		FactoryRegistry r = FactoryRegistry.systemRegistry();
		try {
			audio = r.createAudioDevice();
			audio.open(decoder);
		} catch (JavaLayerException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not start audio subsystem.", e);

		}

	}

	public void notifyAfterPlay() throws IOException {
		notifyStop();
	}

	protected boolean decodeFrame() throws JavaLayerException {
		try {
			AudioDevice out = audio;
			if (out == null)
				return false;

			Header h = bitstream.readFrame();

			if (h == null)
				return false;

			// sample buffer set when decoder constructed
			SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h,
					bitstream);

			synchronized (this) {
				out = audio;
				System.err.println("Attempting write");
				if (out != null) {
					System.err.println("Writing");
					out.write(output.getBuffer(), 0, output.getBufferLength());
				}
			}

			bitstream.closeFrame();
		} catch (RuntimeException ex) {
			throw new JavaLayerException("Exception decoding audio frame", ex);
		}

		return true;
	}

	public void notifyPositionChange() {
	}

	public void notifyStop() {
		AudioDevice out = audio;
		if (out != null) {
			audio = null;
			// this may fail, so ensure object state is set up before
			// calling this method.
			out.close();
			try {
				bitstream.close();
			} catch (BitstreamException ex) {
			}
		}
	}
	
	public void shutdown() {
	}
	
	public void notifyPause() {
	}
}