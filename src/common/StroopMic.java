package common;

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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFileChooser;
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

import other.Trial;
import other.TrialSet;

import common.PictureNaming.PictureTimer;
import common.PictureNaming.States;

public class StroopMic extends JPanel implements KeyListener {

	JFrame frame;

	int x;
	int y;

	int font_size = 46; // this is the size of the stimulus
	int font_size2 = 30; // size of clickable texts

	int ready = 0;

	double micstart = 0;
	double cyclestart=0;

	String participant;

	int N = 0; // actual trial number

	TrialSet set;

	String order;
	
	enum States {
		GREET, MICTEST, CROSSHAIR, TEXT, DETECT, EMPTY, WAIT, PAUSE, FEEDBACK, END, TEXTWAIT
	}

	States state = States.GREET;

	public static String[] names = { "PIROS", "KÉK", "ZÖLD", "SÁRGA" };
	Color[] colors = { Color.RED, ChartColor.LIGHT_BLUE, Color.GREEN,
			Color.YELLOW };

	MicTimer mictimer = new MicTimer();

	Random r = new Random();

	JFrame chart_frame = new JFrame();

	class MicTimer extends TimerTask {

		// MIC DATA
		byte[] data;
		TargetDataLine line = null;
		float sampleRate = 44100.0F;
		// 8000,11025,16000,22050,44100
		int smpcount = 0;
		double vol = 0;
		double maxvol = 0;

		double gain = 0;

		double last;

		double threshold_time = -1;

		int line_started = 0;
		
		long maxscherr = -1;

		MicTimer() {

			int sampleSizeInBits = 8;
			// 8,16
			int channels = 1;
			// 1,2
			boolean signed = true;
			// true,false
			boolean bigEndian = false;
			// true,false
			AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
					channels, signed, bigEndian);

			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); 
			
