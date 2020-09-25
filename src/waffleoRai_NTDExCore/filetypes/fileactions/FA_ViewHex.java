package waffleoRai_NTDExCore.filetypes.fileactions;

import java.awt.Frame;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.dialogs.HexLaunchDialog;
import waffleoRai_NTDExGUI.dialogs.HexPreviewDialog;
import waffleoRai_Files.tree.FileNode;

public class FA_ViewHex implements FileAction{
	
	private static final String DEFO_ENG_STRING = "Preview hex";
	
	private static FA_ViewHex static_me;
	
	public static FileAction getAction()
	{
		if(static_me == null) static_me = new FA_ViewHex();
		return static_me;
	}
	
	private String str;
	
	public FA_ViewHex()
	{
		str = DEFO_ENG_STRING;
	}
	
	@Override
	public void doAction(FileNode node, NTDProject project, Frame gui_parent) 
	{
		long max = node.getLength();
		
		Frame parent = null;
		if(gui_parent instanceof Frame)
		{
			parent = (Frame)gui_parent;
		}
		HexLaunchDialog dialog = new HexLaunchDialog(parent, max);
		dialog.setLocationRelativeTo(gui_parent);
		dialog.setVisible(true);
		
		if(!dialog.getDecompSwitch()) return;
		
		SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	HexPreviewDialog hexform = new HexPreviewDialog();
            	try 
            	{
					hexform.load(node, dialog.getOffset(), dialog.getDecompSwitch());
					hexform.setLocationRelativeTo(gui_parent);
					hexform.setVisible(true);
				} 
            	catch (IOException e) 
            	{
					e.printStackTrace();
					JOptionPane.showMessageDialog(gui_parent, "ERROR: File preview could not be loaded!", 
							"File load failed!", JOptionPane.ERROR_MESSAGE);
					return;
				}
            }
        });
		
	}

	@Override
	public void setString(String s) {str = s;}
	
	public String toString(){return str;}


}
