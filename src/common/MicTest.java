package common;

import java.awt.Dimension;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MicTest {
	
	MicTest() {
	    float sampleRate = 8000.0F;
	    //8000,11025,16000,22050,44100
	    int sampleSizeInBits = 8;
	    //8,16
	    int channels = 1;
	    //1,2
	    boolean signed = true;
	    //true,false
	    boolean bigEndian = false;
	    //true,false
	    AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		
		TargetDataLine line=null;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
			// Handle the error ... 

		}
		// Obtain and open the line.
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format, 100); // buffer size
		} catch (LineUnavailableException ex) {
			// Handle the error ...
			
			System.out.println("ERROR");
			System.exit(0);
		}

	    JFrame chart_frame = new JFrame();
	    chart_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        XYSeries series = new XYSeries("XYGraph");
        XYSeries series2 = new XYSeries("XYGraph");
        XYSeries series3 = new XYSeries("XYGraph");
        //         Add the series to your data set
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
    
        //         Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart("XY Chart", // Title
                "x-axis", // x-axis Label
                "y-axis", // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
            );

        ChartPanel p = new ChartPanel(chart);
        p.setMinimumSize(new Dimension(300,300));
        	        
        chart_frame.add(p);
        chart_frame.setVisible(true);
        chart_frame.pack();
		
        System.out.println("Actual buffer size "+line.getBufferSize());
        
		int numBytesRead;
		
		int chunk = line.getBufferSize() / 5;
		
		// chunk = 100;
		
		byte[] data = new byte[chunk];

		// Begin audio capture.
		line.start();

		// Here, stopped is a global boolean set by another thread.
		
		double x=0;
		double a = 0.1;
		
		int n=0;
		
		while (1==1) {
		   // Read the next chunk of data from the TargetDataLine.
		   numBytesRead =  line.read(data, 0, data.length);

		   // System.out.println("numBytesRead "+numBytesRead);
		   
		   double latency = line.available()/sampleRate*1000.0;

		   // System.out.println("buffered bytes "+line.available()+" latency "+latency);
		   
		   double d=0;
		   
		   for(int i=0; i<numBytesRead; i++) {			   
			   
			   // n++;
			   // series.add(n, data[i]);
			   
			   // System.out.print((int)Math.sqrt(x)+" ");
			   
			   d+=data[i]*data[i];
		   }
		   // System.out.println();

		   d/=numBytesRead;
		   
		   x = x*(1-a) + d*a;
		   
		   series.add(n, d);
		   series2.add(n, x);
		   
		   if(d>x*2) {
			   series3.add(n, 1000);
		   } else series3.add(n, 0);
		   
		   n++;

		   if(n>1000) {
			   series.clear();
			   series2.clear();
			   series3.clear();
			   n = 1;
		   }
		   
		}     
/*
		//line.start();
		
		float level;
		
		while(true) {		
			level = line.getLevel();
			System.out.println(level);
		}
*/		
	}

	public static void main(String [] args) {
		new MicTest();		
		
	}

}
