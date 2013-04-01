package common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import other.PictureSet;

public class PictureNaming extends JPanel implements KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JFrame frame;

	PictureSet set;
	
	int x;
	int y;

	int font_size = 46; // this is the size of the stimulus
	int font_size2 = 30; // size of clickable texts

	int ready = 0;

	double delay = 0;

	String participant;

	int N = 0; // actual trial number

	enum States {
		GREET, MICTEST, CROSSHAIR, TEXT, DETECT, EMPTY, WAIT, PAUSE, FEEDBACK, END
	}

	States state = States.GREET;

	MicTimer mictimer = new MicTimer();

	Random r = new Random();

	JFrame chart_frame = new JFrame();

	class MicTimer extends TimerTask {

		// MIC DATA
		byte[] data;
		TargetDataLine line = null;
		float sampleRate = 8000.0F;
		// 8000,11025,16000,22050,44100
		int smpcount = 0;
		double vol = 0;
		double maxvol = 0;

		double gain = 0;

		double last;

		double threshold_time = 0;

		int line_started = 0;

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

			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format
																					// is
																					// an
																					// AudioFormat
																					// object
			if (!AudioSystem.isLineSupported(info)) {
				// Handle the error ...
				System.out.println("Line not supported!");
				System.exit(0);
			}
			// Obtain and open the line.
			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format, 100); // buffer size
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
			// System.out.println("MIC");

			if (line_started == 0) {
				System.out.println("Line not yet started");
				System.exit(0);
			}

			// double latency = line.available() / sampleRate * 1000.0;

			int numBytesRead = line.read(data, 0, data.length);

			// System.out.println("latency="+latency);

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

			if (threshold_time == -1 && vol * gain > 1.0) {
				threshold_time = System.currentTimeMillis();
			}

			if (smpcount > 0) {
				// System.out.println("vol="+vol+" runavg="+runavg+" latency "+latency);
				smpcount = 0;
				frame.repaint();

				double t = System.currentTimeMillis();
				// System.out.println("dt "+(t-last));
				last = t;
			}
		}
	}

	class PictureTimer extends TimerTask {

		@Override
		public synchronized void run() {
			// TODO Auto-generated method stub
			
			if (state == States.DETECT) {
				
				int wait = (int) (r.nextDouble() * 1000.0 + 2000.0);

				Timer timer = new Timer();
				timer.schedule(new PictureTimer(), wait);

				state = States.WAIT;

				repaint();

				return;
			}

			if (state == States.WAIT) {
				Timer timer = new Timer();
				timer.schedule(new PictureTimer(), 700);

				state = States.CROSSHAIR;

				repaint();

				return;
			}

			if (state == States.CROSSHAIR) {
				Timer timer = new Timer();
				timer.schedule(new PictureTimer(), 50);

				state = States.EMPTY;

				repaint();

				return;
			}

			if (state == States.EMPTY) {

				delay = System.currentTimeMillis();

				state = States.TEXT;

				Timer timer = new Timer();
				timer.schedule(new PictureTimer(), 1);

				mictimer.threshold_time = -1;

				repaint();

				return;
			}

			if (state == States.FEEDBACK) {

				state = States.CROSSHAIR;
				repaint();

				return;
			}

			if (state == States.TEXT) {

				if (mictimer.threshold_time != -1) {
					System.out.println("Volume " + mictimer.vol + " gain "
							+ mictimer.gain);

					UserResponse();
					return;
				}

				Timer timer = new Timer();
				timer.schedule(new PictureTimer(), 10);
			}

		}

	}

	XYSeries series;

	PictureNaming() throws IOException {
		// System.out.println("Haho");

		chart_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame = new JFrame("Picture naming test with microphone");
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

		frame.addKeyListener(this);

		XYSeriesCollection dataset = new XYSeriesCollection();

		series = new XYSeries("Válaszidő");
		dataset.addSeries(series);

		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Teljesítmény", // Title
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
		timer.schedule(mictimer, 10, 10);

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

		set = new PictureSet();
		set.ReadPictureOrder();		

		String ss ;
		ss = "Ebben a feladatban fekete-fehér képeket fog látni.\n" +
			 "A képek tárgyakat ábrázolnak. Kérjük, nevezze meg\n" +
			 "hangosan a tárgyat amit lát, amilyen gyorsan csak tudja.\n" +
			 "Próbáljon meg egyszavas válaszokat adni, amikor csak lehetséges.\n\n" + 
			 "A reakcióidőt mérjük, így a válaszai legyenek minél gyorsabbak\n" +
			 "és pontosabbak. Minden válasz után nézzen a képernyő közepére,\n" +
			 "itt fog megjelenni egy plusz jel, majd a következő kép.";

		JOptionPane.showMessageDialog(frame, ss, "Képmegnevezés teszt",
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
			g.drawString("" + N + " vol=" + (int) (mictimer.gain * 1000), 0, 10);
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

			g.drawLine(x / 2 - s, y / 2, x / 2 + s, y / 2);
			g.drawLine(x / 2, y / 2 - s, x / 2, y / 2 + s);

		}

		if (state == States.TEXT) {

			Font font = new Font(null, Font.BOLD, font_size);
			g.setFont(font);

			// FontMetrics fm = g.getFontMetrics();

			BufferedImage img = set.pictures.get(N);
			int yy = img.getHeight();
			int xx = img.getWidth();
			
			g.drawImage(set.pictures.get(N), x/2-xx/2, y/2-yy/2, null);					
		}

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
			timer.schedule(new PictureTimer(), 700);

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

		if (state != States.TEXT)
			return;

		delay = mictimer.threshold_time - delay;

		// System.out.println("DELAY "+delay+" N "+N);

		state = States.DETECT;

		repaint();

		Timer timer = new Timer();
		timer.schedule(new PictureTimer(), 500);
		
		set.delay.add(delay);

		series.add(N, delay);
		
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

		String fname = "picture_"+set.order;

		fname += "_" + participant + "_" + nowFormatted;

		fname += ".txt";

		File file = new File(fname);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));

		double d = 0;

		for (int i = 0; i < N; i++) {
			d += set.delay.get(i);
		}

		d /= N;

		String s = "Order\t" + set.order + "\r\n";
		output.write(s);

		s = "Trials\t" + N + "\r\n";
		output.write(s);

		s = "Mean RT\t" + (int) d + "\r\n";
		output.write(s);

		output.write("\t"+participant+"\r\n");
		
		output.write("Picture\tDelay\r\n");

		for (int i = 0; i < N; i++) {
			
			s = set.names.get(i) + "\t" + (set.delay.get(i).intValue()) + 
				"\t"+set.nev1nevtobb2.get(i)+"\t"+set.easy1hard2.get(i)+
				"\r\n";
			output.write(s);
		}

		output.close();

		String str = "Próbák száma=" + N + "\n" + "Átlagos reakcióidő="
				+ (int) (d) + "\n";


		String title = " ";
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

	public static void main(String[] args) throws IOException {
		PictureNaming s = new PictureNaming();		
	}
}
