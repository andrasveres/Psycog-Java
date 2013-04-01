package common;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import common.StroopMic.MicTimer;
import common.StroopMic.States;

public class SoundTest implements KeyListener {
	
	MicTimer mictimer = new MicTimer();
	
	JFrame frame = new JFrame();
	JTextArea area = new JTextArea();
	
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

			double latency = line.available() / sampleRate * 1000.0;

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

		    if (vol * gain > 1.0 && now - threshold_time>1000) {
	
				maxscherr=-1;
				
				threshold_time = System.currentTimeMillis();
				
				System.out.println("T "+(threshold_time-last));				
				last = threshold_time;
				// threshold_time=-1;
			}
		    
		    smpcount++;
		    if(smpcount>100) {
		    	// System.out.println("Vol "+vol+" gain "+gain);
		    	smpcount=0;
		    }
		    
		}
	}	
	
	public SoundTest() {
		// TODO Auto-generated constructor stub
		
        Timer timer = new Timer();
		timer.schedule(mictimer, 1, 1);
		
		frame.addKeyListener(this);
		frame.add(area);
		frame.setVisible(true);
		frame.setMinimumSize(new Dimension(600,600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

    public static void main(String[] args) throws LineUnavailableException {
        final AudioFormat af =
            new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, true);
        SourceDataLine line = AudioSystem.getSourceDataLine(af);
        line.open(af, Note.SAMPLE_RATE);
        line.start();
        
        SoundTest test = new SoundTest();
      
        test.play(line, Note.D4, 100000);
        
        line.drain();
        line.close();
    }

    private static void play(SourceDataLine line, Note note, int ms) {
        ms = Math.min(ms, Note.SECONDS * 1000);
        int length = Note.SAMPLE_RATE * ms / 1000;
        int count = line.write(note.data(), 0, length);
    }

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		int key = arg0.getKeyCode();

		if (key == 38) {
			mictimer.gain *= 1.1;
		}

		if (key == 40) {
			mictimer.gain *= 0.9;
		}

		System.out.println("GAIN " + mictimer.gain);

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}

enum Note {

    REST, A4, A4$, B4, C4, C4$, D4, D4$, E4, F4, F4$, G4, G4$, A5;
    public static final int SAMPLE_RATE = 16 * 1024; // ~16KHz
    public static final int SECONDS = 120;
    private byte[] sin = new byte[SECONDS * SAMPLE_RATE];

    Note() {
        int n = this.ordinal();
        if (n > 0) {
            double exp = ((double) n - 1) / 12d;
            double f = 440d * Math.pow(2d, exp);
            
            int s = 0;
            int on = (int) (0.5 * SAMPLE_RATE);
            int off = 4 * SAMPLE_RATE;
            
            for (int i = 0; i < sin.length; i++) {
                double period = (double)SAMPLE_RATE / f;
                double angle = 2.0 * Math.PI * i / period;
                
                if(s<on) {
                   sin[i] = (byte)(Math.sin(angle) * 127f);
                } else {
                   sin[i]=0;                	
                }
                
                if(s>(on+off)) s=0;
                s++;
            }
        }
    }

    public byte[] data() {
        return sin;
    }
}
