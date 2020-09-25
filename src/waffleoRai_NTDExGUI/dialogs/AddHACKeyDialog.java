package waffleoRai_NTDExGUI.dialogs;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class AddHACKeyDialog extends JDialog{

	private static final long serialVersionUID = -8886678577782550093L;

	public static final int WIDTH = 375;
	public static final int HEIGHT = 195;
	
	private JTextField textField1;
	private boolean confirmed;
	private JTextField textField2;

	public AddHACKeyDialog(Frame parent)
	{
		super(parent, true);
		confirmed = false;
		setTitle("Import Switch Keys");
		setResizable(false);
		
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setPreferredSize(sz);
		setLocationRelativeTo(parent);
		
		getContentPane().setLayout(null);
		
		textField1 = new JTextField();
		//textField.setText(startPath);
		textField1.setBounds(10, 36, 251, 20);
		getContentPane().add(textField1);
		textField1.setColumns(10);
		
		JButton btnSet = new JButton("OK");
		btnSet.setBounds(271, 134, 89, 23);
		getContentPane().add(btnSet);
		btnSet.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				confirmed = true;
				closeMe();
			}
			
		});
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.setBounds(271, 35, 89, 23);
		getContentPane().add(btnBrowse);
		btnBrowse.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				browse(0);
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(10, 134, 89, 23);
		getContentPane().add(btnCancel);
		
		JLabel lblBootImagePath = new JLabel("Product Keys:");
		lblBootImagePath.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblBootImagePath.setBounds(10, 11, 180, 14);
		getContentPane().add(lblBootImagePath);
		
		JLabel lblTitleKeys = new JLabel("Title Keys:");
		lblTitleKeys.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblTitleKeys.setBounds(10, 67, 180, 14);
		getContentPane().add(lblTitleKeys);
		
		textField2 = new JTextField();
		textField2.setColumns(10);
		textField2.setBounds(10, 92, 251, 20);
		getContentPane().add(textField2);
		
		JButton button = new JButton("Browse...");
		button.setBounds(271, 91, 89, 23);
		getContentPane().add(button);
		button.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				browse(1);
			}
			
		});
		
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				confirmed = false;
				closeMe();
			}
			
		});
		
	}
	
	public void browse(int tidx)
	{
		JTextField textField = null;
		if(tidx == 0) textField = textField1;
		else textField = textField2;
		
		JFileChooser fc = new JFileChooser(textField.getText());
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		int select = fc.showSaveDialog(this);
		
		if(select == JFileChooser.APPROVE_OPTION)
		{
			textField.setText(fc.getSelectedFile().getAbsolutePath());
			textField.repaint();
		}
		
	}
	
	public String getProductKeysPath(){
		return textField1.getText();
	}
	
	public String getTitleKeysPath(){
		return textField2.getText();
	}
	
	public boolean confirmSelected(){
		return confirmed;
	}
	
	public void closeMe(){
		setVisible(false);
		WindowListener[] listeners = this.getWindowListeners();
		for(WindowListener l : listeners) l.windowClosing(new WindowEvent(this, 0));
		dispose();
	}
}
