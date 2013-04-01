package jna;

import java.nio.ByteBuffer;


public class CheckVersion {
	
	MyHID hid;
	
	CheckVersion(MyHID h) {
		hid = h;		
		
	}
	
	String ReadVersion() {
		byte[] buff = new byte[64];
		
		buff[1] = (byte) 0x38;


		int n=hid.IntSendOutputReport(buff, 65);
		System.out.println("Send "+n);
				
		ByteBuffer bb = ByteBuffer.allocate(65);
		hid.IntReadInputReport(bb, 65);
		
		String version = "";
		
		for(int i=1; i<64; i++) {
           if(bb.array()[i]==0) break;
		   version+= (char) bb.array()[i];
		}
				
		return version;		
	}
	
	public static void main(String[] args) throws InterruptedException 
	{
		MyHID hid = new MyHID();
		boolean res = hid.Connect(0x04d8, 0x003f);
		System.out.println("gethandle "+res);
		CheckVersion c = new CheckVersion(hid);
		String v = c.ReadVersion();
		System.out.println("Version="+v);

		hid.CloseHIDDevice();

	}
}
