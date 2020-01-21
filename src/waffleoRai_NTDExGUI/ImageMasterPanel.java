package waffleoRai_NTDExGUI;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import waffleoRai_NTDExCore.NTDProject;

import java.awt.FlowLayout;
import java.awt.Frame;

public class ImageMasterPanel extends JPanel implements TreePanelListener{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 4594796381602663119L;
	
	public static final int MIN_WIDTH = InfoPanel.MIN_WIDTH;
	public static final int MIN_HEIGHT = InfoPanel.HEIGHT + 200;
	public static final int PREF_HEIGHT = 300;

	/*----- Instance Variables -----*/
	
	private Frame parent;
	private NTDProject myproject;
	
	private JPanel pnlTop;
	private JPanel pnlRight;
	private JPanel pnlLeft;
	
	//private InfoPanel pnlInfo;
	private TreePanel pnlTree;

	/*----- Construction -----*/
	
	public ImageMasterPanel(Frame myparent)
	{
		parent = myparent;
		initGUI();
	}

	private void initGUI()
	{
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 229, 0};
		gridBagLayout.rowHeights = new int[]{InfoPanel.HEIGHT, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		pnlTop = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pnlTop.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setVgap(1);
		flowLayout.setHgap(1);
		InfoPanel pnlInfo = new InfoPanel(null);
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.anchor = GridBagConstraints.WEST;
		gbc_panel_3.weighty = 0.01;
		gbc_panel_3.gridwidth = 2;
		gbc_panel_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 0;
		pnlTop.add(pnlInfo);
		add(pnlTop, gbc_panel_3);
		
		pnlLeft = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.weightx = 0.3;
		gbc_panel_2.weighty = 1.0;
		gbc_panel_2.insets = new Insets(0, 0, 0, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 1;
		add(pnlLeft, gbc_panel_2);
		GridBagLayout gbl_pnlLeft = new GridBagLayout();
		gbl_pnlLeft.rowWeights = new double[]{1.0};
		gbl_pnlLeft.columnWeights = new double[]{1.0};
		pnlLeft.setLayout(gbl_pnlLeft);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 0;
		pnlLeft.add(scrollPane_2, gbc_scrollPane_2);
		
		/*JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		pnlLeft.add(scrollPane, gbc_scrollPane);*/
		
		pnlRight = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.weightx = 1.0;
		gbc_panel_1.weighty = 1.0;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 1;
		add(pnlRight, gbc_panel_1);
		GridBagLayout gbl_pnlRight = new GridBagLayout();
		gbl_pnlRight.columnWidths = new int[]{0, 0};
		gbl_pnlRight.rowHeights = new int[]{0, 0};
		gbl_pnlRight.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlRight.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlRight.setLayout(gbl_pnlRight);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		pnlRight.add(scrollPane_1, gbc_scrollPane_1);
		
		Dimension sz = new Dimension(MIN_WIDTH, MIN_HEIGHT);
		setMinimumSize(sz);
		sz = new Dimension(MIN_WIDTH, PREF_HEIGHT);
		setPreferredSize(sz);
	}
	
	/*----- Enabling -----*/
	
	/*----- GUI Sync -----*/
	
	private void clearProject()
	{
		pnlTop.removeAll();
		InfoPanel pnlInfo = new InfoPanel(null);
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.anchor = GridBagConstraints.WEST;
		gbc_panel_3.weighty = 0.01;
		gbc_panel_3.gridwidth = 2;
		gbc_panel_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 0;
		pnlTop.add(pnlInfo);
		pnlTop.updateUI();
		
		pnlLeft.removeAll();
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		pnlLeft.add(scrollPane, gbc_scrollPane);
		pnlLeft.updateUI();
	}
	
	public void loadProject(NTDProject project)
	{
		myproject = project;
		if(project == null) {
			clearProject();
			return;
		}
		
		//Top panel
		pnlTop.removeAll();
		InfoPanel pnlInfo = new InfoPanel(project);
		pnlTop.add(pnlInfo);
		pnlTop.updateUI();
		
		//Default ROM panel for right (initially)
		pnlRight.removeAll();
		pnlRight.updateUI();
		
		//Tree panel
		pnlLeft.removeAll();
		pnlTree = new TreePanel(project.getTreeRoot());
		pnlTree.addTreeListener(this);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		pnlLeft.add(pnlTree, gbc);
		pnlLeft.updateUI();
		
		this.repaint();
	}
	
	/*----- Tree Actions -----*/
	
	public void onDoubleClickSelection(String path)
	{
		onTreeActionViewNode(path);
	}
	
	public void onRightClickSelection(String path, int choice)
	{
		switch(choice)
		{
		case TreePopupMenu.MENU_OP_RENAME: onTreeActionRename(path); break;
		case TreePopupMenu.MENU_OP_NEWDIR: onTreeActionNewDir(path); break;
		case TreePopupMenu.MENU_OP_MOVEME: onTreeActionMoveNode(path); break;
		case TreePopupMenu.MENU_OP_SPLIT: onTreeActionSplit(path); break;
		case TreePopupMenu.MENU_OP_EXTRACT: onTreeActionExtract(path); break;
		case TreePopupMenu.MENU_OP_EXPORT: onTreeActionExport(path); break;
		case TreePopupMenu.MENU_OP_VIEW: onTreeActionViewNode(path); break;
		}
	}
	
	private void onTreeActionRename(String path)
	{
		//TODO
	}
	
	private void onTreeActionNewDir(String path)
	{
		//TODO
	}
	
	private void onTreeActionMoveNode(String path)
	{
		//TODO
	}
	
	private void onTreeActionSplit(String path)
	{
		//TODO
	}
	
	private void onTreeActionExtract(String path)
	{
		//TODO
	}
	
	private void onTreeActionExport(String path)
	{
		//TODO
	}
	
	private void onTreeActionViewNode(String path)
	{
		//TODO
	}
	
	/*----- Messages -----*/
	
}
