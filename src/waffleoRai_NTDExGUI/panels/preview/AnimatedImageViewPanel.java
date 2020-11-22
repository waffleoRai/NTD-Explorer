package waffleoRai_NTDExGUI.panels.preview;

import waffleoRai_NTDExGUI.DisposableJPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import waffleoRai_GUITools.AnimatedCheckeredImagePane;

import javax.swing.JLabel;
import javax.swing.JButton;

public class AnimatedImageViewPanel extends DisposableJPanel{

	//Has a scrolling panel and options to stop animation or view all frames
	
	/*----- Constants -----*/
	
	private static final long serialVersionUID = -1279156325941983001L;

	//Oops, that's for the manager...
	//protected abstract Collection<AnimatedImagePaneDrawer> getFrames();
	
	/*----- Instance Variables -----*/
	
	private boolean playing;
	private boolean anim_view;
	
	private JButton btnPlay;
	private JButton btnView;
	
	private AnimatedCheckeredImagePane pnlView;
	
	/*----- Construction -----*/
	
	public AnimatedImageViewPanel(){
		initGUI();
		anim_view = true;
	}
	
	private void initGUI(){
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{50, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlControl = new JPanel();
		pnlControl.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlControl = new GridBagConstraints();
		gbc_pnlControl.insets = new Insets(0, 0, 5, 0);
		gbc_pnlControl.fill = GridBagConstraints.BOTH;
		gbc_pnlControl.gridx = 0;
		gbc_pnlControl.gridy = 0;
		add(pnlControl, gbc_pnlControl);
		GridBagLayout gbl_pnlControl = new GridBagLayout();
		gbl_pnlControl.columnWidths = new int[] {0};
		gbl_pnlControl.rowHeights = new int[] {0};
		gbl_pnlControl.columnWeights = new double[]{0.0, 0.0, 1.0};
		gbl_pnlControl.rowWeights = new double[]{1.0};
		pnlControl.setLayout(gbl_pnlControl);
		
		btnPlay = new JButton("Play Animation");
		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.fill = GridBagConstraints.VERTICAL;
		gbc_btnPlay.insets = new Insets(5, 5, 5, 5);
		gbc_btnPlay.gridx = 0;
		gbc_btnPlay.gridy = 0;
		pnlControl.add(btnPlay, gbc_btnPlay);
		
		btnView = new JButton("View All Frames");
		GridBagConstraints gbc_btnView = new GridBagConstraints();
		gbc_btnView.fill = GridBagConstraints.VERTICAL;
		gbc_btnView.insets = new Insets(5, 5, 5, 5);
		gbc_btnView.gridx = 1;
		gbc_btnView.gridy = 0;
		pnlControl.add(btnView, gbc_btnView);
		
		JLabel lblDummy = new JLabel("");
		GridBagConstraints gbc_lblDummy = new GridBagConstraints();
		gbc_lblDummy.fill = GridBagConstraints.VERTICAL;
		gbc_lblDummy.insets = new Insets(0, 0, 5, 0);
		gbc_lblDummy.gridx = 2;
		gbc_lblDummy.gridy = 0;
		pnlControl.add(lblDummy, gbc_lblDummy);
		
		JScrollPane spView = new JScrollPane();
		spView.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spView = new GridBagConstraints();
		gbc_spView.fill = GridBagConstraints.BOTH;
		gbc_spView.gridx = 0;
		gbc_spView.gridy = 1;
		add(spView, gbc_spView);
		
		pnlView = new AnimatedCheckeredImagePane(250,250);
		spView.setViewportView(pnlView);
	}
	
	/*----- Getters -----*/
	
	public AnimatedCheckeredImagePane getViewPanel(){
		stopAnimation();
		return pnlView;
	}
	
	/*----- Actions -----*/
	
	public void startAnimation(){
		if(!anim_view) toAnimatedView();
		btnPlay.setText("Stop Animation");
		pnlView.startAnimation();
		playing = true;
	}
	
	public void stopAnimation(){
		btnPlay.setText("Play Animation");
		pnlView.stopAnimation();
		playing = false;
	}
	
	public void toAnimatedView(){
		btnView.setText("View All Frames");
		anim_view = true;
		pnlView.setToAnimationView();
		//startAnimation(); NOPE, will call each other!
	}
	
	public void toAllFrameView(){
		if(playing) stopAnimation();
		btnView.setText("View Animation");
		anim_view = false;
		pnlView.setToViewAllFrames();
	}
	
	public void dispose() {
		//Makes sure any loose timers are stopped
		stopAnimation();
		pnlView.clearAllFrames();
	}
	
}
