package StroopTest_GSR_20130223;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileSystemView;

import jna.GSRDevice;

import ntp.SntpClient;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class StroopTestLauncherGSR implements StroopInterface {

	String participant="";

	DecimalFormat df = new DecimalFormat("#.##");
	
	boolean debug = false;
	int n_test = 1;
	
	int num;
	
	boolean motivational=true;
	double baseline=0;
	
	String state="";
	
    GSRDevice GSR=null;
	
	String[] names = {"PIROS", "KÉK", "ZÖLD", "SÁRGA"};
	Color[] colors = {Color.RED, ChartColor.LIGHT_BLUE, Color.GREEN, Color.YELLOW};	
	
	TestSet practice;
	Vector<TestSet> test_list = new Vector<TestSet>(); 
	
	SntpClient ntp = new SntpClient();
	
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
	
	StroopTestLauncherGSR() {
		JFrame frame = new JFrame("Stroop test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// RandomizeColors();
		
		//GSR = new GSRDevice();
		//GSR.Connect();
		//GSR.WriteTS();
		//GSR.StartMeas();
		
		//GSR.Disconnect();
		
		
		JPanel panel = new JPanel();
				
		frame.add(panel);
		
		try {
			ntp.Connect();
		} catch (SocketException e) {
			JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
			System.exit(0);

			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Internet ido nem elerheto!");
			System.exit(0);

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        boolean ok=true;

 	    do { 	    
 		   JCheckBox check_mot = new JCheckBox("M"); 
 		   check_mot.setSelected(true);
 		    
 		   String[] sets_s = {"2", "24", "48", "72"};
 	 	   JComboBox combo_sets = new JComboBox(sets_s);
 	 	   combo_sets.setSelectedIndex(2);

		   JTextField sp = new JTextField();
  	       JComponent[] inputs = new JComponent[] {											
						new JLabel("Hány elemből álljon a teszt?"),
						combo_sets,
						check_mot,
						new JLabel("Résztvevő kódja (pl.: IZ001)"),
						sp};
  	       int ret = JOptionPane.showConfirmDialog(null, inputs, "Stroop GSR beállítások", JOptionPane.PLAIN_MESSAGE);
  	       
  	       if(ret == JOptionPane.CLOSED_OPTION) System.exit(0);
  	       System.out.println(""+ret);
  	       
  	       ok=true;
  	       
  	       num = Integer.parseInt(combo_sets.getSelectedItem().toString());
 		   System.out.println(num);
 		   if(num==0) System.exit(0);
 		   if(num==2) debug=true;
 		   else num/=24;
 		  
 		   participant = sp.getText();
 		   if(participant.length()==0) ok=false;
 		   
 		   motivational = check_mot.isSelected();
  	       
 	    } while(!ok);
 	    
 	    if(motivational) n_test=2;
 	    				
        state = "bemelegites";
        int number_of_clicks = 12;
        if(debug) number_of_clicks = 2;
		new StroopClickTestNew(ntp, participant, number_of_clicks, names, colors, this);
        		
		JFileChooser chooser = new JFileChooser();
	    FileSystemView view = chooser.getFileSystemView();
	    System.out.println("The default directory is " + view.getDefaultDirectory());		
	    System.out.println("user.dir: " + System.getProperty("user.dir"));	    
	}
	
	public static void main(String [] args) {
		new StroopTestLauncherGSR();
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
   	   		"Válaszaid legyenek helyesek és minél gyorsabbak. Hibáid számát a bal felső sarokban láthatod.\n" +
   	   		"Minden kattintás után nézz a képernyő közepére, itt fog megjelenni egy plusz jel, \n" +
   	   		"majd a következő szó." +
   	   		"\n\nIndulhat a mérés?";
			JOptionPane.showMessageDialog(null, ss, "Stroop teszt", JOptionPane.PLAIN_MESSAGE);

			StroopKeypadNew st = new StroopKeypadNew(0, ntp, practice, true, names, colors, this, GSR); // learn
			st.frame.setTitle("Gyakorlás");
			
			return;
		}
		
		if(state.equals("gyakorlas")) {
			
			ShowResult(practice);
			
			int ret = JOptionPane.showConfirmDialog(null, "Következik a Stroop feladat, ami kb. 5-10 percig tart. \n\nIndulhat a mérés? \n\n(NO = újra gyakorlás)", "", JOptionPane.YES_NO_OPTION);
					
			if(ret==JOptionPane.NO_OPTION) {
				state = "bemelegites";
				Ready();
				return;
			}
			
			state = "test1";
			
	        //String ss = "Ize\n";
	        //JOptionPane.showMessageDialog(null, ss, "Stroop teszt", JOptionPane.PLAIN_MESSAGE);
	        
	        TestSet test = new TestSet();
	        test.CreateSet(num);
	        
			System.out.println("Size "+test.testset.size());
	        
			test.Randomize();
			
			System.out.println("Size2 "+test.testset.size());
			
			test_list.add(test);
			if(debug) test.testset.setSize(2); // TEST

			StroopKeypadNew st = new StroopKeypadNew(0, ntp, test, true, names, colors, this, GSR);
			st.frame.setTitle("Teszt: "+1);
			return;
		}		
		
		if(state.startsWith("test")) {
			int tnum = test_list.size();
			System.out.println("Finished test "+tnum);
						
			// Show result of finished test
			TestSet set = test_list.get(tnum-1);			

			ShowResult(set);
									
			if(motivational && tnum==1) {
		        TestSet test = new TestSet();
		        test.CreateSet(num);
		        
		        baseline = set.d_correct_avg;
				JOptionPane.showMessageDialog(null, "A következő tesztben, ha az átlagos helyes válaszidőd jobb mint "+(int)baseline+" ms és kevesebb mint 5 hibád van, ajándékot kapsz!");
				JOptionPane.showMessageDialog(null, "Felkészültél?");
				
				tnum++;

				System.out.println("Size "+test.testset.size());		    
				test.Randomize();
								
				test_list.add(test);
				if(debug) test.testset.setSize(2); // TEST
				
				state = "test"+tnum;

				StroopKeypadNew st = new StroopKeypadNew(baseline, ntp, test, true, names, colors, this, GSR);
				st.frame.setTitle("Teszt: "+tnum);
				
				return;
			}
			
			if(motivational) {
				if(set.d_correct_avg < baseline && set.n_err <= 5) 
				 	 JOptionPane.showMessageDialog(null, "Nyertél");
				else JOptionPane.showMessageDialog(null, "Vesztettél");
				
			}

			if(GSR!=null) GSR.Disconnect();
			
			try {
				SaveResult();
				SavePractice();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ShowComplexChart();
			//ShowChart();
			return;

		}		
		
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
       XYSeries series_false;
       series_false = new XYSeries("Hibás");	   	
       series_congruent = new XYSeries("Kongruens ");
       series_incongruent = new XYSeries("Inkongruens");

       int n=0;
       for(int i=0; i<test_list.size(); i++) {
    	   TestSet test = test_list.get(i);

    	   for(int j=0; j<test.testset.size(); j++) {
    		   
    		   TestItem item = test.testset.get(j);
    		   if(item.correct == 1) {
    			   
    			   if(item.col == item.word) {
    				   series_congruent.add(n, item.delay);
    				   System.out.println("congruent "+n+" "+item.delay);
    			   } else {
    				   series_incongruent.add(n, item.delay);
    				   System.out.println("incongruent "+n+" "+item.delay);
    			   }
    			   
    		   } else series_false.add(n, 0);
    		   
    		   n++;
    	   }
       }
       
       dataset.addSeries(series_false);	    
       dataset.addSeries(series_congruent);
       dataset.addSeries(series_incongruent);

       /*
       JFreeChart chart = ChartFactory.createXYLineChart("Stroop teljesítmény", // Title
		                "Próbák", // x-axis Label
		                "Válaszidő [ms]", // y-axis Label
		                dataset, // Dataset
		                PlotOrientation.VERTICAL, // Plot Orientation
		                true, // Show Legend
		                true, // Use tooltips
		                false // Configure chart to generate URLs?
		            );
*/
       
       JFreeChart chart = ChartFactory.createScatterPlot("Stroop teljesítmény", // Title
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
		String str = "Hibás válasz="+test.n_err+"\n" 
		+ "Átlagos helyes válaszidő=" + (int)test.d_correct_avg+" ms\n" ;
		// +"Összegzett válaszidő="+df.format(test.d_all/1000.0)+" másodperc\n" ;
		
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
		
		output.write(fname+"\r\n");
		output.write("Start\t"+practice.start+"\r\n");
		
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
							
			String s = item.showtime+"\t"+names[item.word] + "\t" + names[item.col] + "\t" + item.correct + "\t" + (int)item.delay;
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
		
		output.write(fname+"\r\n");

		String d = "\t\t\t\t\t\t";
		
		output.write("Start\t");
		for(int i=0; i<n; i++) {								
			TestSet test = test_list.get(i);
			output.write(""+test.start);
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
		
		// header for row data		
		output.write("Time\tName\tColor\tCorrect\tDelay");
		output.write("\r\n");
		
		int i=0;
		boolean hasrow = false;
		do {
			//output.write("\t");

			hasrow = false;
			
			for(int j=0; j<n; j++) {
				TestSet test = test_list.get(j);
			
				// if(trial.delay < 3000) sd = ""+(int)trial.delay;
				if(i>=test.testset.size()) {
					// exceeds items
					output.write("\t\t\t\t");
				} else {				
					TestItem item = test.testset.get(i);
					
					hasrow = true;
					
					String sd = ""+(int)item.delay;						
					String s = item.showtime+"\t"+names[item.word] + "\t" + names[item.col] + "\t" + item.correct + "\t" + sd;
					output.write(s);
				}
				if(j<n-1) output.write("\t\t");
			}				
			output.write("\r\n");
			
			i++;
			
		} while(hasrow);
		
		output.close();	
		
		/*

		TestSet test = test_list.get(0);

		for(int i=0; i<test.testset.size(); i++) {
		
			TestItem item = test.testset.get(i);

			output.write(""+item.showtime+"\t");
					
			String s = names[item.word] + "\t" + names[item.col] + "\t" + item.correct + "\t" + (int)item.delay;
			output.write(s);

			output.write("\r\n");			

		}
		
		output.close();	
		*/
	}	
}
