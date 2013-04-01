package gsrGUI;

// GSR communicates with GSR board


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import jna.GSRDevice;
import jna.HardReset;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;


public class GSR_read implements ActionListener, KeyListener {
	
	String VERSION="PsyGSR 0.2.1f";
	String name="";
	JTextField myTitle = new JTextField();
	
	int memsize;
	
	class Record {	
		long t;
		double gsr;
		int bpm;
		int mark;
		int vref;
		double bat;
		Vector<Integer> pp = new Vector<Integer>();
		public int hlvd=0;
		
	}
	
	class Meas {
		// timestamp
		long ts=0;
		long tsmsec=0;
		long msec=0;
		
		long dt=250;
		Vector<Record> data = new Vector<Record>();
	}
	
	
	Vector<Meas> meas_list = new Vector<Meas>();

	JFrame chart_frame = new JFrame(VERSION);
	
    JProgressBar progress = new JProgressBar(0,0);
	
	JCheckBox c_bat = new JCheckBox("Show battery");
	JCheckBox c_pp = new JCheckBox("Show IBT");
	JCheckBox c_vref = new JCheckBox("Show scale change");
	
	int T_GSR = 1;
	int T_MARK = 2; 
	int T_BAT = 3;
	int T_BPM = 4;
	int T_PP = 5;
	int T_HLVD = 6;
	int T_REF = 7;
	
	int[] mem = new int[memsize];
	
	double ymax = 2000;
	double dx = 60;
	
	int USBSIZE = 64;
	
	JFreeChart chart;
    XYSeriesCollection dataset_gsr = new XYSeriesCollection();
    XYSeriesCollection dataset_bpm = new XYSeriesCollection();
    XYSeriesCollection dataset_bat = new XYSeriesCollection();
    XYSeriesCollection dataset_pp = new XYSeriesCollection();
    
	XYSeries gsr_series = new XYSeries("SC");
	XYSeries bpm_series = new XYSeries("BPM");
	XYSeries mark_series = new XYSeries("Marker");
	XYSeries bat_series = new XYSeries("Battery");
	XYSeries pp_series = new XYSeries("IBT");
    
    ValueAxis axis_bpm = new NumberAxis("BPM");
    ValueAxis axis_bat = new NumberAxis("Battery level");
    ValueAxis axis_pp = new NumberAxis("IBT [ms]");


    GSRDevice GSR = new GSRDevice();    

	double GetS(double D, int REF) {
	    double vref = REF * 5.0 / 24.0;
	    double U = (5.0-vref) * D / 1024;
	    double I = (U) / 470000.0;
	    
	    double S = I / vref;
	    
	    //System.out.println("uS="+(S*1e6));
		return S*1e6;
	}
	
	void SetTitle() {
		String title = (VERSION+" Name='"+name+"' ("+memsize/1024+"kbytes memory)");
		chart_frame.setTitle(title);
		chart_frame.invalidate();
	}
	
