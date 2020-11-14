package waffleoRai_NTDTypes;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_GUITools.CheckeredImagePane;
import waffleoRai_GUITools.ImagePaneDrawer;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExportFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.panels.preview.ScrollingImageViewPanel;

public abstract class ImagePanelManager extends TypeManager{

	protected abstract BufferedImage renderImage(FileNode node) throws IOException;
	
	public JPanel generatePreviewPanel(FileNode node, Component gui_parent){

		try {
			BufferedImage img = renderImage(node);
			if(img == null){
				JOptionPane.showMessageDialog(gui_parent, "Error: Image could not be read!", 
						"Loading Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			
			ScrollingImageViewPanel pnl = new ScrollingImageViewPanel();
			CheckeredImagePane ipane = pnl.getImagePanel();
			ipane.setDrawingAreaSize(new Dimension(img.getWidth()+10, img.getHeight()+10));
			ipane.addItem(new ImagePaneDrawer(){

				public int getX() {return 5;}
				public int getY() {return 5;}
				public int getWidth() {return img.getWidth();}
				public int getHeight() {return img.getHeight();}

				@Override
				public void drawMe(Graphics g, int x, int y) {
					g.drawImage(img, x, y, null);
					//g.drawImage(img, x + getX(), y + getY(), null);
				}
				
			});
			
			//pnl.repaint();
			return pnl;
		} 
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "Error: Image could not be read!", 
					"Loading Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return null;
	}
	
	public List<FileAction> getFileActions(){
		//Extract, View Hex, Export
		List<FileAction> list = new ArrayList<FileAction>(3);
		list.add(FA_ExtractFile.getAction());
		list.add(FA_ViewHex.getAction());
		
		Converter c = getStandardConverter();
		if(c == null) return list;
		list.add(new FA_ExportFile(){

			protected Converter getConverter(FileNode node) {
				return c;
			}
			
		});
		
		return list;
	}
	
}
