package waffleoRai_NTDTypes.nx;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import waffleoRai_Containers.nintendo.nx.NXContentMeta;
import waffleoRai_Containers.nintendo.nx.NXNACP;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.WriterPrintable;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDTypes.WriterPanelManager;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_fdefs.nintendo.NXSysDefs;

public class NXSysTypes {

	public static class NXCNMTManager extends WriterPanelManager{

		@Override
		protected WriterPrintable toPrintable(FileNode node) {

			try{
				FileBuffer dat = node.loadDecompressedData();
				NXContentMeta cnmt = NXContentMeta.readCMNT(dat, 0);
				return cnmt;
			}
			catch(Exception x){
				x.printStackTrace();
			}
			
			return null;
		}

		@Override
		public FileTypeNode detectFileType(FileNode node) {
			if(node == null) return null;
			//There isn't a clean magic number.
			//So I guess go based on extension?
			if(node.getFileName().endsWith(".cnmt")){
				//I don't like it, but it should hold for now.
				return new FileTypeDefNode(NXContentMeta.getDefinition());
			}
			
			return null;
		}

		@Override
		public boolean isOfType(FileNode node) {
			return (detectFileType(node) != null);
		}
		
	}
	
	public static class NXNACPManager extends WriterPanelManager{

		@Override
		protected WriterPrintable toPrintable(FileNode node) {

			try{
				FileBuffer dat = node.loadDecompressedData();
				NXNACP nacp = NXNACP.readNACP(dat, 0);
				return nacp;
			}
			catch(Exception x){
				x.printStackTrace();
			}
			
			return null;
		}

		@Override
		public FileTypeNode detectFileType(FileNode node) {
			if(node == null) return null;
			if(node.getFileName().endsWith(".nacp")){
				//I don't like it, but it should hold for now.
				return new FileTypeDefNode(NXNACP.getDefinition());
			}
			
			return null;
		}

		@Override
		public boolean isOfType(FileNode node) {
			return (detectFileType(node) != null);
		}
		
	}
	
	public static class NXNSOManager extends TypeManager{
		public FileTypeNode detectFileType(FileNode node) {
			if(node.getLength() == 0) return null;
			
			FileBuffer dat = null;
			long ed = 0x10;
			if(node.getLength() < ed) ed = node.getLength();
			try {
				if(node.hasCompression()) dat = node.loadDecompressedData();
				else dat = node.loadData(0, ed);
				
				//Check for magic #
				long mcheck = dat.findString(0, ed, "NSO0");
				if(mcheck >= 0) return new FileTypeDefNode(NXSysDefs.getNSODef());
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}

		public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
			return TypeManager.getDefaultManager().generatePreviewPanel(node, gui_parent);
		}

		public List<FileAction> getFileActions() {
			List<FileAction> list = new ArrayList<FileAction>(2);
			list.add(FA_ExtractFile.getAction());
			//list.add(FA_ViewHex.getAction());
			return list;
		}

		public Converter getStandardConverter() {return null;}
		public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

	}
	
	public static class NXNROManager extends TypeManager{
		public FileTypeNode detectFileType(FileNode node) {
			if(node.getLength() == 0) return null;
			
			FileBuffer dat = null;
			long ed = 0x30;
			if(node.getLength() < ed) ed = node.getLength();
			try {
				if(node.hasCompression()) dat = node.loadDecompressedData();
				else dat = node.loadData(0, ed);
				
				//Check for magic #
				long mcheck = dat.findString(0, ed, "NRO0");
				if(mcheck >= 0) return new FileTypeDefNode(NXSysDefs.getNRODef());
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}

		public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
			return TypeManager.getDefaultManager().generatePreviewPanel(node, gui_parent);
		}

		public List<FileAction> getFileActions() {
			List<FileAction> list = new ArrayList<FileAction>(2);
			list.add(FA_ExtractFile.getAction());
			//list.add(FA_ViewHex.getAction());
			return list;
		}

		public Converter getStandardConverter() {return null;}
		public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

	}
	
	public static class NXNRRManager extends TypeManager{
		public FileTypeNode detectFileType(FileNode node) {
			if(node.getLength() == 0) return null;
			
			FileBuffer dat = null;
			long ed = 0x20;
			if(node.getLength() < ed) ed = node.getLength();
			try {
				if(node.hasCompression()) dat = node.loadDecompressedData();
				else dat = node.loadData(0, ed);
				
				//Check for magic #
				long mcheck = dat.findString(0, ed, "NRR0");
				if(mcheck >= 0) return new FileTypeDefNode(NXSysDefs.getNRRDef());
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}

		public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
			return TypeManager.getDefaultManager().generatePreviewPanel(node, gui_parent);
		}

		public List<FileAction> getFileActions() {
			List<FileAction> list = new ArrayList<FileAction>(2);
			list.add(FA_ExtractFile.getAction());
			//list.add(FA_ViewHex.getAction());
			return list;
		}

		public Converter getStandardConverter() {return null;}
		public boolean isOfType(FileNode node) {return (detectFileType(node) != null);}

	}
	
}
