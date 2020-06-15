package waffleoRai_NTDTypes.nitro;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_GUITools.CheckeredImagePane;
import waffleoRai_GUITools.ImagePaneDrawer;
import waffleoRai_Image.nintendo.nitro.NCGR;
import waffleoRai_Image.nintendo.nitro.NCLR;
import waffleoRai_Image.nintendo.nitro.NSCR;
import waffleoRai_NTDExCore.ExportAction;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.dialogs.graphics.ImageResourceMatchDialog;
import waffleoRai_NTDExGUI.dialogs.graphics.ImageResourceMatcher;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.preview.HexPreviewPanel;
import waffleoRai_NTDExGUI.panels.preview.ScrollingImageViewPanel;
import waffleoRai_Utils.FileNode;

public class TM_NitroSCR extends TypeManager {
	
	public FileTypeNode detectFileType(FileNode node) {
		return NitroFiles.detectNitroFile(node, NSCR.MAGIC, NSCR.getTypeDef(), false);
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
		
		try{
			NSCR nscr = NSCR.readNSCR(node.loadDecompressedData());
			NCGR ncgr = NSCR.loadLinkedTileset(node);
			
			if(ncgr == null){
				JOptionPane.showMessageDialog(gui_parent, 
						"Screen data has no linked tile data (NCGR/NBGR)!", 
						"No Tile Data", JOptionPane.WARNING_MESSAGE);
			}
			else{
				NCLR nclr = NSCR.loadLinkedPalette(node);
				BufferedImage img = nscr.renderImage(nclr.getPalettes(), ncgr.getTileset());
				ScrollingImageViewPanel vpnl = new ScrollingImageViewPanel();
				CheckeredImagePane pane = vpnl.getImagePanel();
				pane.setDrawingAreaSize(new Dimension(img.getWidth()+10, img.getHeight()+10));
				pane.addItem(new ImagePaneDrawer(){
					
					public int getX() {return 0;}
					public int getY() {return 0;}
					public int getWidth() {return img.getWidth();}
					public int getHeight() {return img.getHeight();}

					public void drawMe(Graphics g, int x, int y) {
						g.drawImage(img, x, y, null);
						
					}
					
				});
				
				return vpnl;
			}
			
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
		//Extract, export (to PNG), set tileset, set palette, view hex
		List<FileAction> alist = new ArrayList<FileAction>(5);
		alist.add(FA_ExtractFile.getAction());
		alist.add(FA_Export.getStaticInstance());
		alist.add(FA_SetTileset.getStaticInstance());
		alist.add(FA_SetPalette.getStaticInstance());
		alist.add(FA_ViewHex.getAction());
		return alist;
	}
	
	public Converter getStandardConverter(){
		return NSCR.getConverter();
	}
	
	public boolean isOfType(FileNode node){
		return detectFileType(node) != null;
	}

	/*----- Actions -----*/
	
	public static class FA_Export implements FileAction{
		
		private static FA_Export staticme;
		private String str = "Export as PNG";

		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			ExportAction action = new ExportAction(){

				public void doExport(String dirpath, IndefProgressDialog dialog) throws IOException {
					
					dialog.setPrimaryString("Rendering Image");
					String targetpath = dirpath + File.separator + node.getFileName().replace(".nscr", ".png");
					dialog.setSecondaryString("Writing to " + targetpath);
					
					NSCR nscr = NSCR.readNSCR(node.loadDecompressedData());
					NCGR ncgr = NSCR.loadLinkedTileset(node);
					
					if(ncgr == null) throw new IOException("No linked tile data!");
					
					NCLR nclr = NSCR.loadLinkedPalette(node);
					BufferedImage img = nscr.renderImage(nclr.getPalettes(), ncgr.getTileset());
					ImageIO.write(img, "png", new File(targetpath));
					
				}
				
			};
			NTDTools.runExport(gui_parent, action, "I/O Error: Could not read NSCR or export to PNG!");
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
		public static FA_Export getStaticInstance(){
			if(staticme == null) staticme = new FA_Export();
			return staticme;
		}
		
	}

	public static class FA_SetTileset implements FileAction{
		
		private static FA_SetTileset staticme;
		private String str = "Set Tileset";

		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			//Define dialog behavior
			ImageResourceMatcher matcher = new ImageResourceMatcher(){

				private FileNode me = node;
				private NSCR nscr;
				
				private void readMe(){
					try{nscr = NSCR.readNSCR(me.loadDecompressedData());}
					catch(IOException x){
						x.printStackTrace();
						displayIOError();
					}
				}
				
				private void displayIOError(){
					JOptionPane.showMessageDialog(gui_parent, "I/O Error: One or more nodes could not be loaded!", 
							"I/O Error", JOptionPane.ERROR_MESSAGE);
				}
				
				public List<FileNode> getResourceList() {
					if(nscr == null) readMe();
					if(nscr == null) return null;
					
					try{return nscr.searchForTilesets(me);}
					catch(IOException x){
						x.printStackTrace();
						displayIOError();
						return null;
					}
				}

				public void drawSelected(FileNode selected, CheckeredImagePane pane) {

					if(nscr == null) readMe();
					if(nscr == null) return;
					
					try{
						NCGR ncgr = NCGR.readNCGR(selected.loadDecompressedData());
						
						BufferedImage img = nscr.renderImage(ncgr.getTileset());
						pane.addItem(new ImagePaneDrawer(){

							public int getX() {return 0;}
							public int getY() {return 0;}
							public int getWidth() {return img.getWidth();}
							public int getHeight() {return img.getHeight();}
							public void drawMe(Graphics g, int x, int y) {
								g.drawImage(img, x, y, null);
							}
							
						});
					}
					catch(IOException x){
						x.printStackTrace();
						displayIOError();
					}
					
				}

				public void applySelected(FileNode selected) {
					NSCR.linkTilesetNode(me, selected);
				}

				public String getDialogTitle() {
					return "Select Tileset for Nitro Screen Resource";
				}
				
			};

			ImageResourceMatchDialog dialog = new ImageResourceMatchDialog(gui_parent, matcher);
			dialog.setLocationRelativeTo(gui_parent);
			dialog.setVisible(true);
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
		public static FA_SetTileset getStaticInstance(){
			if(staticme == null) staticme = new FA_SetTileset();
			return staticme;
		}

	}
	
	public static class FA_SetPalette implements FileAction{
		
		private static FA_SetPalette staticme;
		private String str = "Set Palette";

		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			//Load the linked NCGR
			NCGR ncgr = null;
			try{ncgr = NSCR.loadLinkedTileset(node);}
			catch(IOException x){
				x.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "I/O Error: Linked tileset could not be loaded!", 
						"I/O Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if(ncgr == null){
				JOptionPane.showMessageDialog(gui_parent, "No linked tileset found! Tileset is required to render image!", 
						"No Tileset", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			//Define dialog behavior
			final NCGR tileset = ncgr;
			ImageResourceMatcher matcher = new ImageResourceMatcher(){

				private FileNode me = node;
				private NSCR nscr;
				
				private void readMe(){
					try{
						nscr = NSCR.readNSCR(me.loadDecompressedData());
					}
					catch(IOException x){
						x.printStackTrace();
						displayIOError();
					}
				}
				
				private void displayIOError(){
					JOptionPane.showMessageDialog(gui_parent, "I/O Error: One or more nodes could not be loaded!", 
							"I/O Error", JOptionPane.ERROR_MESSAGE);
				}
				
				public List<FileNode> getResourceList() {
					if(nscr == null) readMe();
					if(nscr == null) return null;
					
					try{return nscr.searchForPalettes(me, false);}
					catch(IOException x){
						x.printStackTrace();
						displayIOError();
						return null;
					}
				}

				public void drawSelected(FileNode selected, CheckeredImagePane pane) {

					if(nscr == null) readMe();
					if(nscr == null) return;
					
					try{
						NCLR nclr = NCLR.readNCLR(selected.loadDecompressedData());
						
						BufferedImage img = nscr.renderImage(nclr.getPalettes(), tileset.getTileset());
						pane.addItem(new ImagePaneDrawer(){

							public int getX() {return 0;}
							public int getY() {return 0;}
							public int getWidth() {return img.getWidth();}
							public int getHeight() {return img.getHeight();}
							public void drawMe(Graphics g, int x, int y) {
								g.drawImage(img, x, y, null);
							}
							
						});
					}
					catch(IOException x){
						x.printStackTrace();
						displayIOError();
					}
					
				}

				public void applySelected(FileNode selected) {
					NSCR.linkPaletteNode(me, selected);
				}

				public String getDialogTitle() {
					return "Select Palette for Nitro Screen Resource";
				}
				
			};

			ImageResourceMatchDialog dialog = new ImageResourceMatchDialog(gui_parent, matcher);
			dialog.setLocationRelativeTo(gui_parent);
			dialog.setVisible(true);
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
		public static FA_SetPalette getStaticInstance(){
			if(staticme == null) staticme = new FA_SetPalette();
			return staticme;
		}

	}
		
}
