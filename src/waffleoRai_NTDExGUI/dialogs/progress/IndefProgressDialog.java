package waffleoRai_NTDExGUI.dialogs.progress;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

public class IndefProgressDialog extends ProgressListeningDialog implements ActionListener{

	private static final long serialVersionUID = 4635568052368239877L;
	
	public static final int WIDTH = 400;
	public static final int HEIGHT = 170;
	
	private Timer timer;
	
	private int max_dots;
	private volatile int now_dots;
	private volatile double percent;
	
	private String msg_primary;
	private JLabel lblPrimary;
	private JLabel lblSecondary;
	
	public IndefProgressDialog(Frame parent, String title_message)
	{
		super(parent, true);
		setResizable(false);
		max_dots = 12;
		percent = -1;
		
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setPreferredSize(sz);
		if(parent != null) setLocationRelativeTo(parent);
		
		this.setTitle(title_message);
		getContentPane().setLayout(null);
		
		msg_primary = "Primary Message";
		lblPrimary = new JLabel(msg_primary);
		lblPrimary.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPrimary.setBounds(10, 33, 374, 20);
		getContentPane().add(lblPrimary);
		
		lblSecondary = new JLabel("Secondary Message");
		lblSecondary.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblSecondary.setBounds(10, 64, 374, 14);
		getContentPane().add(lblSecondary);
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		timer = new Timer((int)Math.round(1000.0/30.0), this);
	}
	
	public void setMaxDots(int n)
	{
		if(timer.isRunning()) return;
		max_dots = n;
	}
	
	public void render()
	{
		if(timer.isRunning()) return;
		timer.start();
		now_dots = 0;
		setVisible(true);
	}
	
	public synchronized void updatePrimaryMessage(String s)
	{
		msg_primary = s;
		now_dots = 0;
		lblPrimary.setText(msg_primary);
		lblPrimary.repaint();
	}
	
	public synchronized void updateSecondaryMessage(String s)
	{
		lblSecondary.setText(s);
		lblSecondary.repaint();
	}
	
	public synchronized void updateTitle(String s)
	{
		setTitle(s);
		repaint();
	}
	
	public void closeMe()
	{
		timer.stop();
		setVisible(false);
		dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		synchronized(this)
		{
			String pmsg = msg_primary + " ";
			if(percent >= 0) pmsg += "(" + String.format("%.2f", percent) + "%)";
			for(int i = 0; i < now_dots; i++)
			{
				pmsg += ".";
			}
			this.lblPrimary.setText(pmsg);
			lblPrimary.repaint();
			now_dots++;
			if(now_dots > max_dots) now_dots = 0;
		}
	}
	
	public void onStart()
	{
		render();
	}
	
	public void setPrimaryString(String s)
	{
		updatePrimaryMessage(s);
	}
	
	public void setSecondaryString(String s)
	{
		updateSecondaryMessage(s);
	}
	
	public void setPercentage(double p)
	{
		synchronized(this){percent = p;}
	}
	
	public void onFinish()
	{
		closeMe();
	}
	
	public void showWarningMessage(String text)
	{
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}

}
