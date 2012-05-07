package com.synchophy.server.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.synchophy.server.ConfigManager;
import com.synchophy.server.HttpServer;
import com.synchophy.server.player.CommandLineMediaPlayer;
import com.synchophy.server.player.JavaMediaPlayer;
import com.synchophy.server.scanner.tag.FullTagProvider;
import com.synchophy.server.scanner.tag.Mp3OnlyTagProvider;

public class PreferencesDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField musicPathTextField;
	private JLabel lblSynchophyPreferences;
	private JTextField uploadPathTextField;
	private JTextField nightlyPathTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PreferencesDialog dialog = new PreferencesDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public PreferencesDialog() {

		ConfigManager configManager = ConfigManager.getInstance();

		setBounds(100, 100, 451, 279);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		SpringLayout sl_contentPanel = new SpringLayout();
		contentPanel.setLayout(sl_contentPanel);
		{
			lblSynchophyPreferences = DefaultComponentFactory.getInstance()
					.createTitle("Synchophy Preferences");
			sl_contentPanel.putConstraint(SpringLayout.NORTH,
					lblSynchophyPreferences, 10, SpringLayout.NORTH,
					contentPanel);
			sl_contentPanel.putConstraint(SpringLayout.WEST,
					lblSynchophyPreferences, 146, SpringLayout.WEST,
					contentPanel);
			contentPanel.add(lblSynchophyPreferences);
		}

		JLabel lblMusicPath = DefaultComponentFactory.getInstance()
				.createTitle("Music Path");
		contentPanel.add(lblMusicPath);

		musicPathTextField = new JTextField();
		lblMusicPath.setLabelFor(musicPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.WEST, musicPathTextField,
				89, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, lblMusicPath, 6,
				SpringLayout.NORTH, musicPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.EAST, lblMusicPath, -6,
				SpringLayout.WEST, musicPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, musicPathTextField,
				6, SpringLayout.SOUTH, lblSynchophyPreferences);
		musicPathTextField.setText(configManager.getMusicPath());
		contentPanel.add(musicPathTextField);
		musicPathTextField.setColumns(25);

		uploadPathTextField = new JTextField();
		sl_contentPanel.putConstraint(SpringLayout.NORTH, uploadPathTextField,
				6, SpringLayout.SOUTH, musicPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.WEST, uploadPathTextField,
				0, SpringLayout.WEST, musicPathTextField);
		uploadPathTextField.setText(configManager.getUploadPath());
		contentPanel.add(uploadPathTextField);
		uploadPathTextField.setColumns(25);

		JLabel lblUploadPath = DefaultComponentFactory.getInstance()
				.createLabel("Upload Path");
		lblUploadPath.setLabelFor(uploadPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, lblUploadPath, 6,
				SpringLayout.NORTH, uploadPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.EAST, lblUploadPath, 0,
				SpringLayout.EAST, lblMusicPath);
		contentPanel.add(lblUploadPath);

		nightlyPathTextField = new JTextField();
		sl_contentPanel.putConstraint(SpringLayout.NORTH, nightlyPathTextField,
				6, SpringLayout.SOUTH, uploadPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.WEST, nightlyPathTextField,
				0, SpringLayout.WEST, musicPathTextField);
		nightlyPathTextField.setText(configManager.getNightlyPath());
		contentPanel.add(nightlyPathTextField);
		nightlyPathTextField.setColumns(25);

		JLabel lblNightlyPath = DefaultComponentFactory.getInstance()
				.createLabel("Nightly Path");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, lblNightlyPath, 18,
				SpringLayout.SOUTH, lblUploadPath);
		sl_contentPanel.putConstraint(SpringLayout.EAST, lblNightlyPath, 0,
				SpringLayout.EAST, lblMusicPath);
		contentPanel.add(lblNightlyPath);

		JLabel lblMediaPlayer = DefaultComponentFactory.getInstance()
				.createLabel("Media Player");
		sl_contentPanel.putConstraint(SpringLayout.WEST, lblMediaPlayer, 0,
				SpringLayout.WEST, lblNightlyPath);
		contentPanel.add(lblMediaPlayer);

		final JComboBox mediaPlayerComboBox = new JComboBox(new String[] {
				"Java", "Command Line" });
		if (JavaMediaPlayer.class.getName().equals(
				configManager.getPlayerProvider())) {
			mediaPlayerComboBox.setSelectedItem("Java");
		} else if (CommandLineMediaPlayer.class.getName().equals(
				configManager.getPlayerProvider())) {
			mediaPlayerComboBox.setSelectedItem("Command Line");
		}
		lblMediaPlayer.setLabelFor(mediaPlayerComboBox);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, mediaPlayerComboBox,
				11, SpringLayout.SOUTH, nightlyPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, lblMediaPlayer, 4,
				SpringLayout.NORTH, mediaPlayerComboBox);
		sl_contentPanel.putConstraint(SpringLayout.WEST, mediaPlayerComboBox,
				0, SpringLayout.WEST, musicPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.EAST, mediaPlayerComboBox,
				-183, SpringLayout.EAST, contentPanel);
		mediaPlayerComboBox.setMaximumRowCount(25);

		contentPanel.add(mediaPlayerComboBox);

		JLabel lblTagProvider = DefaultComponentFactory.getInstance()
				.createLabel("Tag Provider");
		sl_contentPanel.putConstraint(SpringLayout.WEST, lblTagProvider, 0,
				SpringLayout.WEST, lblMusicPath);
		contentPanel.add(lblTagProvider);

		final JComboBox tagProviderComboBox = new JComboBox(new String[] {
				"MP3 Only", "Full Tag" });
		if (Mp3OnlyTagProvider.class.getName().equals(
				configManager.getTagProvider())) {
			tagProviderComboBox.setSelectedItem("Mp3 Only");
		} else if (FullTagProvider.class.getName().equals(
				configManager.getTagProvider())) {
			tagProviderComboBox.setSelectedItem("Full Tag");
		}
		lblTagProvider.setLabelFor(tagProviderComboBox);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, lblTagProvider, 4,
				SpringLayout.NORTH, tagProviderComboBox);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, tagProviderComboBox,
				6, SpringLayout.SOUTH, mediaPlayerComboBox);
		sl_contentPanel.putConstraint(SpringLayout.WEST, tagProviderComboBox,
				0, SpringLayout.WEST, musicPathTextField);
		sl_contentPanel.putConstraint(SpringLayout.EAST, tagProviderComboBox,
				0, SpringLayout.EAST, mediaPlayerComboBox);
		contentPanel.add(tagProviderComboBox);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						try {
							ConfigManager configManager = ConfigManager
									.getInstance();
							configManager.setNightlyPath(nightlyPathTextField
									.getText());
							configManager.setMusicPath(musicPathTextField
									.getText());
							configManager.setUploadPath(uploadPathTextField
									.getText());

							String tagProviderName = (String) tagProviderComboBox
									.getSelectedItem();
							Class tagProvider = null;
							if ("MP3 Only".equals(tagProviderName)) {
								tagProvider = Mp3OnlyTagProvider.class;
							} else if ("Full Tag".equals(tagProviderName)) {
								tagProvider = FullTagProvider.class;
							}
							configManager.setTagProvider(tagProvider.getName());

							String playerProviderName = (String) mediaPlayerComboBox
									.getSelectedItem();

							Class playerProvider = null;
							if ("Java".equals(playerProviderName)) {
								playerProvider = JavaMediaPlayer.class;
							} else if ("Command Line"
									.equals(playerProviderName)) {
								playerProvider = CommandLineMediaPlayer.class;
							}

							configManager.setPlayerProvider(playerProvider
									.getName());
							configManager.save();
						} catch (IOException e) {
							JOptionPane.showMessageDialog(
									PreferencesDialog.this, "Error saving.");
							e.printStackTrace();
							return;
						}
						HttpServer.getInstance().restart();
						setVisible(false);
					}

				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
					}

				});
				buttonPane.add(cancelButton);
			}
		}
	}

}
