package waffleoRai_NTDTypes;

import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_Files.Converter;
import waffleoRai_Files.WriterPrintable;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.panels.preview.WriterPanel;

public abstract class WriterPanelManager extends TypeManager{
	
	protected abstract WriterPrintable toPrintable(FileNode node);
	
	public JPanel generatePreviewPanel(FileNode node, Component gui_parent){
		WriterPrintable printable = toPrintable(node);
		if(printable == null){
			JOptionPane.showMessageDialog(gui_parent, "Node contents could not be loaded!", "Read Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		WriterPanel pnl = new WriterPanel();
		Writer w = pnl.getWriter();
		try {
			printable.printMeTo(w);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, "There was an error writing file info to GUI!", "Render Error", JOptionPane.ERROR_MESSAGE);
		}
		//doc = pnl.getDocument();
		
		return pnl;
	}
	
	public List<FileAction> getFileActions(){
		//View hex, extract, dump text
		List<FileAction> list = new ArrayList<FileAction>(3);
		list.add(FA_ViewHex.getAction());
		list.add(FA_ExtractFile.getAction());
		list.add(new FA_DumpText());
		
		return list;
	}
	
	public Converter getStandardConverter(){
		return null;
	}

	public class FA_DumpText implements FileAction{

		public static final String DEFO_ENG_STR = "Dump Report";
		private String txt = DEFO_ENG_STR;
		
		@Override
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			//Prompt path
			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXTRACTED));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int select = fc.showSaveDialog(gui_parent);
			
			if(select != JFileChooser.APPROVE_OPTION) return;
			String dir = fc.getSelectedFile().getAbsolutePath();
			
			String path = dir + File.separator + node.getFileName() + ".report.txt";
			WriterPrintable printable = toPrintable(node);
			try{
				BufferedWriter w = new BufferedWriter(new FileWriter(path));
				printable.printMeTo(w);
				w.close();
			}
			catch(IOException x){
				x.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "I/O Error: Report could not be written!", "Write Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}

		@Override
		public void setString(String s) {
			txt = s;
		}
		
		public String toString(){return txt;}
		
	}
	
}