	GSR_read()
	{		

		int res = GSR.Connect();
		
		if(res==-1) {
			JOptionPane.showMessageDialog(null, "Cannot find GSR device. Please connect it first.");
			System.exit(0);
		}
		
		if(res==-2) {
			JOptionPane.showMessageDialog(chart_frame, "Device is already connected to a running program," +
					"\n or another program was not properly disconnected. \n\n Close the other program or switch off and back on device.");
			System.exit(0);
		}
		


//      Generate the graph
        
		chart = ChartFactory.createScatterPlot(null, // Title
	                "time [sec]", // x-axis Label
	                "SC [uSiemens]", // y-axis Label
	                dataset_gsr, // Dataset
	                PlotOrientation.VERTICAL, // Plot Orientation
	                true, // Show Legend
	                false, // Use tooltips
	                false // Configure chart to generate URLs?
	            );
		
		//chart.getXYPlot().getRangeAxis(0).setAutoRange(false);
		//chart.getXYPlot().getRangeAxis(0).setRange(0, ymax);
		// gsr_series.setMaximumItemCount(10000);
		
		//final ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
        
		axis_pp.setVisible(false);
		axis_bat.setVisible(false);
		
        chart.getXYPlot().setRangeAxis(1, axis_bpm);
        chart.getXYPlot().setRangeAxis(2, axis_bat);
        chart.getXYPlot().setRangeAxis(3, axis_pp);

        
        final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
        //renderer2.setPlotShapes(true);
        //renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
        
        //XYItemRenderer renderer = chart.getXYPlot().getRenderer();
        
        chart.getXYPlot().setDataset(1, dataset_bpm);
        chart.getXYPlot().mapDatasetToRangeAxis(1, 1);
        
        chart.getXYPlot().setDataset(2, dataset_bat);
        chart.getXYPlot().mapDatasetToRangeAxis(2, 2);

        chart.getXYPlot().setDataset(3, dataset_pp);
        chart.getXYPlot().mapDatasetToRangeAxis(3, 3);

        //chart.getXYPlot().setRenderer(0, renderer2);
        chart.getXYPlot().setRenderer(1, renderer2);
        
        chart.getXYPlot().getRangeAxis().setAutoRangeMinimumSize(0.2);
        
        
		//chart.getXYPlot().getRangeAxis(0).setRange(0, ymax);
        
		//chart.getXYPlot().getDomainAxis().setFixedAutoRange(dx);
		
        ChartPanel p = new ChartPanel(chart);
        p.setMinimumSize(new Dimension(300,300));
	        
        
        chart_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chart_frame.setLayout(new BorderLayout());
	        
        chart_frame.add(p, BorderLayout.CENTER);
        
        JButton b_save = new JButton("Save");
        JButton b_erase = new JButton("Erase memory");
        JButton b_sync = new JButton("Synchronize");
        JButton b_rename = new JButton("Rename");
              
        b_save.setActionCommand("save");
        b_erase.setActionCommand("erase");
        b_sync.setActionCommand("sync");
        b_rename.setActionCommand("rename");

        b_erase.addActionListener(this);
        b_save.addActionListener(this);
        b_sync.addActionListener(this);
        b_rename.addActionListener(this);

        b_save.setEnabled(false);
        b_erase.setEnabled(false);
        b_sync.setEnabled(false);
        b_rename.setEnabled(false);

        JPanel bpanel = new JPanel();
        chart_frame.add(bpanel, BorderLayout.SOUTH);
       
        bpanel.add(b_save);
        bpanel.add(b_erase);
        bpanel.add(b_sync);
        bpanel.add(b_rename);

        bpanel.add(c_bat);
        bpanel.add(c_pp);
        //bpanel.add(c_vref);
    
        c_bat.addActionListener(this);
        c_pp.addActionListener(this);
        c_vref.addActionListener(this);

        chart_frame.setVisible(true);
        chart_frame.pack();		
        
        progress.setIndeterminate(false);
        progress.setStringPainted(true);
        progress.setEnabled(true);    
        
        bpanel.add(progress);
        
        //rom.Connect();
				
		double alpha = 0.3;
		
	    //double start = System.currentTimeMillis()/1000.0;
	    
	    //ReadVersion();

	    int curr_meas=0;
	    
	    long t=0;
	    Record rec = new Record();
	    rec.t = t;
	    
	    Meas m=null;
	    
	    String version = GSR.ReadVersion();
	    if(version.compareTo(VERSION)!=0) {
			JOptionPane.showMessageDialog(chart_frame, "Version mismatch! \nDevice="+version+" Program="+VERSION);
			System.exit(0);
	    }
	   
	    
	    name = GSR.ReadName();
	    System.out.println("name "+name);
	    
	    memsize = GSR.GetMemorySize();
	    
	    progress.setMaximum(memsize);
	    
		//JOptionPane.showMessageDialog(chart_frame, "Device="+version+"" +
		//		"\nMemory="+memsize/1024+"kbyte");
	    
	    SetTitle();
	    
	    int REF=3;
	    int i=0;
	    do {
	    	int[] b = GSR.rom.ReadEEPROMBlock(i);

	    	
	    	int meas = b[0];
	    	//System.out.println(""+i+" meas "+meas);
	    
	    	
	    	if(meas==0) {
	    		System.out.println(""+i+" meas "+meas);
	    		//meas=1;
	    		// continue;
	    		break;
	    	}
	    	
	    	int pos = 2;
	    	
	    	if(curr_meas!=meas) {
	    		
	    		curr_meas = meas;
	    		
	    		m = new Meas();
	    		meas_list.add(m);
	    	
	    		t=0;
	    		rec = new Record();
	    		rec.t=0;
	    		
	        	ByteBuffer bb = ByteBuffer.allocate(65);
	        	bb.order(ByteOrder.LITTLE_ENDIAN);

	        	long ll=0;
	        	
	        	for(int ii=0; ii<8; ii++) {
	        		bb.put((byte)(b[pos+ii]));
	        		//System.out.println("B "+b[j+ii]);
	        	}

	        	pos+=8;
	        	
	        	// set timestamp
	        	m.ts = bb.getLong(0);
	        	
	        	m.tsmsec = b[pos+3]*256*256*256 + b[pos+2] * 256*256 + b[pos+1] * 256 + b[pos];
	        	pos+=4;

	        	m.msec = b[pos+3]*256*256*256 + b[pos+2] * 256*256 + b[pos+1] * 256 + b[pos];
	        	pos+=2;

	        	// j will be increased by additional two in for cycle
	        			           
	           System.out.println("TS "+m.ts+" TSmsec "+m.tsmsec+" msec "+m.msec);	    		
	    			    		
	    	}
	    		    	
	    	int j; 
	    	for(j=pos; j<64; j+=2) {
	    		
	    		int type = (b[j+1] & 0xe0) >>> 5;
		
		        int data = (b[j+1] & 0x1f) * 256 + b[j];
		
		        //System.out.println(""+i+":"+j+"type "+type+" "+b[j+1]);
		        
		        if(type==0) break;
		        
		        if(type == T_GSR) {
		        	rec.gsr = GetS(data/4.0, REF);
		        	
		        	m.data.add(rec);
		        	rec = new Record();
		        	
		        	if(m.dt==0) {
		        		System.out.println("DT=0 ERROR");
		        		System.exit(0);		        		
		        	}
		        	
		        	t+=m.dt;		       
		        	rec.t = t;
		        	
		        } else if(type == T_BPM) rec.bpm = data;  	
		        else if(type == T_BAT) rec.bat = data * 5.0 / 1024.0;		        
		        else if(type == T_MARK) rec.mark = data;
		        else if(type == T_REF) {REF = data; rec.vref = data;}
		        else if(type == T_PP) rec.pp.add(data);  	
		        else if(type == T_HLVD) {
                   //System.out.println("HLVD");
                   rec.hlvd++;
		        }
		        else {
		        	System.out.println("addr "+j+" UNKNOWN TYPE "+type);
		        	System.exit(0);
		        }
	    		
	    	}
	    	
	    	i+=64;
	    	
	        progress.setValue(i);

	    } while(i<memsize);
	    	
	    GSR.Disconnect();
	    	    
	    System.out.println("MEM="+i);
	    	
	    b_save.setEnabled(true);
        b_erase.setEnabled(true);
        b_sync.setEnabled(true);
        b_rename.setEnabled(true);

	    DisplaySeries();
	    

	    if(i==0) JOptionPane.showMessageDialog(chart_frame, "GSR memory is empty (name='"+name+"')");
	    else JOptionPane.showMessageDialog(chart_frame, "Data loaded from GSR device " +
	    		"\nName='"+name+"'" +
	    		"\nMeasurements: " + meas_list.size()+
	    		"\nMemory: "+(int)(i/memsize*1000.0)/10.0+"%");

		
		//System.exit(0);
	}
	
