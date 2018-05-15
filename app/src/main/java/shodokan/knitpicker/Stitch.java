package shodokan.knitpicker;
import android.content.*;
import android.database.sqlite.*;
import android.util.*;
import android.view.*;

public class Stitch{
	private static final boolean debug = false;
	
	private String type;
	private int base, result;
	private int st;		//number of sts to repeat
	
	//Needs:
	//-better w and t handling
	
	public Stitch(String c){
		String cc = c.trim();
		cc = cc.toLowerCase();
		for (int s = cc.length() - 1; s >= 0; s--) {
			if (!cc.substring(s).matches("[0-9]+")) {
				s++;
				if (s == cc.length()){
					type = cc;
					st = 1;
				}
				else{
					type = c.substring(0, s).trim();
					st = Integer.parseInt(c.substring(s));
				}
				break;
			}
		}
		queryCounts();
	}
	
	public void setType(String t){
		type = t;
	}
	
	public String getType(){
		return type;
	}
	
	public void setSt(int s){
		st = s;
	}
	
	public int getSt(){
		return st;
	}
	
	public void incSt(){
		st++;
	}
	
	public void incSt(int i){
		st += i;
	}
	
	public int getStitchCount(){
		if(debug)
			Log.d("tuck",type+":"+result);
		return result*st;
	}
	
	private void queryCounts(){
		StitchLib stLib = StitchLib.get();
		if(debug)
			Log.d("tuck","type:"+type);
		base = stLib.getBase(type);
		result = stLib.getResult(type);
		if(debug)
			Log.d("tuck","base:"+base+" result:"+result);
	}
	
	public String toString(){
		return type+st;
	}
}
