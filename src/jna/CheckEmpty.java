package jna;

// GSR communicates with GSR 0.1 board


import org.jfree.chart.JFreeChart;

public class CheckEmpty  {

	double ymax = 2000;
	double dx = 60;
	
	JFreeChart chart;
	
	MyHID myHID = new MyHID();
	EEPROM rom = new EEPROM(myHID);
	
	protected void finalize()
	{
		System.out.println("Close HID");
		//myHID.CloseHandles();
	}

	void Scan() {
		int i;
	
		//for(i=0; i<65536*2; i+=64) {
		for(i=0; i<64; i++) {
	    	int b = rom.ReadEEPROMByte(i);
	    	System.out.println(" "+i+" "+b);
		}
	}

	
	CheckEmpty() throws InterruptedException
	{		

		//rom.Connect();
		
		//Scan();
		//System.exit(0);
		
		int i;
		
		byte[] buff = new byte[64];
				
		
	    for(i=0; i<65536*2; i+=64) {
	    	int[] block = rom.ReadEEPROMBlock(i);

			if(i % 10000 == 0) System.out.println("Checking "+i);

	    	for(int j=0; j<64; j++) {
	    		if(block[j]!=0) {
	    			System.out.println("NOT EMPTY "+i+" "+j+" "+block[j]);
	    			//if(i>0) System.exit(0);
	    		}
	    	}
	    	
	    }
				
	    System.out.println("OK");
	}
	
	
	public static void main(String[] args) throws InterruptedException 
	{
		new CheckEmpty();
	}
}

