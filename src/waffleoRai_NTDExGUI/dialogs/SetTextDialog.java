package waffleoRai_NTDExGUI.dialogs;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JTextField;

import waffleoRai_NTDExCore.NTDProgramFiles;

import javax.swing.JButton;

public class SetTextDialog extends JDialog{

	private static final long serialVersionUID = -2412762802960275998L;
	
	public static final int WIDTH = 325;
	public static final int HEIGHT = 120;
	
	private boolean selection;
	private JTextField textField;

	public SetTextDialog(Frame parent, String title, String inittxt)
	{
		super(parent, true);
		setResizable(false);
		setTitle(title);
		getContentPane().setLayout(null);
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		textField = new JTextField();
		textField.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 11));
		if(inittxt != null) textField.setText(inittxt);
		textField.setBounds(10, 24, 300, 20);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(221, 51, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				selection = false;
				setVisible(false);
			}
			
		});
		
		JButton btnOk = new JButton("OK");
		btnOk.setBounds(125, 51, 89, 23);
		getContentPane().add(btnOk);
		btnOk.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				selection = true;
				setVisible(false);
			}
			
		});
	}
	
	public boolean okSelected(){return selection;}
	public String getText(){return textField.getText();}
}
