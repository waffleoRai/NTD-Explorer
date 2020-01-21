package waffleoRai_NTDExGUI.panels;

import javax.swing.JPanel;

import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_Utils.FileNode;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.border.EtchedBorder;

public class FileOptionPanel extends JPanel {

	private static final long serialVersionUID = -8608872993514124132L;
	
	public static final int MIN_WIDTH = 330;
	public static final int HEIGHT = 60;
	
	private JComboBox<FileAction> comboBox;
	private JButton btnGo;
	
	private Frame parent;
	private NTDProject project;
	private FileNode file;
	
	public FileOptionPanel(Frame parent_frame)
	{
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(null);
		setMinimumSize(new Dimension(MIN_WIDTH, HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, HEIGHT));
		
		comboBox = new JComboBox<FileAction>();
		comboBox.setBounds(10, 26, 211, 20);
		add(comboBox);
		comboBox.setSelectedIndex(-1);
		comboBox.setEnabled(false);
		
		btnGo = new JButton("Go!");
		btnGo.setBounds(231, 25, 89, 23);
		add(btnGo);
		btnGo.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				int idx = comboBox.getSelectedIndex();
				if(idx < 0) return;
				
				FileAction action = comboBox.getItemAt(idx);
				action.doAction(file, project, parent);
			}
			
		});
		btnGo.setEnabled(false);
		
		JLabel lblActions = new JLabel("Actions:");
		lblActions.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblActions.setBounds(10, 11, 46, 14);
		add(lblActions);
		
	}

	public void loadActionList(NTDProject proj, FileNode node, List<FileAction> actions)
	{
		project = proj;
		file = node;
		
		if(actions == null || actions.isEmpty())
		{
			btnGo.setEnabled(false);
			comboBox.setModel(new DefaultComboBoxModel<FileAction>());
			comboBox.setSelectedIndex(-1);
			comboBox.setEnabled(false);
			return;
		}
		
		btnGo.setEnabled(true);
		DefaultComboBoxModel<FileAction> model = new DefaultComboBoxModel<FileAction>();
		for(FileAction action : actions)model.addElement(action);
		comboBox.setModel(model);
		comboBox.setSelectedIndex(0);
		comboBox.setEnabled(true);
	}
	
	
}
