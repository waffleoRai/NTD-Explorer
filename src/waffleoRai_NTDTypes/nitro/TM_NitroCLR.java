package waffleoRai_NTDTypes.nitro;

import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Image.Palette;
import waffleoRai_Image.nintendo.nitro.NCLR;
import waffleoRai_NTDExCore.ExportAction;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.preview.HexPreviewPanel;
import waffleoRai_NTDExGUI.panels.preview.PaletteViewPanel;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Files.tree.FileNode;

public class TM_NitroCLR extends TypeManager{
	
	public FileTypeNode detectFileType(FileNode node) {
		return NitroFiles.detectNitroFile(node, NCLR.MAGIC, NCLR.getTypeDef(), false);
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		
		try{
			NCLR nclr = NCLR.readNCLR(node.loadDecompressedData());
			int pcount = nclr.getPaletteCount();
			List<Palette> plist = new LinkedList<Palette>();
			for(int i = 0; i < pcount; i++) plist.add(nclr.getPalette(i));
			
			PaletteViewPanel pvp = new PaletteViewPanel();
			pvp.loadPalettes(plist);
			return pvp;
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
		//Extract, Export to csv, export to png
		List<FileAction> alist = new ArrayList<FileAction>(4);
		alist.add(FA_ExtractFile.getAction());
		alist.add(new FA_ExportCSV());
		alist.add(new FA_ExportImage());
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
	
	public static class FA_ExportCSV implements FileAction{
		
		private String str = "Export CSV";

		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			ExportAction action = new ExportAction(){

				public void doExport(String dirpath, IndefProgressDialog dialog) throws IOException {
					
					dialog.setPrimaryString("Tabling Palette Data");
					String targetpath = dirpath + File.separator + node.getFileName().replace(".nclr", ".csv");
					dialog.setSecondaryString("Writing to " + targetpath);
					NCLR nclr = NCLR.readNCLR(node.loadDecompressedData());
					
					BufferedWriter bw = new BufferedWriter(new FileWriter(targetpath));
					bw.write("Palette,Index,Value(Hexcode),Red8,Green8,Blue8,Value(Raw),Red5,Green5,Blue5\n");
					int pcount = nclr.getPaletteCount();
					for(int i = 0; i < pcount; i++){
						Palette p = nclr.getPalette(i);
						short[] raw = nclr.getRawColors(i);
						int ccount = raw.length;
						for(int j = 0; j < ccount; j++){
							bw.write(i + ","); bw.write(j + ",");
							bw.write("#" + String.format("%06x", (p.getPixel(j).getARGB()&0xFFFFFF)) + ",");
							bw.write(p.getRed(j) + ","); bw.write(p.getGreen(j) + ","); bw.write(p.getBlue(j) + ",");
							if(raw == null) bw.write("NULL,NULL,NULL,NULL");
							else{
								int five = raw[j];
								bw.write(String.format("%04x", five) + ",");
								bw.write((five & 0x1F) + ",");
								bw.write(((five >>> 5) & 0x1F) + ",");
								bw.write(((five >>> 10) & 0x1F));
							}
							bw.write("\n");
						}
					}
					bw.close();
					
				}
				
			};
			NTDTools.runExport(gui_parent, action, "I/O Error: Could not read NCLR or export as CSV!");
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
	}
	
	public static class FA_ExportImage implements FileAction{
		
		private String str = "Save Palette Image";

		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
		
			ExportAction action = new ExportAction(){

				public void doExport(String dirpath, IndefProgressDialog dialog) throws IOException {
					
					dialog.setPrimaryString("Rendering Palette Data");
					String targetpath = dirpath + File.separator + node.getFileName();
					dialog.setSecondaryString("Writing to " + targetpath);
					NCLR nclr = NCLR.readNCLR(node.loadDecompressedData());
					
					if(!FileBuffer.directoryExists(targetpath)) Files.createDirectories(Paths.get(targetpath));
					
					int pcount = nclr.getPaletteCount();
					for(int i = 0; i < pcount; i++){
						String path = dirpath + File.separator + String.format("nclr_%03d.png", i);
						ImageIO.write(nclr.getPalette(i).renderVisual(), "png", new File(path));
					}
					
				}
				
			};
			NTDTools.runExport(gui_parent, action, "I/O Error: Could not read NCLR or export image!");
			
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
	}
	
	
}
