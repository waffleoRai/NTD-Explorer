package waffleoRai_NTDTypes.nx;

import waffleoRai_Containers.nintendo.nx.NXContentMeta;
import waffleoRai_Containers.nintendo.nx.NXNACP;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.WriterPrintable;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDTypes.WriterPanelManager;
import waffleoRai_Utils.FileBuffer;

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
	
}
