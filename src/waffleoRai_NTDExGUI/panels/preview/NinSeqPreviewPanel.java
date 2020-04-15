package waffleoRai_NTDExGUI.panels.preview;

import waffleoRai_NTDExGUI.DisposableJPanel;
import waffleoRai_NTDExGUI.dialogs.SetTextDialog;
import waffleoRai_SeqSound.ninseq.NinSeqDataSource;
import waffleoRai_SeqSound.ninseq.NinSeqSynthPlayer;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_brseqStudioGUI.FullSeqPanel;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.border.BevelBorder;

public class NinSeqPreviewPanel extends DisposableJPanel{

	private static final long serialVersionUID = -4917016248817511934L;
	
	public static final int MIN_WIDTH = 400;
	public static final int HEIGHT_CTRLPNL = 50;
	
	private Frame parent;
	
	private FullSeqPanel pnlView;
	//private JPanel wbdummy;
	
	private NinSeqDataSource sequence;
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
	
	public void loadSeq(NinSeqDataSource seq, String name){
		sequence = seq;
		name_seq = name;
		lblSeqname.setText(name);
		lblSeqname.repaint();
		
		pnlView.loadSeq(sequence);
	}
	
	public void loadBank(SynthBank bnk, String name){
		bank = bnk;
		name_bnk = name;
		lblBankname.setText(name);
		lblBankname.repaint();
	}
	
	private void onChangeName(){
		//Dialog
		
		if(parent == null){
			System.err.println("Can't launch change name dialog without parent frame!");
			return;
		}
		
		SetTextDialog dialog = new SetTextDialog(parent, "Set Sequence Name");
		dialog.setVisible(true);
		name_seq = dialog.getText();
		lblSeqname.setText(name_seq);
		lblSeqname.repaint();
		dialog.dispose();
	}
	
	private void onLaunchPlayer(){
		//TODO
		//If sequence is null, show error and return
		if(sequence == null){
			showError("No sequence loaded!");
			return;
		}
		
		
		//If there is no bank, scan for banks in ROM and ask user to choose one
		if(bank == null){
			
		}
		
		//generate player
		NinSeqSynthPlayer player = new NinSeqSynthPlayer(sequence, bank, 0);
		
		//Generate new frame with the seq panel
		//Include menu option to change bank
		
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void showError(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	
}
