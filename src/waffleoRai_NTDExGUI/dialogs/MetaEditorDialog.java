package waffleoRai_NTDExGUI.dialogs;

import java.awt.Frame;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import waffleoRai_Files.tree.FileNode;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.ListSelectionModel;

public class MetaEditorDialog extends JDialog{

	private static final long serialVersionUID = -6675763669646490031L;
	
	public static final int MIN_WIDTH = 500;
	public static final int MIN_HEIGHT = 360;
	
	private Frame parent;
	private JTable tblDisplay;
	private JList<String> lstKeyList;
	private JTextArea txtValueEditor;
	
	private Map<String, String> map;
	private FileNode node;
	
	public MetaEditorDialog(Frame parent){
		super(parent, true);
		this.parent = parent;
		map = new HashMap<String, String>();
		initGUI();
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setLocationRelativeTo(parent);	
		
		this.addWindowListener(new WindowAdapter(){
			
			
			public void windowClosing(WindowEvent e){
				disposeMe();
			}
			
		});
	}
	
	private void initGUI(){
		setTitle("Edit Node Metadata");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 2.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane spDisplay = new JScrollPane();
		spDisplay.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spDisplay = new GridBagConstraints();
		gbc_spDisplay.insets = new Insets(0, 0, 5, 0);
		gbc_spDisplay.fill = GridBagConstraints.BOTH;
		gbc_spDisplay.gridx = 0;
		gbc_spDisplay.gridy = 0;
		getContentPane().add(spDisplay, gbc_spDisplay);
		
		tblDisplay = new JTable();
		tblDisplay.setFont(new Font("Courier New", Font.PLAIN, 11));
		tblDisplay.setFillsViewportHeight(true);
		spDisplay.setViewportView(tblDisplay);
		
		JPanel pnlEditor = new JPanel();
		GridBagConstraints gbc_pnlEditor = new GridBagConstraints();
		gbc_pnlEditor.fill = GridBagConstraints.BOTH;
		gbc_pnlEditor.gridx = 0;
		gbc_pnlEditor.gridy = 1;
		getContentPane().add(pnlEditor, gbc_pnlEditor);
		GridBagLayout gbl_pnlEditor = new GridBagLayout();
		gbl_pnlEditor.columnWidths = new int[]{200, 200, 0};
		gbl_pnlEditor.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlEditor.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_pnlEditor.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlEditor.setLayout(gbl_pnlEditor);
		
		JScrollPane spKeyList = new JScrollPane();
		spKeyList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spKeyList = new GridBagConstraints();
		gbc_spKeyList.insets = new Insets(0, 0, 5, 5);
		gbc_spKeyList.fill = GridBagConstraints.BOTH;
		gbc_spKeyList.gridx = 0;
		gbc_spKeyList.gridy = 0;
		pnlEditor.add(spKeyList, gbc_spKeyList);
		
		lstKeyList = new JList<String>();
		lstKeyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		spKeyList.setViewportView(lstKeyList);
		lstKeyList.addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {
				onSelectKey();
			}
			
		});
		
		JScrollPane spValueEdit = new JScrollPane();
		spValueEdit.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spValueEdit = new GridBagConstraints();
		gbc_spValueEdit.gridheight = 2;
		gbc_spValueEdit.insets = new Insets(0, 0, 5, 0);
		gbc_spValueEdit.fill = GridBagConstraints.BOTH;
		gbc_spValueEdit.gridx = 1;
		gbc_spValueEdit.gridy = 0;
		pnlEditor.add(spValueEdit, gbc_spValueEdit);
		
		txtValueEditor = new JTextArea();
		spValueEdit.setViewportView(txtValueEditor);
		
		JPanel pnlButtons = new JPanel();
		pnlButtons.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.insets = new Insets(0, 0, 5, 5);
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 1;
		pnlEditor.add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlButtons.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnNew = new JButton("New Entry");
		btnNew.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_btnNew = new GridBagConstraints();
		gbc_btnNew.fill = GridBagConstraints.BOTH;
		gbc_btnNew.insets = new Insets(3, 5, 5, 5);
		gbc_btnNew.gridx = 0;
		gbc_btnNew.gridy = 0;
		pnlButtons.add(btnNew, gbc_btnNew);
		btnNew.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onNewEntry();
			}
			
		});
		
		JButton btnSave = new JButton("Save Value");
		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.BOTH;
		gbc_btnSave.insets = new Insets(3, 0, 5, 5);
		gbc_btnSave.gridx = 2;
		gbc_btnSave.gridy = 0;
		pnlButtons.add(btnSave, gbc_btnSave);
		btnSave.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onSaveValue();
			}
			
		});
		
		JButton btnDelete = new JButton("Delete Entry");
		btnDelete.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.fill = GridBagConstraints.BOTH;
		gbc_btnDelete.insets = new Insets(0, 5, 3, 5);
		gbc_btnDelete.gridx = 0;
		gbc_btnDelete.gridy = 2;
		pnlButtons.add(btnDelete, gbc_btnDelete);
		btnDelete.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onDeleteEntry();
			}
			
		});
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 2;
		pnlEditor.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		panel.add(btnCancel, gbc_btnCancel);
		btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnCancel.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
			
		});
		
		JButton btnOkay = new JButton("Save Changes");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(5, 5, 5, 5);
		gbc_btnOkay.gridx = 2;
		gbc_btnOkay.gridy = 0;
		panel.add(btnOkay, gbc_btnOkay);
		btnOkay.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnOkay.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onOkay();
			}
			
		});
		
	}
	
	private void closeMe(){
		this.setVisible(false);
		//this.dispose();
	}
	
	private void disposeMe(){
		dispose();
	}
	
	private void loadNodeMeta(){
		map.clear();
		if(node == null) return;

		for(String key : node.getMetadataKeys()){
			map.put(key, node.getMetadataValue(key));
		}
		
		refreshGUI();
	}
	
	private void saveNodeMeta(){

		if(node == null) return;
		node.clearMetadata();
		
		for(Entry<String, String> e : map.entrySet()){
			node.setMetadataValue(e.getKey(), e.getValue());
		}
		
	}
	
	public void setNode(FileNode filenode){
		node = filenode;
		loadNodeMeta();
	}
	
	private void refreshTable(){

		String[] cols = new String[]{"KEY", "VALUE"};
		String[][] data = new String[1][2];
		
		if(!map.isEmpty()){
			List<String> keys = new ArrayList<String>(map.size()+1);	
			keys.addAll(map.keySet());
			Collections.sort(keys);
			
			data = new String[map.size()][2];
			int r = 0;
			for(String k : keys){
				data[r][0] = k;
				data[r][1] = map.get(k);
				r++;
			}
			
		}
		
		DefaultTableModel mdl = new DefaultTableModel(data, cols);
		tblDisplay.setModel(mdl);
		tblDisplay.repaint();
		
	}
	
	private void refreshList(){
		DefaultListModel<String> mdl = new DefaultListModel<String>();
		
		if(!map.isEmpty()){
			List<String> keys = new ArrayList<String>(map.size()+1);	
			keys.addAll(map.keySet());
			Collections.sort(keys);
			
			for(String k : keys){mdl.addElement(k);}
			
		}
		
		lstKeyList.setModel(mdl);
		lstKeyList.setSelectedIndex(0);
		lstKeyList.repaint();
		
	}
	
	public void refreshGUI(){
		refreshTable();
		refreshList();
	}
	
	public void onSelectKey(){

		int idx = lstKeyList.getSelectedIndex();
		if(idx < 0){
			txtValueEditor.setText("");
			txtValueEditor.repaint();
			return;
		}
		
		String key = lstKeyList.getSelectedValue();
		String value = map.get(key);
		
		txtValueEditor.setText(value);
		txtValueEditor.repaint();
		
	}

	public void onNewEntry(){
		Random r = new Random();
		SetTextDialog dia = new SetTextDialog(parent, "New Key", "KEY_" + Long.toHexString(r.nextLong()));
		dia.setLocationRelativeTo(this);
		dia.setVisible(true);
		
		String s = dia.getText();
		dia.dispose();
		
		map.put(s, "null");
		refreshGUI();
		lstKeyList.setSelectedValue(s, true);
	}
	
	public void onDeleteEntry(){
		int idx = lstKeyList.getSelectedIndex();
		if(idx < 0){
			JOptionPane.showMessageDialog(this, "Delete Entry", "No entry selected!", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		String key = lstKeyList.getSelectedValue();
		map.remove(key);
		
		refreshGUI();
	}
	
	public void onSaveValue(){
		int idx = lstKeyList.getSelectedIndex();
		if(idx < 0){
			JOptionPane.showMessageDialog(this, "Save Value", "No entry selected!", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		String key = lstKeyList.getSelectedValue();	
		String val = txtValueEditor.getText();
		map.put(key, val);
		refreshTable();
	}
	
	public void onCancel(){
		closeMe();
	}
	
	public void onOkay(){
		saveNodeMeta();
		closeMe();
	}
	
}
