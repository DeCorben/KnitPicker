package shodokan.knitpicker;
import java.util.*;
import android.util.*;

public class Section{
	private ArrayList<Row> rows;
	private String name;
	
	//Needs:
	
	public Section(){
		name = "";
		rows = new ArrayList<Row>();
	}
	
	public Section(String n){
		name = n;
		rows = new ArrayList<Row>();
	}
	
	//build from xml elements?
	
	public void setName(String n){
		name = n;
	}
	
	public String getName(){
		return name;
	}
	
	public void addRow(Row r){
		rows.add(r);
	}
	
	public Row getRow(int i){
		if(i>=rows.size() || i<0)
			return null;
		return rows.get(i);
	}
	
	public void setRow(int i,Row r){
		if(i >= rows.size())
			addRow(r);
		else
			rows.set(i,r);
	}
	
	public int getRowCount(){
		return rows.size();
	}

	public String toString(){
		String s = "<section name=\""+name+"\">\n";
		for(int i=0;i<rows.size();i++){
			s += rows.get(i).toString()+"\n";
		}
		s += "</section>";
		return s;
	}
	
	@Override
	public boolean equals(Object o){
		try{
			Section s = (Section)o;
			if(!name.equals(s.getName()))
				return false;
			if(rows.size() != s.getRowCount())
				return false;
			for(int i=0;i<rows.size();i++){
				if(!rows.get(i).equals(s.getRow(i)))
					return false;
			}
		}
		catch(ClassCastException ex){
			return false;
		}
		return true;
	}
	
	public void delRow(int i){
		for(int z=i;z<rows.size()-1;z++){
			rows.set(z,rows.get(z+1));
		}
		rows.remove(rows.size()-1);
	}
	
	public int findRow(Row r){
		return rows.indexOf(r);
	}
}
