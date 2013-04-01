package StroopTest_GSR_20130223;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jna.GSRDevice;

import ntp.SntpClient;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class StroopKeypadNew extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
		
	JFrame frame;
	
	int x;
	int y;
	
	int font_size = 46;		// this is the size of the stimulus
    int font_size2 = 30;	// size of clickable texts

	int ready = 0;
	
	long delay=0;
	long ts = 0;
	
	int crosshairtime = 4000;
		
	int N=0; // actual trial number

	TestSet test;
	
	GSRDevice GSR=null;
	
	enum States {GREET, CROSSHAIR, TEXT, EMPTY, WAIT, FEEDBACK, END}

	States state=States.GREET;	
	  
	String[] names; // = {"PIROS", "KÉK", "ZÖLD", "SÁRGA"};
	Color[] colors; // = {Color.RED, ChartColor.LIGHT_BLUE, Color.GREEN, Color.YELLOW};
	
	int xx[] = new int[4];
	int yy[] = new int[4];
	int w[] = new int[4];
	int h[] = new int[4];	
	
	//Random r = new Random();
	
    JFrame chart_frame = new JFrame();
    
    class StroopTimer extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if(state==States.WAIT) {			
				Timer timer = new Timer();
				timer.schedule(new StroopTimer(), crosshairtime);
				
				state=States.CROSSHAIR;
								
				repaint();
				
				return;
			}			
			
			if(state==States.CROSSHAIR) {			
				Timer timer = new Timer();
				timer.schedule(new StroopTimer(), 50);
				
				state=States.EMPTY;
								
				repaint();
				
				return;
			}			

			if(state==States.EMPTY) {			

				//Timer timer = new Timer();
				//timer.schedule(new StroopTimer(), 2000);
				
				// delay = System.currentTimeMillis();
				delay = -1;
				
				state=States.TEXT;
				repaint();
												
				return;
			}
			
			//if(state==States.TEXT) {
				
			//	UserResponse(-1);
			//	repaint();
			//	return;				
			//}
			
			if(state == States.FEEDBACK) {
				
				state = States.CROSSHAIR;
				repaint();
			}			
		}    	
    }
    
    XYSeries series_congruent;
    XYSeries series_incongruent;
    XYSeries series_truefalse;
    
    StroopInterface launcher ;
    boolean show_result = false;

	private SntpClient ntp;
	
	double baseline=0;
    
	public StroopKeypadNew(double baseline, SntpClient ntp, TestSet set, boolean show_result, String[] names, Color[] colors, StroopInterface launcher, GSRDevice GSR) {
		
		this.ntp = ntp;
		
		this.baseline = baseline;
		
		this.launcher = launcher;
		this.show_result = show_result;
		
		this.names = names;
		this.colors = colors;
		
		this.GSR = GSR;
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setBackground(Color.BLACK);
		
		frame.add(this);
		frame.pack();
		
		frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
		
		frame.setVisible(true);
		
		x = frame.getWidth();
		y = frame.getHeight();
		
		Graphics g = getGraphics();

		g.setColor(Color.RED);
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		frame.addKeyListener(this);

	    XYSeriesCollection dataset = new XYSeriesCollection();

        series_truefalse = new XYSeries("Hibás");
	    dataset.addSeries(series_truefalse);	    

        series_congruent = new XYSeries("Kongruens ");
	    dataset.addSeries(series_congruent);

        series_incongruent = new XYSeries("Inkongruens");
	    dataset.addSeries(series_incongruent);

        //         Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart("Stroop teljesítmény", // Title
	                "Próbák", // x-axis Label
	                "Válaszidő [ms]", // y-axis Label
	                dataset, // Dataset
	                PlotOrientation.VERTICAL, // Plot Orientation
	                true, // Show Legend
	                true, // Use tooltips
	                false // Configure chart to generate URLs?
	            );

        ChartPanel p = new ChartPanel(chart);
        p.setMinimumSize(new Dimension(300,300));
	        	        
        chart_frame.add(p);
        chart_frame.pack();
        		
		this.test = set;
			
		state=States.CROSSHAIR;
		Timer timer = new Timer();
		timer.schedule(new StroopTimer(), crosshairtime);
		
		try {
			this.test.start = ntp.GetNtp();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
			System.exit(0);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		frame.setTitle(frame.getTitle()+" Ido szinkron OK");
		
		if(GSR!=null) GSR.AddMarker();

		frame.repaint();
	}
		
	public Dimension getPreferredSize() {
	    return new Dimension(x,y);
	}
	    
	protected void paintComponent(Graphics g1) {
	    super.paintComponent(g1);
	    
	    Graphics2D g = (Graphics2D) g1;

	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    if(state==States.GREET) {	    		    
	    	return;
	    }
	    
	    if(state==States.WAIT && show_result) {
	    	int font_size = 46;
	    	
	    	Font font = new Font(null, Font.BOLD, font_size);
	    	g.setFont(font);
	    	
	    	FontMetrics fm = g.getFontMetrics();
	    	
	    	g.setColor(Color.WHITE);
	    	
	    	String str;	    	
	    	if(test.testset.get(N-1).correct==1) {
	    		str = "HELYES";
	    	} else {
	    		str = "Hibás válasz";
	    	}
	    	g.drawString(str, x/2 - fm.stringWidth(str)/2, y/2+font_size/2);

      	    test.CalcResult();

	    	str = "Hibák száma "+test.n_err+" (max=5)";
	    	frame.setTitle(str);
	    	   
	    	if(baseline>0 && test.n_err<=5 && test.d_correct_avg>0) {
	    	   str="";
	    	   
	    	   double status = test.d_correct_avg;
	    	   	    	   
	    	   if(test.testset.get(N-1).correct==1) {
	    	      if(status > baseline) str = "Vigyázz, vesztésre állsz!";
	    	      else str = "Kitűnő, nyerésre állsz!";
	    	   }
	    	   	    	   
	    	   if(str.length()>0) g.drawString(str, x/2 - fm.stringWidth(str)/2, y/2+font_size*2);
	    	   

	    	}
	    }
	    
	    if(state==States.CROSSHAIR) {
	    	int s=20;

		    g.setColor(Color.WHITE);

	    	g.drawLine(x/2-s, y/3, x/2+s, y/3);
	    	g.drawLine(x/2, y/3-s, x/2, y/3+s);	    
	    	
	    }
	    
	    if(state==States.TEXT) {	    		    
	    	
			if(delay==-1) {
				delay = System.currentTimeMillis();
				
				try {
					ts = ntp.GetNtp();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
					System.exit(0);
					e.printStackTrace();
				}
				
				if(GSR!=null) GSR.AddMarker();
			}
	    	
	    	Font font = new Font(null, Font.BOLD, font_size);
	    	g.setFont(font);
	    	
	    	FontMetrics fm = g.getFontMetrics();
	    	
	    	// int n = (int)(r.nextDouble()*4);
	    	// int c = (int)(r.nextDouble()*4);

	    	TestItem item = test.testset.get(N);
	    	
	    	g.setColor(colors[item.col]);
	    	g.drawString(names[item.word], x/2 - fm.stringWidth(names[item.word])/2, y/3+font_size/2);

	    }
	    	    
	    g.setColor(Color.WHITE);
    	Font font2 = new Font(null, Font.BOLD, font_size2);
    	g.setFont(font2);
    	FontMetrics fm = g.getFontMetrics();

    	int s = 30; // space between boxes
	    int ww = (int)(fm.stringWidth("SARGA")*1.1); // box width
	    int hh = font_size2+fm.getDescent(); // box height
	    
	    
	    // new placement
	    xx[0] = x/2 - ww - s/2;
	    xx[1] = x/2 - ww - s/2;
	    xx[2] = x/2 + s/2;
	    xx[3] = x/2 + s/2;
	    
	    yy[0] = y*2/3 ;
	    yy[1] = y*2/3 + hh+s;
	    yy[2] = y*2/3 ;
	    yy[3] = y*2/3 + hh+s;
	    
	    w[0] = w[1] = w[2] = w[3] = ww;
	    h[0] = h[1] = h[2] = h[3] = font_size2+fm.getDescent();
	    
	    for(int i=0; i<4; i++) {
	    	g.drawRect(xx[i], yy[i], w[i], h[i]);
	    	g.drawString(names[i], xx[i] + ww/2 - fm.stringWidth(names[i])/2, yy[i]+font_size2);
	    }    	
	}  
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
       	// System.out.println("Clicked");
   	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
       	
       	int x = arg0.getX();
       	int y = arg0.getY();
       	
       	// check validity
       	for(int i=0; i<4; i++) {
       		if(x>=xx[i] && x<=xx[i]+w[i] && y>=yy[i] && y<=yy[i]+h[i]) {
       			UserResponse(i);
       			return;
       		}
       	}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
       	// System.out.println("mouseDragged");

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

		int key = arg0.getKeyCode();
		// System.out.println("KEY "+key);

		
		if(key=='1') {
		   chart_frame.setVisible(true);
		}
		
		key -= 116;
		if(key<0 || key>3) return; 
		
		UserResponse(key);
		
	}
	
	void UserResponse(int key) {
		
		if(state != States.TEXT) return;
		
		// System.out.println("KEY "+key);
		
		state = States.WAIT;

		delay = System.currentTimeMillis() - delay;
		
		System.out.println("UserResponse "+delay);
							
		repaint();									
		
		int wait = 1000;
		
        Timer timer = new Timer();
		timer.schedule(new StroopTimer(), wait);
		
		TestItem item = test.testset.get(N);
		
		item.delay = delay;
		item.showtime = ts;
		
		boolean congruent = item.word == item.col;
			
		if(key == item.col) {
			item.correct=1;
			
			if(congruent) {
				series_congruent.add(N, delay);										
			} else {
				series_incongruent.add(N, delay);					
			}
			
			series_truefalse.add(N, 0);

		} else {
			item.correct=0;
			series_truefalse.add(N, 100);
			
			// increase the set site with one
			test.AddMore(congruent);				
		}
				
		N++;
		
		if(N==test.testset.size()) {
			state = States.END;
			repaint();
						
			frame.setVisible(false);
			// if(show_result==false) chart_frame.setVisible(true);
			
			test.CalcResult();
			
			launcher.Ready();
		}
	}
	
	
	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("KEY TYPED");
	}
}
