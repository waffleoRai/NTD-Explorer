package waffleoRai_NTDTypes;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_GUITools.AnimatedCheckeredImagePane;
import waffleoRai_GUITools.AnimatedImagePaneDrawer;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExportFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.panels.preview.AnimatedImageViewPanel;

public abstract class AnimatedImagePanelManager extends TypeManager{
	
	protected abstract Collection<AnimatedImagePaneDrawer> getFrames(FileNode node) throws IOException;
	
	public JPanel generatePreviewPanel(FileNode node, Component gui_parent){
	
		try{
			Collection<AnimatedImagePaneDrawer> frames = getFrames(node);
			if(frames == null){
				JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be read!", 
						"Loading Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			
			AnimatedImageViewPanel pnl = new AnimatedImageViewPanel();
			AnimatedCheckeredImagePane ipane = pnl.getViewPanel();
			
			int len = 0;
			for(AnimatedImagePaneDrawer f : frames){
				ipane.addFrame(f);
				len += f.getLengthInMillis();
			}
			
			//Set loop
			ipane.setLoopPoint(len);
			
			pnl.startAnimation();
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
