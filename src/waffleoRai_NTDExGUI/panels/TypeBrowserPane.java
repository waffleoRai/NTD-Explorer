package waffleoRai_NTDExGUI.panels;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JList;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.border.BevelBorder;

import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileDefinitions;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExGUI.icons.TypeIcon;

import javax.swing.ListSelectionModel;

public class TypeBrowserPane extends JPanel{

	private static final long serialVersionUID = 3702085004745453706L;
	
	private Map<FileClass, JCheckBox> groupmap;
	
	private JPanel pnlFilters;
	private JList<FileTypeDefinition> list;
	private JButton btnUpdate;
	
	public TypeBrowserPane(List<FileClass> filteroptions, boolean allow_multi_select){
		initGUI(allow_multi_select);
		if(filteroptions == null){
			list.setEnabled(false);
			btnUpdate.setEnabled(false);
		}
		//Load map
		groupmap = new HashMap<FileClass, JCheckBox>();
		for(FileClass group : filteroptions){
			Icon ico = TypeIcon.getTypeIcon(group);
			JCheckBox cb = null;
			if(ico != null) cb = new JCheckBox(ico);
			else cb = new JCheckBox(group.toString());
			cb.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					updateList();
				}
			});
			groupmap.put(group, cb);
			pnlFilters.add(cb);
		}
		
		updateList();
	}

	private void initGUI(boolean allow_multi_select){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		pnlFilters = new JPanel();
		GridBagConstraints gbc_pnlFilters = new GridBagConstraints();
		gbc_pnlFilters.insets = new Insets(0, 0, 5, 0);
		gbc_pnlFilters.fill = GridBagConstraints.BOTH;
		gbc_pnlFilters.gridx = 0;
		gbc_pnlFilters.gridy = 0;
		add(pnlFilters, gbc_pnlFilters);
		pnlFilters.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);
		
		list = new JList<FileTypeDefinition>();
		if(!allow_multi_select) list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
		
		JPanel pnlCtrl = new JPanel();
		GridBagConstraints gbc_pnlCtrl = new GridBagConstraints();
		gbc_pnlCtrl.fill = GridBagConstraints.BOTH;
		gbc_pnlCtrl.gridx = 0;
		gbc_pnlCtrl.gridy = 2;
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
		
		DefaultListModel<FileTypeDefinition> model = new DefaultListModel<FileTypeDefinition>();
		List<FileClass> filters = new LinkedList<FileClass>();
		for(Entry<FileClass, JCheckBox> e : groupmap.entrySet()){
			if(e.getValue().isSelected()) filters.add(e.getKey());
		}
		
		Collection<FileTypeDefinition> defs = FileDefinitions.getAllRegisteredDefinitions();
		for(FileTypeDefinition def : defs){
			//See if there's a matching group
			for(FileClass g : filters){
				if(def.getFileClass() == g){
					model.addElement(def);
					break;
				}
			}
		}
		
		list.setModel(model);
		list.repaint();
		list.setEnabled(true);
	}
	
	public void setButtonText(String txt){
		btnUpdate.setText(txt);
		btnUpdate.repaint();
	}
	
	public List<FileTypeDefinition> getAllSelected(){
		return list.getSelectedValuesList();
	}
	
	public FileTypeDefinition getSelected(){
		return list.getSelectedValue();
	}
	
	public void addActionListener(ActionListener l){
		btnUpdate.addActionListener(l);
	}
	
}
