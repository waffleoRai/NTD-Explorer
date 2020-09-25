package waffleoRai_NTDTypes.nitro;

import java.awt.Component;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Image.nintendo.nitro.NCGR;
import waffleoRai_NTDExCore.ExportAction;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExCore.NTDValues;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.preview.HexPreviewPanel;
import waffleoRai_NTDExGUI.panels.preview.TilesetViewPanel;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Files.tree.FileNode;

public class TM_NitroCGR extends TypeManager {
	
	public FileTypeNode detectFileType(FileNode node) {
		return NitroFiles.detectNitroFile(node, NCGR.MAGIC, NCGR.getTypeDef(), false);
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		
		try{
			NCGR ncgr = NCGR.readNCGR(node.loadDecompressedData());
			
			List<BufferedImage> list = ncgr.getTileset().renderTileData();
			//System.err.println("Rendered tiles: " + list.size());
			TilesetViewPanel tvpnl = new TilesetViewPanel();
			tvpnl.setTilesPerRow(ncgr.getTileset().getWidthInTiles());
			tvpnl.loadTiles(list);
			return tvpnl;
		}
		catch(Exception x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: Node data could not be loaded!", 
					"I/O Error", JOptionPane.ERROR_MESSAGE);
		}
		
		
		HexPreviewPanel pnl = new HexPreviewPanel();
		try{pnl.load(node, 0, false);}
		catch(IOException e){
			System.err.println("Node points to: " + node.getSourcePath() + " 0x" + 
						Long.toHexString(node.getOffset()));
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: Node data could not be loaded!", 
					"I/O Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return pnl;
	}

	public List<FileAction> getFileActions() {
		List<FileAction> alist = new ArrayList<FileAction>(4);
		alist.add(FA_ExtractFile.getAction());
		alist.add(new FA_ExportSingle());
		alist.add(new FA_ExportMulti());
		alist.add(FA_ViewHex.getAction());
		return alist;
	}
	
	public Converter getStandardConverter(){
		return null;
	}
	
	public boolean isOfType(FileNode node){
		return detectFileType(node) != null;
	}

	/*----- Actions -----*/
	
	public static class FA_ExportSingle implements FileAction{
		
		private String str = "Export as Single Image";

		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			ExportAction action = new ExportAction(){

				public void doExport(String dirpath, IndefProgressDialog dialog) throws IOException {
					
					dialog.setPrimaryString("Rendering Tile Image");
					String targetpath = dirpath + File.separator + node.getFileName().replace(".ncgr", ".png");
					dialog.setSecondaryString("Writing to " + targetpath);
					
					NCGR ncgr = NCGR.readNCGR(node.loadDecompressedData());
					BufferedImage img = ncgr.getTileset().renderImageData(NTDValues.getTLE_ExportWidth());
					ImageIO.write(img, "png", new File(targetpath));
					
				}
				
			};
			NTDTools.runExport(gui_parent, action, "I/O Error: Could not read NCGR or export to PNG!");
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
	}
	
	public static class FA_ExportMulti implements FileAction{
		
		private String str = "Export Tiles to PNG";

		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			ExportAction action = new ExportAction(){

				public void doExport(String dirpath, IndefProgressDialog dialog) throws IOException {
					
					dialog.setPrimaryString("Rendering Tile Image");
					String targetpath = dirpath + File.separator + node.getFileName();
					dialog.setSecondaryString("Writing to " + targetpath);
					
					if(!FileBuffer.directoryExists(targetpath)) Files.createDirectories(Paths.get(targetpath));
					
					NCGR ncgr = NCGR.readNCGR(node.loadDecompressedData());
					int tcount = ncgr.getTileCount();
					for(int i = 0; i < tcount; i++){
						String outpath = targetpath + File.separator + String.format("tile_%05d.png", i);
						BufferedImage img = ncgr.getTileset().renderTileData(i);
						ImageIO.write(img, "png", new File(outpath));
					}
				}
				
			};
			NTDTools.runExport(gui_parent, action, "I/O Error: Could not read NCGR or export to PNG!");
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
	}
	

}
