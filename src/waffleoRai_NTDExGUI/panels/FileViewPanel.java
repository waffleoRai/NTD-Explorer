package waffleoRai_NTDExGUI.panels;

import javax.swing.JPanel;

import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_Utils.FileNode;

import java.awt.GridBagLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class FileViewPanel extends JPanel{

	private static final long serialVersionUID = -5671047900743041533L;

	private JPanel pnlTop;
	private JPanel pnlMid;
	private JPanel pnlBot;
	
	private Frame parent_frame;
	
	public FileViewPanel(Frame parent)
	{
		parent_frame = parent;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		pnlTop = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.weighty = 0.5;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		add(pnlTop, gbc_panel_2);
		
		pnlMid = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.weighty = 0.1;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		add(pnlMid, gbc_panel_1);
		
		pnlBot = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 1.0;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		add(pnlBot, gbc_panel);
		
	}

	public void loadFile(FileNode file, NTDProject proj)
	{
		pnlTop.removeAll();
		FileInfoPanel top = new FileInfoPanel(file);
		pnlTop.add(top);
		pnlTop.updateUI();
		
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
		pnlMid.removeAll();
		FileOptionPanel mid = new FileOptionPanel(parent_frame);
		mid.loadActionList(proj, file, manager.getFileActions());
		pnlMid.add(mid);
		pnlMid.updateUI();
		
		//Bottom panel...
		pnlBot.removeAll();
		JPanel bot = manager.generatePreviewPanel(file, parent_frame);
		pnlBot.add(bot);
		pnlBot.updateUI();
		
		repaint();
		
	}
	
}
