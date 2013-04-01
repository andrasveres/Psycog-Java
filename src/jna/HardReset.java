package jna;

// GSR communicates with GSR 0.1 board



public class HardReset  {
	MyHID myHID = new MyHID();
	
	public HardReset() throws InterruptedException
	{		

		//rom.Connect();
		
		boolean res = myHID.Connect(0x04d8, 0x003f);
		System.out.println("gethandle "+res);
		
		int i;
		
		byte[] buff = new byte[64];
		
		buff[1] = (byte) 0x41; // HARD RESET
		
		myHID.IntSendOutputReport(buff,65);		
				
		myHID.CloseHIDDevice();
	}
	
	

	public static void main(String[] args) throws InterruptedException 
	{
		new HardReset();
	}
}

