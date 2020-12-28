package waffleoRai_NTDExCore.seq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.JPanel;

import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.NodeMatchCallback;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_NTDExGUI.panels.preview.seq.GenSeqEvent;
import waffleoRai_NTDExGUI.panels.preview.seq.GenSeqTrack;
import waffleoRai_SeqSound.psx.SEQP;
import waffleoRai_SeqSound.psx.SeqpPlayer;
import waffleoRai_SoundSynth.SequencePlayer;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.SoundbankDef;

public class SEQPLoader implements SeqLoader{
	
	/*----- Instance Variables -----*/
	
	private SEQP seq;
	private SynthBank bnk;
	
	private SeqpPlayer active_player;
	
	private FileNode seq_node; //For naming
	private FileNode bnk_node;
	
	/*----- Construction -----*/
	
	public SEQPLoader(FileNode seqnode) throws IOException, UnsupportedFileTypeException{
		constructorCore(seqnode);
	}
	
	private void constructorCore(FileNode seqnode) throws IOException, UnsupportedFileTypeException{

		seq_node = seqnode;
		seq = null; bnk = null;
		active_player = null;
		bnk_node = null;
		
		if(seqnode == null) return;
		
		//Load seq...
		try {
			FileBuffer seqdat = seq_node.loadDecompressedData();
			seq = new SEQP(seqdat, 0);
			
			//Look for bank...
			bnk_node = FileUtils.findPartnerNode(seqnode, SEQP.FNMETAKEY_BANKPATH, SEQP.FNMETAKEY_BANKUID);
			
			if(bnk_node != null){
				//Load. Or try to anyway.
				FileTypeNode tn = bnk_node.getTypeChainTail();
				if(tn != null){
					FileTypeDefinition def = tn.getTypeDefinition();
					if(def != null && def instanceof SoundbankDef){
						SoundbankDef sdef = (SoundbankDef)def;
						bnk = sdef.getPlayableBank(bnk_node);
					}
				}
			}
		} 
		catch (InvalidMidiDataException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
	}
	
	/*----- Getters -----*/
	
	public String getSequenceName() {
		String seqn = seq_node.getMetadataValue("TITLE");
		if(seqn == null){
			seqn = seq_node.getFileName();
			seq_node.setMetadataValue("TITLE", seqn);
		}
		return seqn;
	}
	
	public String getBankName() {
		if(bnk_node == null) return null;
		
		String n = bnk_node.getMetadataValue("TITLE");
		if(n == null){
			n = bnk_node.getFileName();
			bnk_node.setMetadataValue("TITLE", n);
		}
		return n;
	}

	public List<GenSeqTrack> getTrackData() {
		if(seq == null) return new LinkedList<GenSeqTrack>();
		
		Sequence midseq = seq.getSequence();
		Track[] tracks = midseq.getTracks();
		int tcount = tracks.length;
		List<GenSeqTrack> tlist = new ArrayList<GenSeqTrack>(tcount+1);
		for(int i = 0; i < tcount; i++){
			Track t = tracks[i];
			if(t == null) continue;
			String tname = "SEQp_Track_" + String.format("%02d", i);
			GenSeqTrack tr = new GenSeqTrack();
			tr.setName(tname);
			tlist.add(tr);
			
			int ecount = t.size();
			for(int j = 0; j < ecount; j++){
				MidiEvent me = t.get(j);
				GenSeqEvent ev = new GenSeqEvent();
				ev.setByteOrder(true);
				tr.addEvent(ev);
				
				MidiMessage msg = me.getMessage();
				ev.setTick(me.getTick());
				ev.setBytes(msg.getMessage());
				ev.setName(MIDIText.eventToText(msg));
			}
			
		}
		
		return tlist;
	}

	public SequencePlayer getPlayer() {
		if(active_player != null) active_player.dispose();
		active_player = new SeqpPlayer(seq, bnk);
		return active_player;
	}

	public Map<String, SynthBank> getAllBanks() {
		Map<String, SynthBank> map = new HashMap<String, SynthBank>();
		if(seq_node == null) return map;
		
		//Find root
		DirectoryNode dir = seq_node.getParent();
		if(dir == null) return map;
		while(dir.getParent() != null) dir = dir.getParent();
		
		//Grab any soundbank typed nodes
		Collection<FileNode> nodes = dir.getNodesThat(new NodeMatchCallback(){

			public boolean meetsCondition(FileNode n) {
				if(n.isDirectory()) return false;
				FileTypeNode ftn = n.getTypeChainTail();
				if(ftn == null) return false;
				FileTypeDefinition def = ftn.getTypeDefinition();
				if(def == null) return false;
				return (def instanceof SoundbankDef);
			}
			
		});
		
		//Load and add to map
		for(FileNode fn : nodes){
			try{
				FileTypeDefinition def = fn.getTypeChainTail().getTypeDefinition();
				if(def instanceof SoundbankDef){
					SoundbankDef sbdef = (SoundbankDef) def;
					SynthBank bank = sbdef.getPlayableBank(fn);
					
					if(bank != null){
						//Look for a name
						String bname = fn.getFileName();
						String mv = fn.getMetadataValue("TITLE");
						if(bank.getName() != null) bname = bank.getName();
						else if (mv != null) bname = mv;
						map.put(bname, bank);
					}
				}
			}
			catch(Exception x){
				x.printStackTrace();
			}	
		}

		return map;
	}

	public JPanel getSpecialPanel() {
		return new JPanel(); //Doesn't have one
	}
	
	/*----- Setters -----*/
	
	public void setSequenceName(String s) {
		if(seq_node == null) return;
		seq_node.setMetadataValue("TITLE", s);
	}
	
	public void setBank(SynthBank bank) {
		if(bnk_node != null) bnk_node = null;
		if(active_player != null) active_player.dispose();
		active_player = null;
		bnk = bank;
	}
	
	/*----- Export -----*/
	
	public boolean exportMIDI(String path) {
		if(seq == null) return false;
		try {
			seq.writeMIDI(path);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean exportWAVE(String path, int loops) {
		if(seq == null || bnk == null) return false;
		
		if(active_player != null) active_player.dispose();
		active_player = new SeqpPlayer(seq, bnk);
		try {
			active_player.writeMixdownTo(path, loops);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/*----- Management -----*/
	
	public void stopPlayback() {
		if(active_player != null) active_player.stop();
	}

	public void reset() {
		if(active_player != null) active_player.dispose();
		try {
			constructorCore(seq_node);
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		if(active_player != null) active_player.dispose();
	}

}
