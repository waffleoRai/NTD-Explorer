package waffleoRai_NTDExCore.filetypes.sound;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.definitions.CompDefNode;
import waffleoRai_Compression.nintendo.DSCompHeader;
import waffleoRai_Compression.nintendo.DSRLE;
import waffleoRai_Compression.nintendo.NinHuff;
import waffleoRai_Compression.nintendo.NinLZ;
import waffleoRai_Containers.nintendo.sar.DSSoundArchive;
import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_NTDExGUI.panels.preview.NinSeqPreviewPanel;
import waffleoRai_SeqSound.ninseq.DSSeq;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileBufferStreamer;
import waffleoRai_Utils.FileNode;
import waffleoRai_Utils.StreamWrapper;
import waffleoRai_soundbank.nintendo.DSBank;

public class TM_NitroSEQ extends TypeManager{
	
	private boolean checkDecompMagic(StreamWrapper in)
	{
		if(in == null) return false;
		byte[] magic_bytes = DSSeq.MAGIC.getBytes();
		for(int i = 0; i < magic_bytes.length; i++)
		{
			if(in.get() != magic_bytes[i]) return false;
		}
		return true;
	}
	
	public FileTypeNode detectFileType(FileNode node) {
		
		String path = node.getSourcePath();
		long offset = node.getOffset();
		
		try 
		{
			FileBuffer buffer = new FileBuffer(path, offset, offset+32, true);
			long magicoff = buffer.findString(0, 0x10, DSSeq.MAGIC);
			if(magicoff == 0)
			{
				//Make sure size is the same as the header says...
				int bom = Short.toUnsignedInt(buffer.shortFromFile(4));
				if(bom == 0xFEFF) buffer.setEndian(true); //Big endian
				else buffer.setEndian(false);
				int expsize = buffer.intFromFile(8);
				if(node.getLength() != expsize) return null;
				return new FileTypeDefNode(DSSeq.getDefinition());
			}
			else
			{
				//Look for DS compression header...

				DSCompHeader chead = DSCompHeader.read(buffer, 0);
				CompDefNode cnode = null;
				switch(chead.getType())
				{
				case DSCompHeader.TYPE_LZ77: 
					AbstractCompDef def = NinLZ.getDefinition();
					cnode = new CompDefNode(def);
					String buffpath = def.decompressToDiskBuffer(new FileBufferStreamer(node.loadData()));
					StreamWrapper decomp = new FileBufferStreamer(new FileBuffer(buffpath, 0, 0x10));
					if(checkDecompMagic(decomp)) cnode.setChild(new FileTypeDefNode(DSSeq.getDefinition()));
					return cnode;
				case DSCompHeader.TYPE_HUFFMAN: return new CompDefNode(NinHuff.getDefinition());
				case DSCompHeader.TYPE_RLE: 
					AbstractCompDef rle = DSRLE.getDefinition();
					cnode = new CompDefNode(rle);
					String buffpath_rle = rle.decompressToDiskBuffer(new FileBufferStreamer(node.loadData()));
					StreamWrapper decomp_rle = new FileBufferStreamer(new FileBuffer(buffpath_rle, 0, 0x10));
					if(checkDecompMagic(decomp_rle)) cnode.setChild(new FileTypeDefNode(DSSeq.getDefinition()));
					return cnode;
				default:
					return null;
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
	}

	public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
	
		//Load seq
		DSSeq seq = null;
		try{
			seq = DSSeq.readSSEQ(node.loadDecompressedData());	
		}
		catch(IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"I/O Error: Sequence file could not be opened!", 
					"SSEQ Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui_parent, 
					"Parser Error: Sequence file could not be read!", 
					"SSEQ Preview Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		//Get bank
		String bnkpath = node.getMetadataValue(DSSoundArchive.FNMETAKEY_BANKLINK);
		if(bnkpath == null){
			bnkpath = DSSoundArchive.findLinkedBank(node);
		}
		
		FileNode bnknode = node.getParent().getNodeAt(bnkpath);
		if(bnknode == null){
			JOptionPane.showMessageDialog(gui_parent, 
					"Bank linked to this sequence node could not be automatically loaded.", 
					"Bank Not Found", JOptionPane.WARNING_MESSAGE);
		}
		
		//Get names
		String seqname = node.getMetadataValue(NTDProgramFiles.FN_METAKEY_TITLE);
		String bnkname = null;
		SynthBank playbank = null;
		if(bnknode != null){
			bnkname = bnknode.getMetadataValue(NTDProgramFiles.FN_METAKEY_TITLE);
			try{
				DSBank dsbank = DSBank.readSBNK(bnknode.loadDecompressedData(), 0);
				playbank = dsbank.generatePlayableBank(DSSoundArchive.loadLinkedWavearcs(bnknode), 0);	
			}
			catch(IOException e){
				playbank = null;
				bnkname = null;
				JOptionPane.showMessageDialog(gui_parent, 
						"Bank linked to this sequence node could not be automatically loaded.", 
						"I/O Error", JOptionPane.WARNING_MESSAGE);
				e.printStackTrace();
			}
			catch(UnsupportedFileTypeException e){
				playbank = null;
				bnkname = null;
				JOptionPane.showMessageDialog(gui_parent, 
						"Bank linked to this sequence node could not be automatically loaded.", 
						"Parser Error", JOptionPane.WARNING_MESSAGE);
				e.printStackTrace();
			}
		}
		
		//Generate panel
		Frame p = null;
		if(gui_parent instanceof Frame) p = (Frame)gui_parent;
		NinSeqPreviewPanel pnl = new NinSeqPreviewPanel(p);
		pnl.loadSeq(seq.getSequenceData(), seqname);
		pnl.loadBank(playbank, bnkname);
		
		return pnl;
	}
	
	public List<FileAction> getFileActions() {
		//Export to MIDI, Extract, View Hex
		List<FileAction> falist = new ArrayList<FileAction>(4);
		falist.add(new FA_ExportMIDI());
		falist.add(FA_ExtractFile.getAction());
		falist.add(FA_ViewHex.getAction());
		return falist;
	}
	
	public Converter getStandardConverter() {return DSSeq.getDefaultConverter();}
	
	public boolean isOfType(FileNode node) {
		return detectFileType(node) != null;
	}
	
	//----------------------------
	
	public static class FA_ExportMIDI implements FileAction{

		private static String DEFO_ENG = "Export to MIDI";
		
		private String str;
		
		public FA_ExportMIDI(){str = DEFO_ENG;}
		
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			JFileChooser fc = new JFileChooser(NTDProgramFiles.getIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int select = fc.showSaveDialog(gui_parent);
			
			if(select != JFileChooser.APPROVE_OPTION) return;
			String dir = fc.getSelectedFile().getAbsolutePath();
			
			Converter conv = DSSeq.getDefaultConverter();
			String targetpath = conv.changeExtension(dir + File.separator + node.getFileName());
			NTDProgramFiles.setIniValue(NTDProgramFiles.INIKEY_LAST_EXPORTED, targetpath);
			IndefProgressDialog dialog = new IndefProgressDialog(gui_parent, "MIDI Conversion");
			dialog.setPrimaryString("Exporting SSEQ to MIDI");
			dialog.setSecondaryString("Writing to " + targetpath);
			
			SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
			{

				protected Void doInBackground() throws Exception 
				{
					try
					{
						FileBuffer dat = node.loadDecompressedData();
						conv.writeAsTargetFormat(dat, targetpath);
					}
					catch(Exception x)
					{
						x.printStackTrace();
						JOptionPane.showMessageDialog(gui_parent, 
								"Unknown Error: Conversion Failed! See stderr for details.", 
								"SSEQ Conversion Error", JOptionPane.ERROR_MESSAGE);
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

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
	}
	
}
