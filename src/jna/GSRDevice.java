package jna;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.JOptionPane;

import ntp.SntpClient;

public class GSRDevice {
	MyHID hid;
	public EEPROM rom;
	
	public int Connect() {
		
		hid = new MyHID();
		
		boolean res = hid.Connect(0x04d8, 0x003f);
		System.out.println("gethandle "+res+" isopened "+hid.isOpened());
		if(!res) return -1;
		
		//int c = CheckConnected();
		//if(c==1) return -2;
		
		//SetConnected();
		
		//c = CheckConnected();
		//if(c==0) System.exit(0);
		
		rom = new EEPROM(hid);
		
		return 0;
	}
	
	int CheckConnected() {
		System.out.println("CheckConnected");

		
		byte[] buff = new byte[65];
					
		buff[1] = (byte) 0x36;

		int n=hid.IntSendOutputReport(buff, 65);
						
		ByteBuffer bb = ByteBuffer.allocate(65);		
		hid.IntReadInputReport(bb, 65);
		
		System.out.println(""+bb.array()[1]);
		if(bb.array()[1]!=0x36) return -1;
			
		int connected = bb.array()[2];
		
		System.out.println("Connected response: "+connected);
		
		return connected;
	}
	
	public int GetMemorySize() {
		System.out.println("GetMemorySize");
		
		byte[] buff = new byte[65];
					
		buff[1] = (byte) 0x30;

		int n=hid.IntSendOutputReport(buff, 65);
						
		ByteBuffer bb = ByteBuffer.allocate(65);		
		hid.IntReadInputReport(bb, 65);
		
		if(bb.array()[1]!=0x30) System.exit(0);
			
		int s = bb.array()[2];
		
		System.out.println(""+s);
		
		return s * 2 * 65536;
	}
	
	void SetConnected() {
		System.out.println("SetConnected");
	
		byte[] buff = new byte[65];
					
		buff[1] = (byte) 0x34;

		int n=hid.IntSendOutputReport(buff, 65);
						
		ByteBuffer bb = ByteBuffer.allocate(65);		
		hid.IntReadInputReport(bb, 65);
			
		return ;				
		
	}    
    
	void ResetConnected() {
		System.out.println("ResetConnected");

		byte[] buff = new byte[65];
					
		buff[1] = (byte) 0x35;

		int n=hid.IntSendOutputReport(buff, 65);
						
		ByteBuffer bb = ByteBuffer.allocate(65);		
		hid.IntReadInputReport(bb, 65);
			
		return ;				
		
	}    
	
	
	public String ReadVersion() {
		System.out.println("ReadVersion");

		byte[] buff = new byte[65];
		
		buff[1] = (byte) 0x38;


		int n=rom.hid.IntSendOutputReport(buff, 65);
				
		ByteBuffer bb = ByteBuffer.allocate(65);
		rom.hid.IntReadInputReport(bb, 65);
		
		String version = "";
		
		for(int i=1; i<64; i++) {
           if(bb.array()[i]==0) break;
		   version+= (char) bb.array()[i];
		}
				
		return version;		
	}
	
	public String ReadName() {
		System.out.println("ReadName");

		byte[] buff = new byte[65];
		
		buff[1] = (byte) 0x81;

		int n=rom.hid.IntSendOutputReport(buff, 65);
				
		ByteBuffer bb = ByteBuffer.allocate(65);
		rom.hid.IntReadInputReport(bb, 65);
		
		if(bb.array()[2]!='%') return "WRONG NAME";
		
		String name="";
		for(int i=0; i<16; i++) {
	       byte b = bb.array()[i+3];
	       if(b==0) break;
		   name+= (char)b;
		}
		
		return name;		
	}	
	
	public int WriteName(String name) {
		System.out.println("WriteName");

		byte[] buff = new byte[65];
		
		buff[1] = (byte) 0x80;

		if(name.length()>15) return-1;
		
		buff[2] = '%';
		
		for(int i=0; i<name.length(); i++) {
           buff[i+3] = (byte) name.charAt(i);
		}
		
		//for(int i=0; i<20; i++) {
		//	System.out.println(""+i+" "+buff[i]+""+(char)buff[i]);
		//}
		
		int n=rom.hid.IntSendOutputReport(buff, 65);
				
		ByteBuffer bb = ByteBuffer.allocate(65);
		rom.hid.IntReadInputReport(bb, 65);
						
		return 1;		
	}	
	
