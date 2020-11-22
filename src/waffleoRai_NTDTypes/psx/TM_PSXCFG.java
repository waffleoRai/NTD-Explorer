package waffleoRai_NTDTypes.psx;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileBufferInputStream;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.panels.preview.WriterPanel;
import waffleoRai_fdefs.psx.PSXSysDefs;

public class TM_PSXCFG extends TypeManager{
	
	public FileTypeNode detectFileType(FileNode node) {
		if(node.getFileName().equalsIgnoreCase("system.cnf")) return new FileTypeDefNode(PSXSysDefs.getConfigDef());
		return null;
	}
	
	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

		try{
			WriterPanel pnl = new WriterPanel();
			FileBufferInputStream is = new FileBufferInputStream(node.loadDecompressedData());
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
			Writer w = pnl.getWriter();
			String line = null;
			while((line = reader.readLine()) != null){
				w.write(line + "\n");
			}
			reader.close();
			w.close();
			
			return pnl;
		}
		catch(IOException x){
			x.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "I/O Error: File could not be loaded for preview!", 
					"Preview Failed", JOptionPane.ERROR_MESSAGE);
		}
		
		return null;
	}

	public List<FileAction> getFileActions() {
		//Extract, View Hex
		List<FileAction> list = new ArrayList<FileAction>(2);
		list.add(FA_ExtractFile.getAction());
		list.add(FA_ViewHex.getAction());
		return list;
	}

	public Converter getStandardConverter() {return null;}
	public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

}
