package waffleoRai_NTDExCore.importer;

import java.time.format.DateTimeFormatter;
import java.util.Collection;

import waffleoRai_Containers.ISO;
import waffleoRai_Containers.ISOXAImage;
import waffleoRai_Files.ISOFileNode;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.memcard.BannerImporter.BannerStruct;
import waffleoRai_NTDExCore.memcard.PSXMCBannerImporter;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileNode;

public class TestPSXImport {

	public static void main(String[] args) {
		
		String testpath = "E:\\Library\\Games\\Console\\PSX_SLPM87176_01.iso";
		
		try{
			//Try to load image.
			System.err.println("Testing ISO parser...");
			ISOXAImage image = new ISOXAImage(new ISO(FileBuffer.createBuffer(testpath), false));
			
			System.err.println("Testing tree generation...");
			DirectoryNode root = image.getRootNode();
			root.printMeToStdErr(0);
			
			System.err.println("Testing tree paths...");
			String volident = root.getMetadataValue(ISOXAImage.METAKEY_VOLUMEIDENT);
			System.err.println("Volume Ident: " + volident);
			FileNode afile = root.getNodeAt("/" + volident + "/SYSTEM.CNF");
			System.err.println("Node is null?: " + (afile == null));
			afile.setSourcePath(testpath);
			
			System.err.println("Testing raw file extraction...");
			String outdir = "C:\\Users\\Blythe\\Documents\\Desktop\\out\\psxtest";
			String outpath = outdir + "\\systemcfg_raw.bin";
			FileBuffer f = ((ISOFileNode)afile).loadRawData();
			f.writeFile(outpath);
			
			System.err.println("Testing file data extraction...");
			outpath = outdir + "\\system.cnf";
			f = afile.loadData();
			f.writeFile(outpath);
			
			//Try generating a project
			System.err.println("Testing project generation...");
			NTDProject proj = NTDProject.createFromPSXTrack(testpath, GameRegion.JPN);
			System.err.println("Banner Title: " + proj.getBannerTitle());
			System.err.println("Publisher: " + proj.getPublisherTag());
			System.err.println("Date: " + proj.getVolumeTime().format(DateTimeFormatter.RFC_1123_DATE_TIME));
			System.err.println("GameCode: " + proj.getGameCode4());
			System.err.println("FullCode: " + proj.getGameCode12());
			System.err.println("Path: " + proj.getROMPath());
			System.err.println("Console: " + proj.getConsole());
			System.err.println("Region: " + proj.getRegion());
			
			//Test memory card preview
			System.err.println("Testing project memory card banner matching...");
			String mcpath = "C:\\Users\\Blythe\\Documents\\Program Files D\\ePSXe\\memcards\\epsxe000.mcr";
			PSXMCBannerImporter bimp = new PSXMCBannerImporter();
			Collection<BannerStruct> banners = bimp.findBanner(mcpath, proj.getGameCode12());
			if(banners.isEmpty())System.err.println("No banners found :(");
			else{
				System.err.println("ユニコード");
				for(BannerStruct b : banners){
					System.err.println("Banner: " + b.title);	
				}
			}
		
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}

	}

}
