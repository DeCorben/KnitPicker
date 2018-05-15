package shodokan.knitpicker;
import java.util.*;
import android.util.*;
import android.view.*;
import android.content.*;

public class Row{
	private String pattern;
	private ArrayList<Stitch> sts;
	
	//Needs:
	
	public Row(){
		pattern = "";
		sts = new ArrayList<>();
	}
	
	public Row(String p){
		sts = new ArrayList<>();
		//populate Stitch objects
		setPattern(p);
	}
	
	public String getPattern(){
		if(sts.size() == 0)
			return "";
		Stitch s = sts.get(0);
		int n = s.getSt();
		String ret = s.getType();
		if(n > 1)
			ret += Integer.toString(n);
		int limit = sts.size();
		for(int i=1;i<limit;i++){
			s = sts.get(i);
			n = s.getSt();
			ret += ", "+s.getType();
			if(n > 1)
				ret += Integer.toString(n);
		}
		return ret;
	}
	
	public ArrayList<Stitch> getStitches(){
		return sts;
	}
	
	public void setPattern(String p){
		sts.clear();
		pattern = p;
		String[] a = p.split(",");
		for(String b:a){
			addStitch(new Stitch(b.trim()));
		}
	}
	
	public void setPattern(Stitch[] p){
		sts.clear();
		for(Stitch s:p){
			sts.add(s);
		}
		pattern = generatePattern();
	}
	
	private String generatePattern(){
		String p = "";
		for(int z=0;z<sts.size();z++){
			p += sts.get(z).toString();
		}
		return p;
	}

	@Override
	public String toString(){
		return "<row pattern=\""+getPattern()+"\"/>";
	}
	
	public void addStitch(String n){
		int z = sts.size();
		if(z > 0){
			Stitch s = sts.get(z-1);
			if(n.equals(s.getType()))
				s.incSt();
			else
				sts.add(new Stitch(n));
		}
		else
			sts.add(new Stitch(n));
	}
	
	public void addStitch(Stitch n){
		if(sts.size() == 0)
			sts.add(n);
		else{
			Stitch s = sts.get(sts.size()-1);
			if(n.getType().equals(s.getType()))
				s.incSt(n.getSt());
			else
				sts.add(n);
		}
	}
	
	public int getStitchCount(){
		int sc = 0;
		for(int i=0;i<sts.size();i++)
			sc += sts.get(i).getStitchCount();
		return sc;
	}
	
	@Override
	public boolean equals(Object o){
		try{
			Row r = (Row)o;
			if(!getPattern().equals(r.getPattern()))
				return false;
		}
		catch(ClassCastException ex){
			return false;
		}
		return true;
	}
}
