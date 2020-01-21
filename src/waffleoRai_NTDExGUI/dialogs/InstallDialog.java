package waffleoRai_NTDExGUI.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JTextField;

import waffleoRai_GUITools.GUITools;
import waffleoRai_NTDExCore.NTDProgramFiles;

import javax.swing.JButton;

public class InstallDialog extends JFrame{

	private static final long serialVersionUID = -2344600400425045452L;
	
	private static final String MESSAGE_ENG_1_NEW = "Init file was not found for user \"%n\"!";
	private static final String MESSAGE_ENG_2 = "(%p)";
	private static final String MESSAGE_ENG_3_NEW = "You will need to create a program directory for NTD Explorer before use!";
	
	private static final String MESSAGE_ENG_1_MOVE = "Init file for user \"%n\" found at: ";
	private static final String MESSAGE_ENG_3_MOVE = "Please specify directory to move installation to:";
	
	public static final int WIDTH = 400;
	public static final int HEIGHT = 250;
	
	public static final int SELECTION_NONE = 0;
	public static final int SELECTION_INSTALL = 1;
	public static final int SELECTION_CANCEL = 2;
	
	private String default_path;
	
	private int selection;
	private JTextField txtPath;
	
	public InstallDialog()
	{
		selection = SELECTION_NONE;
		initGUI(false);
	}
	
	public InstallDialog(boolean move)
	{
		selection = SELECTION_NONE;
		initGUI(move);
	}
	
	private void initGUI(boolean move)
	{
		setTitle("Install NTD Explorer");
		setResizable(false);
		getContentPane().setLayout(null);
		
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setLocation(GUITools.getScreenCenteringCoordinates(this));
		
		String msg = MESSAGE_ENG_1_NEW.replace("%n", NTDProgramFiles.getUsername());
		if(move) msg = MESSAGE_ENG_1_MOVE.replace("%n", NTDProgramFiles.getUsername());
		JLabel lblMain = new JLabel(msg);
		lblMain.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblMain.setBounds(10, 11, 351, 22);
		getContentPane().add(lblMain);
		
		String inipath = NTDProgramFiles.getIniPath();
		msg = MESSAGE_ENG_2.replace("%p", inipath);
		JLabel label = new JLabel(msg);
		label.setFont(new Font("Tahoma", Font.PLAIN, 11));
		label.setBounds(10, 32, 351, 20);
		getContentPane().add(label);
		
		if(move) msg = MESSAGE_ENG_3_MOVE;
		else msg = MESSAGE_ENG_3_NEW;
		JLabel label_1 = new JLabel(msg);
		label_1.setFont(new Font("Tahoma", Font.PLAIN, 11));
		label_1.setBounds(10, 80, 351, 22);
		getContentPane().add(label_1);
		
		//Determine default path...
		default_path = inipath;
		//int lastslash = default_path.lastIndexOf(File.separator);
		//if(lastslash >= 0) default_path = default_path.substring(0, lastslash);
		default_path = default_path.substring(0, default_path.lastIndexOf(File.separatorChar));
		
		txtPath = new JTextField(default_path);
		txtPath.setBounds(10, 128, 374, 20);
		getContentPane().add(txtPath);
		txtPath.setColumns(10);
		
		JLabel lblInstallTo = new JLabel("Install to:");
		lblInstallTo.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblInstallTo.setBounds(10, 113, 46, 14);
		getContentPane().add(lblInstallTo);
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.setBounds(10, 159, 89, 23);
		getContentPane().add(btnBrowse);
		btnBrowse.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e){
				onBrowse();
			}
			
		});
		
		JButton btnInstall = new JButton("Install");
		btnInstall.setBounds(196, 187, 89, 23);
		getContentPane().add(btnInstall);
		btnInstall.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e){
				selection = SELECTION_INSTALL;
				closeMe();
			}
			
		});
		
		JButton btnExit = new JButton("Exit");
		btnExit.setBounds(295, 187, 89, 23);
		getContentPane().add(btnExit);
		btnExit.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e){
				selection = SELECTION_CANCEL;
				closeMe();
			}
			
		});
		
	}

	public void render()
	{
		pack();
		setVisible(true);
	}
	
	public void onBrowse()
	{
		JFileChooser fc = new JFileChooser(default_path);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int retVal = fc.showSaveDialog(this);
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			File f = fc.getSelectedFile();
			String p = f.getAbsolutePath();
			
			this.txtPath.setText(p);
		}
	}
	
	public int getSelection(){return this.selection;}
	public String getPath(){return this.txtPath.getText();}

	public void closeMe()
	{
		setVisible(false);
		WindowListener[] listeners = this.getWindowListeners();
		for(WindowListener l : listeners) l.windowClosing(new WindowEvent(this, 0));
		dispose();
	}
	
}
