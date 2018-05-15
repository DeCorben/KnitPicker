package shodokan.knitpicker;
import android.util.*;
import android.database.sqlite.*;

//check Cursor for valid data before load

public final class StitchLib{
	private SparseArray<String> key;
	private int[] base, result;
	
	private static StitchLib me;
	
	private StitchLib(){
		key = new SparseArray<>();
		base = new int[]{0};
		result = new int[]{0};
	}
	
	public static StitchLib get(){
		if(me == null)
			me = new StitchLib();
		return me;
	}
	
	public void loadLib(SQLiteCursor c){
		if(c.getCount() == 0){
			base = new int[]{1};
			result = new int[]{1};
			key.put(0,"k");
			return;
		}
		base = new int[c.getCount()];
		result = new int[c.getCount()];
		c.moveToFirst();
		do{
			int i = key.size();
			key.put(i,c.getString(1));
			base[i] = c.getInt(2);
			result[i] = c.getInt(3);
		}while(c.moveToNext());
	}
	
	public int getBase(String k){
		int i = 0;
		while(i<key.size() && !key.get(i).equals(k.trim())){
			i++;
		}
		if(i == key.size())
			return 0;
		return base[i];
	}
	
	public int getResult(String k){
		int i = 0;
		while(i<key.size() && !key.get(i).equals(k.trim())){
			i++;
		}
		if(i == key.size())
			return 0;
		return result[i];
	}
	
	public boolean isLoaded(){
		if(key.size() == 0)
			return false;
		return true;
	}
	
	public String[] keyList(){
		String[] kList = new String[key.size()];
		for(int z=0;z<key.size();z++){
			kList[z] = key.valueAt(z);
		}
		return kList;
	}
}
