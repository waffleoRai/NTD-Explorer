package waffleoRai_NTDExGUI.panels.preview;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultTreeModel;

import waffleoRai_Utils.DirectoryNode;

import java.awt.Insets;
import javax.swing.JTree;

public class ArchivePreviewPanel extends JPanel{

	private static final long serialVersionUID = 4175074457003806515L;

	private DirectoryNode treeroot;
	
	public ArchivePreviewPanel(DirectoryNode root)
	{
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		JTree tree = new JTree();
		scrollPane.setViewportView(tree);
		
		treeroot = root;
		tree.setModel(new DefaultTreeModel(treeroot));
		
	}
	
}
