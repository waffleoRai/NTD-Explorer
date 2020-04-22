package waffleoRai_NTDExGUI.panels.preview.soundbank;

import javax.swing.JPanel;

import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_SoundSynth.SynthChannel;
import waffleoRai_SoundSynth.SynthProgram;
import waffleoRai_SoundSynth.general.DefaultSynthChannel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

public class SoundbankKeyboardPanel extends JPanel implements KeyboardListener{

	private static final long serialVersionUID = -5397255504891543838L;
	
	private SynthBank playbank;
	private JComboBox<Integer> cmbxBank;
	private JComboBox<Integer> cmbxProg;
	private MidiKeyboardPanel pnlPiano;
	
	private SourceDataLine outline;
	private SynthChannel channel;
	private PlayerWorker player;
	
	public SoundbankKeyboardPanel(){
		initGUI();
	}

	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlControl = new JPanel();
		pnlControl.setLayout(null);
		GridBagConstraints gbc_pnlControl = new GridBagConstraints();
		gbc_pnlControl.insets = new Insets(0, 0, 5, 0);
		gbc_pnlControl.fill = GridBagConstraints.BOTH;
		gbc_pnlControl.gridx = 0;
		gbc_pnlControl.gridy = 0;
		add(pnlControl, gbc_pnlControl);
		
		JLabel lblBank = new JLabel("Bank:");
		lblBank.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBank.setBounds(10, 11, 33, 14);
		pnlControl.add(lblBank);
		
		cmbxBank = new JComboBox<Integer>();
		cmbxBank.setBounds(41, 8, 70, 20);
		pnlControl.add(cmbxBank);
		cmbxBank.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				onProgramSelect();
			}
			
		});
		
		JLabel lblProgram = new JLabel("Program:");
		lblProgram.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblProgram.setBounds(123, 11, 44, 14);
		pnlControl.add(lblProgram);
		
		cmbxProg = new JComboBox<Integer>();
		cmbxProg.setBounds(175, 8, 70, 20);
		pnlControl.add(cmbxProg);
		cmbxProg.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				onProgramSelect();
			}
			
		});
		
		JPanel pnlBottom = new JPanel();
		GridBagConstraints gbc_pnlBottom = new GridBagConstraints();
		gbc_pnlBottom.fill = GridBagConstraints.BOTH;
		gbc_pnlBottom.gridx = 0;
		gbc_pnlBottom.gridy = 1;
		add(pnlBottom, gbc_pnlBottom);
		GridBagLayout gbl_pnlBottom = new GridBagLayout();
		gbl_pnlBottom.columnWidths = new int[]{0, 0};
		gbl_pnlBottom.rowHeights = new int[]{0, 0};
		gbl_pnlBottom.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlBottom.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlBottom.setLayout(gbl_pnlBottom);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		pnlBottom.add(scrollPane, gbc_scrollPane);
		
		pnlPiano = new MidiKeyboardPanel();
		scrollPane.setViewportView(pnlPiano);
		pnlPiano.addListener(this);
	}
	
	private class PlayerWorker implements Runnable{

		private volatile boolean killflag;
		private volatile boolean done;
		
		@Override
		public void run() {
			while(!killflag){
				try 
				{
					int[] samps = channel.nextSample();
					byte[] bytes = new byte[samps.length * 2];
					
					int j = 0;
					for(int c = 0; c < samps.length; c++){
						int s = samps[c];
						//byte[] bytes = new byte[2];
						bytes[j] = (byte)(s & 0xFF);
						bytes[j+1] = (byte)((s >>> 8) & 0xFF);
						j+=2;
					}
					outline.write(bytes, 0, bytes.length);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
					showError("Unexpected playback interruption!");
					closePlayChannel();
					done = true;
					return;
				}
				
			}	
			done = true;
		}
		
		public synchronized void terminate(){
			killflag = true;
		}
		
		public boolean isDone(){return done;}
		
	}
	
	public void openPlayChannel(){

		if(playbank == null){
			showWarning("Please load a bank!");
			return;
		}
		
		channel = new DefaultSynthChannel(44100, 16);
		channel.setProgram(getSelectedProgram());
		
		AudioFormat fmt = new AudioFormat(44100, 16, 2, true, false);
		
		try 
		{
			outline = AudioSystem.getSourceDataLine(fmt);
			outline.open();
			outline.start();
		} 
		catch (LineUnavailableException e) {
			e.printStackTrace();
			showError("Could not initialize playback!");
			channel = null;
			outline.stop();
			outline.close();
			outline = null;
		}
		
		player = new PlayerWorker();
		Thread t = new Thread(player);
		t.setName("SoundbankKeyboardPlayer");
		t.setDaemon(true);
		t.start();
	}
	
	public void closePlayChannel(){

		if(channel != null) channel.allNotesOff();
		
		if(player != null){
			player.terminate();
			while(!player.isDone()){
				try {Thread.sleep(10);} 
				catch (InterruptedException e) {e.printStackTrace(); break;}
			}
		}
		
		channel = null;
		if(outline != null){
			outline.stop();
			outline.close();
			outline = null;
		}
		player = null;
	}
	
	public void loadBank(SynthBank bank, Collection<Integer> usableBanks, Collection<Integer> usableProgs){
		playbank = bank;
		
		List<Integer> ilist = new LinkedList<Integer>();
		if(usableBanks != null) ilist.addAll(usableBanks);
		if(ilist.isEmpty()) ilist.add(0);
		Collections.sort(ilist);
		
		DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<Integer>();
		for(Integer i : ilist)model.addElement(i);
		cmbxBank.setModel(model);
		cmbxBank.repaint();
		
		ilist = new LinkedList<Integer>();
		if(usableProgs != null) ilist.addAll(usableProgs);
		if(ilist.isEmpty()) ilist.add(0);
		Collections.sort(ilist);
		
		model = new DefaultComboBoxModel<Integer>();
		for(Integer i : ilist)model.addElement(i);
		cmbxProg.setModel(model);
		cmbxProg.repaint();
	}
	
	private SynthProgram getSelectedProgram(){
		if(playbank == null) return null;
		int bankno = (Integer)cmbxBank.getSelectedItem();
		int progno = (Integer)cmbxProg.getSelectedItem();
		
		SynthProgram p = playbank.getProgram(bankno, progno);
		
		return p;
	}
	
	public void onProgramSelect(){
		if(channel == null) return;
		channel.allNotesOff();
		channel.setProgram(getSelectedProgram());
	}
	
	public void onNotePressed(int note, int vel){
		//System.err.println("Note pressed: " + note);
		if(channel == null) return;
		//System.err.println("Channel non null");
		try {channel.noteOn((byte)note, (byte)vel);} 
		catch (InterruptedException e) {e.printStackTrace();}
	}
	
	public void onNoteReleased(int note){
		if(channel == null) return;
		channel.noteOff((byte)note, (byte)0);
	}
	
	public void allNotesOff(){
		if(channel == null) return;
		channel.allNotesOff();
	}
	
	public void showWarning(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
}
