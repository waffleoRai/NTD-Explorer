package waffleoRai_NTDExGUI.panels.preview;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import waffleoRai_Sound.WAV;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class TestWaveRender {

	public static void main(String[] args) {
		
		//Read wav
		String testpath = "C:\\Users\\Blythe\\Documents\\Desktop\\SDAT_STRM_0000.wav";
		double[][] wavdat = null;
		
		
		try {
			WAV wave = new WAV(testpath);
			
			int[] samps = wave.getSamples_16Signed(0);
			wavdat = new double[2][samps.length];
			for(int i = 0; i < samps.length; i++){
				wavdat[0][i] = ((double)(samps[i])) / ((double)(0x7FFF));
			}
			
			samps = wave.getSamples_16Signed(1);
			for(int i = 0; i < samps.length; i++){
				wavdat[1][i] = ((double)(samps[i])) / ((double)(0x7FFF));
			}
		} 
		catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		} 
		catch (UnsupportedFileTypeException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		final double[][] dat = wavdat;
		
		
		SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	JFrame myform = new JFrame();
            	myform.addWindowListener(new WindowAdapter(){
            		public void windowClosing(WindowEvent e) 
					{
            			System.exit(0);
					}
            	});
            	ResizableMultiWavePanel pnl = new ResizableMultiWavePanel(2);
            	pnl.loadWaveData(0, dat[0]);
            	pnl.loadWaveData(1, dat[1]);
            	myform.add(pnl);
            	myform.pack();
            	myform.setVisible(true);
            }
        });
	}

}
