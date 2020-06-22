package waffleoRai_NTDExGUI;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;

import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExGUI.dialogs.SetTextDialog;
import waffleoRai_NTDExGUI.dialogs.TreeDialog;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.FileViewPanel;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileNode;

import java.awt.FlowLayout;
import java.awt.Frame;

public class ImageMasterPanel extends JPanel implements TreePanelListener, FileActionListener{

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
		//System.err.print("Parent is null? " + (parent == null));
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
		pnlRight.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
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
	
	public void setWait()
	{
		//TODO
		disableAll();
	}
	
	public void unsetWait()
	{
		//TODO
		reenable();
		
	}
	
	public void disableAll()
	{
		//TODO
		
	}
	
	public void reenable()
	{
		//TODO
	}
	
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
		
		pnlRight.removeAll();
		pnlRight.updateUI();
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
		resetTree();
		
		this.repaint();
	}
	
	public void resetTree()
	{
		//System.err.println("Refreshin'");
		if(pnlTree != null) pnlTree.clearTreeListeners();

		pnlLeft.removeAll();
		DirectoryNode root = myproject.getTreeRoot();
		root.setFileName("");
		pnlTree = new TreePanel(root);
		pnlTree.addTreeListener(this);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		pnlLeft.add(pnlTree, gbc);
		pnlTree.updateGUITree();
		pnlLeft.updateUI();
	}
	
	public void refreshFileViewPanel(FileNode node)
	{
		Component[] clist = pnlRight.getComponents();
		for(int i = 0; i < clist.length; i++){
			if(clist[i] != null){
				if(clist[i] instanceof FileViewPanel){
					((FileViewPanel)clist[i]).dispose();
				}
			}
		}
		
		pnlRight.removeAll();
		FileViewPanel pnl = new FileViewPanel(parent);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		
		pnl.loadFile(node, myproject);
		pnl.addFileActionListener(this);
		pnlRight.add(pnl,gbc);
		pnlTree.updateUI();
		pnlRight.updateUI();
	}
	
	public void updateTree(){
		pnlTree.updateGUITree();
	}
	
	public void onFileAction(){
		updateTree();
	}
	
	public void refreshMe(){
		resetTree();
		pnlRight.removeAll();
		pnlRight.updateUI();
	}
	
	/*----- Tree Actions -----*/
	
	public void onDoubleClickSelection(String path)
	{
		//System.err.println("double click!");
		onTreeActionViewNode(path);
	}
	
	public void onRightClickSelection(String path, int choice)
	{
		//System.err.println("right click: " + choice);
		switch(choice)
		{
		case TreePopupMenu.MENU_OP_RENAME: onTreeActionRename(path); break;
		case TreePopupMenu.MENU_OP_NEWDIR: onTreeActionNewDir(path); break;
		case TreePopupMenu.MENU_OP_MOVEME: onTreeActionMoveNode(path); break;
		case TreePopupMenu.MENU_OP_SPLIT: onTreeActionSplit(path); break;
		case TreePopupMenu.MENU_OP_EXTRACT: onTreeActionExtract(path); break;
		case TreePopupMenu.MENU_OP_EXPORT: onTreeActionExport(path); break;
		case TreePopupMenu.MENU_OP_VIEW: onTreeActionViewNode(path); break;
		case TreePopupMenu.MENU_OP_REFRESH: resetTree(); break;
		case TreePopupMenu.MENU_OP_CLEARTYPE: onTreeActionClearType(path); break;
		}
	}
	
	private void onTreeActionRename(String path)
	{
		if(myproject == null)
		{
			showError("No project loaded!");
			return;
		}
		if(path == null) return;
		FileNode node = myproject.getNodeAt(path);
		if(node == null){
			System.err.println("ERR -- Tree path \"" + path + "\" could not be matched to a node.");
			return;
		}
		
		SetTextDialog dialog = new SetTextDialog(parent, "Set New Name", node.getFileName());
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		
		if(!dialog.okSelected()) return;
		String newname = dialog.getText();
		if(newname.contains("/"))
		{
			showError("File/directory name cannot contain forward slashes!");
			return;
		}
		
		node.setFileName(newname);
		
		updateTree();
	}
	
	private void onTreeActionNewDir(String path)
	{
		if(myproject == null)
		{
			showError("No project loaded!");
			return;
		}
		if(path == null) return;
		FileNode node = myproject.getNodeAt(path);
		if(node == null){
			System.err.println("ERR -- Tree path \"" + path + "\" could not be matched to a node.");
			return;
		}
		if(!node.isDirectory())
		{
			System.err.println("ERR -- Tree path \"" + path + "\" is not a directory.");
			return;
		}
		
		SetTextDialog dialog = new SetTextDialog(parent, "New Directory Name", "NewFolder");
		dialog.setVisible(true);
		
		if(!dialog.okSelected()) return;
		String newname = dialog.getText();
		if(newname.contains("/"))
		{
			showError("File/directory name cannot contain forward slashes!");
			return;
		}
		
		//Create new directory
		new DirectoryNode((DirectoryNode)node, newname);
		
		updateTree();
		
	}
	
	private void onTreeActionMoveNode(String path)
	{
		if(myproject == null)
		{
			showError("No project loaded!");
			return;
		}
		if(path == null) return;
		FileNode node = myproject.getNodeAt(path);
		if(node == null){
			System.err.println("ERR -- Tree path \"" + path + "\" could not be matched to a node.");
			return;
		}
		
		TreeDialog dialog = new TreeDialog(parent, "Move To", myproject.generateDirectoryTree());
		dialog.setVisible(true);
		
		if(!dialog.okSelected()) return;
		String newpath = dialog.getSelectedTreePath();
		
		//Do move
		if(!myproject.moveNode(node, newpath))
		{
			showError("Unknown error: Node move failed!");
		}
		
		updateTree();
	}
	
	private void onTreeActionSplit(String path)
	{
		if(myproject == null)
		{
			showError("No project loaded!");
			return;
		}
		if(path == null) return;
		FileNode node = myproject.getNodeAt(path);
		if(node == null){
			System.err.println("ERR -- Tree path \"" + path + "\" could not be matched to a node.");
			return;
		}
		
		SetTextDialog dialog = new SetTextDialog(parent, "Enter Offset (Hex) to Split At", "0x00");
		dialog.setVisible(true);
		
		if(!dialog.okSelected()) return;
		String rawoff = dialog.getText();
		if(rawoff.startsWith("0x")) rawoff = rawoff.substring(2);
		long offset = 0;
		try{offset = Long.parseUnsignedLong(rawoff, 16);}
		catch(NumberFormatException x){showError("\"" + rawoff + "\" is not a valid hexadecimal value!"); return;}
		
		if(offset > node.getLength())
		{
			showError("Offset 0x" + Long.toHexString(offset) + " invalid for file node of length 0x" + 
					Long.toHexString(node.getLength()) + "!");
			return;
		}
		
		String confirm_msg = "Split " + path + " at offset 0x" + Long.toHexString(offset) + "?";
		int confirm = JOptionPane.showConfirmDialog(this, confirm_msg, "Split File Node", 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(confirm != JOptionPane.YES_OPTION) return;
		
		//Do split
		if(!node.splitNodeAt(offset)) showError("Unknown Err -- Split failed!");
		
		updateTree();
	}
	
	private void onTreeActionExtract(String path)
	{
		if(myproject == null)
		{
			showError("No project loaded!");
			return;
		}
		if(path == null) return;
		FileNode node = myproject.getNodeAt(path);
		if(node == null){
			System.err.println("ERR -- Tree path \"" + path + "\" could not be matched to a node.");
			return;
		}
		
		//JFileChooser
		String lastpath = NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXTRACTED);
		JFileChooser fc = new JFileChooser(lastpath);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int select = fc.showSaveDialog(this);
		
		if(select != JFileChooser.APPROVE_OPTION) return;
		String dir = fc.getSelectedFile().getAbsolutePath();
		
		//Option dialog asking if should decompress?
		int decomp_choice = JOptionPane.showConfirmDialog(this, 
				"Would you like to decompress extracted file(s)?", 
				"Decompress & Extract", JOptionPane.YES_NO_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if(decomp_choice == JOptionPane.CANCEL_OPTION) return;
		
		//Spawn task and dialog
		String targetpath = dir + File.separator + node.getFileName();
		NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXTRACTED, targetpath);
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "File Extraction");
		dialog.setPrimaryString("Extracting Data");
		dialog.setSecondaryString("Extracting to " + targetpath);
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					boolean decomp = (decomp_choice == JOptionPane.YES_OPTION);
					node.copyDataTo(targetpath, decomp);
				}
				catch(Exception x)
				{
					x.printStackTrace();
					showError("Unknown Error: Extraction Failed! See stderr for details.");
				}
				
				return null;
			}
			
			public void done()
			{
				dialog.closeMe();
				showInfo("Extraction complete!");
			}
		};
		
		task.execute();
		dialog.render();
	}
	
	private void onTreeActionExport(String path)
	{
		if(myproject == null)
		{
			showError("No project loaded!");
			return;
		}
		if(path == null) return;
		FileNode node = myproject.getNodeAt(path);
		if(node == null){
			System.err.println("ERR -- Tree path \"" + path + "\" could not be matched to a node.");
			return;
		}
		
		//JFileChooser
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXTRACTED));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int select = fc.showSaveDialog(this);
		
		if(select != JFileChooser.APPROVE_OPTION) return;
		String dir = fc.getSelectedFile().getAbsolutePath();
		
		//Spawn task and dialog
		String targetpath = dir;
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "File Export");
		dialog.setPrimaryString("Converting & Exporting Data");
		dialog.setSecondaryString("Exporting to " + targetpath);
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					if(!TypeManager.exportNode(node, targetpath, dialog))
					{
						showWarning("WARNING: Export complete. Not all files were properly exported!");
					}
				}
				catch(Exception x)
				{
					x.printStackTrace();
					showError("Unknown Error: Export Failed! See stderr for details.");
				}
				
				return null;
			}
			
			public void done()
			{
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
	}
	
	private void onTreeActionViewNode(String path)
	{
		if(myproject == null)
		{
			showError("No project loaded!");
			return;
		}
		if(path == null) return;
		FileNode node = myproject.getNodeAt(path);
		if(node == null){
			System.err.println("ERR -- Tree path \"" + path + "\" could not be matched to a node.");
			return;
		}
		
		if(node instanceof DirectoryNode) return;
		
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Loading File");
		dialog.setPrimaryString("Loading");
		dialog.setSecondaryString("Loading preview of " + node.getFullPath());
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try
				{
					//setWait();
					if(node.getTypeChainHead() == null || node.getTypeChainTail().isCompression()){
						//Detect...
						//System.err.println("Node: " + node.getFileName() + " | 0x" + Long.toHexString(node.getOffset()) + ": 0x" + Long.toHexString(node.getLength()));
						dialog.setPrimaryString("Type Unknown");
						dialog.setSecondaryString("Detecting file type of " + node.getFullPath());
						FileTypeNode type = TypeManager.detectType(node);
						node.setTypeChainHead(type);
						
						dialog.setPrimaryString("Loading");
						dialog.setSecondaryString("Loading preview of " + node.getFullPath());
					}
					
					refreshFileViewPanel(node);
				}
				catch(Exception x)
				{
					x.printStackTrace();
					showError("Unknown Error: Preview Failed! See stderr for details.");
				}
				
				return null;
			}
			
			public void done()
			{
				//unsetWait();
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
		
	}
	
	private void onTreeActionClearType(String path){
		
		//Dummy checks
		if(myproject == null){
			showError("No project loaded!");
			return;
		}
		if(path == null) return;
		FileNode node = myproject.getNodeAt(path);
		if(node == null){
			System.err.println("ERR -- Tree path \"" + path + "\" could not be matched to a node.");
			return;
		}
		
		//We'll just do it on this thread since it should be fast...
		node.clearTypeChain();
		updateTree();
	}
	
	/*----- Messages -----*/
	
	public void showWarning(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void showInfo(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Notice", JOptionPane.INFORMATION_MESSAGE);
	}
	
	
}
