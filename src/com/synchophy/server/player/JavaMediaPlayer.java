package com.synchophy.server.player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javazoom.jlme.decoder.BitStream;
import javazoom.jlme.decoder.Decoder;
import javazoom.jlme.decoder.Header;
import javazoom.jlme.decoder.SampleBuffer;

import com.synchophy.server.PlayerManager;

public class JavaMediaPlayer implements IMediaPlayer {

	private SourceDataLine line;
	private BitStream bitstream;

	private BitStream loadBitStream(String filename) {

		if (filename == null) {
			return null;
		}

		try {

			return new BitStream(new BufferedInputStream(new FileInputStream(
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
	public boolean play(String filename) throws IOException {

		bitstream = loadBitStream(filename);
		if (bitstream == null) {
			return false;
		}
		int length;
		boolean first = true;
		int currentPosition = PlayerManager.getInstance().getPosition()
				.intValue();

		Header header = bitstream.readFrame();
		Decoder decoder = new Decoder(header, bitstream);
		while (Boolean.TRUE.equals(PlayerManager.getInstance().isPlaying())) {
			try {
				int position = PlayerManager.getInstance().getPosition()
						.intValue();
				if (currentPosition != position) {
					// song has changed, break and get the next song
					return true;
				}
				SampleBuffer output = (SampleBuffer) decoder.decodeFrame();
				length = output.size();
				if (length == 0) {
					// only advance if we actually finish the song
					PlayerManager.getInstance().setPosition(position++);
					return true;
				}
				// {
				if (first) {
					first = false;
					startOutput(new AudioFormat(decoder.getOutputFrequency(),
							16, decoder.getOutputChannels(), true, false));
				}
				line.write(output.getBuffer(), 0, length);
				bitstream.closeFrame();
				header = bitstream.readFrame();
			} catch (Exception e) {
				// e.printStackTrace();
				break;
			}
		}
		bitstream.close();
		if (line != null) {
			line.drain();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.synchophy.server.player.IMediaPlayer#startOutput(javax.sound.sampled
	 * .AudioFormat)
	 */
	private void startOutput(AudioFormat playFormat)
			throws LineUnavailableException {

		DataLine.Info info = new DataLine.Info(SourceDataLine.class, playFormat);

		if (!AudioSystem.isLineSupported(info)) {
			throw new LineUnavailableException("Cannot play sound format.");
		}
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(playFormat);
		line.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchophy.server.player.IMediaPlayer#stopOutput()
	 */
	public void stopOutput() {

		try {
			if (line != null) {
				line.drain();
				line.stop();
				line.close();
				line = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void afterPlay() throws IOException {
		bitstream.close();
		if (line != null) {
			line.drain();
		}
	}

}
