package waffleoRai_NTDExGUI.panels.preview.soundbank;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;

import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import waffleoRai_soundbank.SoundbankNode;

import javax.swing.JTree;

public class SoundbankTreePanel extends JPanel{

	private static final long serialVersionUID = -3598604834327026481L;
	
	private List<SoundbankTreeListener> listeners;
	
	private JScrollPane scrollPane;
	private JTree tree;

	public SoundbankTreePanel(SoundbankNode root){
		
		listeners = new LinkedList<SoundbankTreeListener>();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		tree = new JTree();
		scrollPane.setViewportView(tree);
		tree.setCellRenderer(new SoundbankTreeRenderer());
		tree.addTreeSelectionListener(new TreeSelectionListener(){

			//private SoundbankNode last = null;
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				//System.err.println("Value changed");
				TreePath path = e.getPath();
				Object targ = path.getLastPathComponent();
				if(targ instanceof SoundbankNode){
					/*if(targ == last){
						//Notify listeners
						for(SoundbankTreeListener l : listeners)l.onDoubleClickSelection(last);
					}
					else last = (SoundbankNode)targ;*/
					for(SoundbankTreeListener l : listeners)l.onDoubleClickSelection((SoundbankNode)targ);
				}
			}
			
		});
		
		loadTree(root);
	}
	
	public void loadTree(SoundbankNode root){
		DefaultTreeModel model = new DefaultTreeModel(root);
		tree.setModel(model);
		
		tree.updateUI();
		scrollPane.repaint();
	}
	
	public void addListener(SoundbankTreeListener l){
		listeners.add(l);
	}
	
}
