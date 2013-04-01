package other;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import common.PictureNaming;

public class PictureSet {	
	
	public Vector<BufferedImage> pictures = new Vector<BufferedImage>();
	public Vector<String> names = new Vector<String>();
	public Vector<Double> delay = new Vector<Double>();
	
	public Vector<Integer> nev1nevtobb2 = new Vector<Integer>();
	public Vector<Integer> easy1hard2 = new Vector<Integer>();
	
	public String order;
	
	public int n=0;
	
	public PictureSet() {
		
	}	
	
	public void ReadPictureOrder() throws IOException  {
				
		Object[] possibilities = { "Gyakorlas", "1", "2", "3"};
		String s = (String) JOptionPane.showInputDialog(null, "Válasszon sorrendet:\n", " ", 
				JOptionPane.PLAIN_MESSAGE, null, possibilities, "sss");

	// If a string was returned, say so.
	if (s == null)
		System.exit(0);
	
	order = s;

	String resource_name = "/picture_orders/PictureNamingOrder" + s +".txt";

	System.out.println("Resource chosen:" + resource_name);

		InputStream stream = PictureNaming.class.getResourceAsStream(resource_name);

		if (stream == null)
			JOptionPane.showMessageDialog(null, "Resource not located.");

		Scanner input = null;
		try {
			input = new Scanner (stream, "UTF-8");
			
		} catch (Exception e) {		
			// TODO Auto-generated catch block		
			JOptionPane.showMessageDialog(null, "Scanner error");		
		}
		
		while (input.hasNextLine()) {
			String line = input.nextLine();
			
			System.out.println(line);
			
			String[] ss = line.split("\t");
			
			String pic_resource = "/pictures/"+ss[0]+".jpg";
			
			System.out.println("res "+pic_resource);
			
			InputStream picstream = PictureNaming.class.getResourceAsStream(pic_resource);
			
			BufferedImage img = null;
			img = ImageIO.read(picstream);
			
			names.add(ss[0]);
			
			pictures.add(img);
			
			nev1nevtobb2.add(Integer.parseInt(ss[1]));
			
			easy1hard2.add(Integer.parseInt(ss[2]));
			
			n++;
							
		}
		
	}	
}