	void DisplaySeries() {
		


        double t=0;
        
		for(int i=0; i<meas_list.size(); i++) {
		
			Meas m = meas_list.get(i);
			
			String date="";
			if(m.ts>0) {
				long start = m.ts + (m.msec-m.tsmsec);
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
		        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault());
		        calendar.setTimeInMillis(start);
		        date = sdf.format(calendar.getTime());
		        
		        System.out.println(date);
			}
			
    		Marker marker = new ValueMarker(t/1000.0);
            marker.setPaint(Color.black);
            marker.setLabel("M="+(i+1)+" ("+date+")");
            marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
            chart.getXYPlot().addDomainMarker(marker);
			
			for(int j=0; j<m.data.size(); j++) {
				
				Record r = m.data.get(j);
			
				t += m.dt;
				
				//if(r.gsr>0) {
			    gsr_series.add(t/1000.0, r.gsr);
				//}
				if(r.bpm>0) bpm_series.add(t/1000.0, r.bpm);
				if(r.mark>0) mark_series.add(t/1000.0, 0);
				//if(r.bat>0) bat_series.add(t/1000.0, r.bat);
				if(r.hlvd>0) bat_series.add(t/1000.0, r.hlvd);
				
				if(r.vref>0) {
					marker = new ValueMarker(t/1000.0);
		            marker.setPaint(Color.yellow);
		            marker.setLabel("scale="+r.vref);
		            marker.setLabelAnchor(RectangleAnchor.CENTER);
		            marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		            chart.getXYPlot().addDomainMarker(marker);
				}
				
				for(int k=0; k<r.pp.size(); k++) pp_series.add(t/1000.0, r.pp.get(k));
			}
		}
			
