package waffleoRai_NTDExGUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExGUI.dialogs.InstallDialog;
import waffleoRai_NTDExGUI.dialogs.IntroDialog;
import waffleoRai_NTDScriptAPI.NTDScripts;

public class GUIMain {
	
	public static void launchInstaller()
	{
		SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	InstallDialog mygui = new InstallDialog(false);
            	mygui.addWindowListener(new WindowAdapter(){

					@Override
					public void windowClosing(WindowEvent e) 
					{
						//System.err.println("Close heard!");
						int selection = mygui.getSelection();
						if(selection == InstallDialog.SELECTION_INSTALL)
						{
							String ipath = mygui.getPath();	
							try 
							{
								NTDProgramFiles.installNTD(ipath);
							} 
							catch (IOException e1) 
							{
								e1.printStackTrace();
								JOptionPane.showMessageDialog(null, "Installation failed! See stderr for details.", "Installation Failed", JOptionPane.ERROR_MESSAGE);
								System.exit(1);
							}
							JOptionPane.showMessageDialog(null, "Installation succeeded!", "Success", JOptionPane.INFORMATION_MESSAGE);
							launchProgram();
							//System.exit(0);
						}
						else
						{
							mygui.dispose();
							System.exit(0);
						}
						
					}

            	});
            	mygui.render();
            }
        });
	}
	
	public static void launchProgram()
	{
		SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	IntroDialog intro = new IntroDialog();
            	intro.addWindowListener(new WindowAdapter(){
            		public void windowClosing(WindowEvent e) 
					{
            			int sel = intro.getSelection();
            			intro.dispose();
            			switch(sel)
            			{
            			case IntroDialog.SEL_NONE: System.exit(0);
            			case IntroDialog.SEL_EXIT: System.exit(0);
            			case IntroDialog.SEL_IMPORT: launchWithImport(); break;
            			case IntroDialog.SEL_MOVEINSTALL: 
            				InstallDialog install = new InstallDialog(true);
            				install.addWindowListener(new WindowAdapter(){

            					@Override
            					public void windowClosing(WindowEvent e) 
            					{
            						int selection = install.getSelection();
            						if(selection == InstallDialog.SELECTION_INSTALL)
            						{
            							String ipath = install.getPath();	
            							try 
            							{
            								NTDProgramFiles.moveInstallation(ipath);
            							} 
            							catch (IOException e1) 
            							{
            								e1.printStackTrace();
            								JOptionPane.showMessageDialog(null, "Move failed! See stderr for details.", "Installation Failed", JOptionPane.ERROR_MESSAGE);
            								System.exit(1);
            							}
            							JOptionPane.showMessageDialog(null, "Move succeeded!", "Success", JOptionPane.INFORMATION_MESSAGE);
            							install.dispose();
            							System.exit(0);
            						}
            						else
            						{
            							install.dispose();
            							System.exit(0);
            						}
            						
            					}

                        	});
            				install.render();
            				break;
            			case IntroDialog.SEL_OPEN: launchWithOpen(); break;
            			case IntroDialog.SEL_UNINSTALL: 
            				try
            				{
            					NTDProgramFiles.uninstallNTD();
            					JOptionPane.showMessageDialog(null, "Uninstallation succeeded!", "Success", JOptionPane.INFORMATION_MESSAGE);
            					System.exit(0);
            				}
            				catch(Exception x)
            				{
            					x.printStackTrace();
            					JOptionPane.showMessageDialog(null, "Uninstallation failed! See stderr for details.", "Uninstallation Failed", JOptionPane.ERROR_MESSAGE);
            					System.exit(1);
            				}
            				break;
            			}
					}
            	});
            	intro.render();
            }
        });
	}
	
	public static void launchWithImport()
	{
		SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	ExplorerForm myform = new ExplorerForm();
            	myform.addWindowListener(new WindowAdapter(){
            		public void windowClosing(WindowEvent e) 
					{
            			try
            			{
            				NTDProgramFiles.saveProgramInfo();
            				NTDProgramFiles.clearTempDir();
            				NTDScripts.setActiveForm(null);
            				System.exit(0);
            			}
            			catch(Exception x)
            			{
            				System.err.print("ERROR! Closing time preference save failed!");
            				x.printStackTrace();
            				System.exit(1);
            			}
					}
            	});
            	myform.renderWithImportDialog();
            }
        });
	}
	
	public static void launchWithOpen()
	{
		SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	ExplorerForm myform = new ExplorerForm();
            	myform.addWindowListener(new WindowAdapter(){
            		public void windowClosing(WindowEvent e) 
					{
            			try
            			{
            				NTDProgramFiles.saveProgramInfo();
            				NTDProgramFiles.clearTempDir();
            				NTDScripts.setActiveForm(null);
            				System.exit(0);
            			}
            			catch(Exception x)
            			{
            				System.err.print("ERROR! Closing time preference save failed!");
            				x.printStackTrace();
            				System.exit(1);
            			}
					}
            	});
            	myform.renderWithOpenDialog();
            }
        });
	}

	public static void main(String[] args) 
	{
		boolean installed = false;
		try
		{
			installed = NTDProgramFiles.readIni();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		if(!installed) launchInstaller();
		else launchProgram();
		
	}

}
