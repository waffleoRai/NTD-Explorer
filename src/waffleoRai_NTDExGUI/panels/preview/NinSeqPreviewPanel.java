package waffleoRai_NTDExGUI.panels.preview;

import waffleoRai_NTDExGUI.DisposableJPanel;
import waffleoRai_SeqSound.ninseq.NinSeqDataSource;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_brseqStudioGUI.FullSeqPanel;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;

public class NinSeqPreviewPanel extends DisposableJPanel{

	private static final long serialVersionUID = -4917016248817511934L;
	
	public static final int MIN_WIDTH = 400;
	
	private FullSeqPanel pnlView;
	
	private NinSeqDataSource sequence;
	private SynthBank bank;
	
	private String name_seq;
	private String name_bnk;
	
	private JLabel lblSeqname;
	private JLabel lblBankname;
	
	public NinSeqPreviewPanel(){
		initGUI();
	}
	
	private void initGUI(){
		
		//TODO sizing
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlControl = new JPanel();
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
		GridBagConstraints gbc_pnlView = new GridBagConstraints();
		gbc_pnlView.weighty = 1.0;
		gbc_pnlView.fill = GridBagConstraints.BOTH;
		gbc_pnlView.gridx = 0;
		gbc_pnlView.gridy = 1;
		add(pnlView, gbc_pnlView);
	}

	public String getSeqName(){
		return name_seq;
	}
	
	public void loadSeq(NinSeqDataSource seq, String name){
		//TODO
	}
	
	public void loadBank(SynthBank bnk, String name){
		//TODO
	}
	
	private void onChangeName(){
		//TODO
	}
	
	private void onLaunchPlayer(){
		//TODO
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
