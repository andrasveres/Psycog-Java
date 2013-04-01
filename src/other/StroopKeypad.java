package other;

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

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class StroopKeypad extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
		
	JFrame frame;
	
	int x;
	int y;
	
	int font_size = 46;		// this is the size of the stimulus
    int font_size2 = 30;	// size of clickable texts

	int ready = 0;

	int RunType = 0;
	
	double delay=0;
	
	String participant;
	
	int N=0; // actual trial number

	TrialSet set;
	
	enum States {GREET, CROSSHAIR, TEXT, EMPTY, WAIT, FEEDBACK, END}

	States state=States.GREET;	
	  
	String[] names = {"PIROS", "KÉK", "ZÖLD", "SÁRGA"};
	Color[] colors = {Color.RED, ChartColor.LIGHT_BLUE, Color.GREEN, Color.YELLOW};
	

	int xx[] = new int[4];
	int yy[] = new int[4];
	int w[] = new int[4];
	int h[] = new int[4];
	
	
	Random r = new Random();
	
    JFrame chart_frame = new JFrame();


    
	
    class StroopTimer extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if(state==States.WAIT) {			
				Timer timer = new Timer();
				timer.schedule(new StroopTimer(), 700);
				
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

				// Timer timer = new Timer();
				// timer.schedule(new StroopTimer(), 2000);
				
				delay = System.currentTimeMillis();
				
				state=States.TEXT;
				repaint();
												
				return;
			}
			
			if(state == States.FEEDBACK) {
				
				state = States.CROSSHAIR;
				repaint();
			}
			

		}
    	
    }
    
    XYSeries series_congruent;
    XYSeries series_incongruent;
    XYSeries series_truefalse;
    
	public StroopKeypad(int rtype) {
		// System.out.println("Haho");			
		
		RunType = rtype;
		
		// Runtype = 1: learn
		// Runtype = 2: full test
		
		frame = new JFrame();
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setBackground(Color.BLACK);
		
		frame.add(this);
		frame.pack();
		
		frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
		
		frame.setVisible(true);
		
		x = frame.getWidth();
		y = frame.getHeight();
		
		Graphics g = getGraphics();

		g.setColor(Color.RED);
		
		//g.drawString("This is my custom Panel!",10,20);
		 

		// g.fillOval(200, 500, 20, 20);
		
		// panel.paint(g);
				
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
        // chart_frame.setVisible(true);
        chart_frame.pack();


        // Timer timer = new Timer();
		// timer.schedule(new StroopTimer(), 1000);
        
        String s = (String)JOptionPane.showInputDialog(
                frame, "Resztvevo kodja (pl.: IZ001):\n", "",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if(s==null || s.equals("")) {
        	frame.dispose();
        	return;
        }
        System.out.println(s);
        participant = s;
        
        String ss="SS";
        if(RunType==1) {
       	   ss = "Ebben a feladatban különböző színnel írt szavakat fogsz látni. \n" +
       	   		"Az alsó négy színből kattints az egérrel azt a színre, amelyikkel \n" +
       	   		"a betűk meg vannak jelenítve. A szavak jelentését próbáld meg figyelmen \n" +
       	   		"kívül hagyni. \n \n"+
       	   		"A reakcióidőt mérjük, így a válaszaid legyenek minél gyorsabbak és \n" +
       	   		"pontosabbak. Minden kattintás után nézz a képernyő közepére, itt fog \n" +
       	   		"megjelenni egy kereszt, majd a következő szó.\n\n"+
       	   		"Most egy rövid gyakorló fázis következik (kb. 1 perc), aztán megkezdjük \n" +
       	   		"a tesztelést (ez kb. 5 perc).\n";
        } else {
     	   ss = "Felkészültél? Az OK gombbal indítsd a tesztelést.\n";        	
        }
        
        JOptionPane.showMessageDialog(frame, ss, "Stroop teszt", JOptionPane.PLAIN_MESSAGE);
		
		set = new TrialSet();
		if(RunType==1) {
			set.CreateLearnSet();
		} else set.CreateLargeSet();
			
		set.Randomize();
		// set.print();
						
		state=States.CROSSHAIR;
		Timer timer = new Timer();
		timer.schedule(new StroopTimer(), 700);

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
	    
	    if(state==States.WAIT && RunType == 1) {
	    	int font_size = 46;
	    	
	    	Font font = new Font(null, Font.BOLD, font_size);
	    	g.setFont(font);
	    	
	    	FontMetrics fm = g.getFontMetrics();
	    	
	    	// int n = (int)(r.nextDouble()*4);
	    	// int c = (int)(r.nextDouble()*4);

	    	g.setColor(Color.WHITE);
	    	
	    	String str;
	    	
	    	if(set.trial[N-1].correct==1) {
	    		str = "HELYES";
	    	} else {
	    		str = "Hibás válasz";
	    	}
	    	g.drawString(str, x/2 - fm.stringWidth(str)/2, y/2+font_size/2);	    	
	    }
	    
	    if(state==States.CROSSHAIR) {
	    	int s=20;

		    g.setColor(Color.WHITE);

	    	g.drawLine(x/2-s, y/3, x/2+s, y/3);
	    	g.drawLine(x/2, y/3-s, x/2, y/3+s);	    
	    	
	    }
	    
	    if(state==States.TEXT) {	    		    
	    	
	    	Font font = new Font(null, Font.BOLD, font_size);
	    	g.setFont(font);
	    	
	    	FontMetrics fm = g.getFontMetrics();
	    	
	    	// int n = (int)(r.nextDouble()*4);
	    	// int c = (int)(r.nextDouble()*4);

	    	g.setColor(colors[set.trial[N].col]);
	    	g.drawString(names[set.trial[N].word], x/2 - fm.stringWidth(names[set.trial[N].word])/2, y/3+font_size/2);

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
    	
	    /* OLD PLACEMENT
	    int dx = (x - 4*ww - 3*s)/2;
	    
	    int xxx= dx;
	    
	    // System.out.println(xx+" "+ww/2);	    	     
	    	    
	    for(int i=0; i<4; i++) {
	    		    	
	    	g.drawRect(xxx, y*2/3 - font_size2, ww, hh-fm.getDescent());
	    	
	    	xx[i] = xxx;
	    	yy[i] = y*2/3 - font_size2;
	    	w[i] = ww;
	    	h[i] = hh-fm.getDescent();
	    	
	    	g.drawString(names[i], xxx + ww/2 - fm.stringWidth(names[i])/2, y*2/3);
	    	
	    	xxx += ww + s;
	    }
	    */
	    
	    // g.setColor(Color.DARK_GRAY);
	    // g.drawString(String.valueOf(N), 0, (int)(y*0.9));
	}  
	
	/*
	public static void main(String [] args) {
		StroopKeypad stroop = new StroopKeypad(2);
				
	}
	*/

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
       	// System.out.println("Pressed");
       	
       	/*
       	if(state == States.GREET) {
       		
       		state = States.CROSSHAIR;
            
       		Timer timer = new Timer();
    		timer.schedule(new StroopTimer(), 2000);
    		repaint();
    		
       		return;
       	}
       	*/
       	
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
		
		delay = System.currentTimeMillis() - delay;
		
		// System.out.println("DELAY "+delay+" N "+N);
					
		state = States.WAIT;
		

		repaint();									
		
		int wait = (int) (r.nextDouble()*1000.0+1000.0);
		
        Timer timer = new Timer();
		timer.schedule(new StroopTimer(), wait);
		
		set.trial[N].delay = delay;
		
		if(key == set.trial[N].col) {
			set.trial[N].correct=1;
			
			if(set.trial[N].word == set.trial[N].col) {
				series_congruent.add(N, delay);										
			} else {
				series_incongruent.add(N, delay);					
			}
			
			series_truefalse.add(N, 0);

		} else {
			set.trial[N].correct=0;
			series_truefalse.add(N, 100);
		}
		
		N++;
		
		if(N==set.n) {
			state = States.END;
			repaint();
			
			try {
				CalcResult();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			frame.setVisible(false);
			if(RunType==2) chart_frame.setVisible(true);
		}

		/*
		Robot robot=null;				
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		robot.mouseMove(x/2, y*2/3+font_size2+50);
		*/

	}
	
	void CalcResult() throws IOException {
		
		Date now = new Date();
		SimpleDateFormat formatPattern = new SimpleDateFormat("yyy.MM.dd");
        String nowFormatted = formatPattern.format(now);

		
		String fname = "stroop" ;
		
		if(RunType == 1) fname += "gyak";
		else fname += "test";
		
		fname +="_"+participant+"_"+nowFormatted;
						
		fname += ".txt";
		
		File file= new File(fname);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		
		double d_cong=0;
		double n_cong=0;
		
		double d_all=0;
		
		double d_inc=0;
		int n_inc=0;
		
		int n_err = 0;
		
		for(int i=0; i<N; i++) {
			Trial trial = set.trial[i];
			
			// if(trial.delay > 3000) continue;
			
			if(trial.correct == 1) {
						
				d_all += trial.delay; 
					
				if(trial.col == trial.word) {
					d_cong += trial.delay;
					n_cong ++;
				} else {
					d_inc += trial.delay;
					n_inc ++;					
				}
				
			} else {
				n_err ++;
			}
		}

		d_cong /= n_cong;
		d_inc /= n_inc;
		d_all /= n_cong + n_inc;
		
		output.write(fname+"\r\n");
		
		String s = "Trials\t" + N + "\r\n";
		output.write(s);

		s = "Incorrect\t" + n_err + "\r\n";
		output.write(s);

		s = "Mean RT\t" + (int)d_all + "\r\n";
		output.write(s);

		s = "Congruent\t" + (int)d_cong + "\r\n";
		output.write(s);

		s = "Incongruent\t" + (int)d_inc + "\r\n";
		output.write(s);

		s = "Stroop effect\t" + (int)(d_inc-d_cong) + "\r\n";
		output.write(s);

		
		
		output.write("\r\nName\tColor\tCorrect\tDelay\r\n");

		for(int i=0; i<N; i++) {
			Trial trial = set.trial[i];
			
			String sd = "";
			// if(trial.delay < 3000) sd = ""+(int)trial.delay;
			sd = ""+(int)trial.delay;
			
			s = names[trial.word] + "\t" + names[trial.col] + "\t" + trial.correct + "\t" + sd + "\r\n";
			output.write(s);
		}
		
		output.close();

	
		
		String str = "Próbák száma="+N+"\n" +
				"Hibás válasz="+n_err+"\n" +
				"Átlagos reakcióidő="+(int)(d_all)+"\n" ;

		if(RunType==2) {
			str +=
				"Kongruens="+(int)d_cong+"ms\n" +
				"Inkongruens="+(int)d_inc+"ms\n" +
				"Stroop hatás="+(int)(d_inc-d_cong)+"ms";
		}
		
		String title = "Stroop gyakorlás";
		if(RunType==2) title = "Stroop teszt";
        JOptionPane.showMessageDialog(frame, str, title, JOptionPane.PLAIN_MESSAGE);

        
		
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
