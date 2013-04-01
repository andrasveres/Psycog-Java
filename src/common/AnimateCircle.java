package common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Timer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import other.AnimateCircleTimerTask;

public class AnimateCircle extends JPanel implements MouseListener, MouseMotionListener {
	
	public int x = 50;
    public int y = 150;
    public int size = 100;

    public int mx;
    public int my;
    
    public XYSeries series;
    
	AnimateCircle() {
		// System.out.println("Haho");
		
		JFrame frame = new JFrame("Cicateszt");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(this);
		frame.pack();
		
		frame.setVisible(true);
		
		Graphics g = getGraphics();

		g.setColor(Color.RED);
		
		//g.drawString("This is my custom Panel!",10,20);
		 

		// g.fillOval(200, 500, 20, 20);
		
		// panel.paint(g);
		
		AnimateCircleTimerTask animate = new AnimateCircleTimerTask(this);
		Timer timer = new Timer();		
		timer.schedule(animate, 0, 10);
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
        series = new XYSeries("XYGraph");
	        //         Add the series to your data set
	    XYSeriesCollection dataset = new XYSeriesCollection();
	    dataset.addSeries(series);
	    
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
	        
        JFrame chart_frame = new JFrame();
	        
        chart_frame.add(p);
        chart_frame.setVisible(true);
        chart_frame.pack();
	}
		
	public void moveCircle(int xx, int yy) {
        int OFFSET = 10;
        if ((x!=xx) || (x!=yy)) {
            repaint(x-1,y-1,size+OFFSET,size+OFFSET);
            x=xx;
            y=yy;
            repaint(x-1,y-1,size+OFFSET,size+OFFSET);
        } 
    }

	public Dimension getPreferredSize() {
	    return new Dimension(800,600);
	}
	    
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);       
	    g.setColor(Color.RED);
	    g.fillOval(x,y,size,size);

	    g.setColor(Color.BLUE);
	    g.fillOval(x+size/2-5,y+size/2-5,10,10);

	    // g.setColor(Color.BLACK);
	    // g.drawRect(x,y,size,size);
	}  
	
	
	public static void main(String [] args) {
		new AnimateCircle();		
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
       	System.out.println("Clicked");
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

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
		mx = e.getX();
		my = e.getY();
	}
}
