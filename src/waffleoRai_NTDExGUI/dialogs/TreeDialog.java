package waffleoRai_NTDExGUI.dialogs;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JDialog;

import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.JButton;

public class TreeDialog extends JDialog{
	
	private static final long serialVersionUID = 5269268255241847465L;
	
	public static final int WIDTH = 400;
	public static final int HEIGHT = 300;
	public static final int PNL_HEIGHT = 30;
	
	private boolean selection;
	private String selected_path;

	public TreeDialog(Frame parent, String title, DirectoryNode root)
	{
		super(parent, true);
		setTitle(title);
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[] {0, 30, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weighty = 3.0;
		gbc_scrollPane.insets = new Insets(10, 10, 10, 10);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		JTree tree = new JTree();
		scrollPane.setViewportView(tree);
		
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(WIDTH, PNL_HEIGHT));
		panel.setPreferredSize(new Dimension(WIDTH, PNL_HEIGHT));
		panel.setMaximumSize(new Dimension(WIDTH, PNL_HEIGHT));
		panel.setLayout(null);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 0.1;
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		getContentPane().add(panel, gbc_panel);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(275, 11, 89, 23);
		panel.add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				selection = false;
				setVisible(false);
			}
			
		});
		
		JButton btnOk = new JButton("OK");
		btnOk.setBounds(176, 11, 89, 23);
		panel.add(btnOk);
		btnOk.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				selection = true;
				TreePath tp = tree.getSelectionPath();
				selected_path = FileNode.readTreePath(tp);
				setVisible(false);
			}
			
		});
		
		btnOk.setEnabled(false);
		DefaultTreeModel model = new DefaultTreeModel(root);
		tree.setModel(model);
		tree.addTreeSelectionListener(new TreeSelectionListener(){

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				btnOk.setEnabled(!tree.isSelectionEmpty());
				
			}
			
		});
	}
	
	public boolean okSelected(){return selection;}
	public String getSelectedTreePath(){return this.selected_path;}

}
