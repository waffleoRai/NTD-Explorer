package waffleoRai_NTDExGUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;

public class DirSetDialog extends JDialog{

	private static final long serialVersionUID = 8629825868640257346L;
	
	public static final int WIDTH = 375;
	public static final int HEIGHT = 100;
	
	private JTextField textField;
	private boolean confirmed;

	public DirSetDialog(Frame parent, String startPath)
	{
		super(parent, true);
		confirmed = false;
		setTitle("Set Directory");
		setResizable(false);
		
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setPreferredSize(sz);
		setLocationRelativeTo(parent);
		
		getContentPane().setLayout(null);
		
		textField = new JTextField();
		textField.setText(startPath);
		textField.setBounds(10, 11, 350, 20);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnSet = new JButton("Set");
		btnSet.setBounds(271, 42, 89, 23);
		getContentPane().add(btnSet);
		btnSet.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				confirmed = true;
				closeMe();
			}
			
		});
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.setBounds(172, 42, 89, 23);
		getContentPane().add(btnBrowse);
		btnBrowse.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				browse();
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(10, 42, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				confirmed = false;
				closeMe();
			}
			
		});
		
	}
	
	public void browse()
	{
		JFileChooser fc = new JFileChooser(textField.getText());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int select = fc.showSaveDialog(this);
		
		if(select == JFileChooser.APPROVE_OPTION)
		{
			textField.setText(fc.getSelectedFile().getAbsolutePath());
			textField.repaint();
		}
		
	}
	
	public String getPath()
	{
		return textField.getText();
	}
	
	public boolean confirmSelected()
	{
		return confirmed;
	}
	
	public void closeMe()
	{
		setVisible(false);
		WindowListener[] listeners = this.getWindowListeners();
		for(WindowListener l : listeners) l.windowClosing(new WindowEvent(this, 0));
		dispose();
	}
	
	
}
