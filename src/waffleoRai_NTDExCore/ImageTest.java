package waffleoRai_NTDExCore;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import waffleoRai_Containers.nintendo.NDS;
import waffleoRai_GUITools.GUITools;
import waffleoRai_GUITools.ImagePane;

public class ImageTest {

	public static void main(String[] args) 
	{
		//String dspath = "C:\\Users\\Blythe\\Documents\\Game Stuff\\DS\\Games\\Pokemon Platinum.nds";
		//String testpath = "C:\\Users\\Blythe\\Documents\\Game Stuff\\DS\\Games\\test.png";
		String dspath = "C:\\Users\\Blythe\\Documents\\Game Stuff\\DS\\Games\\Pokemon White Version 2.nds";
		try
		{
			NDS rom = NDS.readROM(dspath, 0);
			String name = rom.getBannerTitle(NDS.TITLE_LANGUAGE_ENGLISH);
			System.err.println("English Banner Title: " + name);
			
			//BufferedImage img = NTDProgramFiles.getDefaultImage_unknown();
			
			BufferedImage[] banner = rom.getBannerIcon();
			ImagePane pane = new ImagePane(banner);
			pane.setRefreshRate((int)Math.round(1000.0/60.0));
			
			//Alright, I guess try printing the banner...?
			/*BufferedImage b0 = banner[0];
			for(int i = 0; i < b0.getHeight(); i++)
			{
				for(int j = 0; j < b0.getWidth(); j++)
				{
					int argb = b0.getRGB(j, i);
					System.err.print(String.format("%08x ", argb));
				}
				System.err.println();
			}*/
			
			//ImageIO.write(banner[0], "png", new File(testpath));
			
			JFrame frame = new JFrame();
			frame.add(pane);
	
			SwingUtilities.invokeLater(new Runnable() 
	        {
	            public void run() 
	            {
	            	frame.addWindowListener(new WindowAdapter()
	            	{

						@Override
						public void windowClosing(WindowEvent e) 
						{
							pane.stopAnimation();
							System.exit(0);
						}
	            		
	            	});
	            	frame.pack();
	            	Point coords = GUITools.getScreenCenteringCoordinates(frame);
	    			frame.setLocation(coords);
	            	frame.setVisible(true);
	            	pane.startAnimation();
	            }

	        });

			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

	}

}
