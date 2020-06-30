package waffleoRai_NTDExGUI;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import waffleoRai_NTDExGUI.icons.TypeIconTreeRenderer;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileNode;

import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TreePanel extends JPanel{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 515876169408920339L;
	
	public static final int MIN_WIDTH = 200;
	public static final int MIN_HEIGHT = 300;
	
	/*----- Instance Variables -----*/
	
	private DirectoryNode tree_root;
	
	private JScrollPane scrollPane;
	private JTree tree;
	
	private List<TreePanelListener> listeners;
	private String last_clicked_path;
	//private MouseEvent last_click;
	
	/*----- Construction -----*/
	
	public TreePanel(DirectoryNode root)
	{
		FileNode.setUseFullPathInToString(false);
		last_clicked_path = "";
		//last_click = null;
		
		listeners = new LinkedList<TreePanelListener>();
		
		tree_root = root;
		initGUI();
		updateGUITree();
	}

	private void initGUI()
	{
		Dimension mindim = new Dimension(MIN_WIDTH, MIN_HEIGHT);
		setMinimumSize(mindim);
		//setPreferredSize(mindim);
		
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
		tree.setExpandsSelectedPaths(true);
		tree.setCellRenderer(new TypeIconTreeRenderer());
		tree.setToolTipText("ROM image internal file system.\r\n(With optional custom link tree)");
		tree.setBorder(null);
		scrollPane.setViewportView(tree);
		tree.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseClicked(MouseEvent e) 
			{
				TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
				if(SwingUtilities.isLeftMouseButton(e))
				{
					onLeftClickNode(path);
				}
				else if(SwingUtilities.isRightMouseButton(e))
				{
					onRightClickNode(path, e.getX(), e.getY());
				}
			}

		});
		
		
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(tree, popupMenu);
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	public void addTreeListener(TreePanelListener l){listeners.add(l);}
	
	public void clearTreeListeners(){listeners.clear();}
	
	/*----- Enable/Disable -----*/
	
	public void disableAll()
	{
		tree.setEnabled(false);
		scrollPane.setEnabled(false);
	}
	
	public void enableAll()
	{
		tree.setEnabled(true);
		scrollPane.setEnabled(true);
	}
	
	/*----- GUI Sync -----*/
	
	public void updateGUITree()
	{
		//TODO: FFS don't collapse if don't need to! (So annoying)
		
		//Sync tree 
		disableAll();
		TreePath path = tree.getSelectionPath();
		DefaultTreeModel model = new DefaultTreeModel(tree_root);
		tree.setModel(model);
		tree.setSelectionPath(path);
		tree.updateUI();
		
		//tree.repaint();
		scrollPane.repaint();
		enableAll();
	}
	
	/*----- On Actions -----*/
	
	public void onLeftClickNode(TreePath tp)
	{
		//TreePath newPath = e.getPath();
		String spath = FileNode.readTreePath(tp);
		//System.err.println("Tree - Left click detected: " + spath);
		if(spath == null) return;
		if(spath.equals(last_clicked_path))
		{
			//Double click
			//System.err.println("Tree - Double left click detected: " + spath);
			for(TreePanelListener l : listeners) l.onDoubleClickSelection(spath);
		}
		else last_clicked_path = spath;
	}
	
	public void onRightClickNode(TreePath tp, int x, int y)
	{
		if(listeners.isEmpty()) return;
		//TreePath path = e.getPath();
		String spath = FileNode.readTreePath(tp);
		//System.err.println("Tree - Right click detected: " + spath);
		FileNode node = tree_root.getNodeAt(spath);
		if(node == null) return;
		//System.err.println("Tree - Node matched: " + node.getFullPath());
		
		//Spawn menu at click site
		TreePopupMenu popup = new TreePopupMenu(spath, node.isDirectory(), listeners);
		popup.addPopupMenuListener(new PopupMenuListener(){

			private boolean cancel = false;
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				//System.err.println("Visible");
				cancel = false;
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				if(cancel) return;
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				cancel = true;
			}
			
		});
		popup.show(tree, x, y);
		
		
		//Once menu closed, notify listeners...
		/*int op = popup.getSelection();
		for(TreePanelListener l : listeners)
		{
			l.onRightClickSelection(spath, op);
		}*/
	}
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	
}
