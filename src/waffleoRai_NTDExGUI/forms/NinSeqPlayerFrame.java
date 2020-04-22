package waffleoRai_NTDExGUI.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExGUI.dialogs.BankSelectDialog;
import waffleoRai_NTDExGUI.dialogs.LabelSelectDialog;
import waffleoRai_NTDExGUI.dialogs.SpinnerDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_SeqSound.ninseq.NinSeq;
import waffleoRai_SeqSound.ninseq.NinSeqLabel;
import waffleoRai_SeqSound.ninseq.NinSeqSynthPlayer;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_seqplayerGUI.SeqPlayerPanel;
import waffleoRai_seqplayerGUI.ninseq.NinSeqRegisterViewPanel;

/*
 * Mostly the existing player panel, but has
 * menu allowing user to change banks, jump to a different label,
 * export MIDI, or export WAV
 */

public class NinSeqPlayerFrame extends JFrame{

	private static final long serialVersionUID = 2621467896838617387L;
	
	private SeqPlayerPanel pnlSeqPlay;
	private NinSeqRegisterViewPanel regpnl;
	
	private NinSeqSynthPlayer player;
	
	private NinSeq sequence;
	private SynthBank bnkDefo;
	private SynthBank current_bank;
	
	private long address;
	private int lbl_idx;
	private Map<String, SynthBank> other_banks;
	
	public NinSeqPlayerFrame(NinSeq seq, SynthBank defo_bank){
		sequence = seq;
		bnkDefo = defo_bank;
		//other_banks = new LinkedList<SynthBank>();
		other_banks = new HashMap<String, SynthBank>();
		initGUI();
	}
	
	private void initGUI(){
		setTitle("Sequence Player");
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);
		
