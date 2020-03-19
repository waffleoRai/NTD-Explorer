package waffleoRai_NTDExCore.filetypes;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;

import waffleoRai_Containers.nintendo.NARC;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.filetypes.archive.TM_NARC;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileNode;


//TODO map multiple types to a string
public abstract class TypeManager {
	
	private static TypeManager BIN_DETECTOR;
	private static Map<String, List<TypeManager>> ext_map;
	
	private static Map<Integer, TypeManager> id_map;
	
	public static void buildDetectorMap()
	{
		//Add known types.
		//Manually in source code.
		//Because I'm lazy
		
		BIN_DETECTOR = new TM_BIN();
		
		ext_map = new ConcurrentHashMap<String, List<TypeManager>>();
		id_map = new ConcurrentHashMap<Integer, TypeManager>();
		
		registerTypeManager(0, "bin", BIN_DETECTOR); //Defo binary - when don't know wtf to do with it
		
		List<String> elist = new LinkedList<String>();
		
		//Archives
		TypeManager tm = new TM_NARC();
		elist.add("narc"); //DS Archive
		elist.add("carc"); //Compressed NARC (MKDS)
		registerTypeManager(NARC.TYPE_ID, elist, tm);
		elist.clear();
		
		
	}
	
	public static boolean registerTypeManager(int id, String ext, TypeManager manager)
	{
		if(ext_map == null || id_map == null) buildDetectorMap();
		id_map.put(id, manager);
		
		List<TypeManager> list = ext_map.get(ext);
		if(list == null)
		{
			list = new LinkedList<TypeManager>();
			ext_map.put(ext, list);
		}
		list.add(manager);
		
		return true;
	}
	
	public static boolean registerTypeManager(int id, Collection<String> extlist, TypeManager manager)
	{
		if(ext_map == null || id_map == null) buildDetectorMap();
		id_map.put(id, manager);
		
		for(String ext : extlist)
		{
			List<TypeManager> list = ext_map.get(ext);
			if(list == null)
			{
				list = new LinkedList<TypeManager>();
				ext_map.put(ext, list);
			}
			list.add(manager);	
		}
		
		return true;
	}
	
	public static FileTypeNode detectType(FileNode node)
	{
		if(ext_map == null) buildDetectorMap();
		
		//Get initial extension...
		String fname = node.getFileName();
		int lastdot = fname.lastIndexOf('.');
		TypeManager d = BIN_DETECTOR;
		if(lastdot >= 0)
		{
			String ext = fname.substring(lastdot + 1).trim().toLowerCase();
			List<TypeManager> list = ext_map.get(ext);
			for(TypeManager tm : list)
			{
				if(tm.isOfType(node))
				{
					d = tm;
					break;
				}
			}
			if(d == null) d = BIN_DETECTOR;
		}
		
		return d.detectFileType(node);
	}
	
	public abstract FileTypeNode detectFileType(FileNode node);
	public abstract JPanel generatePreviewPanel(FileNode node, Component gui_parent);
	public abstract List<FileAction> getFileActions();
	public abstract Converter getStandardConverter();
	public abstract boolean isOfType(FileNode node);

 	public static TypeManager getTypeManager(int typeid)
	{
		if(id_map == null) buildDetectorMap();
		return id_map.get(typeid);
	}
	
	public static TypeManager getDefaultManager()
	{
		if(BIN_DETECTOR == null) buildDetectorMap();
		return BIN_DETECTOR;
	}

	public static List<Converter> getAvailableConversions()
	{
		List<Converter> list = new LinkedList<Converter>();
		
		for(TypeManager manager : id_map.values())
		{
			Converter c = manager.getStandardConverter();
			if(c != null) list.add(c);
		}

		return list;
	}

	public static boolean exportNode(FileNode node, String dirpath, ProgressListeningDialog dialog) throws IOException, UnsupportedFileTypeException
	{
		if(node == null) return false;
		if(dirpath == null || dirpath.isEmpty()) return false;
		
		if(node instanceof DirectoryNode)
		{
			//Recursive
			boolean good = true;
			String outpath = dirpath + File.separator + node.getFileName();
			if(dialog!=null)dialog.setSecondaryString("Extracting directory " + outpath);
			if(!FileBuffer.directoryExists(outpath)) Files.createDirectories(Paths.get(outpath));
			
			List<FileNode> children = ((DirectoryNode)node).getChildren();
			for(FileNode child : children)
			{
				good = good && exportNode(child, outpath, dialog);
			}
			return good;
		}
		else
		{
			FileTypeNode ntype = node.getTypeChainHead();
			TypeManager manager = null;
			if(ntype != null) manager = id_map.get(ntype.getTypeID());
			Converter con = null;
			if(manager != null) con = manager.getStandardConverter();
			
			if(con == null)
			{
				//Just extract
				String outpath = dirpath + File.separator + node.getFileName();
				if(dialog!=null)dialog.setSecondaryString("Extracting to " + outpath);
				node.copyDataTo(outpath, true);
			}
			else
			{
				//Convert
				String outpath = dirpath + File.separator + node.getFileName();
				outpath = con.changeExtension(outpath);
				if(dialog!=null)dialog.setSecondaryString("Extracting to " + outpath);
				FileBuffer nodeload = node.loadDecompressedData();
				con.writeAsTargetFormat(nodeload, outpath);
			}	
		}		
		
		return true;
	}
	
}
