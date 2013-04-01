package jna;

// GSR communicates with GSR 0.1 board


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jfree.chart.JFreeChart;

public class TestTimer  {
	MyHID myHID = new MyHID();

	double ymax = 2000;
	double dx = 60;
	
	JFreeChart chart;
	
	protected void finalize()
	{
		System.out.println("Close HID");
		myHID.CloseHIDDevice();
	}

	void CheckTime() throws InterruptedException {
		byte[] buff = new byte[64];

		long t_start = 0;
        long tick_start = 0;
		long msec_start=0;
		
		long last_tick=0;
		
		do {
			
			buff[1] = (byte) 0x50; // MSEC
						
			long t = System.currentTimeMillis();
			myHID.IntSendOutputReport(buff, 65);	
						
			ByteBuffer bb = ByteBuffer.allocate(65);		
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
			myHID.IntReadInputReport(bb, 65);
			long tt = System.currentTimeMillis()-t;
			
			bb.get();
			bb.get();
			long msec = bb.getInt();
			long tick = bb.getInt();
			
			//System.out.println("C"+c);
			
			//long msec = b1 + b2*256 + b3*65536 + b4*65536*256;

			if(msec_start==0) {
				msec_start = msec;
				t_start = t;
				tick_start = tick;
				continue;
			}
			
			long dmsec = msec - msec_start;
			long dt = t - t_start;
						
			if(dt>0) {
				System.out.println("MSEC "+dmsec+" "+dt+" "+(dt-dmsec)+" Error(sec) after 1 hour:"+(float)(dt-dmsec)/dt*3600.0+" tt "+tt);
				//System.out.println("TICK "+(tick-tick_start)+" msec "+(tick-tick_start)*256.0/32768.0*1000.0+" tt "+tt);
			}
			
			long s1 = System.currentTimeMillis();
			Thread.sleep(10000);
			long s = System.currentTimeMillis()-s1;
			//System.out.println("sleep "+s);
			
			
		} while(true);
	}
	
	TestTimer() throws InterruptedException
	{		
				
		// ANDRAS
		if (!myHID.Connect(0x04d8, 0x003f)) {
			System.out.println("NOT FOUND HID");
			// System.exit(0);
		}else System.out.println("FOUND HID");

		
		CheckTime();
		
	}
	
	public static void main(String[] args) throws InterruptedException 
	{
		new TestTimer();
	}
}

