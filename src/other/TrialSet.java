package other;

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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


import com.sun.corba.se.spi.orbutil.fsm.Input;
import common.StroopMic;

public class TrialSet {
	
	Random r = new Random();

	public int n ;
	
	public Trial[] trial;
	
	public TrialSet() {
		
	}
	
	public void CreateLargeSet() {
		
		n = 36+36;
		
		trial= new Trial[n];
		
		int k=0;
		
		int i,j;
	
		for(i=0; i<n; i++) {
			trial[i] = new Trial();
		}
		
		// congruent
		for(i=0; i<4; i++) {
			for(j=0; j<9; j++) {    				    				
				
				trial[k].word = i;
				trial[k].col = i;
				
				k++;
			}
		}

		// incongruent
		for(i=0; i<4; i++) {
			for(j=0; j<4; j++) {    
				
				if(i==j) continue;
				
				trial[k].word = i;
				trial[k].col = j;
				
				k++;

				trial[k].word = i;
				trial[k].col = j;
				
				k++;

				trial[k].word = i;
				trial[k].col = j;
				
				k++;

			}
		}
		
		if(k!=n) {
			System.out.println("TRIALSET ERROR");
		}

	}
	
	void CreateSmallSet() {
		
		n = 24;
		
		trial= new Trial[n];
		
		int k=0;
		
		int i,j;
	
		for(i=0; i<n; i++) {
			trial[i] = new Trial();
		}
		
		// congruent
		for(i=0; i<4; i++) {
			for(j=0; j<3; j++) {    				    				
				
				trial[k].word = i;
				trial[k].col = i;
				
				k++;
			}
		}

		// incongruent
		for(i=0; i<4; i++) {
			for(j=0; j<4; j++) {    
				
				if(i==j) continue;
				
				trial[k].word = i;
				trial[k].col = j;
				
				k++;
			}
		}
		
		if(k!=n) {
			System.out.println("TRIALSET ERROR");
		}

	}
	
	public void CreateLearnSet() {
		
		n = 16;
		trial= new Trial[n];

		int k=0;
		
		int i,j;
	
		for(i=0; i<n; i++) {
			trial[i] = new Trial();
		}
		
		// congruent
		for(i=0; i<4; i++) {
				
			trial[k].word = i;
			trial[k].col = i;
				
			k++;
		}

		// incongruent
		for(i=0; i<4; i++) {
			for(j=0; j<4; j++) {    
				
				if(i==j) continue;
				
				trial[k].word = i;
				trial[k].col = j;
				
				k++;
			}
		}
		
		if(k!=n) {
			System.out.println("TRIALSET ERROR");
		}

	}
	
	
	public void Randomize() {
		int m=0;
		int f=0;
		
		Trial tr;
		
		while(f==0) {
			int i=0;
			int j=0;
			
			i++;
			if(i>=n) i=0;
			
			j=i;
			while(j==i) {
				j = (int) (r.nextDouble()*n);
			}
			
			
			// replace
			tr = trial[i];
			trial[i] = trial[j];
			trial[j] = tr;

			trial[i].touched++;
			trial[j].touched++;
			
			m++;
			
			if(m>1000) {    				
				f=Check();
			}
		}
		
		System.out.println("Order "+m);
		// print();
	}
	
	int Check() {
		int i;
		
		// System.out.println("CHECK----------------------");
		
		int ew=1;
		int ec=1;
		
		for(i=1; i<n; i++) {
		
			if(trial[i].word == trial[i-1].word) ew++;
			else ew = 1;
			
			if(trial[i].col == trial[i-1].col) ec++;
			else ec = 1;
			
			// System.out.println("CHECK "+trial[i].word+" "+trial[i].col+" "+ew+" "+ec);
			
			if(ew>=3 || ec>=3) return 0;
		}
		    	
		return 1;    		
	}
	
	public void ReadFile(InputStream stream)  {
		Scanner input = null;
		try {
			input = new Scanner (stream, "UTF-8");
			
		} catch (Exception e) {		
			// TODO Auto-generated catch block		
			JOptionPane.showMessageDialog(null, "Scanner error");		
		}
		
		Vector<Trial> vec = new Vector<Trial>();
		
		while (input.hasNextLine()) {
			String line = input.nextLine();
			
			System.out.println(line);
			
			String s[] = line.split("\t");
			
			Trial tr = new Trial();
			
			for(int i=0; i<4; i++) {
				if(StroopMic.names[i].equals(s[0])) {
					tr.word = i;
					
					System.out.println(""+i);
				}
			
				if(StroopMic.names[i].equals(s[1])) {
					tr.col = i;
				}
			}
			
			vec.add(tr);
				
		}
		
		n = vec.size();
		trial = new Trial[vec.size()];
		for(int i=0; i<vec.size(); i++) {
			trial[i] = vec.get(i);
		}
	}
	
	void print() {
		int i;
		for(i=0; i<n; i++) {
			System.out.println(i+" word "+trial[i].word+" color "+trial[i].col+" touched "+trial[i].touched);
		}
	}	
	
}

