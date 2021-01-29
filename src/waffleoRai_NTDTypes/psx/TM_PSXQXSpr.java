package waffleoRai_NTDTypes.psx;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_GUITools.CheckeredImagePane;
import waffleoRai_GUITools.RasterImageDrawer;
import waffleoRai_Image.psx.QXImage;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExportFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.panels.preview.ScrollingImageViewPanel;
import waffleoRai_Utils.FileBuffer;

public class TM_PSXQXSpr extends TypeManager{
	
	//Loaders
	
	public static class PSXQXDefLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new TM_PSXQXSpr();}
		public FileTypeDefinition getDefinition() {return QXImage.getDefinition();}	
	}
	
	//Manager
	
	public FileTypeNode detectFileType(FileNode node) {
		//No autodetect...
		return null;
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		if(node == null) return null;
		
		try {
			FileBuffer dat = node.loadDecompressedData();
			boolean tiled = false;
			String meta = node.getMetadataValue(QXImage.FNMETAKEY_TILED);
			if(meta != null && meta.equalsIgnoreCase("true")) tiled = true;
			if(meta == null) node.setMetadataValue(QXImage.FNMETAKEY_TILED, "false");
			
			QXImage qx = QXImage.readImageData(dat, tiled);
			int fcount = qx.getFrameCount();
			int maxw = 0;
			int maxh = 0;
			List<BufferedImage> imgs = new ArrayList<BufferedImage>(fcount);
			for(int i = 0; i < fcount; i++){
				BufferedImage img = qx.getFrame(i, false);
				if(img.getWidth() > maxw) maxw = img.getWidth();
				if(img.getHeight() > maxh) maxh = img.getHeight();
				imgs.add(img);
				//System.err.println("Current Image Size: " + img.getWidth() + " x " + img.getHeight());
				//System.err.println("Current Max Size: " + maxw + " x " + maxh);
			}
			
			ScrollingImageViewPanel vpnl = new ScrollingImageViewPanel();
			CheckeredImagePane pane = vpnl.getImagePanel();
			int cols = Math.min(fcount, 4);
			int rows = fcount/cols;
			if(fcount % cols != 0) rows++;
			int spacing = 3;
			
			int dwidth = ((maxw+spacing) * cols) + spacing;
			int dheight = ((maxh+spacing) * rows) + spacing;
			//System.err.println("Drawing Dimensions: " + maxw + " x " + maxh);
			pane.setDrawingAreaSize(new Dimension(dwidth, dheight));
			int x = 0; int y = 0; int l = 0;
			for(BufferedImage img : imgs){
				RasterImageDrawer obj = new RasterImageDrawer(img);
				obj.setPosition(x, y);
				pane.addItem(obj);
				
				x += maxw + spacing; l++;
				if(l >= cols){
					y += maxh + spacing;
					l = 0; x = 0;
				}
			}
			
			return vpnl;
		} 
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"File data could not be loaded!", 
					"I/O Error", JOptionPane.WARNING_MESSAGE);
			return null;
		}

	}

	public List<FileAction> getFileActions() {
		List<FileAction> list = new ArrayList<FileAction>(3);
		list.add(FA_ExtractFile.getAction());
		list.add(FA_ViewHex.getAction());
		list.add(new FA_ExportFile(){

			protected Converter getConverter(FileNode node) {
				return getStandardConverter();
			}
			
		});
		return list;
	}

	public Converter getStandardConverter() {return QXImage.getConverter();}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}


}
