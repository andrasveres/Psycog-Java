package StroopTest_Mot_BACKUP_2011;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JOptionPane;

import common.StroopMic;

public class TestSet {
	String[] names = {"PIROS", "KÉK", "ZÖLD", "SÁRGA"};

	Random r = new Random();
	
	public Vector<TestItem> testset = new Vector<TestItem>();
	
	public TestSet() {}
	
	double target = 0;
	String msg = new String();
	
	double d_cong=0;
	double n_cong=0;	
	double d_all=0;	
	double d_correct=0;	
	double d_inc=0;
	int n_inc=0;	
	int n_err = 0;
	
	double d_avg=0;
	double d_correct_avg=0;
	double d_cong_avg=0;
	double d_inc_avg=0;
	
	void AddItem(int word, int col) {
		TestItem item = new TestItem();				
		item.word = word;
		item.col = col;
		testset.add(item);
	}
	
	void CalcResult() {
						
		for(int i=0; i<testset.size(); i++) {
			
			// if(trial.delay > 3000) continue;
			
			TestItem item = testset.get(i);
			
			d_all += item.delay;
			
			if(item.correct == 1) {
						
				d_correct += item.delay; 
					
				if(item.col == item.word) {
					d_cong += item.delay;
					n_cong ++;
				} else {
					d_inc += item.delay;
					n_inc ++;					
				}
				
			} else {
				n_err ++;
			}
		}

		d_cong_avg = d_cong / n_cong;
		d_inc_avg = d_inc / n_inc;
		d_avg = d_all / (testset.size());
		d_correct_avg = d_correct / (n_cong + n_inc);
	}
	
	public void CreateLargeSet() {	
		int n = 36+36;		
		int k=0;		
		int i,j;
	
		// congruent
		for(i=0; i<4; i++) {
			for(j=0; j<9; j++) {    				    				
				AddItem(i,i);				
				k++;
			}
		}

		// incongruent
		for(i=0; i<4; i++) {
			for(j=0; j<4; j++) {    
				
				if(i==j) continue;
				
				for(int l=0; l<3; l++) {
					AddItem(i,j);				
					k++;
				}				
			}
		}
		
		if(k!=n) {
			System.out.println("TRIALSET ERROR");
		}

	}
	
	void CreateSmallSet() {
		
		int n = 24;
				
		int k=0;		
		int i,j;
			
		// congruent
		for(i=0; i<4; i++) {
			for(j=0; j<3; j++) {    				    				
				
				AddItem(i,i);				
				k++;
			}
		}

		// incongruent
		for(i=0; i<4; i++) {
			for(j=0; j<4; j++) {    
				
				if(i==j) continue;
				AddItem(i,j);				
				k++;
			}
		}
		
		if(k!=n) {
			System.out.println("TRIALSET ERROR");
		}

	}
	
	public void CreateLearnSet() {
		
		int n = 16;

		int k=0;
		
		int i,j;
	
		// congruent
		for(i=0; i<4; i++) {				
			AddItem(i,i);				
			k++;
		}

		// incongruent
		for(i=0; i<4; i++) {
			for(j=0; j<4; j++) {    
				
				if(i==j) continue;
				AddItem(i,j);				
				k++;
			}
		}
		
		if(k!=n) {
			System.out.println("TRIALSET ERROR");
		}

	}
	
	void AddMore(boolean congruent) {
		System.out.println("AddMore congruent="+congruent);

		int col = r.nextInt(4);
		int word = col;
		
		if(!congruent) {
			do {
				word = r.nextInt(4);
			} while(col==word);
		}
		
		AddItem(word,col);				
	}
	
	public void Randomize() {

		Vector<TestItem> newset = new Vector<TestItem>();
		
		while(!testset.isEmpty()) {
			int i = r.nextInt(testset.size());
			newset.add(testset.get(i));
			testset.remove(i);
		}		
		
		testset = newset;
	}
		
	/*
	public void ReadFile(InputStream stream)  {
		Scanner input = null;
		try {
			input = new Scanner (stream, "UTF-8");
			
		} catch (Exception e) {		
			// TODO Auto-generated catch block		
			JOptionPane.showMessageDialog(null, "Scanner error");		
		}
		
		Vector<TrialNew> vec = new Vector<TrialNew>();
		
		while (input.hasNextLine()) {
			String line = input.nextLine();
			
			System.out.println(line);
			
			String s[] = line.split("\t");
			
			TrialNew tr = new TrialNew();
			
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
		trial = new TrialNew[vec.size()];
		for(int i=0; i<vec.size(); i++) {
			trial[i] = vec.get(i);
		}
	}
	*/
	
	void print() {
		int i;
		System.out.println("--");
		for(i=0; i<testset.size(); i++) {
			System.out.println(i+" word "+names[testset.get(i).word]+" color "+names[testset.get(i).col]);
		}
	}	
	
}