	    dataset_gsr.addSeries(gsr_series);
	    dataset_gsr.addSeries(mark_series);
	    //dataset_bat.addSeries(bat_series);
	    //dataset_pp.addSeries(pp_series);

	    dataset_bpm.addSeries(bpm_series);
	    
	    XYLineAndShapeRenderer r_gsr = new XYLineAndShapeRenderer();
		XYLineAndShapeRenderer r_bpm = new XYLineAndShapeRenderer();
		XYLineAndShapeRenderer r_bat = new XYLineAndShapeRenderer();
		XYLineAndShapeRenderer r_pp = new XYLineAndShapeRenderer();

		r_gsr.setSeriesShapesVisible(0, false);
		
		r_gsr.setSeriesShapesVisible(1, true);
		r_gsr.setSeriesLinesVisible(1, false);
		
		r_bpm.setSeriesShapesVisible(0, false);
		r_bat.setSeriesShapesVisible(0, false);

		r_pp.setSeriesShapesVisible(1, true);
		r_pp.setSeriesLinesVisible(1, false);

		
		chart.getXYPlot().setRenderer(0, r_gsr);
		chart.getXYPlot().setRenderer(1, r_bpm);
		chart.getXYPlot().setRenderer(2, r_bat);
		chart.getXYPlot().setRenderer(3, r_pp);
		
	    //r_gsr.setSeriesShape(0, ShapeUtilities.createUpTriangle(3));
	    r_gsr.setSeriesPaint(0, Color.red);
	    r_gsr.setSeriesPaint(1, Color.black);
	    r_gsr.setSeriesShape(1, ShapeUtilities.createUpTriangle(3));

	    //r1.set
	    
	    //r_bpm.setSeriesShape(0, ShapeUtilities.createDiamond(3));
	    r_bpm.setSeriesPaint(0, Color.blue);
	    
	    //r_bat.setSeriesShape(0, new Rectangle(3,3));	    
	    r_bat.setSeriesPaint(0, Color.orange);

