package waffleoRai_NTDExCore;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.definitions.CompDefNode;
import waffleoRai_Compression.definitions.CompressionInfoNode;
import waffleoRai_Containers.ArchiveDef;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.LinkNode;

public class NTDTools {
	
	public static List<CompressionInfoNode> getCompressionChain(FileNode archive)
	{
		List<CompressionInfoNode> list = new LinkedList<CompressionInfoNode>();
		FileTypeNode t = archive.getTypeChainHead();
		
		long off = archive.getOffset();
		long len = archive.getLength();
		while(t != null)
		{
			if(t.isCompression())
			{
				AbstractCompDef def = ((CompDefNode)t).getDefinition();
				list.add(new CompressionInfoNode(def, off, len));
				t = t.getChild();
				off = 0;
				len = -1;
			}
			t = t.getChild();
		}
		
		//if(list.isEmpty()) System.err.println("No compression on archive!");
		return list;
	}
	
	public static void notateDir(DirectoryNode dir, List<CompressionInfoNode> chain, FileNode arcnode)
	{
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode)
			{
				notateDir(((DirectoryNode)child), chain, arcnode);
			}
			else
			{
				//System.err.println("Notating node " + child.getFullPath());
				child.setSourcePath(arcnode.getSourcePath());
				if(chain.isEmpty()){
					if(child.sourceDataCompressed()){
						List<CompressionInfoNode> cnodes = child.getCompressionChain();
						for(CompressionInfoNode c : cnodes){
							c.setStartOffset(c.getStartOffset() + arcnode.getOffset());
						}
					}
					else{
						child.setOffset(child.getOffset() + arcnode.getOffset());	
					}
				}
				else{
					//System.err.println("Compression chain found:");
					for(CompressionInfoNode c : chain){
						//System.err.println("Compression c: 0x" + Long.toHexString(c.getStartOffset()));
						child.addCompressionChainNode(c.getDefinition(), c.getStartOffset(), c.getLength());
					}	
				}
			}
		}
		
	}
	
	public static void notateTree(DirectoryNode root, FileNode archive)
	{
		/*Basically, this function notes the source archive file's
		 * compression routines in the nodes of its contents.
		 * 
		 * That way, when the project tree is saved with these noted, these
		 * internal files can be loaded without later without having to re-parse
		 * the source archive. (Though it does have to be decompressed).
		 */
		
		notateDir(root, getCompressionChain(archive), archive);
		
		//Get type chain tail...
		FileTypeNode tp = archive.getTypeChainHead();
		if(tp != null){
			while(tp.getChild() != null) tp = tp.getChild();
			root.setFileClass(tp.getFileClass());
		}
	}
	
	public static void extractArchivesToTree(DirectoryNode dir, ProgressListeningDialog listener, List<FileNode> failed)
	{
		listener.setPrimaryString("Scanning directory");
		listener.setSecondaryString("Scanning " + dir.getFullPath());
		//System.err.println("Scanning " + dir.getFullPath());
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
				//System.err.println("Scanning " + child.getFullPath());
				//Check type...
				FileTypeNode head = child.getTypeChainHead();
				if(head == null)
				{
					//Run detection...
					listener.setPrimaryString("Checking file type");
					head = TypeManager.detectType(child);
					child.setTypeChainHead(head);
					//if(head != null) System.err.println("Type head: " + head.toString());
				}
				if(head == null) continue; //Still unknown
				
				//Go down to tail
				FileTypeNode tail = head;
				while(tail.getChild() != null) tail = tail.getChild();
				//if(tail != null) System.err.println("Type tail: " + tail.toString());
				if(tail != null && (tail.getFileClass() == FileClass.ARCHIVE))
				{
					//System.err.println("Tail is an archive!");
					listener.setPrimaryString("Reading archive");
					listener.setSecondaryString("Parsing " + child.getFullPath());
					try
					{
						//Need to nab the definition
						ArchiveDef def = (ArchiveDef)(tail.getTypeDefinition());
						DirectoryNode arcroot = def.getContents(child);
						
						listener.setPrimaryString("Copying archive tree");
						listener.setSecondaryString("Copying " + child.getFullPath());
						
						//Clean up extracted tree
						NTDTools.notateTree(arcroot, child);
						arcroot.setFileName(child.getFileName());
						doTypeScan(arcroot, listener);
						//arcroot.setFileClass(tail.getFileClass());
						
						listener.setPrimaryString("Copying archive tree");
						listener.setSecondaryString("Copying " + child.getFullPath());
						
						//Add to tree
						dir.removeChild(child);
						arcroot.setParent(dir);
						
						//In case this archive contains more archives
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

	public static void doTypeScan(DirectoryNode dir, ProgressListeningDialog listener){
		if(listener != null) {
			listener.setPrimaryString("Scanning directory");
			listener.setSecondaryString("Scanning " + dir.getFullPath());
		}
		
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode){
				doTypeScan((DirectoryNode)child, listener);
			}
			else{
				if(listener != null) {
					listener.setPrimaryString("Scanning files");
					listener.setSecondaryString("Scanning " + child.getFullPath());	
				}
				if(child.getTypeChainHead() == null){
					//No type set, so scan
					FileTypeNode ftn = TypeManager.detectType(child);
					if(ftn != null) child.setTypeChainHead(ftn);
				}
			}
		}
	}
	
	public static void clearTypeMarkers(DirectoryNode dir, ProgressListeningDialog listener){
		
		listener.setPrimaryString("Scanning directory");
		listener.setSecondaryString("Scanning " + dir.getFullPath());
		
		List<FileNode> children = dir.getChildren();
		for(FileNode child : children)
		{
			if(child instanceof DirectoryNode){
				clearTypeMarkers((DirectoryNode)child, listener);
			}
			else{
				listener.setPrimaryString("Scanning files");
				listener.setSecondaryString("Scanning " + child.getFullPath());
				child.clearTypeChain();
			}
		}
		
	}
	
	private static void scanForType(DirectoryNode dir, FileClass type, List<FileNode> list){

		List<FileNode> children = dir.getChildren();
		for(FileNode child : children){
			if(child instanceof DirectoryNode){
				scanForType((DirectoryNode)child, type, list);
			}
			else{
				if(child.getTypeChainTail().getFileClass() == type) list.add(child);
			}
		}
		
	}
	
	public static List<FileNode> scanForType(FileNode node, FileClass type){
		//Backtrack to root.
		DirectoryNode dn = node.getParent();
		if(dn == null && (node instanceof DirectoryNode)){
			dn = (DirectoryNode)node;
		}
		else{
			while(dn.getParent() != null) dn = dn.getParent();
		}
		
		List<FileNode> list = new LinkedList<FileNode>();
		scanForType(dn, type, list);
		
		return list;
	}
	
	public static void runExport(Frame gui_parent, ExportAction exp, String failmsg){
		
		JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int select = fc.showSaveDialog(gui_parent);
		
		if(select != JFileChooser.APPROVE_OPTION) return;
		String dir = fc.getSelectedFile().getAbsolutePath();
		NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED, dir);
		
		IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "Data Export");
		dialog.setPrimaryString("Exporting Data");
		dialog.setSecondaryString("Writing to " + dir);
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
		{

			protected Void doInBackground() throws Exception 
			{
				try{
					exp.doExport(dir, dialog);
				}
				catch(Exception x)
				{
					x.printStackTrace();
					JOptionPane.showMessageDialog(gui_parent, failmsg, 
							"Export Failed", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			public void done()
			{
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
		
		
	}
	
}
