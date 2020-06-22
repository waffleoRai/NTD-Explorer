package waffleoRai_NTDExGUI.panels.preview;

import waffleoRai_NTDExGUI.DisposableJPanel;
import waffleoRai_NTDExGUI.dialogs.BankSelectDialog;
import waffleoRai_NTDExGUI.dialogs.SetTextDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.forms.NinSeqPlayerFrame;
import waffleoRai_SeqSound.ninseq.NinSeq;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileNode;
import waffleoRai_brseqStudioGUI.FullSeqPanel;
import waffleoRai_soundbank.SoundbankDef;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.border.BevelBorder;

import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;

public class NinSeqPreviewPanel extends DisposableJPanel{

	private static final long serialVersionUID = -4917016248817511934L;
	
	public static final int MIN_WIDTH = 400;
	public static final int HEIGHT_CTRLPNL = 50;
	
	private Frame parent;
	
	private FullSeqPanel pnlView;
	//private JPanel wbdummy;
	
	private FileNode node;
	
	//private NinSeqDataSource sequence;
	private NinSeq sequence;
	private SynthBank bank;
	
	private String name_seq;
	private String name_bnk;
	
	private JLabel lblSeqname;
	private JLabel lblBankname;
	
	public NinSeqPreviewPanel(Frame parent){
		this.parent = parent;
		initGUI();
	}
	
