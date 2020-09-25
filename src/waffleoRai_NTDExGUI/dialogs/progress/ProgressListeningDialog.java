package waffleoRai_NTDExGUI.dialogs.progress;

import java.awt.Frame;

import javax.swing.JDialog;

public abstract class ProgressListeningDialog extends JDialog{

	private static final long serialVersionUID = -2063520194777412355L;
	
	public ProgressListeningDialog(Frame parent, boolean modal){
		super(parent, modal);
	}
	
	public abstract void onStart();
	public abstract void setPrimaryString(String s);
	public abstract void setSecondaryString(String s);
	public abstract void setPercentage(double p);
	public abstract void onFinish();
	public abstract void showWarningMessage(String text);
	
}
