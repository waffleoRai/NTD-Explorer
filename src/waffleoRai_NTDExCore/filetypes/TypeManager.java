package waffleoRai_NTDExCore.filetypes;

import java.awt.Component;
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
import waffleoRai_Utils.FileNode;

public abstract class TypeManager {
	
	private static TypeManager BIN_DETECTOR;
	private static Map<String, TypeManager> ext_map;
	
	private static Map<Integer, TypeManager> id_map;
	
	public static void buildDetectorMap()
	{
		//Add known types.
		//Manually in source code.
		//Because I'm lazy
		
		BIN_DETECTOR = new TM_BIN();
		
		ext_map = new ConcurrentHashMap<String, TypeManager>();
		ext_map.put("bin", BIN_DETECTOR); //Defo binary - when don't know wtf to do with it
		
		id_map = new ConcurrentHashMap<Integer, TypeManager>();
		
		//Archives
		TypeManager tm = new TM_NARC();
		ext_map.put("narc", tm); //DS Archive
		ext_map.put("carc", tm); //Compressed NARC (MKDS)
		id_map.put(NARC.TYPE_ID, tm);
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
			d = ext_map.get(ext);
			if(d == null) d = BIN_DETECTOR;
		}
		
		return d.detectFileType(node);
	}
	
	public abstract FileTypeNode detectFileType(FileNode node);
	public abstract JPanel generatePreviewPanel(FileNode node, Component gui_parent);
	public abstract List<FileAction> getFileActions();
	public abstract Converter getStandardConverter();

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
}
