package waffleoRai_NTDExGUI.dialogs;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import waffleoRai_SeqSound.ninseq.NinSeqLabel;

public class LabelSelectDialog extends JDialog{


	private static final long serialVersionUID = 3508956974568535048L;
	
	private boolean okay;
	private NinSeqLabel select;
	
	public LabelSelectDialog(Frame parent, Collection<NinSeqLabel> srclist){
		super(parent, true);
		setTitle("Select Label");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane spBanks = new JScrollPane();
		spBanks.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spBanks = new GridBagConstraints();
		gbc_spBanks.insets = new Insets(5, 5, 5, 5);
		gbc_spBanks.fill = GridBagConstraints.BOTH;
		gbc_spBanks.gridx = 0;
		gbc_spBanks.gridy = 0;
		getContentPane().add(spBanks, gbc_spBanks);
		
		JList<NinSeqLabel> lstLabels = new JList<NinSeqLabel>();
		spBanks.setViewportView(lstLabels);
		
		JPanel pnlControl = new JPanel();
		GridBagConstraints gbc_pnlControl = new GridBagConstraints();
		gbc_pnlControl.insets = new Insets(5, 5, 5, 5);
		gbc_pnlControl.fill = GridBagConstraints.BOTH;
		gbc_pnlControl.gridx = 0;
		gbc_pnlControl.gridy = 1;
		getContentPane().add(pnlControl, gbc_pnlControl);
		pnlControl.setLayout(new BoxLayout(pnlControl, BoxLayout.X_AXIS));
		
		JButton btnConfirm = new JButton("Confirm");
		pnlControl.add(btnConfirm);
		btnConfirm.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				okay = true;
				select = lstLabels.getSelectedValue();
				closeMe();
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		pnlControl.add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				okay = false;
				select = null;
				closeMe();
			}
			
		});
		
		loadList(srclist, lstLabels);
	}
	
	private void loadList(Collection<NinSeqLabel> srclist, JList<NinSeqLabel> jlist){
		if(srclist == null){
			jlist.setModel(new DefaultListModel<NinSeqLabel>());
			return;
		}
		
		DefaultListModel<NinSeqLabel> model = new DefaultListModel<NinSeqLabel>();
		for(NinSeqLabel l : srclist){model.addElement(l);}
		jlist.setModel(model);
		jlist.repaint();
	}
	
	public boolean getConfirmed(){return okay;}
	
	public NinSeqLabel getSelection(){return select;}
	
	public void closeMe(){
		this.setVisible(false);
	}
	
}
