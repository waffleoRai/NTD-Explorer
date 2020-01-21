package waffleoRai_NTDExGUI.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import waffleoRai_GUITools.RadioButtonGroup;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_Utils.FileBuffer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

public class AddKeyDialog extends JDialog{

	private static final long serialVersionUID = -1474806177359613150L;
	
	public static final int WIDTH = 365;
	public static final int HEIGHT = 235;
	
	public static final int ENTRYMETHOD_HEX = 0;
	public static final int ENTRYMETHOD_UPLOAD = 1;
	
	public static final String INIKEY_LASTOPENED = "LAST_KEY_UPLOAD";
	
	private JComboBox<String> cmbxKey;
	private JTextField txtHex;
	private JTextField txtFile;
	private RadioButtonGroup rbGroup;
	
	private boolean save_selected;
	private byte[] data;

	public AddKeyDialog(Frame parent)
	{
		super(parent, true);
		rbGroup = new RadioButtonGroup(2);
		
		initGUI(parent);
	}
	
	private void initGUI(Frame parent)
	{
		setResizable(false);
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setPreferredSize(sz);
		setLocationRelativeTo(parent);
		
		setTitle("Add Decryption Key");
		getContentPane().setLayout(null);
		
		JLabel lblKey = new JLabel("Key:");
		lblKey.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblKey.setBounds(60, 14, 33, 14);
		getContentPane().add(lblKey);
		
		cmbxKey = new JComboBox<String>();
		cmbxKey.setBounds(103, 11, 199, 20);
		getContentPane().add(cmbxKey);
		loadCombobox();
		
		JRadioButton rbHex = new JRadioButton("Enter hex: 0x");
		rbHex.setFont(new Font("Tahoma", Font.PLAIN, 11));
		rbHex.setBounds(6, 64, 91, 23);
		getContentPane().add(rbHex);
		rbGroup.addButton(rbHex, 0);
		
		txtHex = new JTextField();
		txtHex.setBounds(103, 65, 244, 20);
		getContentPane().add(txtHex);
		txtHex.setColumns(10);
		
		JRadioButton rbFile = new JRadioButton("Upload file:");
		rbFile.setBounds(6, 90, 91, 23);
		getContentPane().add(rbFile);
		rbGroup.addButton(rbFile, 1);
		
		txtFile = new JTextField();
		txtFile.setBounds(103, 94, 244, 20);
		getContentPane().add(txtFile);
		txtFile.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.setBounds(258, 125, 89, 23);
		getContentPane().add(btnBrowse);
		btnBrowse.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				browse();
			}
			
		});
		
		JButton btnSave = new JButton("Save");
		btnSave.setBounds(258, 173, 89, 23);
		getContentPane().add(btnSave);
		btnSave.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				save_selected = true;
				try
				{
					loadData();
				}
				catch(IOException x)
				{
					showError("I/O Error: Input file could not be read!");
					return;
				}
				catch(NumberFormatException x)
				{
					showError("Input hex value could not be parsed. Please enter"
							+ "valid hexidecimal value!");
					return;
				}
				closeMe();
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(164, 173, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				save_selected = false;
				closeMe();
			}
			
		});
		
		rbGroup.select(0);
		rbHex.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				txtHex.setEnabled(true);
				txtFile.setEnabled(false);
				btnBrowse.setEnabled(false);
			}
			
		});
		rbFile.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				txtHex.setEnabled(false);
				txtFile.setEnabled(true);
				btnBrowse.setEnabled(true);
			}
			
		});
	}
	
	private void loadCombobox()
	{
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
		for(String s : NTDProgramFiles.ALL_KEYKEYS) model.addElement(s);
		cmbxKey.setModel(model);
	}
	
	private void browse()
	{
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(INIKEY_LASTOPENED));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		int select = fc.showOpenDialog(this);
		
		if(select == JFileChooser.APPROVE_OPTION)
		{
			txtFile.setText(fc.getSelectedFile().getAbsolutePath());
			txtFile.repaint();
		}
	}
	
	public boolean saveSelected()
	{
		return save_selected;
	}
	
	public int getSelectedMethod()
	{
		return rbGroup.getSelectedIndex();
	}
	
	public String getKey()
	{
		int idx = cmbxKey.getSelectedIndex();
		return cmbxKey.getItemAt(idx);
	}
	
	private int readChar(char c)
	{
		switch(c)
		{
		case '0': return 0x0;
		case '1': return 0x1;
		case '2': return 0x2;
		case '3': return 0x3;
		case '4': return 0x4;
		case '5': return 0x5;
		case '6': return 0x6;
		case '7': return 0x7;
		case '8': return 0x8;
		case '9': return 0x9;
		case 'a': return 0xa;
		case 'b': return 0xb;
		case 'c': return 0xc;
		case 'd': return 0xd;
		case 'e': return 0xe;
		case 'f': return 0xf;
		case 'A': return 0xa;
		case 'B': return 0xb;
		case 'C': return 0xc;
		case 'D': return 0xd;
		case 'E': return 0xe;
		case 'F': return 0xf;
		}
		
		throw new NumberFormatException();
	}
	
	private void loadData() throws IOException, NumberFormatException
	{
		int method = rbGroup.getSelectedIndex();
		
		if(method == ENTRYMETHOD_HEX)
		{
			//Try to read the string...
			Deque<Byte> q = new LinkedList<Byte>();
			String raw = txtHex.getText();
			raw.replace("0x", "");
			int len = raw.length();
			int pos = len-1;
			
			while(pos >= 0)
			{
				//Grab two characters
				char c0 = '\0';
				char c1 = '\0';
				
				if((pos-1) < 0) c0 = '0';
				else c0 = raw.charAt(pos-1);
				c1 = raw.charAt(pos);
				
				int hi = readChar(c0);
				int lo = readChar(c1);
				
				int bi = (hi << 4) | lo;
				q.push((byte)bi);
				
				pos-=2;
			}
			
			data = new byte[q.size()];
			int i = 0;
			while(!q.isEmpty())
			{
				data[i] = q.pop();
				i++;
			}
			
		}
		else if(method == ENTRYMETHOD_UPLOAD)
		{
			data = FileBuffer.createBuffer(txtFile.getText()).getBytes();
		}
		
	}
	
	public byte[] getData()
	{
		return data;
	}
	
	public void closeMe()
	{
		setVisible(false);
		WindowListener[] listeners = this.getWindowListeners();
		for(WindowListener l : listeners) l.windowClosing(new WindowEvent(this, 0));
		dispose();
	}
	
	public void showWarning(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
}
