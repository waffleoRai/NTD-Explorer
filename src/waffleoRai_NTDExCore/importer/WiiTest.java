package waffleoRai_NTDExCore.importer;

import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.consoleproj.WiiProject;

public class WiiTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try{
			String wiipath = "E:\\Library\\Games\\Console\\RVL_SOUE_USA.wii";
			
			//Try to read it as a WiiProject (without key)
			WiiProject proj = WiiProject.createFromWiiImage(wiipath, GameRegion.USA);
			proj.getTreeRoot().printMeToStdErr(0);
			
		}
		catch(Exception x){
			x.printStackTrace();
			System.exit(1);
		}
		
	}

}
