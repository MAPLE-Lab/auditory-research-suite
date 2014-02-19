package edu.mcmaster.maplelab.rhythm;

import javax.sound.midi.MidiSystem;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Widget for selection of midi devices.
 */
public class DeviceSelectionPanel extends JPanel {
	
	private JComboBox _toneSynth;
	private JComboBox _tapInput;
	private JComboBox _tapSynth;
	
	
	public DeviceSelectionPanel() {
		super(new MigLayout("insets 0 0 0 0", "[right]10px[center]10px[center]10px[center]", "0px[]3px[]0px"));
		setBorder(BorderFactory.createTitledBorder("MIDI Devices"));
		add(new JLabel("<html><div style=\"text-align: center;\">Tone Synth<br>(Must have RECEIVE)"), "skip 1");
		add(new JLabel("<html><div style=\"text-align: center;\">Tap Source<br>(Must have TRANS)"));
		add(new JLabel("<html><div style=\"text-align: center;\">Tap Synth<br>(Must have RECEIVE)"), "wrap");
		add(new JLabel("Device #:"));
		
		_toneSynth = new JComboBox();
		_tapInput = new JComboBox();
		_tapSynth = new JComboBox();
		
		initDeviceCount(MidiSystem.getMidiDeviceInfo().length);
		
		add(_toneSynth, "sgx");
		add(_tapInput, "sgx");
		add(_tapSynth, "sgx");
	}
	
	private void initDeviceCount(int count) {
		String[] toneSynthItems = new String[count];
		String[] tapInputItems = new String[count + 1];
		tapInputItems[0] = "None (Comp. Keys)";
		String[] tapSynthItems = new String[count + 1];
		tapSynthItems[0] = "None";
		
		for (int i = 0; i < count; i++) {
			toneSynthItems[i] = String.valueOf(i);
			tapInputItems[i + 1] = String.valueOf(i);
			tapSynthItems[i + 1] = String.valueOf(i);
		}
		
		_toneSynth.setModel(new DefaultComboBoxModel(toneSynthItems));
		_tapInput.setModel(new DefaultComboBoxModel(tapInputItems));
		_tapSynth.setModel(new DefaultComboBoxModel(tapSynthItems));
		updateSelections(0, 0, 0);
	}
	
	public void updateSelections(int toneSynth, int tapInput, int tapSynth) {
		if (toneSynth < _toneSynth.getItemCount()) {
			_toneSynth.setSelectedIndex(toneSynth);
		}
		if (tapInput < _tapInput.getItemCount()) {
			_tapInput.setSelectedIndex(tapInput);
		}
		if (tapSynth < _tapSynth.getItemCount()) {
			_tapSynth.setSelectedIndex(tapSynth);
		}
	}
	
	public int getToneSynthIndex() { return _toneSynth.getSelectedIndex(); }
	public int getTapInputIndex() { return _tapInput.getSelectedIndex(); }
	public int getTapSynthIndex() { return _tapSynth.getSelectedIndex(); }

}