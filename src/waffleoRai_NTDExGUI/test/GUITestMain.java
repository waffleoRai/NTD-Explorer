package waffleoRai_NTDExGUI.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import waffleoRai_Containers.nintendo.NDS;
import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.consoleproj.DSProject;
import waffleoRai_NTDExGUI.dialogs.OpenDialog;

public class GUITestMain {

	public static void main(String[] args) 
	{
		String indir = "E:\\Library\\Games\\Console";
		
		List<NTDProject> projlist = new LinkedList<NTDProject>();
		
		try
		{
			//List<String> pathlist = new LinkedList<String>();
			System.err.println("Looking for nds files in " + indir);
			DirectoryStream<Path> dirstr = Files.newDirectoryStream(Paths.get(indir));
			for(Path p : dirstr)
			{
				String pstr = p.toAbsolutePath().toString();
				if(pstr.endsWith(".nds")) {
					//Try to get region...
					System.err.println("Reading " + pstr);
					String reg = pstr.substring(pstr.lastIndexOf("_") + 1, pstr.lastIndexOf("."));
					NDS image = NDS.readROM(pstr, 0);
					GameRegion gr = GameRegion.getRegion(reg);
					if(gr == null) gr = GameRegion.USA;
					NTDProject proj = DSProject.createFromNDSImage(image, gr);
					projlist.add(proj);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		Map<Console, Collection<NTDProject>> map = new HashMap<Console, Collection<NTDProject>>();
		map.put(Console.DS, projlist);
		
		System.err.println("Images read! Now showing GUI");
		SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	//Dummy frame
            	//JFrame frame = new JFrame();
            	//frame.setLocation(GUITools.getScreenCenteringCoordinates(frame));
            	
            	System.err.println("Preparing GUI...");
            	OpenDialog d = new OpenDialog(null, map);
            	
            	d.addWindowListener(new WindowAdapter(){
            		public void windowClosing(WindowEvent e)
            		{
            			System.exit(0);
            		}
            	});
            	
            	d.setVisible(true);
            }
        });
		
	}

}
