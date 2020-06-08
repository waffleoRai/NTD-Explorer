package waffleoRai_NTDExCore.seq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JPanel;

import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExCore.NTDTools;
import waffleoRai_NTDExGUI.panels.preview.seq.GenSeqEvent;
import waffleoRai_NTDExGUI.panels.preview.seq.GenSeqTrack;
import waffleoRai_SeqSound.MIDI;
import waffleoRai_SeqSound.misc.SMD;
import waffleoRai_SeqSound.misc.smd.SMDEvent;
import waffleoRai_SeqSound.misc.smd.SMDNoteEvent;
import waffleoRai_SeqSound.misc.smd.SMDPlayer;
import waffleoRai_SeqSound.misc.smd.SMDTrack;
import waffleoRai_SoundSynth.SequencePlayer;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileNode;
import waffleoRai_soundbank.SoundbankDef;
import waffleoRai_soundbank.procyon.SWD;

public class SMDLoader implements SeqLoader{
	
	/*----- Instance Variables -----*/
	
	private SMD seq;
	private SWD defo_bank;
	
	private SynthBank current_bank;
	
	private SMDPlayer last_player; //Stop and dispose if being replaced!
	
	private FileNode smd_node; //For finding alt banks & setting seq name
	private FileNode swd_node;
	
	
	/*----- Construction -----*/
	
	public SMDLoader(FileNode smd) throws UnsupportedFileTypeException, IOException{
		//Find swd from smd node
		
		smd_node = smd;
		seq = new SMD(smd_node.loadDecompressedData(), 0);
		
		defo_bank = SWD.loadSMDPartner(smd_node);
		if(defo_bank != null){
			String bpath = smd_node.getMetadataValue(SWD.FNMETAKEY_SMDPATH);
			swd_node = smd_node.getParent().getNodeAt(bpath);
		}
		
		current_bank = defo_bank;
	}
	
	/*----- Getters -----*/
	
	public String getSequenceName() {
		String seqn = smd_node.getMetadataValue("TITLE");
		if(seqn == null){
			seqn = seq.getInternalName();
			smd_node.setMetadataValue("TITLE", seqn);
		}
		return seqn;
	}
	
	public String getBankName() {
		if(swd_node == null) return null;
		
		String n = swd_node.getMetadataValue("TITLE");
		if(n == null){
			n = seq.getInternalName();
			swd_node.setMetadataValue("TITLE", n);
		}
		return n;
	}

	public List<GenSeqTrack> getTrackData() {

		int tcount = seq.getNumberTracks();
		List<GenSeqTrack> tlist = new ArrayList<GenSeqTrack>(tcount+1);
		for(int i = 0; i < tcount; i++){
			SMDTrack t = seq.getTrack(i);
			if(t == null) continue;
			String tname = "SMD_Track_" + String.format("%02d", i);
			GenSeqTrack tr = new GenSeqTrack();
			tr.setName(tname);
			
			//Events
			long tick = 0;
			long lastwait = 0;
			int lastnotelen = 0;
			int ecount = t.getNumberEvents();
			for(int j = 0; j < ecount; j++){
				SMDEvent e = t.getEvent(j);
				GenSeqEvent ev = new GenSeqEvent();
				ev.setByteOrder(true);
				ev.setBytes(e.serializeMe());
				ev.setTick(tick);
				
				switch(e.getType()){
				case NA_DELTATIME: tick += e.getWait(); break;
				case WAIT_AGAIN:
					tick += lastwait; break;
				case WAIT_ADD:
					long add = e.getWait();
					lastwait += add;
					tick += lastwait;
					break;
				case WAIT_1BYTE:
				case WAIT_2BYTE:
					lastwait = e.getWait();
					tick += lastwait;
					break;
				default:
					//Map to current tick
					if(e instanceof SMDNoteEvent){
						SMDNoteEvent ne = (SMDNoteEvent)e;
						if(ne.getLength() < 0){
							//Presumably length of previous
							ne.setLength(lastnotelen);
						}
						lastnotelen = ne.getLength();
					}
					break;
				}
				ev.setName(e.toString());
				tr.addEvent(ev);
			}
			
			
			//Add to list
			tlist.add(tr);
		}
		
		return tlist;
	}

	public SequencePlayer getPlayer() {
		if(last_player != null) last_player.dispose();
		
		last_player = new SMDPlayer(seq, current_bank);
		return last_player;
	}
	
	public Map<String, SynthBank> getAllBanks() {
		//TODO is this getting disposed of properly?
		//Scan ROM for usable SWDs
		List<FileNode> bfiles = NTDTools.scanForType(smd_node, FileClass.SOUNDBANK);
		Map<String, SynthBank> bmap = new HashMap<String, SynthBank>();
		
		for(FileNode fn : bfiles){
			FileTypeNode t = fn.getTypeChainTail();
			if(t.getTypeDefinition() instanceof SoundbankDef){
				SynthBank bnk = ((SoundbankDef)t.getTypeDefinition()).getPlayableBank(fn);
				if(bnk != null){
					bmap.put(bnk.getName(), bnk);
				}
			}
		}
		
		return bmap;
	}

	public JPanel getSpecialPanel() {
		return new JPanel(); //Doesn't have one
	}
	
	/*----- Setters -----*/
	
	public void setSequenceName(String s) {
		smd_node.setMetadataValue("TITLE", s);
	}
	
	public void setBank(SynthBank bank) {
		current_bank = bank;
		if(last_player != null) last_player.dispose();
		last_player = null;
	}
	
	/*----- Export -----*/
	
	public boolean exportMIDI(String path) {
		try {
			MIDI mid = seq.toMIDI();
			mid.writeMIDI(path);
		} 
		catch (InvalidMidiDataException e) {
			e.printStackTrace();
			return false;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean exportWAVE(String path, int loops) {
		if(last_player == null) getPlayer();
		if(last_player == null) return false;
		
		try {
			last_player.writeMixdownTo(path, loops);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/*----- Management -----*/
	
	public void stopPlayback() {
		if(last_player != null){
			last_player.stop();
		}
	}

	public void reset() {
		stopPlayback();
		if(last_player != null) last_player.dispose();
		last_player = null;
		current_bank = defo_bank;
	}

	public void dispose() {
		stopPlayback();
		if(last_player != null) last_player.dispose();
		last_player = null;
		current_bank = null;
		defo_bank.clearPlaybackCache();
	}


}
