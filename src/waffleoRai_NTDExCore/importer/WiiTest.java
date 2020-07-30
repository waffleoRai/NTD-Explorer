package waffleoRai_NTDExCore.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Containers.nintendo.GCWiiDisc;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.consoleproj.WiiProject;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileNode;

public class WiiTest {

	public static void mapByOffset(Map<Long, FileNode> osmap, DirectoryNode dir){
		
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children){
			if(child instanceof DirectoryNode){
				mapByOffset(osmap, ((DirectoryNode)child));
			}
			else{
				osmap.put(child.getOffset(), child);
			}
		}
		
	}
	
	public static void main(String[] args) {

		try{
			String wiipath = "E:\\Library\\Games\\Console\\RVL_RSBE_USA.wii";
			
			//To load key path...
			//NTDProgramFiles.readIni();
			
			//Try to read it as a WiiProject
			WiiProject proj = WiiProject.createFromWiiImage(wiipath, GameRegion.USA, null);
			proj.getTreeRoot().printMeToStdErr(0);
			
			//Copy data so can inspect...
			/*String testpath = "E:\\Library\\Games\\Console\\decbuff\\test\\SOUE_p0-0";
			FileNode fn = proj.getTreeRoot().getNodeAt("/00/part00/h3.bin");
			fn.loadData().writeFile(testpath + "\\h3.bin");
			fn = proj.getTreeRoot().getNodeAt("/00/part00/data.aes");
			fn.loadData().writeFile(testpath + "\\data.aes");*/
			
			//Just the partition tree...
			/*String wiipart = "E:\\Library\\Games\\Console\\decbuff\\RVL_SOUE_USA\\rvl_aes_part00_01.wiip";
			GCWiiDisc part = new GCWiiDisc(wiipart);
			part.getDiscTree().printMeToStdErr(0);*/
			
			//Instead sort by offset...
			/*Map<Long, FileNode> osmap = new TreeMap<Long, FileNode>();
			mapByOffset(osmap, part.getDiscTree());
			List<Long> keylist = new ArrayList<Long>(osmap.size()+1);
			keylist.addAll(osmap.keySet());
			Collections.sort(keylist);
			for(Long k : keylist){
				FileNode fn = osmap.get(k);
				String offstr = Long.toHexString(fn.getOffset());
				String edstr = Long.toHexString(fn.getOffset() + fn.getLength());
				System.err.println(fn.getFullPath() + "(0x" + offstr + " - 0x" + edstr + ")");
			}*/
			
		}
		catch(Exception x){
			x.printStackTrace();
			System.exit(1);
		}
		
	}

}
