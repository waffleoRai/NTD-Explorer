package waffleoRai_NTDExGUI.panels.preview.seq;

import java.util.List;

import waffleoRai_NTDExGUI.DisposableJPanel;
import waffleoRai_NTDExGUI.dialogs.SetTextDialog;
import waffleoRai_NTDExGUI.forms.GeneralSeqPlayerFrame;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JComboBox;
import javax.swing.border.BevelBorder;

import waffleoRai_NTDExCore.seq.SeqLoader;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

public class GeneralSeqPreviewPanel extends DisposableJPanel{

	private static final long serialVersionUID = 2997223548632518553L;

	private Frame parent;
	private JComboBox<GenSeqTrack> cmbxTrack;
	
	private JLabel lblBank;
	private JLabel lblTitle;
	private GeneralSeqEventTable pnlBot;
	
	private SeqLoader seq_data;
	
	private GeneralSeqPlayerFrame openFrame;
	
	public GeneralSeqPreviewPanel(Frame parent, SeqLoader seqdat){
		this.parent = parent;
		seq_data = seqdat;
		initGUI();
		loadTrackList(seqdat.getTrackData());
	}
	
	private void initGUI(){
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{75, 40, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlTop = new JPanel();
		pnlTop.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		GridBagConstraints gbc_pnlTop = new GridBagConstraints();
		gbc_pnlTop.insets = new Insets(0, 0, 5, 0);
		gbc_pnlTop.fill = GridBagConstraints.BOTH;
		gbc_pnlTop.gridx = 0;
		gbc_pnlTop.gridy = 0;
		add(pnlTop, gbc_pnlTop);
		GridBagLayout gbl_pnlTop = new GridBagLayout();
		gbl_pnlTop.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlTop.rowHeights = new int[]{0, 0, 0};
		gbl_pnlTop.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlTop.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		pnlTop.setLayout(gbl_pnlTop);
		
		JLabel lblT = new JLabel("Title:");
		lblT.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblT = new GridBagConstraints();
		gbc_lblT.insets = new Insets(5, 10, 5, 5);
		gbc_lblT.gridx = 0;
		gbc_lblT.gridy = 0;
		pnlTop.add(lblT, gbc_lblT);
		
		lblTitle = new JLabel("[none]");
		if(seq_data != null) lblTitle.setText(seq_data.getSequenceName());
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.anchor = GridBagConstraints.WEST;
		gbc_lblTitle.insets = new Insets(5, 0, 5, 5);
		gbc_lblTitle.gridx = 1;
		gbc_lblTitle.gridy = 0;
		pnlTop.add(lblTitle, gbc_lblTitle);
		lblTitle.repaint();
		
		JButton btnTitle = new JButton("Set Title");
		GridBagConstraints gbc_btnTitle = new GridBagConstraints();
		gbc_btnTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnTitle.insets = new Insets(0, 0, 5, 10);
		gbc_btnTitle.gridx = 2;
		gbc_btnTitle.gridy = 0;
		pnlTop.add(btnTitle, gbc_btnTitle);
		btnTitle.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onChangeTitle();
			}
			
		});
		
		JLabel lblB = new JLabel("Bank:");
		lblB.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblB = new GridBagConstraints();
		gbc_lblB.insets = new Insets(0, 10, 0, 5);
		gbc_lblB.gridx = 0;
		gbc_lblB.gridy = 1;
		pnlTop.add(lblB, gbc_lblB);
		
		lblBank = new JLabel("[none]");
		if(seq_data != null) lblBank.setText(seq_data.getBankName());
		lblBank.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblBank = new GridBagConstraints();
		gbc_lblBank.anchor = GridBagConstraints.WEST;
		gbc_lblBank.insets = new Insets(0, 0, 0, 5);
		gbc_lblBank.gridx = 1;
		gbc_lblBank.gridy = 1;
		pnlTop.add(lblBank, gbc_lblBank);
		lblBank.repaint();
		
		JButton btnPlay = new JButton("Launch Player");
		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.insets = new Insets(0, 0, 0, 10);
		gbc_btnPlay.gridx = 2;
		gbc_btnPlay.gridy = 1;
		pnlTop.add(btnPlay, gbc_btnPlay);
		btnPlay.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				openPlayerFrame();
			}
			
		});
		
		JPanel pnlMid = new JPanel();
		pnlMid.setLayout(null);
		GridBagConstraints gbc_pnlMid = new GridBagConstraints();
		gbc_pnlMid.insets = new Insets(0, 0, 5, 0);
		gbc_pnlMid.fill = GridBagConstraints.BOTH;
		gbc_pnlMid.gridx = 0;
		gbc_pnlMid.gridy = 1;
		add(pnlMid, gbc_pnlMid);
		
		JLabel lblTrack = new JLabel("Track:");
		lblTrack.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblTrack.setBounds(10, 11, 46, 14);
		pnlMid.add(lblTrack);
		
		cmbxTrack = new JComboBox<GenSeqTrack>();
		cmbxTrack.setBounds(66, 8, 198, 20);
		pnlMid.add(cmbxTrack);
		cmbxTrack.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				int idx = cmbxTrack.getSelectedIndex();
				//System.err.println("Track at idx " + idx + " is null? " + (cmbxTrack.getItemAt(idx) == null));
				loadTable(cmbxTrack.getItemAt(idx));
			}
			
		});
		
		pnlBot = new GeneralSeqEventTable(null);
		GridBagConstraints gbc_pnlBot = new GridBagConstraints();
		gbc_pnlBot.fill = GridBagConstraints.BOTH;
		gbc_pnlBot.gridx = 0;
		gbc_pnlBot.gridy = 2;
		add(pnlBot, gbc_pnlBot);
		
	}
	
	private void loadTrackList(List<GenSeqTrack> tracks){

		DefaultComboBoxModel<GenSeqTrack> model = new DefaultComboBoxModel<GenSeqTrack>();
		for(GenSeqTrack t : tracks) model.addElement(t);
		cmbxTrack.setModel(model);
		cmbxTrack.repaint();
		
		if(!tracks.isEmpty()) loadTable(cmbxTrack.getItemAt(0));
		else loadTable(null);
	}
	
	private void loadTable(GenSeqTrack track){
		pnlBot.setTrack(track);
		pnlBot.repaint();
	}
	
	private void onChangeTitle(){
		
		if(parent == null){
			System.err.println("Can't launch change name dialog without parent frame!");
			return;
		}
		
		SetTextDialog dialog = new SetTextDialog(parent, "Set Sequence Name");
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		String name_seq = dialog.getText();
		lblTitle.setText(name_seq);
		lblTitle.repaint();
		dialog.dispose();
		
		seq_data.setSequenceName(name_seq);
	}
	
	private void openPlayerFrame(){
		if(openFrame != null){
			openFrame.setVisible(false);
		}
		
		openFrame = new GeneralSeqPlayerFrame(seq_data);
		openFrame.setLocationRelativeTo(parent);
		openFrame.render();
	}
	
	public void dispose() {
		if(openFrame != null){
			openFrame.setVisible(false);
		}
		seq_data.dispose();
		cmbxTrack.setModel(new DefaultComboBoxModel<GenSeqTrack>());
	}
	
}
