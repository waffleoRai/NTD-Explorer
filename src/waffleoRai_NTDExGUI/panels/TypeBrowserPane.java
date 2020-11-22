package waffleoRai_NTDExGUI.panels;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JList;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileDefinitions;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_NTDExGUI.icons.TypeIcon;

import javax.swing.ListSelectionModel;

public class TypeBrowserPane extends JPanel{

	private static final long serialVersionUID = 3702085004745453706L;
	
	private Map<FileClass, JCheckBox> groupmap;
	//private Map<FileClass, JLabel> icomap;
	
	private JPanel pnlFilters;
	private JList<FileTypeDefinition> list;
	private JButton btnUpdate;
	
	public static class CBPanel extends JPanel{

		private static final long serialVersionUID = -6857287859963140640L;
		
		//private JCheckBox cb;
		//private JLabel lbl;
		
		public CBPanel(JCheckBox checkbox, JLabel label){
			GridBagLayout gbl = new GridBagLayout();
			gbl.columnWidths = new int[]{0,0};
			gbl.rowHeights = new int[]{0};
			gbl.columnWeights = new double[]{Double.MIN_VALUE, 1};
			gbl.rowWeights = new double[]{Double.MIN_VALUE};
			setLayout(gbl);
			
			//setBorder(new LineBorder(new Color(0, 0, 0)));
			
			//Add components
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.ipadx = 2;
			//gbc.ipady = 2;
			gbc.anchor = GridBagConstraints.CENTER;
			add(checkbox, gbc);
			
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.ipadx = 2;
			//gbc.ipady = 2;
			gbc.anchor = GridBagConstraints.WEST;
			add(label, gbc);
			
			
		}
		
	}
	
	public TypeBrowserPane(List<FileClass> filteroptions, boolean allow_multi_select, int cols){
		//cols = cols << 1; //One for box, one for label
		int total = 1;
		if(filteroptions != null) total = filteroptions.size();
		int rows = total/cols;
		if(total%cols != 0) rows++;
		
		initGUI(allow_multi_select, cols, rows);
		if(filteroptions == null){
			list.setEnabled(false);
			btnUpdate.setEnabled(false);
		}
		//Load map
		groupmap = new HashMap<FileClass, JCheckBox>();
		//icomap = new HashMap<FileClass, JLabel>();
		int r = 0;
		int l = 0;
		for(FileClass group : filteroptions){
			Icon ico = TypeIcon.getTypeIcon(group);
			
			JCheckBox cb = new JCheckBox();
			JLabel lbl = null;
			if(ico != null) lbl = new JLabel(ico);
			else lbl = new JLabel(group.toString());
			cb.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					updateList();
				}
			});
			groupmap.put(group, cb);
			
			//Add to panel
			CBPanel minipnl = new CBPanel(cb, lbl);
			GridBagConstraints gbc = spawnGBC(l++, r);
			pnlFilters.add(minipnl, gbc);

			if(l >= cols){
				l = 0; r++;
			}
		}
		
		updateList();
	}
	
	private GridBagConstraints spawnGBC(int gx, int gy){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.ipadx = 2;
		gbc.ipady = 2;
		gbc.insets = new Insets(0,5,0,5);
		//if(gx % 2 == 0) gbc.anchor = GridBagConstraints.CENTER;
		//else gbc.anchor = GridBagConstraints.WEST;

		return gbc;
	}

	private void initGUI(boolean allow_multi_select, int cols, int rows){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.1, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		pnlFilters = new JPanel();
		GridBagConstraints gbc_pnlFilters = new GridBagConstraints();
		gbc_pnlFilters.insets = new Insets(0, 0, 5, 0);
		gbc_pnlFilters.fill = GridBagConstraints.BOTH;
		gbc_pnlFilters.gridx = 0;
		gbc_pnlFilters.gridy = 0;
		add(pnlFilters, gbc_pnlFilters);
		GridBagLayout gbl_pnlFilters = new GridBagLayout();
		gbl_pnlFilters.columnWidths = new int[cols+1];
		gbl_pnlFilters.rowHeights = new int[rows];
		gbl_pnlFilters.columnWeights = new double[cols+1];
		Arrays.fill(gbl_pnlFilters.columnWeights, Double.MIN_VALUE);
		gbl_pnlFilters.columnWeights[cols] = 1;
		gbl_pnlFilters.rowWeights = new double[rows];
		Arrays.fill(gbl_pnlFilters.rowWeights, Double.MIN_VALUE);
		//Arrays.fill(gbl_pnlFilters.rowWeights, 1.0);
		pnlFilters.setLayout(gbl_pnlFilters);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weighty = 1.0;
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
		
		//ALPHABETIZE - how they hell you gonna find anything???
		Map<String, FileTypeDefinition> map = new HashMap<String, FileTypeDefinition>();
		List<String> namelist = new LinkedList<String>();
		Collection<FileTypeDefinition> defs = FileDefinitions.getAllRegisteredDefinitions();
		for(FileTypeDefinition def : defs){
			//See if there's a matching group
			for(FileClass g : filters){
				if(def.getFileClass() == g){
					//model.addElement(def);
					String s = def.toString();
					namelist.add(s);
					map.put(s, def);
					break;
				}
			}
		}
		
		Collections.sort(namelist);
		for(String s : namelist){
			model.addElement(map.get(s));
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