		JMenuItem mntmChangeBank = new JMenuItem("Change Bank...");
		mnOptions.add(mntmChangeBank);
		mntmChangeBank.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				onChangeBank();
			}
			
		});
		
		JMenuItem mntmJumpToLabel = new JMenuItem("Jump To Label...");
		mnOptions.add(mntmJumpToLabel);
		mntmJumpToLabel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				onJumpToLabel();
			}
			
		});
		
		JMenuItem mntmExportMidi = new JMenuItem("Export MIDI...");
		mnOptions.add(mntmExportMidi);
		mntmExportMidi.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				onExportMidi();
			}
			
		});
		
		JMenuItem mntmExportWav = new JMenuItem("Export WAV...");
		mnOptions.add(mntmExportWav);
		mntmExportWav.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				onExportWav();
			}
			
		});
		
		//pnlSeqPlay = new SeqPlayerPanel();
		address = 0;
		if(sequence.getLabelCount() > 0){
			NinSeqLabel lbl = sequence.getLabel(0);
			if(lbl != null){
				address = lbl.getAddress();
			}
		}
		initializePlayerPanel(bnkDefo, address);
	}
	
	public void render(){
		pack();
		setVisible(true);
	}
	
	private void initializePlayerPanel(SynthBank bank, long addr){
		
		current_bank = bank;
		player = new NinSeqSynthPlayer(sequence.getSequenceData(), bank, addr);
		regpnl = new NinSeqRegisterViewPanel();
		regpnl.loadPlayer(player);
		
		pnlSeqPlay = new SeqPlayerPanel(player, regpnl);
		this.removeAll();
		this.setContentPane(pnlSeqPlay);
		this.repaint();
	}
	
	public void addAlternateBanks(Map<String, SynthBank> banks){
		if(banks == null){
			other_banks = null;
			return;
		}
		//other_banks = new ArrayList<SynthBank>(banks.size()+1);
		//other_banks.addAll(banks);
		other_banks.clear();
		for(String k : banks.keySet()) other_banks.put(k, banks.get(k)); 
	}
	
	private void onChangeBank(){

		if(other_banks == null || other_banks.isEmpty()){
			showWarning("No alternative banks loaded!");
			return;
		}
		
		//Spawn bank selection dialog
		BankSelectDialog dialog = new BankSelectDialog(this, other_banks.values());
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		
		if(dialog.getConfirmed()){
			//Change the bank
			SynthBank bnk = dialog.getSelection();
			initializePlayerPanel(bnk, address);
		}
		
		dialog.dispose();
	}
	
	private void onJumpToLabel(){
		//Look at labels, let user select, then jump to appropriate address
		
		if(sequence == null){
			showError("No sequence loaded!");
			return;
		}
		
		List<NinSeqLabel> llist = sequence.getLabels();
		if(llist == null || llist.isEmpty()){
			showWarning("Sequence has no alternate start points!");
			return;
		}
		
		LabelSelectDialog dialog = new LabelSelectDialog(this, llist);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		
		if(dialog.getConfirmed()){
			NinSeqLabel lbl = dialog.getSelection();
			lbl_idx = lbl.getIndex();
			String bid = Integer.toString(lbl.getBankID());
			SynthBank bnk = other_banks.get(bid);
			if(bnk == null) bnk = current_bank;
			if(lbl != null){
				initializePlayerPanel(bnk, lbl.getAddress());
				showInfo("Sequence start set to label \"" + lbl.getName() + "\"");
			}
		}
		
		dialog.dispose();
	}
	
	private void onExportMidi(){
		
		//Will use values current set in panel table.
		//1. Warn user that it will use values set in panel. Continue?
		//2. Choose whether to export all labels at once or just current
		//3. Choose path (don't forget to save to ini)
		//4. Write
		
		//Warn user about values
		String msg_title = "Initialization Values";
		String msg_message = "The values currently set in the form table will be used"
				+ "\n as the player initialization values for MIDI export.\n"
				+ "Is this okay?";
		int op = JOptionPane.showConfirmDialog(this, msg_message, msg_title, 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(op != JOptionPane.YES_OPTION) return;
		
		//Export all labels?
		List<NinSeqLabel> lbls = sequence.getLabels();
		boolean alllbls = false;
		if(lbls != null && !lbls.isEmpty()){
			msg_title = "Export All Labels";
			msg_message = "This sequence data file contains multiple subsequences\n"
					+ "(denoted by \"labels\").\n"
					+ "Would you like to export all subsequences?";
			op = JOptionPane.showConfirmDialog(this, msg_message, msg_title, 
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if(op == JOptionPane.CANCEL_OPTION) return;
			alllbls = (op == JOptionPane.YES_OPTION);
		}
		
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_MIDI_EXPORT));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int retVal = fc.showSaveDialog(this);
		if (retVal != JFileChooser.APPROVE_OPTION) return;
		
		//Spawn task and dialog
		String dir = fc.getSelectedFile().getAbsolutePath();
		//String targetpath = dir + File.separator + sequence.getName();
		NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_MIDI_EXPORT, dir);
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Export MIDI");
		dialog.setPrimaryString("Exporting MIDI");
		dialog.setSecondaryString("Writing to " + dir);
		
		JFrame me = this;
		final boolean alll = alllbls;
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					if(alll){
						int l = 0;
						for(NinSeqLabel lbl : lbls){
							String outpath = dir + File.separator + lbl.getName() + ".mid";
							dialog.setSecondaryString("Writing to " + outpath);
							short[] vals = regpnl.getValues();
							for(int i = 0; i < vals.length; i++) sequence.setVariable(i, vals[i]);
							sequence.writeMIDI(l++, outpath, true);
						}
					}
					else{
						String outpath = dir + File.separator + sequence.getName() + ".mid";
						//Load values
						short[] vals = regpnl.getValues();
						for(int i = 0; i < vals.length; i++) sequence.setVariable(i, vals[i]);
						sequence.writeMIDI(lbl_idx, outpath, true);
					}
				}
				catch(Exception x)
				{
					x.printStackTrace();
					JOptionPane.showMessageDialog(me, 
							"Unknown Error: MIDI Export Failed! See stderr for details.", 
							"MIDI Conversion Error", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			public void done()
			{
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
		
	}

	private void onExportWav(){
		
		//1. Ask user how many loops to export (default is 2)
		//2. Choose whether to export all labels at once or just current
		//3. Choose path (don't forget to save to ini)
		//4. Write
		
		//Ask for loop count
		SpinnerDialog spndia = new SpinnerDialog(this, "Export Loop Count", "Loops");
		spndia.setLocationRelativeTo(this);
		spndia.setVisible(true);
		
		if(!spndia.getConfirmed()) return;
		int loops = spndia.getValue();
		if(loops < 1){
			showError("Please set at least one loop!");
			return;
		}
		
		//Ask if export all subsequences
		List<NinSeqLabel> lbls = sequence.getLabels();
		boolean alllbls = false;
		if(lbls != null && !lbls.isEmpty()){
			String msg_title = "Export All Labels";
			String msg_message = "This sequence data file contains multiple subsequences\n"
					+ "(denoted by \"labels\").\n"
					+ "Would you like to export all subsequences?";
			int op = JOptionPane.showConfirmDialog(this, msg_message, msg_title, 
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if(op == JOptionPane.CANCEL_OPTION) return;
			alllbls = (op == JOptionPane.YES_OPTION);
		}
		
		//Choose path
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_SEQWAV_EXPORT));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int retVal = fc.showSaveDialog(this);
		if (retVal != JFileChooser.APPROVE_OPTION) return;
		
		//Spawn task and dialog
		String dir = fc.getSelectedFile().getAbsolutePath();
		NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_SEQWAV_EXPORT, dir);
		IndefProgressDialog dialog = new IndefProgressDialog(this, "Render WAVE");
		dialog.setPrimaryString("Exporting sound");
		dialog.setSecondaryString("Writing to " + dir);
		JFrame me = this;
		final boolean alll = alllbls;
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					if(alll){
						short[] vals = regpnl.getValues();
						for(NinSeqLabel lbl : lbls){
							String outpath = dir + File.separator + lbl.getName() + ".wav";
							//Get bank
							SynthBank lbank = other_banks.get(Integer.toString(lbl.getBankID()));
							if(lbank == null) lbank = current_bank;
							NinSeqSynthPlayer plr = new NinSeqSynthPlayer(sequence.getSequenceData(), lbank, lbl.getAddress());
							for(int i = 0; i < vals.length; i++) plr.setVariableValue(i, vals[i]);
							plr.writeMixdownTo(outpath, loops);
						}
					}
					else{
						String outpath = dir + File.separator + sequence.getName() + ".wav";
						//Spawn player for export
						NinSeqSynthPlayer plr = new NinSeqSynthPlayer(sequence.getSequenceData(), current_bank, address);
						//Load values
						short[] vals = regpnl.getValues();
						for(int i = 0; i < vals.length; i++) plr.setVariableValue(i, vals[i]);
						plr.writeMixdownTo(outpath, loops);
					}
				}
				catch(Exception x)
				{
					x.printStackTrace();
					JOptionPane.showMessageDialog(me, 
							"Unknown Error: WAV Export Failed! See stderr for details.", 
							"WAV Render Error", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			public void done(){
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
		
	}
	
	public void showWarning(String text){
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String text){
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void showInfo(String text){
		JOptionPane.showMessageDialog(this, text, "Info", JOptionPane.INFORMATION_MESSAGE);
	}
	
}
