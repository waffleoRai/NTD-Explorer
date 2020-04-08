package waffleoRai_NTDExGUI.panels.preview;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;

import waffleoRai_NTDExGUI.DisposableJPanel;
import waffleoRai_Sound.Sound;
import waffleoRai_SoundSynth.AudioSampleStream;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

public class SoundPreviewPanel extends DisposableJPanel{

	private static final long serialVersionUID = 5648631942904234822L;
	
	private ArrayList<double[]> sdat;
	
	private Sound sound;
	private Map<String, String> infomap;
	private boolean loopset;
	
	private PlayerWorker worker;
	
	private JComboBox<TrackSelection> cmbxTrack;
	private JButton btnPlay;
	private JButton btnLoop;
	private JTextPane txpInfo;
	private JPanel pnlWav;
	
	private boolean psIconState; //false = play, true = stop
	
	private static class TrackSelection{
		public int idx;
		
		public TrackSelection(int n){idx = n;}
		public String toString(){return "Track " + idx;}
	}
	
	public SoundPreviewPanel(){
		initGUI();
	}
	
	private void initGUI(){
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		drawWaveforms();
		
		JPanel pnlControl = new JPanel();
		pnlControl.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		pnlControl.setLayout(null);
		GridBagConstraints gbc_pnlControl = new GridBagConstraints();
		gbc_pnlControl.insets = new Insets(0, 0, 0, 5);
		gbc_pnlControl.fill = GridBagConstraints.BOTH;
		gbc_pnlControl.gridx = 0;
		gbc_pnlControl.gridy = 1;
		add(pnlControl, gbc_pnlControl);
		
		JLabel lblTrack = new JLabel("Track:");
		lblTrack.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblTrack.setBounds(10, 11, 37, 14);
		pnlControl.add(lblTrack);
		
		cmbxTrack = new JComboBox<TrackSelection>();
		cmbxTrack.setBounds(51, 8, 120, 20);
		pnlControl.add(cmbxTrack);
		
		btnPlay = new JButton("Play");
		setIconStatePlay();
		btnPlay.setBounds(10, 36, 64, 64);
		pnlControl.add(btnPlay);
		btnPlay.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(soundPlaying()) onStop();
				else onPlay();
			}
			
		});
		
		btnLoop = new JButton("Loop");
		btnLoop.setBounds(82, 36, 64, 64);
		pnlControl.add(btnLoop);
		btnLoop.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				onSetLoop();
			}
			
		});
		
		JPanel pnlInfo = new JPanel();
		GridBagConstraints gbc_pnlInfo = new GridBagConstraints();
		gbc_pnlInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlInfo.gridx = 1;
		gbc_pnlInfo.gridy = 1;
		add(pnlInfo, gbc_pnlInfo);
		GridBagLayout gbl_pnlInfo = new GridBagLayout();
		gbl_pnlInfo.columnWidths = new int[]{0, 0};
		gbl_pnlInfo.rowHeights = new int[]{0, 0};
		gbl_pnlInfo.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlInfo.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlInfo.setLayout(gbl_pnlInfo);
		
		JScrollPane spInfo = new JScrollPane();
		GridBagConstraints gbc_spInfo = new GridBagConstraints();
		gbc_spInfo.insets = new Insets(2, 2, 2, 2);
		gbc_spInfo.fill = GridBagConstraints.BOTH;
		gbc_spInfo.gridx = 0;
		gbc_spInfo.gridy = 0;
		pnlInfo.add(spInfo, gbc_spInfo);
		
		txpInfo = new JTextPane();
		spInfo.setViewportView(txpInfo);
	}
	
	private void drawWaveforms(){
		
		//TODO more accurate size retrieval
		
		pnlWav = new JPanel();
		GridBagConstraints gbc_pnlWav = new GridBagConstraints();
		gbc_pnlWav.gridwidth = 2;
		gbc_pnlWav.insets = new Insets(0, 0, 5, 5);
		gbc_pnlWav.fill = GridBagConstraints.BOTH;
		gbc_pnlWav.gridx = 0;
		gbc_pnlWav.gridy = 0;
		add(pnlWav, gbc_pnlWav);
		GridBagLayout gbl_pnlWav = new GridBagLayout();
		gbl_pnlWav.columnWidths = new int[]{0};
		gbl_pnlWav.rowHeights = new int[]{0};
		gbl_pnlWav.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_pnlWav.rowWeights = new double[]{Double.MIN_VALUE};
		pnlWav.setLayout(gbl_pnlWav);
		
		//Get my size
		Dimension mysize = pnlWav.getSize();
		
		//Determine whether sound is mono or stereo...
		if(sound == null){
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
			pnlWav.add(new WaveRenderPanel(mysize.width, mysize.height), gbc);
			
			return;
		}
		
		
		//add one or two WaveRenderPanels, load in data
		int ch = sound.totalChannels();
		if(ch == 1){
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
			WaveRenderPanel ch0 = new WaveRenderPanel(mysize.width, mysize.height);
			ch0.setData(getDataSamples(0));
			pnlWav.add(ch0, gbc);
		}
		else{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(0, 0, 2, 0);
			WaveRenderPanel ch0 = new WaveRenderPanel(mysize.width, (mysize.height/2)-4);
			ch0.setData(getDataSamples(0));
			pnlWav.add(ch0, gbc);
			
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(2, 0, 0, 0);
			WaveRenderPanel ch1 = new WaveRenderPanel(mysize.width, (mysize.height/2)-4);
			ch1.setData(getDataSamples(1));
			pnlWav.add(ch1, gbc);
		}
		
	}
	
	private void updateControlButtons(){
		if(sound == null){
			btnPlay.setEnabled(false);
			btnLoop.setEnabled(false);
			return;
		}
		
		btnPlay.setEnabled(true);
		btnLoop.setEnabled(true);
	}
	
	public void updateMe(){

		//Render waveform(s)
		drawWaveforms();
		
		//Set track cmbx
		DefaultComboBoxModel<TrackSelection> model = new DefaultComboBoxModel<TrackSelection>();
		int tcount = sound.countTracks();
		if(sound == null || tcount < 2){
			model.addElement(new TrackSelection(0));
			cmbxTrack.setModel(model);
			cmbxTrack.setEnabled(false);
		}
		else{
			for(int i = 0; i < tcount; i++){
				model.addElement(new TrackSelection(i));
			}
			cmbxTrack.setModel(model);
			cmbxTrack.setEnabled(true);
		}
		updateControlButtons();
		
		//Write info
		if(infomap == null || infomap.isEmpty()) txpInfo.setText("<NO INFO>");
		else{
			List<String> keys = new LinkedList<String>();
			keys.addAll(infomap.keySet());
			Collections.sort(keys);
			
			StringBuilder sb = new StringBuilder(4096);
			for(String k : keys){
				String val = infomap.get(k);
				sb.append(k + ": " + val + "\n");
			}
			txpInfo.setText(sb.toString());
		}
		
		repaint();
	}
	
	private double[] getDataSamples(int channel){

		if(sdat == null) return null;
		if(sdat.get(channel) != null) return sdat.get(channel);
		
		int ch = sound.totalChannels();
		for(int c = 0; c < ch; c++){
			int[] raw = sound.getSamples_16Signed(c);
			double[] targ = new double[raw.length];
			for(int i = 0; i < raw.length; i++){
				double val = (double)raw[i]/(double)0x7FFF;
				targ[i] = val;
			}
			sdat.add(targ);	
		}
		
		return sdat.get(channel);
	}
	
	public void setSound(Sound s){
		sdat = new ArrayList<double[]>(s.totalChannels());
		sound = s;
		updateMe();
	}
	
	public void setSoundInfo(Map<String, String> info){
		infomap = info;
		updateMe();
	}

	private class PlayerWorker implements Runnable{

		private volatile boolean killme;
		//private Sound sound;
		private SourceDataLine playback_line;
		private int bytes_per_samp;
		
		private volatile boolean running;
		private boolean autostop;
		private boolean loopme;
		
		public PlayerWorker(){
			loopme = loopset;
		}
		
		@Override
		public void run() {

			running = true;
			
			AudioSampleStream strm = sound.createSampleStream(loopme);
			AudioFormat fmt = new AudioFormat(strm.getSampleRate(), strm.getBitDepth(), strm.getChannelCount(), true, false);
			bytes_per_samp = strm.getBitDepth() >>> 3;
			try {
				playback_line = AudioSystem.getSourceDataLine(fmt);
				playback_line.open();
			}
			catch(Exception x){
				showError("Sound playback could not be initialized!");
				x.printStackTrace();
				running = false;
				return;
			}
			
			//Runs until either sound is done playing
			//or worker is terminated by user
			while(!killme){
				try {
					int[] samps = strm.nextSample();
					for(int c = 0; c < samps.length; c++){
						int s = samps[c];
						
						int mask = 0xFF;
						int shift = 0;
						byte[] bytes = new byte[bytes_per_samp];
						for(int i = 0; i < bytes_per_samp; i++){
							bytes[i] = (byte)((s & mask) >>> shift);
							mask = mask << 8;
							shift += 8;
						}
						
						playback_line.write(bytes, 0, bytes_per_samp);
					}
					
					if(strm.done()) {
						synchronized(this){killme = true;}
						autostop = true;
					}
					
				} 
				catch (InterruptedException e) {
					showError("ERROR: Playback was unexpectedly interrupted!");
					e.printStackTrace();
					playback_line.close();
					running = false;
					return;
				}
			}
			
			playback_line.close();
			running = false;
			if(autostop) setIconStatePlay();
		}
		
		public synchronized void terminate(){
			killme = true;
		}
		
		public boolean isRunning(){return running;}
		
	}
	
	public void onSetLoop(){
		loopset = !loopset;
		if(loopset) btnLoop.setForeground(Color.blue);
		else btnLoop.setForeground(Color.black);
	}
	
	private void setPlayIcon(){
		btnPlay.setText("Play");
		btnPlay.setForeground(Color.green);
	}
	
	private void setStopIcon(){
		btnPlay.setText("Stop");
		btnPlay.setForeground(Color.red);
	}
	
	private synchronized void setIconStatePlay(){
		if(!psIconState) return;
		setPlayIcon();
		btnLoop.setEnabled(true);
		psIconState = false;
	}
	
	private synchronized void setIconStateStop(){
		if(psIconState) return;
		setStopIcon();
		btnLoop.setEnabled(false);
		psIconState = true;
	}
	
	private void playSound(){
		if(worker != null && worker.isRunning()) worker.terminate();
		
		worker = new PlayerWorker();
		Thread t = new Thread(worker);
		t.setName("SoundPreviewPanelPlayer");
		t.setDaemon(true);
		t.start();
	}
	
	public void onPlay(){
		setIconStateStop();
		playSound();
	}
	
	public void onStop(){
		if(worker != null && worker.isRunning()) worker.terminate();
		setIconStatePlay();
	}
	
	public boolean soundPlaying(){
		if(worker == null) return false;
		return worker.isRunning();
	}
	
	public void showError(String text){
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void dispose(){
		onStop();
		worker = null;
	}
	
}