	public double ReadGSR() {
		byte[] buff = new byte[65];
		ByteBuffer bb = ByteBuffer.allocate(65);

		buff[1] = (byte) 0x37; // GSR
		hid.IntSendOutputReport(buff, 65);
		hid.IntReadInputReport(bb, 65);
		int b0 = (0xFF & bb.get(1));
				
		int b1 = (0xFF & bb.get(2));
		int b2 = (0xFF & bb.get(3));
		int b3 = (0xFF & bb.get(4));
		int b4 = (0xFF & bb.get(5));
		//double gsr = (b2*256 + b1)/4.0;
		double gsr = (b2*256 + b1);
		
		return gsr;
	}
	
	public class pulse {
		public int bpm;
		public int avg_pulse;
		public int avg_pulse_fast;
		public int pp;
	}
	
	public pulse ReadPulse() {
		byte[] buff = new byte[65];
		ByteBuffer bb = ByteBuffer.allocate(65);
		int b0;		
		
		do{
			
			buff[1] = (byte) 0x42; // PULSE
			
			hid.IntSendOutputReport(buff, 65);
			hid.IntReadInputReport(bb, 65);
						
						
			b0 = (0xFF & bb.get(1));	
			if(b0!=0x42) System.out.println("ReadPulse error");		 
		} while (b0 != 0x42);
		
		int b1 = (0xFF & bb.get(2));
		int b2 = (0xFF & bb.get(3));

		int b3 = (0xFF & bb.get(4));
		int b4 = (0xFF & bb.get(5));
        
		int b5 = (0xFF & bb.get(6));
		int b6 = (0xFF & bb.get(7));

		int b7 = (0xFF & bb.get(8));
		int b8 = (0xFF & bb.get(9));

		pulse p = new pulse();
		
		p.bpm = b2*256 + b1;
		p.avg_pulse = b4*256 + b3;            			
		p.avg_pulse_fast = b6*256 + b5;
		p.pp = b8*256 + b7;
		
		return p;
	}
	
	public void AddMarker() {
		System.out.println("AddMarker");

		byte[] buff = new byte[65];
		ByteBuffer bb = ByteBuffer.allocate(65);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		buff[1] = 0x62;
				
		hid.IntSendOutputReport(buff, 65);
		hid.IntReadInputReport(bb, 65);

	}
	
	public void StartMeas() {
		System.out.println("StartMeas");

		byte[] buff = new byte[65];
		ByteBuffer bb = ByteBuffer.allocate(65);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		buff[1] = 0x63;
				
		hid.IntSendOutputReport(buff, 65);
		hid.IntReadInputReport(bb, 65);

	}
	
	public void WriteTS() {
		System.out.println("WriteTS");

		byte[] buff = new byte[65];
		ByteBuffer bb = ByteBuffer.allocate(65);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		buff[1] = 0x60;
		
		long ts=0;
		SntpClient ntp = new SntpClient();
		try {
			ntp.Connect();
			ts = ntp.GetNtp();
		} catch (SocketException e) {
			JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bb.putLong(ts);
		bb.rewind();
		
		for(int i=0; i<8; i++) {
			buff[i+2] = bb.get(i);
			//System.out.println("B "+buff[i+2]);
		}
		
		hid.IntSendOutputReport(buff, 65);
		hid.IntReadInputReport(bb, 65);

		System.out.println("WriteTS finished "+ts);

	}

	public long ReadTS() {
		System.out.println("ReadTS");

		byte[] buff = new byte[65];
		ByteBuffer bb = ByteBuffer.allocate(65);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		buff[1] = 0x61;
		
		hid.IntSendOutputReport(buff, 65);
		hid.IntReadInputReport(bb, 65);


		bb.get();
		bb.get();
		long ts = bb.getLong();
		int tsmsec = bb.getInt();
		int msec = bb.getInt();
		
		//int c0 = bb.get();
		//int c1 = bb.get();
		//int c2 = bb.get();
		//int c3 = bb.get();
		
		//System.out.println(""+ts+" "+tsmsec+" "+msec);
		
		long t = ts + msec - tsmsec;
		return t;
	}
	
	public long ReadMsec() {
		System.out.println("ReadMsec");

		byte[] buff = new byte[65];
		ByteBuffer bb = ByteBuffer.allocate(65);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		buff[1] = 0x50;
		
		hid.IntSendOutputReport(buff, 65);
		hid.IntReadInputReport(bb, 65);

		bb.get();
		bb.get();
		int msec = bb.getInt();
		
		return msec;
	}
	
	public void Disconnect() {
		System.out.println("Disconnect");
	    ResetConnected();

	    System.out.println("Close hid");
	    hid.CloseHIDDevice();
	}
}