	    r_pp.setSeriesPaint(0, Color.cyan);
	    r_pp.setSeriesShape(0, ShapeUtilities.createUpTriangle(3));
	}
	
	public static void main(String[] args) 
	{
		new GSR_read();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		System.out.println("Command "+arg0.getActionCommand());
		
		if(arg0.getActionCommand().compareTo("save")==0) Save();
		
		if(arg0.getActionCommand().compareTo("sync")==0) {
			int res = GSR.Connect();
			GSR.WriteTS();
			GSR.Disconnect();
		}
		
		if(arg0.getActionCommand().compareTo("rename")==0) {

			JTextField sp = new JTextField();
			
			int r = JOptionPane.showConfirmDialog(chart_frame, sp, "New name? (old = '"+name+"')", JOptionPane.OK_CANCEL_OPTION);
			
			if(r!=JOptionPane.OK_OPTION) return;
			String s = sp.getText();
			if(s.length()==0) return;
			
			r = JOptionPane.showConfirmDialog(chart_frame, "Are you sure? New name = "+s, "Warning!", JOptionPane.YES_NO_OPTION);
			if(r!=JOptionPane.OK_OPTION) return;

			System.out.println("ff "+r);
			
			int res = GSR.Connect();
			GSR.WriteName(s);
			String name = GSR.ReadName();
			
			if(name.compareTo(s)!=0) JOptionPane.showMessageDialog(chart_frame, "ERROR Setting new name!");
			
			SetTitle();
			
			//GSR.WriteTS();
			GSR.Disconnect();
		}

		
		if(arg0.getActionCommand().compareTo("erase")==0) {
			
			int r = JOptionPane.showConfirmDialog(chart_frame, "Erase memory?", "Warning!", JOptionPane.YES_NO_OPTION);
			
			System.out.println(" "+r);
			
			if(r==1) return;
						
			try {
				new HardReset();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Hard reset command");
						
			SwingWorker worker = new SwingWorker<Void, Void>() {
			    @Override
			    public Void doInBackground() {
			 
			    	chart_frame.setEnabled(false);
			    	progress.setString("Erasing");
			    	progress.setIndeterminate(true);
			    			
			    	try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    	
			    	
			    	while(1==1) {
			    	   
			    	   try {
							Thread.sleep(1000);
						
				    	   } catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						
				    	   }
			    	   
			    	   int res = GSR.Connect();
			    	   if(res==0) break;
			    		   			    		   
			    	}
			    	
			    	progress.setString("Checking");
			    				    		
			    	for(long i=0; i<memsize; i+=64) {
		    			int d = GSR.rom.ReadEEPROMByte(i);
		    			if(d!=0) {
		    				JOptionPane.showMessageDialog(chart_frame, "ERROR! Please restart program and try erasing again!");
		    				System.exit(0);
		    			}
		    		}     
			    		
			    	GSR.Disconnect();

			    	progress.setIndeterminate(false);
		    		progress.setString("Erased");
		    		   
		   			JOptionPane.showMessageDialog(chart_frame, "Erasing OK, program now exits.");
		    		 
		    		System.exit(0);
					return null;

			    	
			    	//chart_frame.setEnabled(true);
			    }

			    @Override
			    public void done() {
			        //Remove the "Loading images" label.
			        
			    	
			    }
			};
			
			worker.execute();
			
			return;
			 
			//JOptionPane.showMessageDialog(chart_frame, "This program will exit now. Do not power GSR device down while formatting is in progress.");

			//System.exit(0);
		}
	
		if(arg0.getSource()==c_bat) {
			if(c_bat.isSelected()) {
				dataset_bat.addSeries(bat_series);				
				
			} else dataset_bat.removeSeries(bat_series);
			
			axis_bat.setVisible(c_bat.isSelected());
		}
		
		if(arg0.getSource()==c_pp) {
			if(c_pp.isSelected()) {
				dataset_pp.addSeries(pp_series);
				
			} else dataset_pp.removeSeries(pp_series);
			
			axis_pp.setVisible(c_pp.isSelected());
		}

	}
	
	
	void Save() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int returnVal = chooser.showOpenDialog(chart_frame);		    
		if(returnVal!= JFileChooser.APPROVE_OPTION) return;
		
		File dir = chooser.getSelectedFile();
		
		//dir.list();
		for(int i=0; i<dir.list().length; i++) if(dir.list()[i].startsWith("gsr_")) {
			JOptionPane.showMessageDialog(chart_frame, "Directory contains gsr log files already!\nPlease empty it first or choose another directory.");
			return;
		}

		for(int i=0; i<meas_list.size(); i++)
			try {
				SaveMeas(dir, i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		JOptionPane.showMessageDialog(chart_frame, "Measurements saved.");
		
	}
	
	void SaveMeas(File dir, int i) throws IOException {
		
		//NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		//format.setMaximumFractionDigits(3);

		String fname = "gsr_"+(i+1)+"_"+name ;				
		fname += ".txt";
			
		File file= new File(dir.getAbsolutePath()+"\\"+fname);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
						
		Meas m = meas_list.get(i);
		
		output.write("Name\t"+name+"\tVersion\t"+VERSION+"\r\n");
								
		output.write("Time\tGSR\tPulse\tMarker\tBattery\tIBT\r\n");
		
		for(int j=0; j<m.data.size(); j++) {
				
			Record r = m.data.get(j);
			
			double g = ((int)(r.gsr*1000.0)/1000.0);
			
			long time = r.t + (m.ts + m.msec - m.tsmsec);
			
			output.write(""+time+"\t");
			output.write(""+(g)+"\t");
			if(r.bpm>0)  output.write(""+r.bpm+"\t");   else output.write("\t");
			if(r.mark>0) output.write(""+r.mark+"\t");  else output.write("\t");
			if(r.bat>0)  output.write(""+r.bat+"\t");   else output.write("\t");
			
			for(int k=0; k<r.pp.size(); k++) {
				output.write(""+r.pp.get(k)+"\t");
				
			}
			
			output.write("\r\n");
		}
			
		output.close();	
		
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

}

