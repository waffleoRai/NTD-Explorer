package waffleoRai_NTDExGUI.panels;

import javax.swing.JPanel;

import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExGUI.DisposableJPanel;
import waffleoRai_NTDExGUI.FileActionListener;
import waffleoRai_Files.tree.FileNode;

import java.awt.GridBagLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.EtchedBorder;

public class FileViewPanel extends JPanel{

	private static final long serialVersionUID = -5671047900743041533L;
	
	//private static final double WEIGHT_TOP = 0.3;

	/*private JPanel pnlTop;
	private JPanel pnlMid;
	private JPanel pnlBot;*/
	
	private FileInfoPanel pnlTop;
	private FileOptionPanel pnlMid;
	private JPanel pnlBot;
	
	private Frame parent_frame;
	
	public FileViewPanel(Frame parent)
	{
		parent_frame = parent;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{330, 0};
		gridBagLayout.rowHeights = new int[]{65, 60, 71, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.2, 0.1, 0.7, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		//Top (File Info)
		pnlTop = new FileInfoPanel(null);
		add(pnlTop, getTopPanelGBC());
		
		//Mid (File Actions)
		pnlMid = new FileOptionPanel(parent);
		add(pnlMid, getMidPanelGBC());
		
		//Bottom (Preview - this is a dummy initially)
		pnlBot = new JPanel();
		pnlBot.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		add(pnlBot, getBottomPanelGBC());
		
	}
	
	private GridBagConstraints getTopPanelGBC(){
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		return gbc;
	}
	
	private GridBagConstraints getMidPanelGBC(){
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 0.1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridx = 0;
		gbc.gridy = 1;
		
		return gbc;
	}

	private GridBagConstraints getBottomPanelGBC(){
	
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 3.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridx = 0;
		gbc.gridy = 2;
	
		return gbc;
	}

	public void loadFile(FileNode file, NTDProject proj)
	{
		this.removeAll();
		
		pnlTop = new FileInfoPanel(file);
		this.add(pnlTop, getTopPanelGBC());
		
		//Now, we need to get the manager...
		FileTypeNode head = file.getTypeChainHead();
		FileTypeNode tail = head;
		TypeManager manager = TypeManager.getDefaultManager();
		if(head != null)
		{
			while(tail.getChild() != null) tail = tail.getChild();
			manager = TypeManager.getTypeManager(tail.getTypeID());
		}
		
		//Mid panel...
		pnlMid = new FileOptionPanel(parent_frame);
		pnlMid.loadActionList(proj, file, manager.getFileActions());
		add(pnlMid, getMidPanelGBC());
		
		//Bottom panel...
		pnlBot = manager.generatePreviewPanel(file, parent_frame);
		if(pnlBot != null) add(pnlBot, getBottomPanelGBC());
		
		repaint();
		this.updateUI();
	}

	public void addFileActionListener(FileActionListener l){
		if(pnlMid != null) pnlMid.addFileActionListener(l);
	}
	
	public void dispose(){
		if(pnlBot instanceof DisposableJPanel){
			((DisposableJPanel)pnlBot).dispose();
		}
	}
	
}
