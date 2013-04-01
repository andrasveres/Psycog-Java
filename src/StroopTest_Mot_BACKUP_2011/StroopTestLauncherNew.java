package StroopTest_Mot_BACKUP_2011;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileSystemView;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class StroopTestLauncherNew implements StroopInterface {

	String participant="";

	DecimalFormat df = new DecimalFormat("#.##");
	
	boolean debug = false;
	int n_intrinsic = 4;
	
	String state="";
	
	String[] names = {"PIROS", "KÉK", "ZÖLD", "SÁRGA"};
	Color[] colors = {Color.RED, ChartColor.LIGHT_BLUE, Color.GREEN, Color.YELLOW};	
	
	TestSet practice;
	Vector<TestSet> test_list = new Vector<TestSet>(); 
	
	void RandomizeColors() {
		int i;
		Random r = new Random();
		
		for(i=0; i<1000; i++) {
		   int e1 = r.nextInt(4); 	
		   int e2 = r.nextInt(4);
		   
		   String n = names[e1];
		   Color c = colors[e1];
		   
		   names[e1] = names[e2];
		   colors[e1] = colors[e2];
		   
		   names[e2] = n;
		   colors[e2] = c;
		}				
	}
	
	StroopTestLauncherNew() {
		JFrame frame = new JFrame("Stroop test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// RandomizeColors();
		
		JPanel panel = new JPanel();
				
		frame.add(panel);
		
		while(participant.length()==0) {        
			String s = (String)JOptionPane.showInputDialog(frame, "Résztvevő kódja (pl.: IZ001):\n", "Stroop test", JOptionPane.PLAIN_MESSAGE,
                null, null, null);        
		    if(s!=null) participant = s;
		    else {
		    	System.exit(0);
		    	return;
		    }
        }
	        
        state = "bemelegites";
        int number_of_clicks = 12;
		new StroopClickTestNew(participant, number_of_clicks, names, colors, this);
        		
		JFileChooser chooser = new JFileChooser();
	    FileSystemView view = chooser.getFileSystemView();
	    System.out.println("The default directory is " + view.getDefaultDirectory());		
	    System.out.println("user.dir: " + System.getProperty("user.dir"));	    
	}
	
	public static void main(String [] args) {
		new StroopTestLauncherNew();
	}

	@Override
	public void Ready() {
		// TODO Auto-generated method stub
		
		System.out.println("Ready: "+state);
		
		if(state.equals("bemelegites")) {        
			JOptionPane.showMessageDialog(null, "Következik a Stroop teszt gyakorló része.");
			
			state = "gyakorlas";

	        practice = new TestSet();
	        practice.CreateLearnSet();
	        practice.Randomize();
			if(debug) practice.testset.setSize(2); // TEST

			String ss = "Ebben a feladatban különböző színekkel írt szavakat fogsz látni. Az alsó\n" +
			"négy színből kattints az egérrel arra a színre, amelyikkel a betűk meg vannak\n" +
			"jelenítve. A szavak jelentését próbáld meg figyelmen kívül hagyni. \n \n"+
   	   		"A reakcióidőt mérjük, így a válaszaid legyenek minél gyorsabbak és \n" +
   	   		"pontosabbak. Minden kattintás után nézz a képernyő közepére, itt fog \n" +
   	   		"megjelenni egy plusz jel, majd a következő szó.\n\n"+
   	   		"Most egy rövid gyakorló fázis következik.\n\n" +
   	   		"A feladat addig tart, amíg "+practice.testset.size()+" helyes válasz nem születik.\n" +
   	   		"A feladat végén minden válaszidőt összegzünk, a cél tehát az, hogy a\n" +
   	   		"válaszaid legyenek helyesek és minél gyorsabbak. Minden kattintás után\n" +
   	   		"nézz a képernyő közepére, itt fog megjelenni egy plusz jel, majd a következő szó.";
			JOptionPane.showMessageDialog(null, ss, "Stroop teszt", JOptionPane.PLAIN_MESSAGE);

			StroopKeypadNew st = new StroopKeypadNew(practice, true, names, colors, this); // learn
			st.frame.setTitle("Gyakorlás");
			
			return;
		}
		
		if(state.equals("gyakorlas")) {
			
			ShowResult(practice);
			
			int ret = JOptionPane.showConfirmDialog(null, "Következhet az 1. teszt? (NO = újra gyakorlás)", "", JOptionPane.YES_NO_OPTION);
					
			if(ret==JOptionPane.NO_OPTION) {
				state = "bemelegites";
				Ready();
				return;
			}
			
			state = "test1";
			
	        //String ss = "Ize\n";
	        //JOptionPane.showMessageDialog(null, ss, "Stroop teszt", JOptionPane.PLAIN_MESSAGE);
	        
	        TestSet test = new TestSet();
	        test.CreateLearnSet();
			test.Randomize();
			test_list.add(test);
			if(debug) test.testset.setSize(2); // TEST

			StroopKeypadNew st = new StroopKeypadNew(test, false, names, colors, this);
			st.frame.setTitle("Teszt: "+1);
			return;
		}		
		
		if(state.startsWith("test")) {
			int tnum = test_list.size();
			System.out.println("Finished test "+tnum);
						
			// Show result of finished test
			TestSet set = test_list.get(tnum-1);			

			ShowResult(set);
			
			set.msg = GetOpinion(set.d_all);
			System.out.println(set.msg);
			
			tnum++;

	        TestSet next_test = new TestSet();	        
	        next_test.CreateLearnSet();
			next_test.Randomize();
			if(debug) next_test.testset.setSize(2); // TEST

			// Number of intrinsic motivations
			if(tnum<=n_intrinsic) {
				JOptionPane.showMessageDialog(null, "Következik a(z) "+tnum+". teszt");
				state = "test"+tnum;
				test_list.add(next_test);
				
				double target = GetTarget(set.d_all);
				next_test.target = target;	        

			} else {
				double baseline = GetBaseline();
								
				JOptionPane.showMessageDialog(null, "A következő tesztben, ha jobb az eredmenyed mint "+df.format(baseline/1000.0)+" másodperc, ajándékot kapsz!");
				state = "extrensic";				
				test_list.add(next_test);
			}
									
			JOptionPane.showMessageDialog(null, "Felkészültél?");
			
			StroopKeypadNew st = new StroopKeypadNew(next_test, false, names, colors, this);
			st.frame.setTitle("Teszt: "+tnum);
			
			return;
		}		
		
		if(state.equals("extrensic")) {
			// Show result of finished test
			TestSet set = test_list.get(test_list.size()-1);			

			ShowResult(set);
			double baseline = GetBaseline();

			if(set.d_all < baseline) {
				
				JOptionPane.showMessageDialog(null, "Nyertél!");
				
			} else {
				if(test_list.size() == n_intrinsic+1) {
					
					int ret = JOptionPane.showConfirmDialog(null, "Nem nyertél! Megpróbálod mégegyszer?", "", JOptionPane.YES_NO_OPTION);
					if(ret==JOptionPane.OK_OPTION) {
					
						TestSet next_test = new TestSet();	        
						next_test.CreateLearnSet();
						next_test.Randomize();
						if(debug) next_test.testset.setSize(2); // TEST		
						
						test_list.add(next_test);

						StroopKeypadNew st = new StroopKeypadNew(next_test, false, names, colors, this);
						st.frame.setTitle("Masodik probalkozas");
						
						return;
					}
				}
				
				JOptionPane.showMessageDialog(null, "Nem nyertél!");
			}

			try {
				SaveResult();
				SavePractice();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ShowComplexChart();
			ShowChart();

		}
	}
	
	double GetBaseline() {		
		double best = Double.MAX_VALUE;
		for(int i=0; i<n_intrinsic; i++) {
			if(test_list.get(i).d_all < best) best = test_list.get(i).d_all;
		}
		return best;
	}
	
	double GetTarget(double prev_result) {
		double target = 0;
		do {		
			
			String svallal = JOptionPane.showInputDialog(null, "A válaszidők összege "+df.format(prev_result/1000)+
					" másodperc volt.\nMit gondolsz, a következő próbában milyen válaszidő összeget sikerül majd elérned?" +
					"(Tizedes pontot használj!)", "", JOptionPane.QUESTION_MESSAGE);
			try{
				target = Double.parseDouble(svallal);			
			} catch (NumberFormatException e) {			    
				JOptionPane.showMessageDialog(null, "Hibás érték, kérem adjon meg egy számot.");			    
				target = 0;			
			} catch (NullPointerException e) {
				JOptionPane.showMessageDialog(null, "Hibás érték, kérem adjon meg egy számot.");			    
				target = 0;
			}
		} while(target <=0);

		return target*1000.0;
	}
	
	String GetOpinion(double prev_result) {
		String msg="";
		do {		
			JTextArea textArea = new JTextArea();
			textArea.setEditable(true);
			JScrollPane scrollPane = new JScrollPane(textArea);
			textArea.setLineWrap(true);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setPreferredSize(new Dimension(350, 150));			
			JOptionPane.showMessageDialog(null, scrollPane, "Mit gondolsz miért "+df.format(prev_result/1000)+" másodperc lett az eredményed?", JOptionPane.QUESTION_MESSAGE);
		
			msg = textArea.getText();
			
			if(msg.length()==0) {
				JOptionPane.showMessageDialog(null, "Kérem adjon választ.");
			}
			
		} while(msg.length()==0);

		return msg;
	}
	
	void ShowChart() {
    	System.out.println("Show chart\n");

		JFrame chart_frame = new JFrame();
		// chart_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	    // XYSeriesCollection dataset = new XYSeriesCollection();
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

	    //XYSeries series_resptime = new XYSeries("Reakcióidő");
	    //dataset.addSeries(series_resptime);	    

	    //XYSeries series_target = new XYSeries("Vállalás");
	    //dataset.addSeries(series_target);	    

	    // Only for tests >1
	    dataset.setValue(0, "Vállalás", "teszt-"+(1)); // dummy data
	    for(int i=1; i<n_intrinsic; i++) {
	    	double target = test_list.get(i).target;
	    	dataset.setValue(target/1000.0, "Vállalás", "teszt-"+(i+1));

	    	//series_target.add(i+1, target);
	    }

	    for(int i=0; i<test_list.size(); i++) {
	    	double d_all = test_list.get(i).d_all;
	    	// series_resptime.add(i+1, d_all);
	    	dataset.setValue(d_all/1000.0, "Válaszidő", "teszt-"+(i+1));
	    }
	    
	    JFreeChart chart = ChartFactory.createBarChart
	    ("Stroop eredmény","Tesztek", "Válaszidő [sec]", dataset, 
	     PlotOrientation.VERTICAL, true, true, false);
	    
        //         Generate the graph
	    /*
        JFreeChart chart = ChartFactory.createXYLineChart("Stroop teljesítmény", // Title
	                "Tesztek", // x-axis Label
	                "Válaszidő [ms]", // y-axis Label
	                dataset, // Dataset
	                PlotOrientation.VERTICAL, // Plot Orientation
	                true, // Show Legend
	                true, // Use tooltips
	                false // Configure chart to generate URLs?
	            );
        */
	    
        ChartPanel p = new ChartPanel(chart);
        p.setMinimumSize(new Dimension(300,300));
	        	        
        chart_frame.add(p);
        chart_frame.pack();
        
        chart_frame.setVisible(true);
        
        Date now = new Date();
		SimpleDateFormat formatPattern = new SimpleDateFormat("yyy.MM.dd");
        String nowFormatted = formatPattern.format(now);
        
		File file= new File("chart_"+participant+"_"+nowFormatted+".png");
        try {
			ChartUtilities.saveChartAsPNG(file, chart, 800, 600);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void ShowComplexChart() {
       XYSeriesCollection dataset = new XYSeriesCollection();
       XYSeries series_congruent;
       XYSeries series_incongruent;
       XYSeries series_truefalse;
       series_truefalse = new XYSeries("Hibás");	   	
       series_congruent = new XYSeries("Kongruens ");
       series_incongruent = new XYSeries("Inkongruens");

       int n=0;
       for(int i=0; i<test_list.size(); i++) {
    	   TestSet test = test_list.get(i);

    	   for(int j=0; j<test.testset.size(); j++) {
    		   
    		   TestItem item = test.testset.get(j);
    		   if(item.correct == 1) {
    			   
    			   if(item.col == item.word) series_congruent.add(n, item.delay);
    			   else series_incongruent.add(n, item.delay);    			   
    			   
    		   }
    		   series_truefalse.add(n, item.correct);
    		   
    		   n++;
    	   }
       }
       
       dataset.addSeries(series_truefalse);	    
       dataset.addSeries(series_congruent);
       dataset.addSeries(series_incongruent);

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
		        	        		    
       JFrame frame = new JFrame();	
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       frame.add(p);
		    // chart_frame.setVisible(true);		
       frame.pack();
       
       frame.setVisible(true);
       
       Date now = new Date();	
       SimpleDateFormat formatPattern = new SimpleDateFormat("yyy.MM.dd");
       String nowFormatted = formatPattern.format(now);
       
       File file= new File("chart_"+participant+"_stroop_"+nowFormatted+".png");
       try {
			ChartUtilities.saveChartAsPNG(file, chart, 800, 600);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 

	
	void ShowResult(TestSet test) {
		String str = "Hibás válasz="+test.n_err+"\n" +
		"Összegzett válaszidő="+df.format(test.d_all/1000.0)+" másodperc\n" ;
		
		//str +=
		//	"Kongruens="+(int)set.d_cong+"ms\n" +
		//	"Inkongruens="+(int)set.d_inc+"ms\n" +
		//	"Stroop hatás="+(int)(set.d_inc-set.d_cong)+"ms";
		
		JOptionPane.showMessageDialog(null, str, "Eredmény", JOptionPane.PLAIN_MESSAGE);
	}
	
	void SavePractice() throws IOException {
		
		Date now = new Date();
		SimpleDateFormat formatPattern = new SimpleDateFormat("yyy.MM.dd");
        String nowFormatted = formatPattern.format(now);
		
		String fname = "practice" ;				
		fname +="_"+participant+"_"+nowFormatted;						
		fname += ".txt";
		
		File file= new File(fname);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		
		output.write("Items\t"+practice.testset.size());
		output.write("\r\n");

		output.write("Errors\t"+practice.n_err);
		output.write("\r\n");

		output.write("Total delay (all)\t"+practice.d_all);
		output.write("\r\n");
		
		output.write("Total delay (correct)\t"+practice.d_correct);
		output.write("\r\n");

		output.write("Avg delay (correct)\t"+practice.d_correct_avg);
		output.write("\r\n");

		output.write("Congruent\t"+practice.d_cong_avg);
		output.write("\r\n");

		output.write("Incongruent\t"+practice.d_inc_avg);
		output.write("\r\n");

		output.write("Stroop\t");
		output.write(""+(int)(practice.d_inc_avg - practice.d_cong_avg));
		output.write("\r\n");

		// header for row data		
		output.write("Name\tColor\tCorrect\tDelay");
		output.write("\r\n");

		for(int i=0; i<practice.testset.size(); i++) {
			
			// if(trial.delay < 3000) sd = ""+(int)trial.delay;
			TestItem item = practice.testset.get(i);
					
			String sd = ""+(int)item.delay;						
			String s = names[item.word] + "\t" + names[item.col] + "\t" + item.correct + "\t" + sd;
			output.write(s);
			output.write("\r\n");
			
		} ;
		
		output.close();			
	}
	
	void SaveResult() throws IOException {
				
		Date now = new Date();
		SimpleDateFormat formatPattern = new SimpleDateFormat("yyy.MM.dd");
        String nowFormatted = formatPattern.format(now);
		
		String fname = "stroop" ;				
		fname +="_"+participant+"_"+nowFormatted;						
		fname += ".txt";
		
		File file= new File(fname);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		
		int n=test_list.size();

		String d = "\t\t\t\t\t";

		output.write(fname+"\tBaseline"+d);		
		for(int i=1; i<n_intrinsic; i++) {								
			output.write("Intrinsic"+i);
			if(i<n-1) output.write(d);
		}
		for(int i=1; i<n-n_intrinsic+1; i++) {								
			output.write("Extrinsic"+i);
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");

		output.write("Items\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+test.testset.size());
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");

		output.write("Errors\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+test.n_err);
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");

		output.write("Total delay (all)\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+(int)test.d_all);
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");
		
		output.write("Total delay (correct)\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+(int)test.d_correct);
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");

		output.write("Avg delay (correct)\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+(int)test.d_correct_avg);
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");

		output.write("Congruent\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+(int)test.d_cong_avg);
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");

		output.write("Incongruent\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+(int)test.d_inc_avg);
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");

		output.write("Stroop\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+(int)(test.d_inc_avg - test.d_cong_avg));
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");

		output.write("Target\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+(int)test.target);
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");
		
		output.write("Comment\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			
			String s = test.msg.replaceAll("\t", " ").replaceAll("\n", " ");
			
			output.write(s);
			if(i<n-1) output.write(d);
		}
		output.write("\r\n");

		// header for row data		
		output.write("\t");
		for(int i=0; i<n; i++) {										
			output.write("Name\tColor\tCorrect\tDelay");
			if(i<n-1) output.write("\t\t");
		}
		output.write("\r\n");


		int i=0;
		boolean hasrow = false;
		do {
			output.write("\t");

			hasrow = false;
			
			for(int j=0; j<n; j++) {
				TestSet test = test_list.get(j);
			
				// if(trial.delay < 3000) sd = ""+(int)trial.delay;
				if(i>=test.testset.size()) {
					// exceeds items
					output.write("\t\t\t");
				} else {				
					TestItem item = test.testset.get(i);
					
					hasrow = true;
					
					String sd = ""+(int)item.delay;						
					String s = names[item.word] + "\t" + names[item.col] + "\t" + item.correct + "\t" + sd;
					output.write(s);
				}
				if(j<n-1) output.write("\t\t");
			}				
			output.write("\r\n");
			
			i++;
			
		} while(hasrow);
		
		output.close();	
	}	
}
