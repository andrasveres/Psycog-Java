package jna;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import ntp.SntpClient;

// GSR communicates with GSR 0.1 board



public class TestSync  {

	GSRDevice GSR = new GSRDevice();
	
	TestSync()  
	{		


		int res = GSR.Connect();
		if(res!=0) {
			JOptionPane.showMessageDialog(null, "GSR nem elerheto, vagy masik program hasznalja!");
			System.exit(0);
		}
		
		SntpClient ntp = new SntpClient();
		try {
			ntp.Connect();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
			System.exit(0);

			e.printStackTrace();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
			System.exit(0);

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(""+System.currentTimeMillis());
		//GSR.WriteTS();
		long tsg = GSR.ReadTS();
		
		
		long ts=0;
		try {
			ts = ntp.GetNtp();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
			System.exit(0);

			e.printStackTrace();
		}
		
		JOptionPane.showMessageDialog(null, "GSR "+tsg+" NTP "+ts+" DIFF "+(tsg-ts));
		

		GSR.Disconnect();

		
		
	}
	
	public static void main(String[] args) throws InterruptedException, IOException 
	{
		new TestSync();
	}
}

