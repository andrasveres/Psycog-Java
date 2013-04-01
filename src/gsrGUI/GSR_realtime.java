package gsrGUI;

// GSR communicates with GSR 0.1 board



import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jna.GSRDevice;
import jna.GSRDevice.pulse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GSR_realtime implements ActionListener, KeyListener, WindowListener {
	GSRDevice GSR = new GSRDevice();

	String VERSION="PsyGSR 0.2.1f";

	
	//double ymax = 2000;
	double dx = 60;
	
	JFreeChart chart1;
	JFreeChart chart2;
	JFreeChart chart3;
	JFreeChart chart4;

	protected void finalize()
	{
		//ToggleConnected();

		System.out.println("Close HID");
		GSR.Disconnect();
	}
	
	
	double GetS(double D) {
		
		return D / 1000.0;
		/*
	    double vref = 3 * 5.0 / 24.0;
	    double U = (5.0-vref) * D / 1024;
	    double I = (U) / 470000.0;
	    
	    double S = I / vref;
	    
	    //System.out.println("uS="+(S*1e6));
		return S*1e6;
		*/
	}
	
	GSR_realtime() throws InterruptedException
	{		
        // DATASET == GSR
		XYSeriesCollection dataset1 = new XYSeriesCollection();
		
		XYSeries gsr_series = new XYSeries("SC");
		XYSeries hgsr_series = new XYSeries("SC");
	    dataset1.addSeries(hgsr_series);
	    //dataset1.addSeries(gsr_series);

	    
	    // DATASET2 == BPM
		XYSeriesCollection dataset2 = new XYSeriesCollection();
		
		XYSeries bpm_series = new XYSeries("BPM");
		dataset2.addSeries(bpm_series);
		
		// DATASET3 = PULSE
		XYSeriesCollection dataset3 = new XYSeriesCollection();

		XYSeries avg_pulse_fast_series = new XYSeries("pulse signal");
		XYSeries avg_pulse_series = new XYSeries("smoothed pulse");

	    dataset3.addSeries(avg_pulse_fast_series);
	    dataset3.addSeries(avg_pulse_series);

		// DATASET4 = PP
		XYSeriesCollection dataset4 = new XYSeriesCollection();
		XYSeries pp_series = new XYSeries("PP");
		dataset4.addSeries(pp_series);
	    
//      Generate the graph
        
		chart1 = ChartFactory.createScatterPlot("", // Title
	                "time", // x-axis Label
	                "Skin Conductance [uSiemens]", // y-axis Label
	                dataset1, // Dataset
	                PlotOrientation.VERTICAL, // Plot Orientation
	                true, // Show Legend
	                false, // Use tooltips
	                false // Configure chart to generate URLs?
	            );
		
		chart2 = ChartFactory.createScatterPlot("", // Title
                "time", // x-axis Label
                "BPM", // y-axis Label
                dataset2, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                false, // Use tooltips
                false // Configure chart to generate URLs?
            );
		
		chart3 = ChartFactory.createScatterPlot("", // Title
                "time", // x-axis Label
                "Pulse", // y-axis Label
                dataset3, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                false, // Use tooltips
                false // Configure chart to generate URLs?
            );
		
		chart4 = ChartFactory.createScatterPlot("", // Title
                "time", // x-axis Label
                "Inter Beat Interval [ms]", // y-axis Label
                dataset4, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                false, // Use tooltips
                false // Configure chart to generate URLs?
            );
		
		
		//chart1.getXYPlot().getRangeAxis().setAutoRange(false);
		//chart1.getXYPlot().getRangeAxis().setRange(0, ymax);
		// gsr_series.setMaximumItemCount(10000);

		
		ValueAxis domain = chart1.getXYPlot().getDomainAxis();
		chart2.getXYPlot().setDomainAxis(domain);
		chart3.getXYPlot().setDomainAxis(domain);
		chart4.getXYPlot().setDomainAxis(domain);
		
		chart1.getXYPlot().getRangeAxis().setAutoRangeMinimumSize(0.2);
		
		domain.setFixedAutoRange(dx);
		
		XYLineAndShapeRenderer r1 = new XYLineAndShapeRenderer();
		XYLineAndShapeRenderer r2 = new XYLineAndShapeRenderer();
		XYLineAndShapeRenderer r3 = new XYLineAndShapeRenderer();
		XYLineAndShapeRenderer r4 = new XYLineAndShapeRenderer();
		
		
		
		chart1.getXYPlot().setRenderer(0, r1);
		chart2.getXYPlot().setRenderer(0, r2);
		chart3.getXYPlot().setRenderer(0, r3);
		chart4.getXYPlot().setRenderer(0, r4);
		        
		chart1.setRenderingHints( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );
		
        // GSR        
	    //r1.setSeriesShape(0, ShapeUtilities.createUpTriangle(3));
	    r1.setSeriesPaint(0, Color.blue);
	    r1.setSeriesShapesVisible(0, false);
	    
	    Stroke stroke = new BasicStroke(3);
	    r1.setSeriesStroke(0, stroke);
	    
	    r1.setSeriesPaint(1, Color.yellow);
	    r1.setSeriesShapesVisible(1, false);
	    
	    
	    r2.setSeriesPaint(0, Color.magenta);	    
	    r2.setSeriesShapesVisible(0, false);
	    
	    r3.setSeriesShapesVisible(0, false);
	    r3.setSeriesShapesVisible(1, false);
	    
	    r4.setSeriesPaint(0, Color.magenta);
	    r4.setSeriesShapesVisible(0, false);
	    
        ChartPanel p1 = new ChartPanel(chart1);
        ChartPanel p2 = new ChartPanel(chart2);
        ChartPanel p3 = new ChartPanel(chart3);
        ChartPanel p4 = new ChartPanel(chart4);
        
        //p1.setMinimumSize(new Dimension(300,300));        
        //p2.setMinimumSize(new Dimension(300,300));
        //p3.setMinimumSize(new Dimension(300,300));
        //p4.setMinimumSize(new Dimension(300,300));
        
        JFrame chart_frame = new JFrame(VERSION);
        chart_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chart_frame.addWindowListener(this);

        chart_frame.setLayout(new BorderLayout());
        
	        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,2));
        
        panel.setMinimumSize(new Dimension(300,300));
        
        panel.add(p1);
        panel.add(p2);
        panel.add(p3);
        panel.add(p4);
        
        chart_frame.add(panel, BorderLayout.CENTER);
        
        JButton bXScaleInc = new JButton("X zoom in");
        JButton bXScaleDec = new JButton("X zoom out");
        
        bXScaleInc.setActionCommand("xin");
        bXScaleDec.setActionCommand("xout");
        
        bXScaleInc.addActionListener(this);
        bXScaleDec.addActionListener(this);
        
        //chart_frame.addKeyListener(this);
        //p.addKeyListener(this);

        JPanel bpanel = new JPanel();
        chart_frame.add(bpanel, BorderLayout.SOUTH);
       
        bpanel.add(bXScaleInc);
        bpanel.add(bXScaleDec);

        chart_frame.setVisible(true);
        chart_frame.pack();		
				
		byte[] buff = new byte[64];
		
		double alpha = 0.3;
		double f = 0;
				
	    double start = System.currentTimeMillis()/1000.0;
	    
	    int res = GSR.Connect();
		if(res==-1) {
			System.out.println("Connect error");
			JOptionPane.showMessageDialog(null, "Cannot find GSR device. Please connect it first.");
			System.exit(0);
		}
		
	    
		if(res==-2) {
			JOptionPane.showMessageDialog(chart_frame, "Device is already connected to a running program," +
					"\n or another program was not properly disconnected. \n\n Close the other program or switch off and back on device.");
			System.exit(0);
		}
		
	    String version = GSR.ReadVersion();
	    if(version.compareTo(VERSION)!=0) {
			JOptionPane.showMessageDialog(chart_frame, "Version mismatch! \nDevice="+version+" Program="+VERSION);
			System.exit(0);
	    }

	    double avg_gsr = -10;
	    
		int n=0;
		do {
			/*
			buff[1] = (byte) 0x80;
			if (!myHID.IntSendOutputReport(buff))
				System.out.println(" Send failed");		
			else System.out.println(" Send OK");
			*/
			
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		    double dt = System.currentTimeMillis()/1000.0 - start;			

            double gsr = GSR.ReadGSR();
			//int gsr = b2*256 + b1;  
			
			if(avg_gsr == -10) avg_gsr = gsr;
			avg_gsr = 0.5*avg_gsr + 0.5*gsr;
			//avg_gsr = hgsr;
			
			//double S = GetS(gsr);
			//if(S<0) S=0;
			
			//gsr_series.add(dt, S);
			
			double S = GetS(avg_gsr);
			hgsr_series.add(dt, S);


			pulse p = GSR.ReadPulse();
			
			double r = p.avg_pulse_fast;
			//double r = 5.0*b/1024.0;
			//double r = CalcRes(b);
			
			// r = 1/r * 1e3;

			//System.out.println("ret "+b1+" "+b2+" "+b);
			
			if(f==0) f = r;
		    f = (1-alpha)*f + alpha*r;
		    
		    if(p.pp>0) {pp_series.add(dt, p.pp); pp_series.add(dt+0.001, 0);} 
		    else pp_series.add(dt, null);
		    
		    
			bpm_series.add(dt, p.bpm);
			avg_pulse_series.add(dt, p.avg_pulse);
			if(p.avg_pulse_fast==0) p.avg_pulse_fast=-1;
			avg_pulse_fast_series.add(dt, p.avg_pulse_fast);

			n++;
					 
		} while(true);
		
		//System.exit(0);
	}
	
	double CalcRes(int b) {
		// volt
		double u = 5.0 * b / 1024.0 ;
		
		// resistance
		double r1 = 1000; // kohm (top)
		double r2 = 1000; // kohm (bottom) in parallel to sensor
		
		double r = r1 / (5.0 / u - r1/r2 -1);			

		return r;
	}

	public static void main(String[] args) throws InterruptedException 
	{
		new GSR_realtime();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		System.out.println("Command "+arg0.getActionCommand());
		
		if(arg0.getActionCommand().compareTo("xin")==0) {
			dx *= 0.5;
			chart1.getXYPlot().getDomainAxis().setFixedAutoRange(dx);
		}

		if(arg0.getActionCommand().compareTo("xout")==0) {
			dx *= 2;
			chart1.getXYPlot().getDomainAxis().setFixedAutoRange(dx);
		}
	
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Pressed "+arg0.getKeyChar());

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Typed "+arg0.getKeyChar());
	}


	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
	
		GSR.Disconnect();
		System.out.print("Closing");
	}


	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}

