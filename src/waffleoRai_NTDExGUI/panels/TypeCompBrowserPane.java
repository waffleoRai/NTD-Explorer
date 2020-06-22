package waffleoRai_NTDExGUI.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.definitions.CompressionDefs;

public class TypeCompBrowserPane extends JPanel{

	private static final long serialVersionUID = 575208658481593139L;
	
	private JList<AbstractCompDef> list;
	private JButton btnUpdate;

	public TypeCompBrowserPane(boolean allow_multi_select){
		initGUI(allow_multi_select);
		updateList();
	}
	
	private void initGUI(boolean allow_multi_select){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		list = new JList<AbstractCompDef>();
		if(!allow_multi_select) list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
		
		JPanel pnlCtrl = new JPanel();
		GridBagConstraints gbc_pnlCtrl = new GridBagConstraints();
		gbc_pnlCtrl.fill = GridBagConstraints.BOTH;
		gbc_pnlCtrl.gridx = 0;
		gbc_pnlCtrl.gridy = 1;
		add(pnlCtrl, gbc_pnlCtrl);
		GridBagLayout gbl_pnlCtrl = new GridBagLayout();
		gbl_pnlCtrl.columnWidths = new int[]{0, 0, 0};
		gbl_pnlCtrl.rowHeights = new int[]{0, 0};
		gbl_pnlCtrl.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlCtrl.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlCtrl.setLayout(gbl_pnlCtrl);
		
		btnUpdate = new JButton("Update");
		GridBagConstraints gbc_btnUpdate = new GridBagConstraints();
		gbc_btnUpdate.insets = new Insets(5, 5, 5, 5);
		gbc_btnUpdate.gridx = 1;
		gbc_btnUpdate.gridy = 0;
		pnlCtrl.add(btnUpdate, gbc_btnUpdate);
		
	}
	
	public void updateList(){
		list.setEnabled(false);
		
		DefaultListModel<AbstractCompDef> model = new DefaultListModel<AbstractCompDef>();
		Collection<AbstractCompDef> alldefs = CompressionDefs.getAllRegisteredDefinitions();
		for(AbstractCompDef d : alldefs)model.addElement(d);
		
		list.setModel(model);
		list.repaint();
		list.setEnabled(true);
	}
	
	public void setButtonText(String txt){
		btnUpdate.setText(txt);
		btnUpdate.repaint();
	}
	
	public void addActionListener(ActionListener l){
		btnUpdate.addActionListener(l);
	}
	
	public List<AbstractCompDef> getAllSelected(){
		return list.getSelectedValuesList();
	}
	
	public AbstractCompDef getSelected(){
		return list.getSelectedValue();
	}
	
}
