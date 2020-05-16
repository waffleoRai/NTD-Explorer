package waffleoRai_NTDExGUI.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.seq.SeqLoader;
import waffleoRai_NTDExGUI.dialogs.BankSelectDialog;
import waffleoRai_NTDExGUI.dialogs.SpinnerDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_seqplayerGUI.SeqPlayerPanel;

public class GeneralSeqPlayerFrame extends JFrame{

	private static final long serialVersionUID = -4808500042994643731L;
	
	public static final int MIN_WIDTH = 700;
	public static final int MIN_HEIGHT = 400;
	
	private SeqLoader seq_data;
	private SeqPlayerPanel pnlSeqPlay;

	public GeneralSeqPlayerFrame(SeqLoader seqdat){
		seq_data = seqdat;
		initGUI();
	}
	
	private void initGUI(){
		setTitle("Sequence Player");
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
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
		
		initializePlayerPanel();
		
		this.addWindowListener(new WindowAdapter(){
			
			public void windowClosing(WindowEvent e){
				onClose();
			}
		});

	}
	
	public void render(){
		pack();
		setVisible(true);
	}
	
	private void initializePlayerPanel(){
		
		if(pnlSeqPlay != null)disposePlayerPanel();
		pnlSeqPlay = new SeqPlayerPanel(seq_data.getPlayer(), seq_data.getSpecialPanel());
		this.getContentPane().add(pnlSeqPlay);
	}
	
	private void onChangeBank(){

		Map<String, SynthBank> other_banks = seq_data.getAllBanks();
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
			seq_data.setBank(bnk);
			initializePlayerPanel();
		}
		
		dialog.dispose();
	}
	
	private void onExportMidi(){
		
		//Will use values current set in panel table.
		//1. Choose path (don't forget to save to ini)
		//2. Write
				
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
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					String outpath = dir + File.separator + seq_data.getSequenceName() + ".mid";
					if(!seq_data.exportMIDI(outpath)){
						JOptionPane.showMessageDialog(me, 
								"Unknown Error: MIDI Export Failed! See stderr for details.", 
								"MIDI Conversion Error", JOptionPane.ERROR_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(me, 
								"MIDI export complete!", 
								"MIDI Conversion", JOptionPane.INFORMATION_MESSAGE);
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
				dialog.dispose();
			}
		};
		
		task.execute();
		dialog.render();
		
	}

	private void onExportWav(){
		
		//1. Ask user how many loops to export (default is 2)
		//2. Choose path (don't forget to save to ini)
		//3. Write
		
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

		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try{
					String outpath = dir + File.separator + seq_data.getSequenceName() + ".wav";
					if(!seq_data.exportWAVE(outpath, loops)){
						JOptionPane.showMessageDialog(me, 
								"Unknown Error: WAV Export Failed! See stderr for details.", 
								"WAV Render Error", JOptionPane.ERROR_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(me, 
								"WAV written successfully to " + outpath, 
								"WAV Export", JOptionPane.INFORMATION_MESSAGE);
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
				dialog.dispose();
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
		
	public void disposePlayerPanel(){
		pnlSeqPlay.dispose();
		this.getContentPane().removeAll();
		pnlSeqPlay = null;
	}
	
	public void onClose(){
		//Stop player
		//seq_data.stopPlayback();
		
		//Clean up
		seq_data.reset();
		disposePlayerPanel();
		this.dispose();
	}
	
}
