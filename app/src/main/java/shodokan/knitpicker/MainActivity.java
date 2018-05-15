package shodokan.knitpicker;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.sqlite.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.NumberPicker.*;
import java.io.*;
import com.blackmanatee.manatb.*;
import com.blackmanatee.lagoon.*;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener,OnValueChangeListener{
	private Pattern p;
	private int r;
	private String[] pList;
	private File path;
	private String pName;
	private boolean first;
	private int sleep;
	private ArrayAdapter<String> pa;
	
	private ContractDbHelper stHelp;
	private Contract stCon;

	public static final String DB_NAME = "knitpick.db";
	public static final String SAMPLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<pattern name=\"sample\">\n<section name=\"start\">\n<row pattern=\"co39\"/>\n<repeat rep=\"7\">\n<row pattern=\"k39\"/>\n</repeat>\n<repeat rep=\"2\">\n<row pattern=\"p39\"/>\n<row pattern=\"k39\"/>\n</repeat>\n<row pattern=\"p39\"/>\n<repeat rep=\"4\">\n<row pattern=\"k39\"/>\n</repeat>\n</section>\n<section name=\"middle\">\n<repeat rep=\"100\">\n<row pattern=\"k2,p2,k2,p2,k2,p2,k2,p2,k2,p2,k2,p2,k2,p2,k2,p2,k2,p2,k2,p\"/>\n</repeat>\n</section>\n<section name=\"finish\">\n<repeat rep=\"4\">\n<row pattern=\"p39\"/>\n</repeat>\n<repeat rep=\"2\">\n<row pattern=\"k39\"/>\n<row pattern=\"p39\"/>\n</repeat>\n<repeat rep=\"7\">\n<row pattern=\"k39\"/>\n</repeat>\n<row pattern=\"bo39\"/>\n</section>\n</pattern>";

	//Needs:
	//new MakerActivity layout
	//total pattern stitch count
	//project timer
	//project instance meta data
	//pattern correction
	//-identify repeats?
    //mark pattern position
	//special instruction tag
	//eliminate Log warnings
	//permission request explantion for end-users

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
		Log.d("Main.create","New Program!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		ManaTB tb = ManaTB.get();
		stCon = new Contract();
		tb.setDb(DB_NAME);
		stCon.setName("stitch");
		stCon.setColumns(new String[]{"name","base","result"});
		stCon.setTypes(new int[]{Contract.T_TEXT,Contract.T_INT,Contract.T_INT});
		stCon.setLabels(new String[]{"Stitch Name","Base","Result"});
		tb.addTable(stCon);
		stHelp = new ContractDbHelper(this,tb.getDb(),stCon);
		runScript();
		
		StitchLib stLib = StitchLib.get();
		if(!stLib.isLoaded())
			stLib.loadLib((SQLiteCursor)stHelp.getReadableDatabase().query(stCon.getName(),null,null,null,null,null,null,null));
		
		path = getExternalFilesDir("pattern");
		pList = path.list();
		if(pList.length <= 0){
			//load sample pattern from resources
			try{
				FileWriter defFile = new FileWriter(new File(path,"sample.xml"));
				//Log.d("MainActivity.onCreate",getResources().getString(R.string.section_name));
				defFile.write(SAMPLE);
				defFile.flush();
				defFile.close();
			}
			catch(IOException ex){
				Log.d("MainActivity.onCreate",ex.toString());
			}
			pList = path.list();
		}
		pa = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,pList);
		pa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner s = (Spinner)findViewById(R.id.select);
		s.setAdapter(pa);
		s.setOnItemSelectedListener(this);
		NumberPicker np = (NumberPicker)findViewById(R.id.rowPick);
		np.setOnValueChangedListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu mi){
		getMenuInflater().inflate(R.menu.action,mi);
		return true;
	}

	@Override
	public void onResume(){
		super.onResume();
		first = true;
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		pName = pref.getString("selectedPattern",pList[0]);
		int i = 0;
		while(i < pList.length && !pName.equals(pList[i])){
			i++;
		}
		if(i >= pList.length)
			i = 0;
		((Spinner)findViewById(R.id.select)).setSelection(i);
		//set screen off timeout to sufficiently long value
		try{
			ContentResolver cr = getContentResolver();
			sleep = Settings.System.getInt(cr,Settings.System.SCREEN_OFF_TIMEOUT);
			if(Settings.System.canWrite(this))
				Settings.System.putInt(cr,Settings.System.SCREEN_OFF_TIMEOUT,600000);
			else{
				Intent in = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
				in.setData(Uri.parse("package:"+getPackageName()));
				startActivity(in);
			}
		}
		catch(Settings.SettingNotFoundException ex){
			Log.d("Main.onResume()",ex.toString());
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
		if(requestCode == 39 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,600000);
		else
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onPause(){
		super.onPause();
		SharedPreferences.Editor pref = getPreferences(MODE_PRIVATE).edit();
		String s = (String)((Spinner)findViewById(R.id.select)).getSelectedItem();
		pref.putString("selectedPattern",s);
		pref.putInt(s,r);
		pref.commit();
		//reset timeout to previous setting
		try{
			Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,sleep);
		}
		catch(Exception ex){
			Log.d("Main.onPause()",ex.toString());
		}
	}

 	public void tapClick(View v){
		r++;
		NumberPicker np = (NumberPicker)findViewById(R.id.rowPick);
		np.setValue(r);
		render();
	}

	public void newRow(View v){
		r = 0;
		render();
	}

	private void render(){
		((TextView)findViewById(R.id.row)).setText("Row: "+r);
		((TextView)findViewById(R.id.section)).setText("Section: "+p.nameAt(r));
		((TextView)findViewById(R.id.pattern)).setText(p.patternAt(r));
		((TextView)findViewById(R.id.stsBox)).setText(Integer.toString(p.getStitchCount(r)));
	}

	private void parseFile(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			p = Pattern.buildFromXml(path.getPath()+"/"+pName);
			NumberPicker np = (NumberPicker)findViewById(R.id.rowPick);
			np.setMinValue(0);
			np.setMaxValue(p.getRowCount());
			np.setWrapSelectorWheel(false);
		}
		else
			Log.d("MainActivity.onResume()","Files unavailable.");
	}

	@Override
	public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4){
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		//execution on init before r is set is erasing saved r
		if(first == false)
			pref.edit().putInt(pName,r).commit();
		else
			first = false;
		pName = (String)p1.getItemAtPosition(p3);
		parseFile();
		r = pref.getInt(pName,0);
		((NumberPicker)findViewById(R.id.rowPick)).setValue(r);
		render();
	}

	@Override
	public void onNothingSelected(AdapterView<?> p1){
	}

	@Override
	public void onValueChange(NumberPicker p, int os, int ns){
		r = ns;
		render();
	}
	
	public void newAction(MenuItem m){
		startActivity(new Intent(this,MakerActivity.class));
	}

	public void editAction(MenuItem m){
		Intent in = new Intent(this,MakerActivity.class);
		in.putExtra("pattern",(String)((Spinner)findViewById(R.id.select)).getSelectedItem());
		startActivity(in);
	}
	
	public void stDbAction(MenuItem m){
		Intent in = new Intent(this,ManaTBActivity.class);
		in.putExtra("contract",stCon.getName());
		startActivity(in);
	}
	
	private void runScript(){
		try{
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				File script = new File(getExternalFilesDir(null),"script.txt");
				if(script != null){
					SQLiteDatabase db = stHelp.getWritableDatabase();
					BufferedReader in = new BufferedReader(new FileReader(script));
					while(in.ready()){
						db.execSQL(in.readLine());
					}
					in.close();
					FileWriter out = new FileWriter(script);
					out.write("");
					out.flush();
					out.close();
				}
			}
		}
		catch(Exception ex){
			try{
				FileWriter script = new FileWriter(new File(getExternalFilesDir(null),"script.txt"));
				script.write("");
				script.flush();
				script.close();
			}
			catch(Exception exc){
				Log.d("ScriptError",ex.toString());
			}
		}
	}
}

