package gsrGUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFileChooser;

public class StroopGsrMerge {

	File dir;
	
	class Pair {
		String stroop;
		String gsr;
	}
	
	Vector<String> lines = new Vector();

	
	HashMap<String, Pair> map = new HashMap<String, Pair>();
	
	void AddGsr(String name) {
		int i1 = name.indexOf('_');
		int i2 = name.indexOf('_', i1+1);
				
		String code = name.substring(i1+1, i2);
		
		System.out.println("AddGSR "+name+" "+i1+" "+i2+" "+code);
		
		Pair p = new Pair();
		p.gsr = name;
		
		map.put(code, p);
	}
	
	void AddStroop(String name) {
		int i1 = name.indexOf('_');
		int i2 = name.indexOf('_', i1+1);
				
		String code = name.substring(i1+1, i2);
		
		System.out.println("AddStroop "+name+" "+i1+" "+i2+" "+code);

		if(!map.containsKey(code)) {
			System.out.println("Not found "+name);
			return;
		}
		
		Pair p = map.get(code);	
		p.stroop = name;
		
	}

	String AppendAt(String s, String a, int k) {
		
		int n=0;
		for(int i=0; i<s.length(); i++) if(s.charAt(i)=='\t') n++;
				
		for(int i=0; i<k-n; i++) s += "\t";
		
		s+=a;
		
		return s;
		
	}
	
	void Merge(Pair p) throws IOException {
		
		Vector<String> gsrlines = new Vector(); 
		Vector<String> strooplines = new Vector();

		
		System.out.println("\n\nMerge "+p.gsr+" "+p.stroop);
		
		File f = new File(dir, p.gsr);
		FileReader fr = new FileReader (f);
		BufferedReader br = new BufferedReader(fr);
		
		while(1==1) {
			String s = br.readLine();
			if(s==null) break;
			
			gsrlines.add(s);
		}
		
		br.close();
		fr.close();
		
		f = new File(dir, p.stroop);
		fr = new FileReader (f);
		br = new BufferedReader(fr);
		
		int i=0;
		while(1==1) {
			String s = br.readLine();
						
			if(s==null) break;
			
			strooplines.add(s);
			
		}

		int max = gsrlines.size();
		if(strooplines.size()>max) max = strooplines.size();
		
		for(i=0; i<max; i++) {
			String line = new String();			
			if(i<gsrlines.size()) line = gsrlines.elementAt(i);
			
			if(i<strooplines.size()) line = AppendAt(line, strooplines.get(i), 10);
			
			System.out.println(line);
					
			lines.add(line);
		}		
		
		return;
	}
	
	void Write(File dir, String fname) throws IOException {
		File file= new File(dir, "merged_"+fname);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
					
		for(int i=0; i<lines.size(); i++)						
			output.write(lines.get(i)+"\r\n");
		
		output.close();
	}
	
	public StroopGsrMerge() {
		   
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int returnVal = chooser.showOpenDialog(null);		    
		if(returnVal!= JFileChooser.APPROVE_OPTION) return;
		
		dir = chooser.getSelectedFile();
		
		//dir.list();
		for(int i=0; i<dir.list().length; i++) {
			
			String name = dir.list()[i];
			
			if(name.startsWith("gsr_")) {
				AddGsr(name);				
			}
			
		}

		for(int i=0; i<dir.list().length; i++) {
			
			String name = dir.list()[i];
			
			if(name.startsWith("stroop_")) {
				AddStroop(name);				
			}
			
		}

		for(String code : map.keySet()) {
			try {
				Merge(map.get(code));
				Write(dir, code);
				lines.clear();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		
		//JOptionPane.showMessageDialog(null, "ize");
		//return;

		//File file= new File(dir.getAbsolutePath()+"\\"+fname);
		//BufferedWriter output = new BufferedWriter(new FileWriter(file));

		
		
	}
	
	public static void main(String[] args) 
	{
		new StroopGsrMerge();
	}
}
