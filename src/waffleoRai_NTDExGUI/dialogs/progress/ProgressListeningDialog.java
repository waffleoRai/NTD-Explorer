package waffleoRai_NTDExGUI.dialogs.progress;

public interface ProgressListeningDialog {

	public void onStart();
	public void setPrimaryString(String s);
	public void setSecondaryString(String s);
	public void setPercentage(double p);
	public void onFinish();
	public void showWarningMessage(String text);
	
}
