package waffleoRai_NTDExCore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import waffleoRai_Containers.ArchiveDef;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.LinkNode;

public class NTDTools {

	public static void extractArchivesToTree(DirectoryNode dir, ProgressListeningDialog listener, List<FileNode> failed)
	{
		listener.setPrimaryString("Scanning directory");
		listener.setSecondaryString("Scanning " + dir.getFullPath());
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode)
			{
				extractArchivesToTree((DirectoryNode)child, listener, failed);
			}
			else if(child instanceof LinkNode){}
			else
			{
				listener.setPrimaryString("Scanning files");
				listener.setSecondaryString("Scanning " + child.getFullPath());
				//Check type...
				FileTypeNode head = child.getTypeChainHead();
				if(head == null)
				{
					//Run detection...
					listener.setPrimaryString("Checking file type");
					head = TypeManager.detectType(child);
					child.setTypeChainHead(head);
				}
				if(head == null) continue; //Still unknown
				
				//Go down to tail
				FileTypeNode tail = head;
				while(tail.getChild() != null) tail = tail.getChild();
				if(tail instanceof ArchiveDef)
				{
					listener.setPrimaryString("Reading archive");
					listener.setSecondaryString("Parsing " + child.getFullPath());
					try
					{
						DirectoryNode arcroot = ((ArchiveDef)tail).getContents(child);
						arcroot.setFileName(child.getFileName());
						
						//Replace in tree
						listener.setPrimaryString("Copying archive tree");
						listener.setSecondaryString("Copying " + child.getFullPath());
						dir.removeChild(child);
						arcroot.setParent(dir);
						
						extractArchivesToTree(arcroot, listener, failed);
					}
					catch(Exception x)
					{
						x.printStackTrace();
						failed.add(child);
					}
				}
			}
		}
		
	}

	public static void doConversionDump(DirectoryNode dir, ProgressListeningDialog listener, List<FileNode> failed, Collection<Converter> included, String path) throws IOException
	{
		if(!FileBuffer.directoryExists(path)) Files.createDirectories(Paths.get(path));
		listener.setPrimaryString("Scanning directory");
		listener.setSecondaryString("Scanning " + dir.getFullPath());
		List<FileNode> children = dir.getChildren();
		
		for(FileNode child : children)
		{
			String mypath = dir + File.separator + child.getFileName();
			if(child instanceof DirectoryNode)
			{
				doConversionDump((DirectoryNode)child, listener, failed, included, mypath);
			}
			else if(child instanceof LinkNode){}
			else
			{
				listener.setPrimaryString("Scanning files");
				listener.setSecondaryString("Scanning " + child.getFullPath());
				//Check type...
				FileTypeNode head = child.getTypeChainHead();
				if(head == null)
				{
					//Run detection...
					listener.setPrimaryString("Detecting file type");
					head = TypeManager.detectType(child);
					child.setTypeChainHead(head);
				}
				
				listener.setPrimaryString("Checking available conversions");
				if(head != null)
				{
					//See if there is an available converter...
					FileTypeNode tail = head;
					while(tail.getChild() != null) tail = tail.getChild();
					TypeManager manager = TypeManager.getTypeManager(tail.getTypeID());
					Converter con = manager.getStandardConverter();
					if(con != null)
					{
						boolean match = false;
						for(Converter inc : included)
						{
							if(con == inc) {match = true; break;}
						}
						
						if(match)
						{
							//Do conversion... and return
							String npath = con.changeExtension(mypath);
							listener.setPrimaryString("Converting");
							listener.setSecondaryString("Generating " + npath);
							try{con.writeAsTargetFormat(child.loadDecompressedData(), npath);}
							catch(Exception x){x.printStackTrace(); failed.add(child);}
							return;
						}
					}
					//Just copy file
					listener.setPrimaryString("Extracting");
					listener.setSecondaryString("Writing " + mypath);
					try{child.loadDecompressedData().writeFile(mypath);}
					catch(Exception x){x.printStackTrace(); failed.add(child);}
				}
			}
		}
	}

}
