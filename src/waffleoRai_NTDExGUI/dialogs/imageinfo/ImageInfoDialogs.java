package waffleoRai_NTDExGUI.dialogs.imageinfo;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import waffleoRai_Containers.nintendo.NDS;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.ExplorerForm;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;

public class ImageInfoDialogs {
	
	public static void showImageInfoDialog(ExplorerForm parent, NTDProject proj)
	{
		switch(proj.getConsole())
		{
		case DS:
			showDSDialog(parent, proj);
			break;
		case DSi:
			showDSDialog(parent, proj);
			break;
		case GAMECUBE:
			break;
		case GB:
			break;
		case GBA:
			break;
		case GBC:
			break;
		case N64:
			break;
		case NES:
			break;
		case NEW_3DS:
			break;
		case SNES:
			break;
		case SWITCH:
			break;
		case UNKNOWN:
			break;
		case WII:
			break;
		case WIIU:
			break;
		case _3DS:
			break;
		default:
			break;
		
		}
	}
	
	private static void showDSDialog(ExplorerForm parent, NTDProject proj)
	{
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Loading ROM Info");
		
		String path = proj.getROMPath();
		dialog.setPrimaryString("Scanning ROM");
		dialog.setSecondaryString("Reading " + path);
		
		SwingWorker<NDS, Void> task = new SwingWorker<NDS, Void>()
		{

			protected NDS doInBackground() throws Exception 
			{
				NDS nds = null;
				try
				{
					nds = NDS.readROM(path, 0);
				}
				catch(IOException x)
				{
					x.printStackTrace();
					parent.showError("I/O Error: ROM load failed!");
				}
				
				return nds;
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
			NDS nds = task.get();
			DSImageInfoDialog dsdia = new DSImageInfoDialog(parent, nds);
			dsdia.setVisible(true);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			parent.showError("Async Error: ROM load failed!");
		} 
		catch (ExecutionException e) 
		{
			e.printStackTrace();
			parent.showError("Excution Error: ROM load failed!");
		}
		
	}

}
