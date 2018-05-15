package shodokan.knitpicker;
import android.os.Environment;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import android.view.*;
import android.content.*;

//Needs:
//delete section
//create chart/grid

public class Pattern{
	protected ArrayList<Section> secs;
	protected String name;

	public Pattern(){
		name = "";
		secs = new ArrayList<>();
	}

	public Pattern(String n){
		name = n;
		secs = new ArrayList<>();
	}

	public static Pattern buildFromXml(String x){
		//String x is FULL PATH file name
		Pattern pat = new Pattern();

		try{
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(x));
				Element root = doc.getDocumentElement();
				if(root.getTagName().equals("project") || root.getTagName().equals("pattern")){
					pat.setName(root.getAttribute("name"));
					NodeList nl = root.getElementsByTagName("section");
					for(int i=0;i<nl.getLength();i++){
						Element s = (Element)nl.item(i);
						pat.addSection(new Section(s.getAttribute("name")));
						NodeList arr = s.getChildNodes();
						for(int j=0;j<arr.getLength();j++){
							//Log.d("Pattern.build","Lap: "+j);
							parseRow(pat,arr.item(j));
						}
					}
				}
			}
			else
				Log.d("Pattern.build","Media unavailable");
		}
		catch(Exception ex){
			Log.d("Pattern.build", ex.toString());
		}

		return pat;
	}

	private static void parseRow(Pattern p, Node n){
		if(n.getNodeType() == Node.ELEMENT_NODE){
			Element u = (Element)n;
			if(u.hasChildNodes()){
				NodeList ch = u.getChildNodes();
				int rep = Integer.parseInt((u.getAttribute("rep")));
				for(int j=0;j<rep;j++)
					for (int k = 0; k < ch.getLength(); k++)
						parseRow(p, ch.item(k));
			}
			else{
				String pat = u.getAttribute("pattern");
				//Log.d("Pattern.parseRow",pat);
				p.addRow(new Row(pat));
			}
		}
		
	}

	public void setName(String n){
		name = n;
	}

	public String getName(){
		return name;
	}

	public void addSection(Section r){
		secs.add(r);
	}

	public Section getSection(int i){
		if(i >= secs.size())
			return null;
		return secs.get(i);
	}
	
	public void setSection(int i,Section s){
		secs.set(i,s);
	}
	
	public int getSectionCount(){
		return secs.size();
	}
	
	public void addRow(Row r){
		secs.get(secs.size()-1).addRow(r);
	}
	
	public Row getRow(int i){
		int r = i;
		int s = 0;
		while(r >= secs.get(s).getRowCount()){
			r -= secs.get(s).getRowCount();
			s++;
			//end handling
			if(s >= secs.size())
				return null;
		}
		return secs.get(s).getRow(r);
	}

	public String toString(){
		String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<pattern name=\""+name+"\">\n";
		for(int i=0;i<secs.size();i++){
			s += secs.get(i).toString()+"\n";
		}
		s += "</pattern>";
		return s;
	}
	
	public int getRowCount(){
		int r = 0;
		for(int i=0;i<secs.size();i++)
			r += secs.get(i).getRowCount();
		return r;
	}
	
	public String nameAt(int i){
		//handle empty pattern
		if(secs.size() == 0)
			return "";
		int s = 0;
		while(i>=secs.get(s).getRowCount()){
			i -= secs.get(s).getRowCount();
			s++;
			if(s >= secs.size())
				return "End";
		}
		return secs.get(s).getName();
	}
	
	public String patternAt(int i){
		try{
			return getRow(i).getPattern();
		}
		catch(Exception ex){
			return "End";
		}
	}
	
	public void addStitch(String s){
		Section cur = secs.get(secs.size()-1);
		cur.getRow(cur.getRowCount()-1).addStitch(s);
	}
	
	public int getStitchCount(int i){
		try{
			return getRow(i).getStitchCount();
		}
		catch(NullPointerException ex){
			return 0;
		}
		catch(Exception ex){
			Log.d("Pattern.getStitchCount",ex.toString());
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object o){
		try{
			Pattern p = (Pattern)o;
			if(!name.equals(p.getName()))
				return false;
			if(secs.size() != p.getSectionCount())
				return false;
			for(int i=0;i<secs.size();i++){
				if(!secs.get(i).equals(p.getSection(i)))
					return false;
			}
		}
		catch(ClassCastException ex){
			return false;
		}
		return true;
	}
	
	public void delSec(int i){
		for(int z=i;z<secs.size()-1;z++){
			secs.set(z,secs.get(z+1));
		}
		secs.remove(secs.size()-1);
	}
}