	private void initGUI(){
		
		//TODO sizing
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlControl = new JPanel();
		pnlControl.setMinimumSize(new Dimension(MIN_WIDTH, HEIGHT_CTRLPNL));
		pnlControl.setPreferredSize(new Dimension(MIN_WIDTH, HEIGHT_CTRLPNL));
		pnlControl.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		pnlControl.setLayout(null);
		GridBagConstraints gbc_pnlControl = new GridBagConstraints();
		gbc_pnlControl.weighty = 0.1;
		gbc_pnlControl.insets = new Insets(0, 0, 5, 0);
		gbc_pnlControl.fill = GridBagConstraints.BOTH;
		gbc_pnlControl.gridx = 0;
		gbc_pnlControl.gridy = 0;
		add(pnlControl, gbc_pnlControl);
		
		JLabel lblTitle = new JLabel("Title:");
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblTitle.setBounds(10, 11, 33, 14);
		pnlControl.add(lblTitle);
		
		lblSeqname = new JLabel("(title)");
		lblSeqname.setBounds(45, 11, 215, 14);
		pnlControl.add(lblSeqname);
		
		JLabel lblBank = new JLabel("Bank:");
		lblBank.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBank.setBounds(10, 36, 33, 14);
		pnlControl.add(lblBank);
		
		lblBankname = new JLabel("(bank)");
		lblBankname.setBounds(45, 36, 215, 14);
		pnlControl.add(lblBankname);
		
		JButton btnPlayer = new JButton("Launch Player");
		btnPlayer.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnPlayer.setBounds(270, 32, 122, 23);
		pnlControl.add(btnPlayer);
		btnPlayer.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				onLaunchPlayer();
			}
			
		});
		
		JButton btnSetTitle = new JButton("Set Title");
		btnSetTitle.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnSetTitle.setBounds(270, 7, 122, 23);
		pnlControl.add(btnSetTitle);
		btnSetTitle.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				onChangeName();
			}
			
		});
		
		pnlView = new FullSeqPanel();
		//wbdummy = new JPanel();
		GridBagConstraints gbc_pnlView = new GridBagConstraints();
		gbc_pnlView.weighty = 1.0;
		gbc_pnlView.fill = GridBagConstraints.BOTH;
		gbc_pnlView.gridx = 0;
		gbc_pnlView.gridy = 1;
		add(pnlView, gbc_pnlView);
		//add(wbdummy, gbc_pnlView);
	}

	public String getSeqName(){
		return name_seq;
	}
	
	public void loadSeq(FileNode seqnode, NinSeq seq, String name){
		node = seqnode;
		sequence = seq;
		name_seq = name;
		if(name == null || name.isEmpty()) name_seq = seqnode.getFileName();
		lblSeqname.setText(name_seq);
		lblSeqname.repaint();
		
		pnlView.loadSeq(sequence.getSequenceData());
	}
	
	public void loadBank(SynthBank bnk, String name){
		bank = bnk;
		name_bnk = name;
		lblBankname.setText(name_bnk);
		lblBankname.repaint();
	}
	
	private static void scanForBanks(DirectoryNode dn, Map<String, SynthBank> bnkmap){
		List<FileNode> children = dn.getChildren();
		
		List<DirectoryNode> dchildren = new LinkedList<DirectoryNode>();
		for(FileNode child : children){
			if(child instanceof DirectoryNode){
				dchildren.add((DirectoryNode)child);
			}
			else{
				FileTypeNode tail = child.getTypeChainTail();
				if(tail.getFileClass() == FileClass.SOUNDBANK){
					//Check if definition has methods to read soundbank
					FileTypeDefinition def = tail.getTypeDefinition();
					if(def instanceof SoundbankDef){
						//Get ID and read as bank
						SoundbankDef sbdef = (SoundbankDef)def;
						String id = sbdef.getBankIDKey(child);
						//If already in map, just skip
						if(bnkmap.get(id) != null)continue;
						SynthBank bank = sbdef.getPlayableBank(child);
						bnkmap.put(id, bank);
					}
				}
			}
		}
		
		for(DirectoryNode child : dchildren){
			scanForBanks(child, bnkmap);
		}
	}
	
	private void onChangeName(){
		//Dialog
		
		if(parent == null){
			System.err.println("Can't launch change name dialog without parent frame!");
			return;
		}
		
		SetTextDialog dialog = new SetTextDialog(parent, "Set Sequence Name", name_seq);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		name_seq = dialog.getText();
		lblSeqname.setText(name_seq);
		lblSeqname.repaint();
		dialog.dispose();
		
		node.setMetadataValue("TITLE", name_seq);
	}
	
	private void onLaunchPlayer(){
	
		//If sequence is null, show error and return
		if(sequence == null){
			showError("No sequence loaded!");
			return;
		}
		
		//--- Scan ROM for banks and generate bank map
		Map<String, SynthBank> bnkmap = new HashMap<String, SynthBank>();
		//Spawn progress dialog in case this takes a lot of time?
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Scanning for SoundBanks");
		dialog.setPrimaryString("Scanning");
		dialog.setSecondaryString("Searching image tree for marked soundbanks");
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					scanForBanks(node.getParent(), bnkmap);
				}
				catch(Exception x)
				{
					x.printStackTrace();
					JOptionPane.showMessageDialog(parent, 
							"Unknown Error: Exception thrown during bank scan! See stderr for details.", 
							"Bank Scan Error", JOptionPane.WARNING_MESSAGE);
				}
				
				return null;
			}
			
			public void done(){
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
		
		//Wait for task to complete
		while(!task.isDone()){
			try {Thread.sleep(10);} 
			catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
		
		//--- If there is no default bank, ask user which bank to load
		SynthBank initbank = bank;
		if(bank == null){
			JOptionPane.showMessageDialog(this, "Please choose a soundbank to initialize player.", 
					"No Default Bank", JOptionPane.WARNING_MESSAGE);
			
			List<SynthBank> blist = new LinkedList<SynthBank>();
			List<String> keys = new LinkedList<String>();
			keys.addAll(bnkmap.keySet());
			Collections.sort(keys);
			for(String k : keys) blist.add(bnkmap.get(k));
			
			BankSelectDialog dlg = new BankSelectDialog(parent, blist);
			dlg.setLocationRelativeTo(parent);
			dlg.setVisible(true);
			
			if(!dlg.getConfirmed()) return;
			initbank = dlg.getSelection();
			
			dlg.dispose();
		}
		
		//--- Load the player GUI
		NinSeqPlayerFrame pframe = new NinSeqPlayerFrame(sequence, initbank);
		pframe.setSequenceName(name_seq);
		pframe.setBankName(name_bnk);
		pframe.addAlternateBanks(bnkmap);
		pframe.setLocationRelativeTo(parent);
		pframe.setVisible(true);
		
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void showError(String text){
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	
}
