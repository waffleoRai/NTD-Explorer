package waffleoRai_NTDExCore.seq;

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import waffleoRai_NTDExGUI.panels.preview.seq.GenSeqTrack;
import waffleoRai_SoundSynth.SequencePlayer;
import waffleoRai_SoundSynth.SynthBank;

public interface SeqLoader {

	public void setSequenceName(String s);
	public String getSequenceName();
	public String getBankName();
	
	public List<GenSeqTrack> getTrackData();
	
	public SequencePlayer getPlayer();
	public Map<String, SynthBank> getAllBanks();
	public JPanel getSpecialPanel();
	public void setBank(SynthBank bank);
	
	public boolean exportMIDI(String path);
	public boolean exportWAVE(String path, int loops);
	
	public void stopPlayback();
	
	public void reset();
	public void dispose();
	
	
}