			if (!AudioSystem.isLineSupported(info)) {
				// Handle the error ...
				System.out.println("Line not supported!");
				System.exit(0);
			}
			// Obtain and open the line.
			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format, 1000); // buffer size
			} catch (LineUnavailableException ex) {
				// Handle the error ...

				System.out.println("MIC ERROR");
				System.exit(0);
			}

			System.out.println("Actual buffer size " + line.getBufferSize());

			int chunk = line.getBufferSize() / 1;

			data = new byte[chunk];

			// Begin audio capture.
			line.start();

			line_started = 1;

		}

		public synchronized void run() {
			
			long now = System.currentTimeMillis();
			long scherr = now - scheduledExecutionTime();
			if(scherr > maxscherr) {
				maxscherr = scherr;
				System.out.println("************************ "+scherr);
			}
			
			if (line_started == 0) {
				System.out.println("Line not yet started");
				System.exit(0);
			}

			// double latency = line.available() / sampleRate * 1000.0;

			int numBytesRead = line.read(data, 0, data.length);

			vol = 0;

			for (int i = 0; i < numBytesRead; i++) {
				vol += data[i] * data[i];
			}

			vol /= numBytesRead;

			// System.out.println("vol="+vol+" "+(data[0]));

			if (vol > maxvol)
				maxvol = vol;

			if (gain == 0)
				gain = 1.0 / vol / 100.0;

			smpcount++;

		    if (threshold_time == -1 && vol * gain > 1.0 && state == States.TEXT) {
		    // if (vol * gain > 1.0 && now - threshold_time>1000) {
	
				// maxscherr=-1;
				
				threshold_time = System.currentTimeMillis();
				
				System.out.println("T "+(threshold_time-last));
				
				last = threshold_time;
				// threshold_time=-1;
				
				UserResponse();
			}

		    
			if (state != States.TEXT) {
				// System.out.println("vol="+vol+" runavg="+runavg+" latency "+latency);
				smpcount = 0;
				frame.repaint();
			}
			
		}
	}

	class StroopTimer extends TimerTask {
		
		@Override
		public synchronized void run() {
			// TODO Auto-generated method stub
			
			double scherr = System.currentTimeMillis() - scheduledExecutionTime();
			
			if (state == States.DETECT) {
				
				System.out.println("DETECT "+scherr+" "+(System.currentTimeMillis()-cyclestart));
				
				int wait = (int) (r.nextDouble() * 1000.0 + 1000.0);
				if(order.compareTo("gyakorlas")==0) wait = 1000;

				Timer timer = new Timer();
				timer.schedule(new StroopTimer(), wait);

				state = States.WAIT;

				repaint();

				return;
			}

			if (state == States.WAIT) {
				
				System.out.println("WAIT "+scherr+" "+(System.currentTimeMillis()-cyclestart));
				
				Timer timer = new Timer();
				timer.schedule(new StroopTimer(), 700);

				state = States.CROSSHAIR;

				repaint();

				return;
			}

			if (state == States.CROSSHAIR) {
				
				System.out.println("CROSS "+scherr+" "+(System.currentTimeMillis()-cyclestart));
				
				Timer timer = new Timer();
				timer.schedule(new StroopTimer(), 50);

				state = States.EMPTY;

				repaint();

				return;
			}

			if (state == States.EMPTY) {
				// now we ended the empty period, let;s show the text
				// initialize micstart to current time
				
				double now = System.currentTimeMillis(); 
				System.out.println("EMPTY "+" "+(System.currentTimeMillis()-cyclestart));
				
				state = States.TEXT;
				mictimer.threshold_time = -1;

				micstart = now;
				repaint();

				return;
			}

			if (state == States.FEEDBACK) {

				System.out.println("FEEDBACK "+scherr+" "+(System.currentTimeMillis()-cyclestart));
				
				state = States.CROSSHAIR;
				repaint();

				return;
			}
			

		}

	}

	XYSeries series_congruent;
	XYSeries series_incongruent;

	StroopMic() {
		// System.out.println("Haho");

		chart_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame = new JFrame("Stroop test with microphone");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setBackground(Color.BLACK);

		frame.add(this);
		frame.pack();

		frame.setExtendedState(frame.getExtendedState()
						| JFrame.MAXIMIZED_BOTH);

		frame.setVisible(true);

		x = frame.getWidth();
		y = frame.getHeight();

		Graphics g = getGraphics();

		g.setColor(Color.RED);

		// g.drawString("This is my custom Panel!",10,20);

		// g.fillOval(200, 500, 20, 20);

		// panel.paint(g);

		frame.addKeyListener(this);

		XYSeriesCollection dataset = new XYSeriesCollection();

		series_congruent = new XYSeries("Kongruens ");
		dataset.addSeries(series_congruent);

		series_incongruent = new XYSeries("Inkongruens");
		dataset.addSeries(series_incongruent);

		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Stroop teljesítmény", // Title
				"Próbák", // x-axis Label
				"Válaszidő [ms]", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);

		ChartPanel p = new ChartPanel(chart);
		p.setMinimumSize(new Dimension(300, 300));

		chart_frame.add(p);
		// chart_frame.setVisible(true);
		chart_frame.pack();

		Timer timer = new Timer();
		timer.schedule(mictimer, 1, 1);

		// Timer timer = new Timer();
		// timer.schedule(new StroopTimer(), 1000);

		String s = (String) JOptionPane.showInputDialog(frame,
				"Résztvevő kódja (pl.: IZ001):\n", "",
				JOptionPane.PLAIN_MESSAGE, null, null, null);

		if (s == null || s.equals("")) {
			System.exit(0);
		}
		System.out.println(s);
		participant = s;

		// choose set
		Object[] possibilities = { "gyakorlas", "sorrend_1", "sorrend_2",
				"sorrend_3" };
		s = (String) JOptionPane.showInputDialog(frame,
				"Válasszon sorrendet:\n", " ",
				JOptionPane.PLAIN_MESSAGE, null, possibilities, "gyakorlas");
		// If a string was returned, say so.
		if (s == null)
			System.exit(0);

		order = s;
		
		set = new TrialSet();

		String resource_name = "/resources/" + s;

		System.out.println("Resource chosen:" + resource_name);

		InputStream stream = StroopMic.class.getResourceAsStream(resource_name);

		if (stream == null)
			JOptionPane.showMessageDialog(null, "Resource not located.");

		set.ReadFile(stream);

		String ss ;
		ss = "Ebben a feladatban különböző színnel írt szavakat fog látni.\n" +
			 "Kérjük, mondja ki hangosan azt a SZÍNT, amelyikkel a betűk meg vannak jelenítve.\n" +
			 "A szavak jelentését próbálja meg figyelmen kívül hagyni." +
			 "\n\n" +
			 "A reakcióidőt mérjük, így a válaszai legyenek minél gyorsabbak és\n" +
			 "pontosabbak. Minden válasz után nézzen a képernyő közepére, itt fog megjelenni\n" +
			 "egy plusz jel, majd a következő szó.";

		JOptionPane.showMessageDialog(frame, ss, "Stroop teszt",
				JOptionPane.PLAIN_MESSAGE);

		// ////////////////////////
		// set.n=10;

		state = States.MICTEST;

		frame.repaint();

	}

	public Dimension getPreferredSize() {
		return new Dimension(x, y);
	}

	protected void paintComponent(Graphics g1) {
		super.paintComponent(g1);

		Graphics2D g = (Graphics2D) g1;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		
		if (state == States.MICTEST) {

			// paint volume

			// maximum volume since last repaint
			double xr = mictimer.maxvol * mictimer.gain;

			xr *= x / 2;
			if (xr > x)
				xr = x;

			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, (int) xr, 10);
			// System.out.println("xr "+xr);

			// reset max volume to current value
			mictimer.maxvol = mictimer.vol;

			// threshold line in the middle
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(x / 2, 0, x / 2, 10);			
		}
		
		g.setColor(Color.LIGHT_GRAY);
		g.drawString("" + N + " vol=" + (int) (mictimer.gain * 1000), 0, 10);
			
		if (state == States.GREET) {
			return;
		}
		
		if (state == States.DETECT) {
			
			int s=10;
			g.setColor(Color.YELLOW);
			g.fillOval(x-s*2, 0, s, s);
			
			return;
		}

		if (state == States.MICTEST) {
			g.setColor(Color.WHITE);
			g.drawString("Mikrofon beállítása. Ha kész, nyomja meg a szóközt!", 100, y / 2);
			return;
		}

		if (state == States.CROSSHAIR) {
			int s = 20;

			g.setColor(Color.WHITE);

			g.drawLine(x / 2 - s, y / 3, x / 2 + s, y / 3);
			g.drawLine(x / 2, y / 3 - s, x / 2, y / 3 + s);

		}

		if (state == States.TEXT) {

			Font font = new Font(null, Font.BOLD, font_size);
			g.setFont(font);

			FontMetrics fm = g.getFontMetrics();

			// int n = (int)(r.nextDouble()*4);
			// int c = (int)(r.nextDouble()*4);

			g.setColor(colors[set.trial[N].col]);
			g.drawString(names[set.trial[N].word], x / 2
					- fm.stringWidth(names[set.trial[N].word]) / 2, y / 3
					+ font_size / 2);

		}

		// g.setColor(Color.DARK_GRAY);
		// g.drawString(String.valueOf(N), 0, (int)(y*0.9));
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

		int key = arg0.getKeyCode();
		System.out.println("KEY " + key);

		if (key == 38) {
			mictimer.gain *= 1.1;
			return;
		}

		if (key == 40) {
			mictimer.gain *= 0.9;
			return;
		}

		if (key == 32 && state == States.MICTEST) {
			Timer timer = new Timer();
			timer.schedule(new StroopTimer(), 700);

			state = States.CROSSHAIR;
			frame.repaint();
			return;
		}

		if (key == 32 && state != States.MICTEST) {
			state = States.MICTEST;
		}

		if (key == '1') {
			chart_frame.setVisible(true);
			return;
		}
	}

	void UserResponse() {

		double delay = mictimer.threshold_time - micstart;
		
		double now = System.currentTimeMillis();
		

		System.out.println("DELAY "+delay+" "+(now-cyclestart)+" N "+N);

		cyclestart = now;
		
		state = States.DETECT;

		repaint();

		Timer timer = new Timer();
		timer.schedule(new StroopTimer(), 500);

		set.trial[N].delay = delay;

		if (set.trial[N].word == set.trial[N].col) {
			series_congruent.add(N, delay);
		} else {
			series_incongruent.add(N, delay);
		}

		N++;

		if (N == set.n) {
			state = States.END;
			repaint();

			try {
				CalcResult();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// frame.setVisible(false);
			chart_frame.setVisible(true);
		}
	}

	void CalcResult() throws IOException {

		Date now = new Date();
		SimpleDateFormat formatPattern = new SimpleDateFormat("yyy.MM.dd");
		String nowFormatted = formatPattern.format(now);

		String fname = "stroopmic";

		fname += "_"+order;

		fname += "_" + participant + "_" + nowFormatted;

		fname += ".txt";

		File file = new File(fname);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));

		double d_cong = 0;
		double n_cong = 0;

		double d_all = 0;

		double d_inc = 0;
		int n_inc = 0;

		for (int i = 0; i < N; i++) {
			Trial trial = set.trial[i];

			d_all += trial.delay;

			if (trial.col == trial.word) {
				d_cong += trial.delay;
				n_cong++;
			} else {
				d_inc += trial.delay;
				n_inc++;
			}

		}

		d_cong /= n_cong;
		d_inc /= n_inc;
		d_all /= n_cong + n_inc;

		String s = "Trials\t" + N + "\r\n";
		output.write(s);

		s = "Order\t" + order + "\r\n";
		output.write(s);

		s = "Mean RT\t" + (int) d_all + "\r\n";
		output.write(s);

		s = "Congruent\t" + (int) d_cong + "\r\n";
		output.write(s);

		s = "Incongruent\t" + (int) d_inc + "\r\n";
		output.write(s);

		s = "Stroop effect\t" + (int) (d_inc - d_cong) + "\r\n";
		output.write(s);

		output.write("\t"+participant+"\r\n");
		
		output.write("Name\tColor\tDelay\r\n");

		for (int i = 0; i < N; i++) {
			Trial trial = set.trial[i];

			s = names[trial.word] + "\t" + names[trial.col] + "\t"
					+ (int) trial.delay + "\r\n";
			output.write(s);
		}

		output.close();

		String str = "Próbák száma=" + N + "\n" + "Átlagos reakcióidő="
				+ (int) (d_all) + "\n";

		// if(RunType==2) {
		str += "Kongruens=" + (int) d_cong + "ms\n" + "Inkongruens="
				+ (int) d_inc + "ms\n" + "Stroop hatás="
				+ (int) (d_inc - d_cong) + "ms";
		// }

		String title = "Stroop teszt";
		JOptionPane.showMessageDialog(frame, str, title,
				JOptionPane.PLAIN_MESSAGE);

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

	public static void main(String[] args) {
		StroopMic s = new StroopMic();
		s.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
