package waffleoRai_NTDExCore.importer;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import waffleoRai_Containers.nintendo.NDS;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.consoleproj.DSProject;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public class DSParserOption implements ParserOption {

	public static final String DEFO_ENG_STR = "Nintendo DS/DSi Game ROM (.nds)";
	public static final String DEFO_ENG_FFSTR = "Nintendo DS/DSi Cart ROM Dump";
	
	private String optionString;
	private String fileFilterString;
	
	public DSParserOption()
	{
		optionString = DEFO_ENG_STR;
		fileFilterString = DEFO_ENG_FFSTR;
	}
	
	@Override
	public void setOptionString(String op) 
	{
		optionString = op;
	}

	public void setFileFilterString(String s)
	{
		fileFilterString = s;
	}
	
	@Override
	public NTDProject generateProject(String path, GameRegion reg, Frame dialog_spawn_parent) throws IOException 
	{
		//Spawn dialog...
		IndefProgressDialog dialog = new IndefProgressDialog(dialog_spawn_parent, "Importing DS Image...");
		
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Parsing DS ROM data from " + path);
		
		NTDProject proj = null;
		
		SwingWorker<NTDProject, NTDProject> task = new SwingWorker<NTDProject, NTDProject>()
		{

			protected NTDProject doInBackground() throws Exception 
			{
				NTDProject p = null;
				try
				{
					//Read ROM
					NDS image = NDS.readROM(path, 0);
					//System.err.println("NDS ROM Read");
					
					//Check to see if that ROM has already been imported...
					boolean match = false;
					if(image.hasTWL()) match = NTDProgramFiles.gameHasBeenLoaded(Console.DSi, image.getGameCode(), reg);
					else match = NTDProgramFiles.gameHasBeenLoaded(Console.DS, image.getGameCode(), reg);
					//System.err.println("ROM match checked");
					
					if(match)
					{
						dialog.showWarningMessage("An image of this game has apparently been imported previously!\n"
								+ "Import will be cancelled.");
						return null;
					}
					
					//Load project
					dialog.setPrimaryString("Importing");
					dialog.setSecondaryString("Loading data from game \"" + image.getGameCode() + "\"");
					p = DSProject.createFromNDSImage(image, reg);
					//System.err.println("Project generated");
					
					//Decrypt if necessary
					dialog.setPrimaryString("Decrypting");
					dialog.setSecondaryString("Checking for modcrypted sectors...");
					boolean modcrypt = image.hasModcryptRegions();
					
					if(modcrypt)
					{
						
						dialog.showWarningMessage("This image contains modcrypted regions that cannot currently be read.\n"
								+ "Only data in these regions will be affected.\n"
								+ "It will be possible to reattempt decryption for any image once the modcrypt\n"
								+ "routine has been fully implemented!");
						
						
						//Load common key....
						/*byte[] dsi_key = NTDProgramFiles.getKey(NTDProgramFiles.KEYNAME_DSI_COMMON);
						if(dsi_key == null)
						{
							dialog.showWarningMessage("This image contains modcrypted regions that cannot be read without the DSi common key.\n"
									+ "Only data in these regions will be affected.\n"
									+ "It will be possible to reattempt decryption for any image once the key"
									+ "has been registered!");
							return p;
						}*/
						
						/*String decstem = p.getDecryptedDataDir() + File.separator + NTDProgramFiles.DECSTEM_DSI_MC;
						p.setDecryptStem(decstem);
						
						if(image.hasModcryptRegion1())
						{
							String path = decstem + "01.bin";
							dialog.setSecondaryString("Decrypting modcrypt region 1 to disk buffer at " + path);
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
							image.decryptSecureRegion1(bos);
							bos.close();
						}
						
						if(image.hasModcryptRegion2())
						{
							String path = decstem + "02.bin";
							dialog.setSecondaryString("Decrypting modcrypt region 2 to disk buffer at " + path);
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
							image.decryptSecureRegion2(bos);
							bos.close();
						}*/
						
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					dialog.showWarningMessage("ERROR: Import failed! See stderr for details.");
				}
				return p;
			}
			
			public void done()
			{
				dialog.closeMe();
			}
			
		};
		task.execute();
		dialog.render();
		
		try 
		{
			proj = task.get();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		} 
		catch (ExecutionException e) 
		{
			e.printStackTrace();
		}
		
		//System.err.println("Returning project. Project null: " + (proj == null));
		return proj;
	}

	@Override
	public Console getConsole() {return Console.DS;}

	public String toString(){return optionString;}
	
	public List<FileFilter> getExtFilters()
	{
		List<FileFilter> list = new LinkedList<FileFilter>();
		list.add(new FileFilter(){

			@Override
			public boolean accept(File f) 
			{
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath();
				return path.endsWith(".nds") || path.endsWith(".NDS");
			}

			@Override
			public String getDescription() 
			{
				return fileFilterString + " (.nds)";
			}
			
		});
		return list;
	}

}
