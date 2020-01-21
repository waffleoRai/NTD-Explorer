package waffleoRai_NTDExGUI.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JCheckBox;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.JButton;

public class HexLaunchDialog extends JDialog{

	private static final long serialVersionUID = -1668197474067660750L;
	
	public static final int WIDTH = 210;
	public static final int HEIGHT = 135;
	
	private JTextField textField;
	
	private long max;
	
	private boolean decomp;
	private long offset;
	private boolean cont;

	public HexLaunchDialog(Frame parent, long max_off)
	{
		super(parent, true);
		setResizable(false);
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setPreferredSize(sz);
		
		setTitle("Hex Preview");
		getContentPane().setLayout(null);
		
		JCheckBox chckbxViewDecompressed = new JCheckBox("View Decompressed");
		chckbxViewDecompressed.setFont(new Font("Tahoma", Font.PLAIN, 11));
		chckbxViewDecompressed.setBounds(6, 7, 135, 23);
		getContentPane().add(chckbxViewDecompressed);
		
		JLabel lblStartOffset = new JLabel("Start Offset/0x10: 0x");
		lblStartOffset.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblStartOffset.setBounds(6, 37, 112, 14);
		getContentPane().add(lblStartOffset);
		
		textField = new JTextField();
		textField.setText("0");
		textField.setBounds(116, 34, 58, 20);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		JLabel label = new JLabel("0");
		label.setFont(new Font("Tahoma", Font.PLAIN, 11));
		label.setBounds(177, 37, 18, 14);
		getContentPane().add(label);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(6, 62, 182, 2);
		getContentPane().add(separator);
		
		JDialog me = this;
		JButton btnContinue = new JButton("Continue");
		btnContinue.setBounds(6, 73, 89, 23);
		getContentPane().add(btnContinue);
		btnContinue.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				decomp = chckbxViewDecompressed.isSelected();
				
				//Try to read the offset
				long raw = -1;
				try
				{
					raw = Long.parseUnsignedLong(textField.getText(), 16);
				}
				catch(NumberFormatException x)
				{
					JOptionPane.showMessageDialog(me, "Please enter a valid hexidecimal offset!", 
							"Invalid Offset", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				raw = raw << 4;
				if(raw > max)
				{
					JOptionPane.showMessageDialog(me, "Please enter a valid hexidecimal offset!", 
							"Invalid Offset", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				offset = raw;
				cont = true;
				setVisible(false);
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(105, 73, 75, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				cont = false;
				setVisible(false);
			}
			
		});
	}

	public boolean getDecompSwitch()
	{
		return decomp;
	}
	
	public long getOffset()
	{
		return offset;
	}
	
	public boolean getSelection()
	{
		return cont;
	}
	
}
