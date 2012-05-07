package com.synchophy.server;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import com.synchophy.server.ui.PreferencesDialog;

public class Synchophy {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (!SystemTray.isSupported()) {
			System.err
					.println("Cannot start Synchophy application. Try using the startserver.sh script instead.");
			return;
		}

		SystemTray tray = SystemTray.getSystemTray();
		Image image = Toolkit.getDefaultToolkit().getImage(
				Synchophy.class.getResource("/icon.png"));

		PopupMenu popup = new PopupMenu();

		ActionListener preferencesListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PreferencesDialog dialog = new PreferencesDialog();
				//dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
			}
		};

		MenuItem preferencesItem = new MenuItem("Preferences...");
		preferencesItem.addActionListener(preferencesListener);
		popup.add(preferencesItem);

		ActionListener exitListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Exiting...");
				System.exit(0);
			}
		};

		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(exitListener);
		popup.add(exitItem);

		final TrayIcon trayIcon = new TrayIcon(image, "Tray Demo", popup);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				trayIcon.displayMessage("Action Event",
						"An Action Event Has Been Performed!",
						TrayIcon.MessageType.INFO);
			}
		};

		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(actionListener);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.err.println("TrayIcon could not be added.");
		}
		HttpServer.getInstance().start();
	}

}
