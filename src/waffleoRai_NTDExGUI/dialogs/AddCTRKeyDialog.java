package waffleoRai_NTDExGUI.dialogs;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;

public class AddCTRKeyDialog extends JDialog{
	

	private static final long serialVersionUID = 5766701968917157651L;
	
	public static final int WIDTH = 375;
	public static final int HEIGHT = 130;
	
	private JTextField textField;
	private boolean confirmed;

	public AddCTRKeyDialog(Frame parent, String startPath)
	{
		super(parent, true);
		confirmed = false;
		setTitle("Generate boot9 Keyset");
		setResizable(false);
		
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setPreferredSize(sz);
		setLocationRelativeTo(parent);
		
		getContentPane().setLayout(null);
		
		textField = new JTextField();
		textField.setText(startPath);
		textField.setBounds(10, 36, 350, 20);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnSet = new JButton("OK");
		btnSet.setBounds(271, 67, 89, 23);
		getContentPane().add(btnSet);
		btnSet.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				confirmed = true;
				closeMe();
			}
			
		});
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.setBounds(172, 67, 89, 23);
		getContentPane().add(btnBrowse);
		btnBrowse.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				browse();
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(10, 67, 89, 23);
		getContentPane().add(btnCancel);
		
		JLabel lblBootImagePath = new JLabel("Boot9 Image Path:");
		lblBootImagePath.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblBootImagePath.setBounds(10, 11, 180, 14);
		getContentPane().add(lblBootImagePath);
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
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
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
