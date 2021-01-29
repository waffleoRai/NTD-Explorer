package waffleoRai_NTDExGUI.panels.preview;

import waffleoRai_GUITools.DisposableJPanel;
import waffleoRai_Sound.Sound;
import waffleoRai_Video.AVPlayerPanel;
import waffleoRai_Video.IVideoSource;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.border.EtchedBorder;

public class MultitrackAVPanel extends DisposableJPanel{

	private static final long serialVersionUID = 3511794211804692635L;

	private JLabel lblLength;
	private JComboBox<Integer> cmbxVid;
	private JComboBox<Integer> cmbxAud;
	
	private AVPlayerPanel pnlPlayer;
	
	private IVideoSource[] vTracks;
	private Sound[] aTracks;
	
	public MultitrackAVPanel(int vtracks, int atracks){
		if(vtracks > 0) vTracks = new IVideoSource[vtracks];
		if(atracks > 0) aTracks = new Sound[atracks];
		initGUI();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 75, 0, 75, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblVideoTrack = new JLabel("Video Track:");
		GridBagConstraints gbc_lblVideoTrack = new GridBagConstraints();
		gbc_lblVideoTrack.anchor = GridBagConstraints.EAST;
		gbc_lblVideoTrack.insets = new Insets(5, 5, 5, 5);
		gbc_lblVideoTrack.gridx = 0;
		gbc_lblVideoTrack.gridy = 0;
		panel_1.add(lblVideoTrack, gbc_lblVideoTrack);
		
		cmbxVid = new JComboBox<Integer>();
		GridBagConstraints gbc_cmbxVid = new GridBagConstraints();
		gbc_cmbxVid.insets = new Insets(5, 5, 5, 5);
		gbc_cmbxVid.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxVid.gridx = 1;
		gbc_cmbxVid.gridy = 0;
		panel_1.add(cmbxVid, gbc_cmbxVid);
		cmbxVid.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				System.err.println("cmbxVid Selection Event");
				int cidx = cmbxVid.getSelectedIndex();
				int v = -1;
				if(cidx >= 0) v = cmbxVid.getItemAt(cidx);
				setVideoTrack(v);
			}
			
		});
		
		JLabel lblAudioTrack = new JLabel("Audio Track:");
		GridBagConstraints gbc_lblAudioTrack = new GridBagConstraints();
		gbc_lblAudioTrack.anchor = GridBagConstraints.EAST;
		gbc_lblAudioTrack.insets = new Insets(5, 5, 5, 5);
		gbc_lblAudioTrack.gridx = 2;
		gbc_lblAudioTrack.gridy = 0;
		panel_1.add(lblAudioTrack, gbc_lblAudioTrack);
		
		cmbxAud = new JComboBox<Integer>();
		GridBagConstraints gbc_cmbxAud = new GridBagConstraints();
		gbc_cmbxAud.insets = new Insets(5, 0, 5, 5);
		gbc_cmbxAud.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxAud.gridx = 3;
		gbc_cmbxAud.gridy = 0;
		panel_1.add(cmbxAud, gbc_cmbxAud);
		cmbxAud.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				System.err.println("cmbxAud Selection Event");
				int cidx = cmbxAud.getSelectedIndex();
				int a = -1;
				if(cidx >= 0) a = cmbxAud.getItemAt(cidx);
				setAudioTrack(a);
			}
			
		});
		
		lblLength = new JLabel("Length: 00:00:00");
		GridBagConstraints gbc_lblLength = new GridBagConstraints();
		gbc_lblLength.insets = new Insets(5, 5, 5, 5);
		gbc_lblLength.gridx = 5;
		gbc_lblLength.gridy = 0;
		panel_1.add(lblLength, gbc_lblLength);
		
		pnlPlayer = new AVPlayerPanel(null, null, true, true, true);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(pnlPlayer, gbc_panel);
	}
	
	public void setWait(){
		pnlPlayer.setWait();
		cmbxVid.setEnabled(false);
		cmbxAud.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		setCursor(null);
		pnlPlayer.unsetWait();
		cmbxVid.setEnabled(true);
		cmbxAud.setEnabled(true);
	}
	
	public void addVideoTrack(int i, IVideoSource src){
		if(i < 0) return;
		if(vTracks == null){
			vTracks = new IVideoSource[i+1];
		}
		if(i >= vTracks.length){
			//Realloc
			IVideoSource[] old = vTracks;
			vTracks = new IVideoSource[i+1];
			for(int j = 0; j < old.length; j++) vTracks[j] = old[j];
		}
		vTracks[i] = src;
		System.err.println("Video Track added: " + i + " | " + src.toString());
	}
	
	public void addAudioTrack(int i, Sound src){
		if(i < 0) return;
		if(aTracks == null){
			aTracks = new Sound[i+1];
		}
		if(i >= aTracks.length){
			//Realloc
			Sound[] old = aTracks;
			aTracks = new Sound[i+1];
			for(int j = 0; j < old.length; j++) aTracks[j] = old[j];
		}
		aTracks[i] = src;
		System.err.println("Audio Track added: " + i + " | " + src.toString());
	}
	
	public void refreshComboBoxes(){
		DefaultComboBoxModel<Integer> mv = new DefaultComboBoxModel<Integer>();
		DefaultComboBoxModel<Integer> ma = new DefaultComboBoxModel<Integer>();
		
		int v = -1;
		if(vTracks != null){
			for(int i = 0; i < vTracks.length; i++){
				if(vTracks[i] != null){
					if(v == -1) v = i;
					mv.addElement(i);
				}
			}	
		}
		
		int a = -1;
		if(aTracks != null){
			for(int i = 0; i < aTracks.length; i++){
				if(aTracks[i] != null){
					if(a == -1) a = i;
					ma.addElement(i);
				}
			}	
		}
		
		cmbxVid.setModel(mv);
		cmbxAud.setModel(ma);
		
		if(mv.getSize() > 0) cmbxVid.setSelectedIndex(0);
		if(ma.getSize() > 0) cmbxAud.setSelectedIndex(0);
		cmbxVid.repaint();
		cmbxAud.repaint();
		
		System.err.println("Combo boxes refreshed - setting v" + v + ", a" + a);
		setAudioVideoTracks(v, a);
	}
	
	public void setAudioVideoTracks(int v, int a){
		if(pnlPlayer == null) return;
		setWait();
		IVideoSource vid = null;
		if(vTracks != null && v >= 0 && v < vTracks.length){
			vid = vTracks[v];
			System.err.println("Setting video " + v + ": " + vid);
		}
		
		Sound aud = null;
		if(aTracks != null && a >= 0 && a < aTracks.length){
			aud = aTracks[a];
			System.err.println("Setting audio " + a + ": " + aud);
		}
		
		//TODO Okay I think this is the problem.
		//Panel ref can't just be replaced without removed and readding to GUI
		//Probably easier & cleaner to hook up A/V flush/change in existing panel.
		pnlPlayer.dispose();
		
		pnlPlayer = new AVPlayerPanel(vid, aud, true, true, true);
		
		//Get length.
		int frames = vid.getFrameCount();
		int seconds = (int)Math.ceil((double)frames/vid.getFrameRate());
		int hours = seconds/3600;
		seconds = seconds % 3600;
		int minutes = seconds/60;
		seconds = seconds % 60;
		lblLength.setText("Length: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
		
		repaint();
		unsetWait();
	}
	
	public void setVideoTrack(int i){
		//Get audio...
		int a = -1;
		int idx = cmbxAud.getSelectedIndex();
		if(idx >= 0) a = cmbxAud.getItemAt(idx);
		setAudioVideoTracks(i, a);
	}
	
	public void setAudioTrack(int i){
		//Get video...
		int v = -1;
		int idx = cmbxVid.getSelectedIndex();
		if(idx >= 0) v = cmbxVid.getItemAt(idx);
		setAudioVideoTracks(v, i);
	}
	
	public void dispose() {
		pnlPlayer.dispose();
		pnlPlayer = null;
	}

}
