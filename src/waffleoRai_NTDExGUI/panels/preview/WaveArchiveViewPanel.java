package waffleoRai_NTDExGUI.panels.preview;

import java.awt.GridBagLayout;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.DefaultListModel;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_NTDExGUI.DisposableJPanel;
import waffleoRai_Sound.Sound;
import waffleoRai_SoundSynth.AudioSampleStream;

public class WaveArchiveViewPanel extends DisposableJPanel{

	private static final long serialVersionUID = -2773909919382538024L;

	private JList<WaveSelection> list;
	private JButton btnPlay;
	
	private PlayerWorker worker; //Currently playing sound
	
	private static class WaveSelection{
		
		public Sound sound;
		public String name;
		
		public WaveSelection(Sound snd, String str){
			sound = snd;
			name = str;
		}
		
		public String toString(){return name;}
		
	}
	
	public WaveArchiveViewPanel(Map<String, Sound> sounds){

		initGUI();
		loadList(sounds);
	}

	private void initGUI(){
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		list = new JList<WaveSelection>();
		scrollPane.setViewportView(list);
		list.addListSelectionListener(new ListSelectionListener(){

			private int last = -1;
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int idx = list.getSelectedIndex();
				if(idx == last){
					WaveSelection wave = list.getSelectedValue();
					if(wave != null){
						onPlay(wave.sound);
					}
				}
				else last = idx;
			}
			
		});
		
		btnPlay = new JButton("Play");
		setPlayIcon();
		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.insets = new Insets(5, 5, 5, 5);
		gbc_btnPlay.gridx = 0;
		gbc_btnPlay.gridy = 1;
		add(btnPlay, gbc_btnPlay);
		btnPlay.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(soundPlaying()) {onStop(); return;}
				WaveSelection wave = list.getSelectedValue();
				if(wave != null){
					onPlay(wave.sound);
				}
			}
			
		});
	}
	
	private void loadList(Map<String, Sound> sounds){

		DefaultListModel<WaveSelection> model = new DefaultListModel<WaveSelection>();
		if(sounds == null){
			list.setModel(model);
			return;
		}
		for(String k : sounds.keySet()){
			model.addElement(new WaveSelection(sounds.get(k), k));	
		}
		list.setModel(model);
		
		list.repaint();
	}
	
	private class PlayerWorker implements Runnable{

		private volatile boolean killme;
		private Sound sound;
		private SourceDataLine playback_line;
		private int bytes_per_samp;
		
		private volatile boolean running;
		private boolean autostop;
		
		public PlayerWorker(Sound s){
			sound = s;
		}
		
		@Override
		public void run() {

			running = true;
			
			AudioSampleStream strm = sound.createSampleStream(false);
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
			if(autostop) setPlayIcon();
		}
		
		public synchronized void terminate(){
			killme = true;
		}
		
		public boolean isRunning(){return running;}
		
	}
	
	private void playSound(Sound snd){
		if(worker != null && worker.isRunning()) worker.terminate();
		
		worker = new PlayerWorker(snd);
		Thread t = new Thread(worker);
		t.setName("WavarcPreviewPanelSoundPlayer");
		t.setDaemon(true);
		t.start();
	}
	
	public void onPlay(Sound snd){
		setStopIcon();
		playSound(snd);
	}
	
	public void onStop(){
		if(worker != null && worker.isRunning()) worker.terminate();
		setPlayIcon();
	}
	
	public boolean soundPlaying(){
		if(worker == null) return false;
		return worker.isRunning();
	}
	
	private synchronized void setPlayIcon(){
		btnPlay.setText("Play");
		btnPlay.setForeground(Color.green);
	}
	
	private synchronized void setStopIcon(){
		btnPlay.setText("Stop");
		btnPlay.setForeground(Color.red);
	}
	
	public void showError(String text){
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void dispose() {
		onStop();
	}
	
}
