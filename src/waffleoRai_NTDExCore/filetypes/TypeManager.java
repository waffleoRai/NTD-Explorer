package waffleoRai_NTDExCore.filetypes;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;

import waffleoRai_Containers.nintendo.NARC;
import waffleoRai_Containers.nintendo.sar.DSSoundArchive;
import waffleoRai_Executable.nintendo.DSExeDefs;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Image.nintendo.nitro.NCGR;
import waffleoRai_Image.nintendo.nitro.NCLR;
import waffleoRai_Image.nintendo.nitro.NSCR;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.filetypes.archive.*;
import waffleoRai_NTDExCore.filetypes.bincode.*;
import waffleoRai_NTDExCore.filetypes.img.TM_NitroCGR;
import waffleoRai_NTDExCore.filetypes.img.TM_NitroCLR;
import waffleoRai_NTDExCore.filetypes.img.TM_NitroSCR;
import waffleoRai_NTDExCore.filetypes.sound.*;
import waffleoRai_NTDExGUI.dialogs.progress.ProgressListeningDialog;
import waffleoRai_SeqSound.misc.SMD;
import waffleoRai_SeqSound.ninseq.DSMultiSeq;
import waffleoRai_SeqSound.ninseq.DSSeq;
import waffleoRai_Sound.nintendo.DSStream;
import waffleoRai_Sound.nintendo.DSWarc;
import waffleoRai_Sound.nintendo.DSWave;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.nintendo.DSBank;
import waffleoRai_soundbank.procyon.SWD;
import waffleoRai_Utils.FileNode;

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
		
		//Bincode
		elist.add("arm9"); elist.add("ARM9");
		registerTypeManager(DSExeDefs.DSARM9ExeDef.getTypeIDStatic(), elist, new TM_DSARM9());
		elist.clear();
		elist.add("arm7"); elist.add("ARM7");
		registerTypeManager(DSExeDefs.DSARM7ExeDef.getTypeIDStatic(), elist, new TM_DSARM7());
		elist.clear();
		elist.add("arm9i"); elist.add("ARM9i");
		registerTypeManager(DSExeDefs.DSARM9iExeDef.getTypeIDStatic(), elist, new TM_DSARM9i());
		elist.clear();
		elist.add("arm7i"); elist.add("ARM7i");
		registerTypeManager(DSExeDefs.DSARM7iExeDef.getTypeIDStatic(), elist, new TM_DSARM7i());
		elist.clear();
		
		//DS Sound
		elist.add("sdat"); elist.add("bnsar"); elist.add("nsar");
		registerTypeManager(DSSoundArchive.getTypeDef().getTypeID(), elist, new TM_SDAT());
		elist.clear();
		elist.add("swav"); elist.add("bnwav"); elist.add("nwav");
		registerTypeManager(DSWave.TYPE_ID, elist, new TM_NitroWAV());
		elist.clear();
		elist.add("swar"); elist.add("bnwar"); elist.add("nwar");
		registerTypeManager(DSWarc.TYPE_ID, elist, new TM_NitroWAR());
		elist.clear();
		elist.add("strm"); elist.add("bnstm"); elist.add("nstm");
		registerTypeManager(DSStream.TYPE_ID, elist, new TM_NitroSTM());
		elist.clear();
		elist.add("sbnk"); elist.add("bnbnk"); elist.add("nbnk");
		registerTypeManager(DSBank.TYPE_ID, elist, new TM_NitroBNK());
		elist.clear();
		elist.add("sseq"); elist.add("bnseq"); elist.add("nseq");
		registerTypeManager(DSSeq.TYPE_ID, elist, new TM_NitroSEQ());
		elist.clear();
		elist.add("ssar"); elist.add("bnseq"); elist.add("nseq");
		registerTypeManager(DSMultiSeq.TYPE_ID, elist, new TM_NitroSAR());
		elist.clear();
		elist.add("smd"); elist.add("smdl");
		registerTypeManager(SMD.TYPE_ID, elist, new TM_ProcyonSMD());
		elist.clear();
		elist.add("swd"); elist.add("swdl");
		registerTypeManager(SWD.TYPE_ID, elist, new TM_ProcyonSWD());
		elist.clear();
		
		//DS Image
		elist.add("nclr");
		registerTypeManager(NCLR.TYPE_ID, elist, new TM_NitroCLR());
		elist.clear();
		elist.add("ncgr");
		registerTypeManager(NCGR.TYPE_ID, elist, new TM_NitroCGR());
		elist.clear();
		elist.add("nscr");
		registerTypeManager(NSCR.TYPE_ID, elist, new TM_NitroSCR());
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
	
	public static Collection<TypeManager> getAllAvailableTypeManagers(){
		if(id_map == null) buildDetectorMap();
		List<TypeManager> list = new ArrayList<TypeManager>(id_map.size()+1);
		list.addAll(id_map.values());
		return list;
	}
	
	public static FileTypeNode detectType(FileNode node)
	{
		if(ext_map == null) buildDetectorMap();
		
		//Get initial extension...
		String fname = node.getFileName();
		int lastdot = fname.lastIndexOf('.');
		TypeManager d = null;
		if(lastdot >= 0)
		{
			String ext = fname.substring(lastdot + 1).trim().toLowerCase();
			if(!ext.equals("bin")){
				//System.err.println("ext = " + ext);
				List<TypeManager> list = ext_map.get(ext);
				if(list != null){
					for(TypeManager tm : list){
						if(tm.isOfType(node))
						{
							d = tm;
							break;
						}
					}	
				}
			}
		}
		
		if(d == null){
			//Try scanning internally...
			Collection<TypeManager> alltypes = getAllAvailableTypeManagers();
			for(TypeManager tm : alltypes){
				if(tm instanceof TM_BIN) continue; //It's always true, we don't need it getting stuck there...
				if(tm.isOfType(node))
				{
					d = tm;
					break;
				}
			}
		}
		if(d == null) d = BIN_DETECTOR;
		
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
		TypeManager tm = id_map.get(typeid);
		if(tm == null) return getDefaultManager();
		return tm;
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
				//FileBuffer nodeload = node.loadDecompressedData();
				con.writeAsTargetFormat(node, outpath);
			}	
		}		
		
		return true;
	}
	
}
