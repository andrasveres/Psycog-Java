package common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ClickCircle extends JPanel implements MouseListener {
		
	int x;
    int y;
    int size = 100;
    int sweet = 30;
    
    int run=1;

    int n;
    
    int n_init = 10;
    
    long last_time;
   
    int last_x, last_y;
    
    Random rnd = new Random();
    
    XYSeries series_back;
    XYSeries series_random;
    
	BufferedWriter out;
    
    void Init() {
    	x = 400;
        y = 300;
        
        n=0;
        
        last_time=0;
        
        series_back.clear();
        series_random.clear();
    }
    
	ClickCircle() {
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
				
		this.addMouseListener(this);
		
        series_back = new XYSeries("jump back");
        series_random = new XYSeries("jump random");

        XYSeriesCollection dataset = new XYSeriesCollection();
	    dataset.addSeries(series_back);
	    dataset.addSeries(series_random);

	    Init();
	    
		try {
			out = new BufferedWriter(new FileWriter("outfilename.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    
        //         Generate the graph
        JFreeChart chart = ChartFactory.createScatterPlot("XY Chart", // Title
	                "distance", // x-axis Label
	                "time", // y-axis Label
	                dataset, // Dataset
	                PlotOrientation.VERTICAL, // Plot Orientation
	                true, // Show Legend
	                false, // Use tooltips
	                false // Configure chart to generate URLs?
	            );

        ChartPanel p = new ChartPanel(chart);
        p.setMinimumSize(new Dimension(300,300));
	        
        JFrame chart_frame = new JFrame();
	        
        chart_frame.add(p);
        chart_frame.setVisible(true);
        chart_frame.pack();
        
	}
		
	void MoveCircle() {

        last_x = x;
        last_y = y;

        int xx, yy;
        
        if(n>n_init || n % 2 == 1) {
        	xx = (int) (rnd.nextDouble() * (800-2*size))+size;
        	yy = (int) (rnd.nextDouble() * (600-2*size))+size;
        } else {
        	xx = 400;
        	yy = 300;
        }
						
        int OFFSET = 10;
        repaint(x-size/2-1,y-size/2-1,size+OFFSET,size+OFFSET);
        x=xx;
        y=yy;
        repaint(x-size/2-1,y-size/2-1,size+OFFSET,size+OFFSET);
        
    }

	public Dimension getPreferredSize() {
	    return new Dimension(800,600);
	}
	    
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);       
	    
    	g.setColor(Color.RED);
	    g.fillOval(x-size/2,y-size/2,size,size);

	    g.setColor(Color.BLUE);
	    g.fillOval(x-sweet/2,y-sweet/2,sweet,sweet);

	    // g.setColor(Color.BLACK);
	    // g.drawRect(x,y,size,size);
	}  
	
	
	public static void main(String [] args) {
		new ClickCircle();		
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
       	int mx = e.getX();
       	int my = e.getY();

		System.out.println("Clicked "+mx+" "+my+" "+x+" "+y);
		
		double dist = Math.sqrt((x-mx)*(x-mx)+(y-my)*(y-my));				
		
		if(dist<=sweet/2) {
			
			long time = System.currentTimeMillis();
			
			if(last_time==0) {
				last_time=time;
				return;
			}
			
			int dt = (int) (time-last_time);
			last_time=time;
						
			if(n>0) {
				// calculate distance between this and last circle

				double circ_dist = Math.sqrt((x-last_x)*(x-last_x)+(y-last_y)*(y-last_y));
				
				if(n % 2 == 0) {
					series_back.add(circ_dist,dt);			
				} else {
					series_random.add(circ_dist,dt);								
				}
			}
			
			if(n==n_init) {
				FinishRun();
			}

			n++;

			System.out.println("Jo hely!");
			MoveCircle();
		}
		
	}

	void FinishRun() {
		try {
			WriteFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(run==3) {
	        try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}        
			System.exit(0);
		}
	
		JOptionPane.showMessageDialog(this, "Pihenes!");
	
		Init();
		
        run++;
	
	}
	
	void WriteFile() throws IOException {
		
		String eol = System.getProperty("line.separator"); 
				
		System.out.println("item count: "+series_back.getItemCount()+" "+series_random.getItemCount());
		
		for(int i=0; i<series_back.getItemCount(); i++) {
			out.write((2*i)+","+series_random.getX(i)+","+series_random.getY(i)+","+run+eol);
			out.write((2*i+1)+","+series_back.getX(i)+","+series_back.getY(i)+","+run+eol);
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
