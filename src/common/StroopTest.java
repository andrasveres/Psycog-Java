package common;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import other.StroopClickTest;
import other.StroopKeypad;

public class StroopTest implements ActionListener {

	String s1, s2, s3;
	
	StroopTest() {
		JFrame frame = new JFrame("Stroop test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
				
		frame.add(panel);
		
		s1 = "Bemelegítés";
		s2 = "Stroop gyakorlás";
		s3 = "Stroop teszt";
		
		JButton button1 = new JButton(s1);
		JButton button2 = new JButton(s2);
		JButton button3 = new JButton(s3);
		
		button1.setActionCommand(s1);
		button2.setActionCommand(s2);
		button3.setActionCommand(s3);		

		
		panel.add(button1);
		panel.add(button2);
		panel.add(button3);
		
		button1.addActionListener(this);
		button2.addActionListener(this);
		button3.addActionListener(this);
		

		frame.pack();
		
		frame.setVisible(true);
		
		JFileChooser chooser = new JFileChooser();
	    FileSystemView view = chooser.getFileSystemView();

	    System.out.println("The default directory is " + view.getDefaultDirectory());
		
	    System.out.println("user.dir: " + System.getProperty("user.dir"));
	    
	}
	
	public static void main(String [] args) {
		new StroopTest();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		if(arg0.getActionCommand().equals(s1)) {
			new StroopClickTest();
		}

		if(arg0.getActionCommand().equals(s2)) {
			new StroopKeypad(1);
		}
		
		if(arg0.getActionCommand().equals(s3)) {
			new StroopKeypad(2);
		}
		
	}
}
