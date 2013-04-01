package StroopTest_Thermo_2runs_20120915;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MovingBall extends JFrame implements MouseMotionListener {

	
	BufferStrategy strategy;
	
	int xmax, ymax;
	
	double x,y; 
	int mx, my;
	
	Random r = new Random();
	
	double alpha = 0.1;
	double dalpha = 0;
	double v= 0.1;
	double maxdalpha = 0.02;
	
	double avge=0;
	
	double start;
	double last;
	double lasterr;
	double lastspeed;
	
	StroopInterface stroopif;
	
	Vector<Integer> dist = new Vector<Integer>(); 
	Vector<Integer> tmeas = new Vector<Integer>(); 
	Vector<Double> speed = new Vector<Double>(); 

	class Move extends TimerTask {
		
		public void run() {
			// System.out.println("ize "+x+" "+y+" "+xmax+" "+ymax);
			double now = System.currentTimeMillis();
			
			
			if(dist.size()>=600*3) {
				setVisible(false);
				stroopif.Ready();
				this.cancel();
				return;
			}
			
			if(now-lastspeed >= 10000) {
				lastspeed = now;
				v *= 1.2;
				
				//setTitle("Speed "+v);
			}
			
		    double e = Math.sqrt((x-mx)*(x-mx)+(y-my)*(y-my));
		    avge = (0.98)*avge + 0.02*e;
		    
			if(now-lasterr >= 100) {
				lasterr += 100;
				dist.add((int)e);
				tmeas.add((int)(now-start));
				speed.add(v);
			}
			
			double dt = now - last;
			last = now;
					
			double p = 0.01;
			dalpha = (1-p)*dalpha + p * (r.nextDouble()-0.5);
			
			if(dalpha > maxdalpha) dalpha = maxdalpha;
			if(dalpha < -maxdalpha) dalpha = -maxdalpha;

			//dalpha = maxdalpha;
			
			if(x>200 && x<xmax-200 && y>200 && y<xmax-200) alpha += dalpha;
			
			double px = Math.cos(alpha)*v;
			double py = -Math.sin(alpha)*v;
			
			x += px*dt;
			y += py*dt;
			
			if(y<100 || y > ymax-100) {
				alpha = -alpha;
				y -= py*dt;
			}
			
			if(x<100 || x > xmax-100) {
				alpha = -Math.PI-alpha;
				x -= px*dt;
			}
				

			Paint(e);
		}
		
	}
	
	MovingBall(StroopInterface stroopif) {
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.stroopif = stroopif;
		
		this.setBackground(Color.BLACK);
				
		setExtendedState(getExtendedState()|JFrame.MAXIMIZED_BOTH);
		setVisible(true);	
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				
		xmax = getWidth();
		ymax = getHeight();
		
		x = xmax/2;
		y = ymax/2;
		
		mx = (int)x;
		my = (int)y;
				
		last = System.currentTimeMillis();
		lasterr = last;
		lastspeed = last;				
		start = last;
		
		Timer timer = new Timer();
		timer.schedule(new Move(), 20, 20);
		
		addMouseMotionListener(this);
	}
	
	void Paint(double e) {
		
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	    //g.clearRect((int)(x-150), (int)(y-150), 250+1, 250+1);
	    g.clearRect(0, 0, xmax, ymax);
	    
	    int h = (int)e*2;
	    if(h>100) h=100;
	    if(h<10) h=10;

	    //g.setColor(Color.GRAY);
	    //g.fillOval((int)(x-100), (int)(y-100), 200+1, 200+1);

	    int hh = (int)avge*2;
	    g.setColor(Color.YELLOW);
	    g.fillOval((int)(x-hh/2), (int)(y-hh/2), hh, hh);
	    
	    //Color c = new Color(0, 1, 1, (float)0.1);
	    //g.setColor(c);
	    //g.fillOval((int)(x-h/2), (int)(y-h/2), h+1, h+1);
	    
	    g.setColor(Color.BLACK);
	    g.fillOval((int)x-3, (int)y-3, 6, 6);
	    
		//g.dispose();
		strategy.show();
	}

	public void addNotify() {
        super.addNotify();
        // Buffer
        createBufferStrategy(2);           
        strategy = getBufferStrategy();
    }

	public static void main(String [] args) {
		MovingBall m = new MovingBall(null);
		m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		
	}

}
