package com.synchophy.server;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javazoom.jlme.decoder.BitStream;
import javazoom.jlme.decoder.Decoder;
import javazoom.jlme.decoder.Header;
import javazoom.jlme.decoder.SampleBuffer;

import com.synchophy.server.db.DatabaseManager;


public class PlayerManager {

  private final Object lock = new Object();
  private static PlayerManager instance;
  private SourceDataLine line;
  private boolean running;
  private boolean playable;
  private boolean done;
  private Thread playThread;
  private int position;


  private PlayerManager() {

    init();
  }


  private void init() {

    position = 0;
    running = true;
    playThread = new Thread("PlayThread") {

      public void run() {

        while (running) {
          synchronized (lock) {
            try {
              lock.wait();
              playList();
            } catch (Exception e) {
              running = false;
            }
          }

        }
      }
    };
    playThread.start();
  }


  public static synchronized PlayerManager getInstance() {

    if (instance == null) {
      instance = new PlayerManager();
    }
    return instance;
  }


  private BitStream getNextBitStream() {

    List queue = DatabaseManager.getInstance().loadQueueFiles();
    try {
      if (queue.size() > position) {
        String filename = (String) ((Map) queue.get(position)).get("file");
        return new BitStream(new BufferedInputStream(new FileInputStream(filename), 2048));
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }


  public void next() {

    playable = false;
    position++;
    List queue = DatabaseManager.getInstance().loadQueueFiles();
    if (position > queue.size() - 1) {
      position = queue.size() - 1;
    }
    waitToFinish();
    play();
  }


  public void previous() {

    playable = false;
    position--;
    if (position < 0) {
      position = 0;
    }
    waitToFinish();
    play();
  }


  public void first() {

    playable = false;
    position = 0;
    waitToFinish();
    play();
  }


  public void last() {

    playable = false;
    List queue = DatabaseManager.getInstance().loadQueueFiles();
    position = queue.size() - 1;
    waitToFinish();
    play();
  }


  public void select(int index) {

    playable = false;
    List queue = DatabaseManager.getInstance().loadQueueFiles();
    position = index;
    if (position < 0) {
      position = 0;
    }
    if (position > queue.size() - 1) {
      position = queue.size() - 1;
    }
    waitToFinish();
    play();
  }


  private void playList() {

    done = false;
    try {
      playable = true;
      while (playable) {
        boolean first = true;
        int length;
        BitStream bitstream = getNextBitStream();
        if (bitstream == null) {
          playable = false;
          continue;
        }
        Header header = bitstream.readFrame();
        Decoder decoder = new Decoder(header, bitstream);
        while (playable) {
          try {
            SampleBuffer output = (SampleBuffer) decoder.decodeFrame();
            length = output.size();
            if (length == 0) {
              // only advance if we actually finish the song
              position++;
              break;
            }
            // {
            if (first) {
              first = false;
              startOutput(new AudioFormat(decoder.getOutputFrequency(), 16,
                  decoder.getOutputChannels(), true, false));
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
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    playable = false;
    stopOutput();
    done = true;
  }


  public void startOutput(AudioFormat playFormat) throws LineUnavailableException {

    DataLine.Info info = new DataLine.Info(SourceDataLine.class, playFormat);

    if (!AudioSystem.isLineSupported(info)) {
      throw new LineUnavailableException("Cannot play sound format.");
    }
    line = (SourceDataLine) AudioSystem.getLine(info);
    line.open(playFormat);
    line.start();
  }


  private void stopOutput() {

    if (line != null) {
      line.drain();
      line.stop();
      line.close();
      line = null;
    }
  }


  public void play() {

    synchronized (lock) {
      lock.notify();
    }
  }


  public void stop() {

    playable = false;
  }


  public void shutdown() {

    playable = false;
    running = false;
    if (playThread != null) {
      try {
        synchronized (lock) {
          lock.notify();
        }
        playThread.join();
      } catch (InterruptedException e) {

      }
    }
    waitToFinish();
  }


  private void waitToFinish() {

    while (!done) {
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
      }
    }
    
  }


  public Map getCurrentSong() {

    List queue = DatabaseManager.getInstance().loadQueueFiles();
    if (position >= queue.size() - 1) {
      return null;
    }
    String song = (String) ((Map) queue.get(position)).get("file");
    List list = DatabaseManager.getInstance()
        .query("select trim(LEADING '0' FROM title_sort), artist_sort, album_sort from song where file = ?",
               new Object[]{
                 song
               },
               new String[]{
                   "name", "artist", "album"
               });
    if (list.size() > 0) {
      return (Map) list.get(0);
    }
    return null;
  }


  public Boolean isPlaying() {

    return Boolean.valueOf(playable);
  }


  public Integer getPosition() {

    return Integer.valueOf(position);
  }

}
