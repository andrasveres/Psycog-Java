package StroopTest_Mot_BACKUP_2011;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;


public class ClickTrialSetNew {
	
	Random r = new Random();

	int n = 500;
	
	public int periodic_color=-1;
	public int periodic_start=-1;
	
	public ClickTrialNew[] trial= new ClickTrialNew[n];
	
	public ClickTrialSetNew() {
		
		int i;
		
		periodic_start = (int) (r.nextDouble()*3);
		periodic_color = (int)(r.nextDouble()*4);
		
		System.out.println(periodic_start+" "+periodic_color);
		
		int l=0;
	
		for(i=0; i<n; i++) {
			trial[i] = new ClickTrialNew();					
			
			if(l==periodic_start) {
				trial[i].col = periodic_color;
			} else {		
				
				do {
					trial[i].col = (int)(r.nextDouble()*4);
					
					if(i==0) break;
				} while(trial[i].col == trial[i-1].col || trial[i].col == periodic_color); 
			}			
			
			l++;
			
			if(l==3) l=0;
		}
		
	}			
}

