package org.mozilla;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.media.PlayerListener;

public class AudioRecorder extends MIDlet implements CommandListener,
		ItemCommandListener {
	private Form form;

	private Display display;

	private StringItem recordButton;

	private boolean recording = false;

	private Player player = null;

	private RecordControl recordControl = null;

	private ByteArrayOutputStream outputStream = null;

	private void logMessage(String msg) {
		this.form.insert(2, new StringItem(null, msg));
	}

	private class StopThread extends Thread {
		public void run() {
			try {
				recordControl.stopRecord();
				recordControl.commit();
				player.stop();
				player.close();

				logMessage("Start to playback the recorded audio.");
				String audioEncodings = System.getProperty("audio.encodings");
				Player playback = Manager.createPlayer(
						new ByteArrayInputStream(outputStream.toByteArray()),
						(audioEncodings != null && audioEncodings.trim() != "") ? audioEncodings : "audio/amr");
				playback.realize();
				playback.prefetch();
				playback.start();
			} catch (IOException e) {
				logMessage("Error occurs when stop recording: " + e);
			} catch (MediaException e) {
				logMessage("Error occurs when stop recording: " + e);
			}

			logMessage("Recording stopped.");
		}
	}

	private class RecordingThread extends Thread {
		public void run() {
			logMessage("Start a new thread to record audio.");
			try {
				player = Manager.createPlayer("capture://audio");
				player.realize();

				player.addPlayerListener(new PlayerListener() {
					public void playerUpdate(Player player, String event, Object eventData) {
						if (PlayerListener.RECORD_ERROR.equals(event)) {
							logMessage("Error occurs when start recording: " + eventData);
							recording = false;
							updateRecordingMessage();
						}
					}
				});

				recordControl = (RecordControl) player
						.getControl("RecordControl");
				outputStream = new ByteArrayOutputStream();
				recordControl.setRecordStream(outputStream);
				recordControl.startRecord();
				player.start();
			} catch (IOException e) {
				recording = false;
				updateRecordingMessage();
				logMessage("Error occurs when capturing audio: " + e);
			} catch (MediaException e) {
				recording = false;
				updateRecordingMessage();
				logMessage("Error occurs when capturing audio: " + e);
			}
		}
	}

	public AudioRecorder() {
	}

	private void stopRecording() {
		this.recording = false;
		this.updateRecordingMessage();
		new StopThread().start();
	}

	private void startRecording() {
		this.recording = true;
		this.updateRecordingMessage();
		new RecordingThread().start();
	}

	private void updateRecordingMessage() {
		this.recordButton.setText(this.recording ? "Stop" : "Start");
	}

	private void toggleRecorderStatus() {
		// Check if the device support audio capturing.
		if (!"true".equals(System.getProperty("supports.audio.capture"))) {
			this.logMessage("This device doesn't support audio capture!");
			return;
		}

		if (this.recording) {
			this.stopRecording();
		} else {
			this.startRecording();
		}
	}

	public void commandAction(Command command, Item item) {
		if (item == this.recordButton) {
			this.toggleRecorderStatus();
		}
	}

	public void commandAction(Command c, Displayable d) {
	}

	protected void destroyApp(boolean unconditional)
			throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		this.recordButton = new StringItem(null, "Start", Item.BUTTON);
		Command toggleRecordingCMD = new Command("Click", Command.ITEM, 1);
		this.recordButton.addCommand(toggleRecordingCMD);
		this.recordButton.setDefaultCommand(toggleRecordingCMD);
		this.recordButton.setItemCommandListener(this);

		this.form = new Form(null, new Item[] {
				new StringItem(null, "Audio Recorder"), this.recordButton });

		this.display = Display.getDisplay(this);
		this.display.setCurrent(this.form);
	}
}
