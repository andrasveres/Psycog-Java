package common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class taganim extends JPanel implements MouseMotionListener, MouseListener, ActionListener {
	    	

	Vector<Rectangle> tags = new Vector<Rectangle>();
    Random rnd = new Random();

	int x=400;
	int y=300;
	
	Timer t = new Timer(1, this);
	
	taganim() {
		// 
		JFrame frame = new JFrame("Cicateszt");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(this);
		frame.pack();
		
		frame.setVisible(true);
		
		Graphics g = getGraphics();

		g.setColor(Color.RED);
		
				
		this.addMouseMotionListener(this);

		this.addMouseListener(this);
		
		int i;
		for(i=0; i<100; i++) {
			
			int f=0;
			while(f==0) {			
				
				int x = (int)(rnd.nextDouble()*800);
				int y = (int)(rnd.nextDouble()*600);
				int w = (int)(rnd.nextDouble()*30)+1;
				int h = (int)(rnd.nextDouble()*30)+1;
				Rectangle r = new Rectangle(x,y, w, h);
				
				int j;
		    	for(j=0; j<tags.size(); j++) {		    	
		    		if(r.intersects(tags.get(j))) break;
		    	}
		    	
		    	if(j==tags.size()) {
		    		f=1;
		    		tags.add(r);
		    	}
		    			
			}		
		}

		t.start();
	}
		
	public Dimension getPreferredSize() {
	    return new Dimension(800,600);
	}
	    
	void move() {
		
	    for(int i=0; i<tags.size(); i++) {
		// for(int i=0; i<2; i++) {
			
	    	Rectangle r = tags.get(i);
	    	
	    	double dx = x- r.getCenterX() ;
	    	double dy = y-r.getCenterY() ;
	    	
	    	if(Math.abs(dx)<1) dx=0;
	    	if(Math.abs(dy)<1) dy=0;
	    	
	    	dx = Math.signum(dx);
	    	dy = Math.signum(dy);

	    	Rectangle nrx = (Rectangle) r.clone();
	    	nrx.x += (int) dx;

	    	Rectangle nry = (Rectangle) r.clone();
	    	nry.y += (int) dy;

	    	Rectangle nr = (Rectangle) r.clone();
	    	nr.x += (int) dx;
	    	nr.y += (int) dy;
	    	
	    	// check possibility to move
	    	int j;
	    	for(j=0; j<tags.size(); j++) {
	    		if(i==j) continue;
	    		
	    		Rectangle rr=tags.get(j);
	    		
	    		if(!rr.intersects(nr) && dx!=0 && dy!=0) {
	    			continue;
	    		}
	    			
	    		if(!rr.intersects(nrx) && dx!=0) {
	    			nr = nrx;
	    			continue;
	    		} 

	    		if(!rr.intersects(nry) && dy!=0) {
		    		nr = nry;
		    		continue;
	    		}

                break;
	    	}
	    	
	    	if(j==tags.size()) {	    			
	    		r.setLocation(nr.x, nr.y);	    		
	    		// System.out.println("move "+i+" "+r.x+" "+r.y);
	    	}
	    }

		
	}
	
	void grow(Rectangle r) {
		
		System.out.println("Grow "+r);
		
		r.height++;
		r.width++;
		
		// if(true) return;

		Point p = new Point();
		p.x = (int) r.getCenterX();
		p.y = (int) r.getCenterY();

		int i,j;
		int f=1;
		while(f==1) {
			
			f=0;
		
			for(i=0; i<tags.size(); i++) {
				
				Rectangle ri=tags.get(i);

				for(j=0; j<tags.size(); j++) {

					if(i==j) continue;
					
					Rectangle rj=tags.get(j);
				
					Rectangle s = ri.intersection(rj);

					if(s.isEmpty()) continue;
				
					f=1;
				
					// which one to move? The one that is farther

					double di = p.distance(ri.getCenterX(), ri.getCenterY());
					double dj = p.distance(rj.getCenterX(), rj.getCenterY());
					
					if(di>dj) {
				    	double dx = ri.getCenterX() - r.getCenterX() ;
				    	double dy = ri.getCenterY() - r.getCenterY() ;
				    	
				    	if(Math.abs(dx)<1) dx=0;
				    	if(Math.abs(dy)<1) dy=0;
				    	
				    	dx = Math.signum(dx);
				    	dy = Math.signum(dy);
				    	
				    	ri.x+=dx;
				    	ri.y+=dy;
					} else {
				    	double dx = rj.getCenterX() - r.getCenterX() ;
				    	double dy = rj.getCenterY() - r.getCenterY() ;
				    	
				    	if(Math.abs(dx)<1) dx=0;
				    	if(Math.abs(dy)<1) dy=0;
				    	
				    	dx = Math.signum(dx);
				    	dy = Math.signum(dy);
				    	
				    	rj.x+=dx;
				    	rj.y+=dy;					
					}
					
					repaint();
				}
			}
		}
		
	}
	
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);       

	    for(int i=0; i<tags.size(); i++) {
	    	
	    	Rectangle r = tags.get(i);
	    	g.drawRect(r.x, r.y, r.width, r.height);
	    }
	    
	    // g.setColor(Color.BLACK);
	    // g.drawRect(x,y,size,size);
	}  
	
	
	public static void main(String [] args) {
		new taganim();		
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
		Point p = e.getPoint();		
		
		x=p.x;
		y=p.y;
		
		// move();
		// repaint();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		if(arg0.getSource()==t) {
			move();
			repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	
		Point p = arg0.getPoint();
		
		int i;
		for(i=0; i<tags.size(); i++) {
			Rectangle r=tags.get(i);				
			
			if(r.contains(p)) {
				grow(r);
				break;
			}
		}
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
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
