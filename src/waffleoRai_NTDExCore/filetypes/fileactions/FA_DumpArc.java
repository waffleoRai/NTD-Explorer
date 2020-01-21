package waffleoRai_NTDExCore.filetypes.fileactions;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import waffleoRai_Containers.ArchiveDef;
import waffleoRai_Containers.nintendo.NARC;
import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.LinkNode;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class FA_DumpArc {
	
	private static final String DEFO_ENG_STRING = "Dump archive contents to disk";
	
	private static FADumpArc_Narc static_narc;
	
	public static FADumpArc_Narc getNARCAction()
	{
		if(static_narc == null) static_narc = new FADumpArc_Narc();
		return static_narc;
	}
	
	private static boolean dump(DirectoryNode dir, String path, boolean recursive) throws IOException, UnsupportedFileTypeException
	{
		if(!FileBuffer.directoryExists(path)) Files.createDirectories(Paths.get(path));
		
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode)
			{
				String subpath = path + File.separator + child.getFileName();
				return dump((DirectoryNode)child, subpath, recursive);
			}
			else if (child instanceof LinkNode); //Skip
			else
			{
				if(recursive)
				{
					//See if it's an archive
					FileTypeNode type = TypeManager.detectType(child);
					if(type != null)
					{
						while(type.getChild() != null) type = type.getChild();
						if(type.getFileClass() == FileClass.ARCHIVE)
						{
							if(type instanceof ArchiveDef)
							{
								DirectoryNode inner = ((ArchiveDef)type).getContents(child);
								String subpath = path + File.separator + child.getFileName();
								return dump(inner, subpath, recursive);
							}
						}
					}
				}
				//Just dump the file...
				FileBuffer file = child.loadData();
				file.writeFile(path + File.separator + child.getFileName());
			}
		}
		
		return true;
	}
	
	public static class FADumpArc_Narc implements FileAction
	{
		private String str;
		
		public FADumpArc_Narc()
		{
			str = DEFO_ENG_STRING;
		}
		
		@Override
		public void doAction(FileNode node, NTDProject project, Component gui_parent) 
		{
			int rop = JOptionPane.showOptionDialog(gui_parent, "Would you like to attempt detection"
					+ ", parsing and dumping of internal archives?\n"
					+ "(ie. Would you like to do a recursive dump?)", 
					"Archive Dump", JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE, null, null, 0);
			
			boolean recursive = false;
			switch(rop)
			{
			case JOptionPane.YES_OPTION: recursive = true; break;
			case JOptionPane.NO_OPTION: recursive = false; break;
			case JOptionPane.CANCEL_OPTION: return;
			}
			
			//Path
			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_ARCDUMP));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int sel = fc.showOpenDialog(gui_parent);
			if(sel != JFileChooser.APPROVE_OPTION) return;
			File file = fc.getSelectedFile();
			String target = file.getAbsolutePath();
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_ARCDUMP, target);
			
			try
			{
				FileBuffer buffer = NTDProgramFiles.openAndDecompress(node);
				NARC arc = NARC.readNARC(buffer, 0);
				
				DirectoryNode root = arc.getArchiveTree();
				target += File.separator + node.getFileName();
				dump(root, target, recursive);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "ERROR: File could not be read!", "I/O Error", JOptionPane.ERROR_MESSAGE);
			} 
			catch (UnsupportedFileTypeException e) 
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "ERROR: File could not be read as NARC!\n"
						+ "Parser Message: " + e.getErrorMessage(), "Parser Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}

		@Override
		public void setString(String s) {str = s;}
		
		public String toString(){return str;}
	}

}
