package waffleoRai_NTDTypes.psx;

import java.awt.Component;
import java.awt.Frame;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import waffleoRai_Files.Converter;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.psx.XAStreamFile;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Files.tree.ISOFileNode;
import waffleoRai_NTDExCore.FileAction;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExCore.NTDTypeLoader;
import waffleoRai_NTDExCore.filetypes.TypeManager;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExportFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ExtractFile;
import waffleoRai_NTDExCore.filetypes.fileactions.FA_ViewHex;
import waffleoRai_NTDExGUI.dialogs.TextPaneDialog;
import waffleoRai_NTDExGUI.panels.preview.MultitrackAVPanel;
import waffleoRai_NTDExGUI.panels.preview.SoundPreviewPanel;
import waffleoRai_NTDExGUI.panels.preview.WriterPanel;
import waffleoRai_Sound.psx.PSXXAStream;
import waffleoRai_Sound.psx.XAAudioStream;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Video.psx.XAVideoSource;

public class TM_PSXXAStream {

	//Loaders
	public static class PSXXAAudioStrTypeLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new AudioStreamManager();}
		public FileTypeDefinition getDefinition() {return PSXXAStream.getAudioDefinition();}	
	}
	
	public static class PSXXAVideoStrTypeLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new VideoStreamManager();}
		public FileTypeDefinition getDefinition() {return PSXXAStream.getVideoDefinition();}	
	}
	
	public static class PSXXAAVStrTypeLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new AVStreamManager();}
		public FileTypeDefinition getDefinition() {return PSXXAStream.getMultimediaDefinition();}	
	}
	
	public static class PSXXAMultiStrTypeLoader implements NTDTypeLoader{
		public TypeManager getTypeManager() {return new MultiStreamManager();}
		public FileTypeDefinition getDefinition() {return PSXXAStream.getMultiStreamDefinition();}	
	}
	
	//Managers
	
	private static MovieInfoAction stat_fainfo;
	public static MovieInfoAction getMoveInfoAction(){
		if(stat_fainfo == null) stat_fainfo = new MovieInfoAction();
		return stat_fainfo;
	}
	
	public static FileTypeNode detectFileTypeCommon(FileNode node) {

		try{
			//Try to extract raw data first sector (if CD node, load raw)
			FileBuffer mydata = null;
			if(node instanceof ISOFileNode){
				ISOFileNode cdnode = (ISOFileNode)node;
				if(cdnode.getSectorTotalSize() < 0x930) return null;
				if(cdnode.getLengthInSectors() < 1) return null;
				
				mydata = cdnode.loadRawData(0, 1);
			}
			else{
				if(node.getLength() < 0x930) return null;
				mydata = node.loadData(0, 0x930);
			}
		
			//Look for sync pattern
			if(mydata.findString(0, 0x20, PSXXAStream.SYNC_PATTERN) != 0L) return null;
			
			//Check mode 2
			//@0x0F
			if(mydata.getByte(0x0f) != 2) return null; 
			
			//Check form 2
			//@0x12 bit 5
			int b = Byte.toUnsignedInt(mydata.getByte(0x12));
			if((b & 0x20) == 0) return null;
			
			//Check for copy of subheader
			//Int @ 0x10 == 0x14
			int c1 = mydata.intFromFile(0x10);
			int c2 = mydata.intFromFile(0x14);
			if(c1 != c2) return null;
			
			//Check channel number validity
			//@0x11
			b = Byte.toUnsignedInt(mydata.getByte(0x11));
			if((b & 0xe0) != 0) return null;
			
			//Check CI byte validity
			//@0x13
			b = Byte.toUnsignedInt(mydata.getByte(0x13));
			if((b & 0x80) != 0) return null; //Highest bit should not be set.
			
			//If it passes, parse full file to check all channels...
			if(node instanceof ISOFileNode){
				ISOFileNode cdnode = (ISOFileNode)node;
				node = cdnode.getRawDataNode();
			}
			PSXXAStream str = PSXXAStream.readStream(node);
			
			//Return type
			if(str.countFiles() > 1) return new FileTypeDefNode(PSXXAStream.getMultiStreamDefinition());
			XAStreamFile sfile = str.getFile(0);
			boolean v = sfile.hasVideo() || sfile.hasData();
			boolean a = sfile.hasAudio();
			if(v && a) return new FileTypeDefNode(PSXXAStream.getMultimediaDefinition());
			if(v) return new FileTypeDefNode(PSXXAStream.getVideoDefinition());
			if(a) return new FileTypeDefNode(PSXXAStream.getAudioDefinition());
			
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}
		
		return null;
	}
	
	public static class MovieInfoAction implements FileAction{

		private String str = "View Movie Info";
		//private IVideoSource vid;
		//private Sound aud;
		
		public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
			
			try{
				
				PSXXAStream fullstr = PSXXAStream.readStream(node);
				XAStreamFile sfile = fullstr.getFile(0);
				
				//Spawn dialog
				TextPaneDialog dialog = new TextPaneDialog(gui_parent);
				StyledDocument doc = dialog.getTextPaneDoc();
				SimpleAttributeSet as = new SimpleAttributeSet();
				StyleConstants.setFontFamily(as, "Courier New");
				StyleConstants.setFontSize(as, 10);
				
				//Source video (if present)
				for(int i = 0; i < 32; i++){
					if(sfile.streamExists(PSXXAStream.STYPE_DATA, i)){
						XAVideoSource vid = new XAVideoSource(sfile, i);

						doc.insertString(doc.getLength(), 
								"Data Track " + i + "--\n", as);
						doc.insertString(doc.getLength(), 
								"\tFrame Size: " + vid.getWidth() + " x " + vid.getHeight() + "\n", as);
						doc.insertString(doc.getLength(), 
								"\tFrame Rate: " + vid.getFrameRate() + " fps\n", as);
						doc.insertString(doc.getLength(), 
								"\tCodec: PlayStation 1 MDEC\n", as);
						int frames = vid.getFrameCount();
						doc.insertString(doc.getLength(), 
								"\tTotal Frames: " + frames + "\n", as);
						
						int seconds = (int)Math.ceil((double)frames/vid.getFrameRate());
						int hours = seconds/3600;
						seconds = seconds % 3600;
						int minutes = seconds/60;
						seconds = seconds % 60;
						
						doc.insertString(doc.getLength(), 
								"\tLength: " + String.format("%02d:%02d:%02d", hours, minutes, seconds) + "\n", as);
					
						doc.insertString(doc.getLength(), 
								"\n", as);
						
					}
				}
				
				//Source audio (if present)
				for(int i = 0; i < 32; i++){
					if(sfile.streamExists(PSXXAStream.STYPE_AUDIO, i)){
						
						XAAudioStream astr = new XAAudioStream(sfile, i);
						
						doc.insertString(doc.getLength(), 
								"Audio Track " + i + "--\n", as);
						doc.insertString(doc.getLength(), 
								"\tSample Rate: " + astr.getSampleRate() + " hz\n", as);
						doc.insertString(doc.getLength(), 
								"\tBit Depth: " + astr.getSourceBitDepth() + " bits\n", as);
						doc.insertString(doc.getLength(), 
								"\tChannels: " + astr.totalChannels() + "\n", as);
						doc.insertString(doc.getLength(), 
								"\tCodec: Sony VAG ADPCM (XA)\n", as);
						
						int frames = astr.totalFrames();
						doc.insertString(doc.getLength(), 
								"\tFrames: " + frames + "\n", as);
						
						int seconds = (int)Math.ceil((double)frames/(double)astr.getSampleRate());
						int hours = seconds/3600;
						seconds = seconds % 3600;
						int minutes = seconds/60;
						seconds = seconds % 60;
						
						doc.insertString(doc.getLength(), 
								"\tLength: " + String.format("%02d:%02d:%02d", hours, minutes, seconds) + "\n", as);
						
						doc.insertString(doc.getLength(), 
								"\n", as);
					}
				}
				
				//Show dialog
				dialog.setVisible(true);
				
			}
			catch(IOException x){
				x.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be loaded!", 
						"I/O Error", JOptionPane.ERROR_MESSAGE);
			} 
			catch (UnsupportedFileTypeException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be read!", 
						"Format Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: General exception thrown. See stderr.", 
						"Display Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}

		public void setString(String s) {str = s;}
		public String toString(){return str;}
		
	}
	
	public static class AudioStreamManager extends TypeManager{

		public FileTypeNode detectFileType(FileNode node) {
			//Use common method
			FileTypeNode dnode = detectFileTypeCommon(node);
			if(dnode == null) return null;
			if(dnode.getTypeDefinition().getTypeID() == PSXXAStream.DEF_ID_A) return dnode;
			return null;
		}

		public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
			//Sound stream panel
			try{
				//Load Data into XAAudioStream
				if(node instanceof ISOFileNode){
					ISOFileNode cdnode = (ISOFileNode)node;
					node = cdnode.getRawDataNode();
				}
				PSXXAStream str = PSXXAStream.readStream(node);
				XAStreamFile sfile = str.getFile(0);
				// (Figure out which channels are audio channels)
				LinkedList<Integer> chlist = new LinkedList<Integer>();
				for(int i = 0; i < 32; i++){
					if(sfile.streamExists(PSXXAStream.STYPE_AUDIO, i)) chlist.add(i);
				}
				int[] charr = new int[chlist.size()];
				int c = 0;
				for(Integer i : chlist) charr[c++] = i;
				XAAudioStream astr = new XAAudioStream(sfile, charr);
				
				//Set Info
				SoundPreviewPanel pnl = new SoundPreviewPanel();
				Map<String, String> imap = new HashMap<String, String>();
				imap.put("Sample Rate", astr.getSampleRate() + " hz");
				imap.put("Bit Depth", astr.getSourceBitDepth() + " bits");
				imap.put("Channels", Integer.toString(astr.totalChannels()));
				imap.put("Tracks", Integer.toString(astr.countTracks()));
				
				//	(Calculate length in time)
				int frames = astr.totalFrames();
				int t = (int)Math.ceil((double)frames/(double)astr.getSampleRate());
				int hr = t/3600;
				t -= hr * 3600;
				int min = t/60;
				t -= (min * 60);
				imap.put("Length", String.format("%02d:%02d:%02d", hr, min, t));
				
				pnl.setSoundInfo(imap);
				
				//Load sound
				pnl.setSound(astr);
				
				return pnl;
			}
			catch(IOException x){
				x.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: File could not be loaded!", 
						"I/O Error", JOptionPane.ERROR_MESSAGE);
			}
			catch(UnsupportedFileTypeException x){
				x.printStackTrace();
			}
			
			return null;
		}

		public List<FileAction> getFileActions() {
			List<FileAction> list = new ArrayList<FileAction>(3);
			list.add(FA_ExtractFile.getAction());
			list.add(FA_ViewHex.getAction());
			list.add(new FA_ExportFile(){

				protected Converter getConverter(FileNode node) {
					return getStandardConverter();
				}
				
			});
			return list;
		}

		public Converter getStandardConverter() {return XAAudioStream.getWavConv();}

		public boolean isOfType(FileNode node) {
			FileTypeNode det = detectFileType(node);
			if(det == null) return false;
			return (det.getTypeID() == PSXXAStream.DEF_ID_A);
		}
		
	}
	
	public static class VideoStreamManager extends TypeManager{

		public FileTypeNode detectFileType(FileNode node) {
			FileTypeNode dnode = detectFileTypeCommon(node);
			if(dnode == null) return null;
			if(dnode.getTypeDefinition().getTypeID() == PSXXAStream.DEF_ID_V) return dnode;
			return null;
		}

		public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

			try{
				PSXXAStream fullstr = PSXXAStream.readStream(node);
				XAStreamFile sfile = fullstr.getFile(0);
				
				Map<Integer, XAVideoSource> vmap = new HashMap<Integer, XAVideoSource>();
				
				for(int i = 0; i < 32; i++){
					if(sfile.streamExists(PSXXAStream.STYPE_DATA, i)){
						XAVideoSource vid = new XAVideoSource(sfile, i);
						if(vid != null) vmap.put(i, vid);
					}
				}
				
				MultitrackAVPanel pnl = new MultitrackAVPanel(vmap.size(), 0);
				for(Integer k : vmap.keySet()) pnl.addVideoTrack(k, vmap.get(k));
				
				pnl.refreshComboBoxes();
				return pnl;
			}
			catch(IOException x){
				x.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be loaded!", 
						"I/O Error", JOptionPane.ERROR_MESSAGE);
			} 
			catch (UnsupportedFileTypeException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be read!", 
						"Format Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: General exception thrown. See stderr.", 
						"Display Error", JOptionPane.ERROR_MESSAGE);
			}
			
			return null;
		}

		public List<FileAction> getFileActions() {
			//TODO Export will be added with MKV
			List<FileAction> list = new ArrayList<FileAction>(4);
			list.add(FA_ExtractFile.getAction());
			list.add(FA_ViewHex.getAction());
			list.add(getMoveInfoAction());
			return list;
		}

		public Converter getStandardConverter() {
			// TODO
			//This will eventually be MKV once MediaAdapter is in.
			return null;
		}

		public boolean isOfType(FileNode node) {
			FileTypeNode det = detectFileType(node);
			if(det == null) return false;
			return (det.getTypeID() == PSXXAStream.DEF_ID_V);
		}
		
	}
	
	public static class AVStreamManager extends TypeManager{

		public FileTypeNode detectFileType(FileNode node) {
			FileTypeNode dnode = detectFileTypeCommon(node);
			if(dnode == null) return null;
			if(dnode.getTypeDefinition().getTypeID() == PSXXAStream.DEF_ID_AV) return dnode;
			return null;
		}

		public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {
			
			try{
				PSXXAStream fullstr = PSXXAStream.readStream(node);
				XAStreamFile sfile = fullstr.getFile(0);
				
				Map<Integer, XAVideoSource> vmap = new HashMap<Integer, XAVideoSource>();
				Map<Integer, XAAudioStream> amap = new HashMap<Integer, XAAudioStream>();
				
				int vmax = -1;
				int amax = -1;
				for(int i = 0; i < 32; i++){
					if(sfile.streamExists(PSXXAStream.STYPE_DATA, i)){
						XAVideoSource vid = new XAVideoSource(sfile, i);
						if(vid != null){ 
							System.err.println("Video Track found: " + i); 
							vmax = i;
							vmap.put(i, vid);
						}
					}
					if(sfile.streamExists(PSXXAStream.STYPE_AUDIO, i)){
						XAAudioStream aud = new XAAudioStream(sfile, i);
						if(aud != null){ 
							System.err.println("Audio Track found: " + i); 
							amax = i;
							amap.put(i, aud);
						}
					}
				}
				
				MultitrackAVPanel pnl = new MultitrackAVPanel(vmax+1, amax+1);
				for(Integer k : vmap.keySet()) pnl.addVideoTrack(k, vmap.get(k));
				for(Integer k : amap.keySet()) pnl.addAudioTrack(k, amap.get(k));
				
				pnl.refreshComboBoxes();
				return pnl;
			}
			catch(IOException x){
				x.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be loaded!", 
						"I/O Error", JOptionPane.ERROR_MESSAGE);
			} 
			catch (UnsupportedFileTypeException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be read!", 
						"Format Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: General exception thrown. See stderr.", 
						"Display Error", JOptionPane.ERROR_MESSAGE);
			}
			
			return null;
		}

		public List<FileAction> getFileActions() {
			//TODO Export will be added with MKV
			List<FileAction> list = new ArrayList<FileAction>(4);
			list.add(FA_ExtractFile.getAction());
			list.add(FA_ViewHex.getAction());
			list.add(getMoveInfoAction());
			return list;
		}

		public Converter getStandardConverter() {
			//TODO
			//This will eventually be MKV once MediaAdapter is in.
			return null;
		}

		public boolean isOfType(FileNode node) {
			FileTypeNode det = detectFileType(node);
			if(det == null) return false;
			return (det.getTypeID() == PSXXAStream.DEF_ID_AV);
		}
		
	}
	
	public static class MultiStreamManager extends TypeManager{

		public FileTypeNode detectFileType(FileNode node) {
			FileTypeNode dnode = detectFileTypeCommon(node);
			if(dnode == null) return null;
			if(dnode.getTypeDefinition().getTypeID() == PSXXAStream.DEF_ID_ARC) return dnode;
			return null;
		}

		public JPanel generatePreviewPanel(FileNode node, Component gui_parent) {

			try{
				PSXXAStream fullstr = PSXXAStream.readStream(node);
				WriterPanel pnl = new WriterPanel();
				Writer w = pnl.getWriter();
				
				int fcount = fullstr.countFiles();
				w.write("Stream Files: " + fcount + "\n");
				for(int i = 0; i < fcount; i++){
					XAStreamFile sfile = fullstr.getFile(i);
					if(sfile == null) continue;
					w.write("\n");
					w.write("FILE " + i + "---\n");
					w.write("\tVideo Tracks: " + sfile.countVideoChannels() + "\n");
					w.write("\tAudio Tracks: " + sfile.countAudioChannels() + "\n");
					w.write("\tData Tracks: " + sfile.countDataChannels() + "\n");
				}
				
				w.close();
				return pnl;
			}
			catch(IOException x){
				x.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be loaded!", 
						"I/O Error", JOptionPane.ERROR_MESSAGE);
			} 
			catch (UnsupportedFileTypeException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be read!", 
						"Format Error", JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(gui_parent, "Error: General exception thrown. See stderr.", 
						"Display Error", JOptionPane.ERROR_MESSAGE);
			}
			
			return null;
		}

		public List<FileAction> getFileActions() {
			//Extract, View Hex, Split
			List<FileAction> list = new ArrayList<FileAction>(3);
			list.add(FA_ExtractFile.getAction());
			list.add(FA_ViewHex.getAction());
			list.add(new FileAction(){

				private String str = "Split";
				
				public void doAction(FileNode node, NTDProject project, Frame gui_parent) {
					try{
						PSXXAStream fullstr = PSXXAStream.readStream(node);
						DirectoryNode dir = fullstr.toDirectory(node.getFileName());
						DirectoryNode parent = node.getParent();
						node.setFileName("~" + node.getFileName());
						node.setParent(dir);
						
						//Mount
						dir.setParent(parent);
					}
					catch(IOException x){
						x.printStackTrace();
						JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be loaded!", 
								"I/O Error", JOptionPane.ERROR_MESSAGE);
					} 
					catch (UnsupportedFileTypeException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(gui_parent, "Error: Data could not be read!", 
								"Format Error", JOptionPane.ERROR_MESSAGE);
					}
					catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(gui_parent, "Error: General exception thrown. See stderr.", 
								"Display Error", JOptionPane.ERROR_MESSAGE);
					}
					
				}

				public void setString(String s) {str = s;}
				public String toString(){return str;}
				
			});
			return list;
		}

		public Converter getStandardConverter() {
			//TODO
			//This will eventually be MKV once MediaAdapter is in.
			return null;
		}

		public boolean isOfType(FileNode node) {
			FileTypeNode det = detectFileType(node);
			if(det == null) return false;
			return (det.getTypeID() == PSXXAStream.DEF_ID_ARC);
		}
		
	}
	
}
