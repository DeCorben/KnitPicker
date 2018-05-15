package shodokan.knitpicker;

import android.app.*;
import android.database.sqlite.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import shodokan.knitpicker.*;
import android.content.*;
import com.blackmanatee.manatb.*;

public class MakerActivity extends Activity implements NumberPicker.OnValueChangeListener,TextView.OnEditorActionListener, OnClickListener{
	private static final boolean debug = false;
	
	private Pattern wip;
	private Section sip;
	private Row rip;
	private StitchLib stLib;

	//Needs:
	//stitch console
	//-aligning stitches for inc/dec
	//-stitch console: display unworked sts
	//"to end" and "even" meta-buttons
	//repeat tag encoding
	//repeat tag input
	//repeats w/in rows
	
	//Has workaround:
	//reload on screen rotation causes pattern(name) transposition w/ first section
	
	@Override
	public void onCreate(Bundle state){
		super.onCreate(state);
		setContentView(R.layout.maker);
		
		//stitch library insurance
		stLib = StitchLib.get();
		if(!stLib.isLoaded()){
			ManaTB tb = ManaTB.get();
			Contract stCon = tb.getDefaultTable();
			stLib.loadLib((SQLiteCursor)new ContractDbHelper(this,tb.getDb(),stCon).getReadableDatabase().query(stCon.getName(),null,null,null,null,null,null,null));
		}
			
		//screen rotation rebuilds activity
		
		String x = getIntent().getStringExtra("pattern");
		if(x == null)
			wip = new Pattern();
		else
			wip = Pattern.buildFromXml(getExternalFilesDir("pattern").getPath()+"/"+x);
		sip = wip.getSection(0);
		if(sip != null){
			rip = sip.getRow(0);
			if(rip == null){
				rip = new Row();
				sip.addRow(rip);
			}
		}
		else{
			sip = new Section();
			wip.addSection(sip);
			rip = new Row();
			sip.addRow(rip);
		}
		((EditText)findViewById(R.id.patternName)).setText(wip.getName());
		//((NumberPicker)findViewById(R.id.sectionNum)).setWrapSelectorWheel(false);
		((NumberPicker)findViewById(R.id.sectionNum)).setOnValueChangedListener(this);
		//((NumberPicker)findViewById(R.id.rowNum)).setWrapSelectorWheel(false);
		((NumberPicker)findViewById(R.id.rowNum)).setOnValueChangedListener(this);
		((EditText)findViewById(R.id.sectionName)).setText(sip.getName());
		((EditText)findViewById(R.id.sectionName)).setOnEditorActionListener(this);
		((EditText)findViewById(R.id.rowPattern)).setText(rip.getPattern());
		((EditText)findViewById(R.id.rowPattern)).setOnEditorActionListener(this);
		
		//populate stitch console
		int dis = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		if(dis == Surface.ROTATION_0 || dis == Surface.ROTATION_180){
			makeConsole();
		}
		else{
			ViewGroup.LayoutParams parm = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
			((HorizontalScrollView)findViewById(R.id.stitchScroll)).addView(buildRowConsole(),parm);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		altUpdate(-1);
		NumberPicker sect = (NumberPicker)findViewById(R.id.sectionNum);
		sect.setMinValue(0);
		int sc = wip.getSectionCount();
		sect.setMaxValue(sc);
		//sect.setWrapSelectorWheel(false);
		NumberPicker row = (NumberPicker)findViewById(R.id.rowNum);
		row.setMinValue(0);
		row.setMaxValue(sc > 0 ? wip.getSection(0).getRowCount() : 0);
		//row.setWrapSelectorWheel(false);
	}

	@Override
	public void onPause(){
		super.onPause();
		altUpdate(-1);
		try{
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				FileWriter fw = new FileWriter(new File(getExternalFilesDir("pattern"),wip.getName()+".xml"));
				fw.write(wip.toString());
				fw.flush();
				fw.close();
			}
		}
		catch(IOException ex){
			Log.d("Maker.onResume()", ex.toString());
		}
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal){
		altUpdate(picker.getId());
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
		altUpdate(-1);
		return true;
	}

	@Override
	public void onClick(View p1){
		String k = ((TextView) p1).getText().toString();
		rip.addStitch(new Stitch(k));
		NumberPicker rowNum = (NumberPicker)findViewById(R.id.rowNum);
		int ri = rowNum.getValue();
		sip.setRow(ri,rip);
		if(ri == rowNum.getMaxValue()){
			rowNum.setMaxValue(sip.getRowCount());
			rowNum.setWrapSelectorWheel(false);
		}
		((EditText)findViewById(R.id.rowPattern)).setText(rip.getPattern());
	}
	
	private void makeConsole(){
		GridLayout rel = (GridLayout)findViewById(R.id.console);
		//link with StitchLib
		for(String k:stLib.keyList()){
			Button b = new Button(this);
			b.setText(k);
			b.setOnClickListener(this);
			rel.addView(b,new GridLayout.LayoutParams());
		}
	}
	
	public void clickSectionDelete(View v){
		//pull section id
		NumberPicker secPick = (NumberPicker)findViewById(R.id.sectionNum);
		int s = secPick.getValue();
		if(s < secPick.getMaxValue()){
			//delete from section
			wip.delSec(s);
			//refresh sec Picker
			sip = wip.getSection(s);
			((TextView)findViewById(R.id.sectionName)).setText("");
			altUpdate(-1);
			secPick.setMaxValue(wip.getRowCount());
			secPick.setWrapSelectorWheel(false);
			//refresh row picker
			NumberPicker rowPick = (NumberPicker)findViewById(R.id.rowNum);
			rowPick.setMaxValue(sip.getRowCount());
			rowPick.setWrapSelectorWheel(false);
			rowPick.setValue(0);
			rip = sip.getRow(0);
			if(rip == null){
				rip = new Row();
				((EditText)findViewById(R.id.rowPattern)).setText("");
			}
			else
				((EditText)findViewById(R.id.rowPattern)).setText(rip.getPattern());
		}
	}

	public void clickRowDelete(View v){
		//pull row id
		NumberPicker rowPick = (NumberPicker)findViewById(R.id.rowNum);
		int r = rowPick.getValue();
		if(r < rowPick.getMaxValue()){
			//delete from section
			sip.delRow(r);
			//refresh/update row picker
			rip = sip.getRow(r);
			((TextView)findViewById(R.id.rowPattern)).setText("");
			altUpdate(-1);
		}
	}

	private View buildRowConsole(){
		GridLayout grid = new GridLayout(this);
		grid.setRowCount(2);
		grid.setOrientation(GridLayout.VERTICAL);
		if(rip != null){
			//stack current row
			//stack prev row
			//pop current stitch
			//-consume (cur.base) result stitches from prev
			int p_consumed = 0;
			Row p_row = sip.getRow(((NumberPicker)findViewById(R.id.rowNum)).getValue()-1);
			ArrayDeque<Stitch> prevRow = new ArrayDeque<Stitch>();
			if(p_row != null)
				for(Stitch s:p_row.getStitches())
					for(int z=0;z<s.getSt();z++)
						prevRow.addLast(new Stitch(s.getType()));
			Stitch p_stitch = prevRow.size()>0?prevRow.pop():null;
			ArrayDeque<Stitch> cur_row = new ArrayDeque<Stitch>();
			for(Stitch s:rip.getStitches())
				for(int z=0;z<s.getSt();z++)
					cur_row.addLast(new Stitch(s.getType()));
			if(debug)
				Log.d("tuck","cur_row.size:"+cur_row.size());
			for(Stitch s:cur_row){
				Button st = new Button(this);
				st.setText(s.getType());
				grid.addView(st,new GridLayout.LayoutParams());
				TextView prev = new TextView(this);
				if(p_stitch != null){
					if(debug)
						Log.d("tuck","Prev:"+p_stitch.getType()+"->Cur:"+s.getType());
					prev.setText(p_stitch.getType());
					p_consumed++;
					if(p_consumed == p_stitch.getStitchCount()){
						p_consumed = 0;
						if(prevRow.isEmpty())
							p_stitch = null;
						else
							p_stitch = prevRow.pop();
					}
				}
				else
					prev.setText(" ");
				prev.setGravity(Gravity.CENTER_HORIZONTAL);
				grid.addView(prev,new GridLayout.LayoutParams());
			}
		}
		return grid;
	}
	
	public void prevClick(View v){
		NumberPicker np = (NumberPicker)findViewById(R.id.rowNum);
		int r = np.getValue();
		if(r == 0){
			NumberPicker sp = (NumberPicker)findViewById(R.id.sectionNum);
			int s = sp.getValue();
			if(s > 0){
				sp.setValue(s-1);
				np.setValue(np.getMaxValue());
				altUpdate(R.id.sectionNum);
			}
		}
		else{
			np.setValue(r-1);
			altUpdate(R.id.rowNum);
		}
		
		HorizontalScrollView hsv = (HorizontalScrollView)findViewById(R.id.stitchScroll);
		ViewGroup.LayoutParams parm = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
		hsv.removeAllViews();
		hsv.addView(buildRowConsole(),parm);
	}
	
	public void nextClick(View v){
		NumberPicker np = (NumberPicker)findViewById(R.id.rowNum);
		int r = np.getValue();
		if(r == np.getMaxValue()){
			NumberPicker sp = (NumberPicker)findViewById(R.id.sectionNum);
			int s = sp.getValue();
			if(s < sp.getMaxValue()){
				sp.setValue(s+1);
				np.setValue(0);
				altUpdate(R.id.sectionNum);
			}
		}
		else{
			np.setValue(r+1);
			altUpdate(R.id.rowNum);
		}
		
		HorizontalScrollView hsv = (HorizontalScrollView)findViewById(R.id.stitchScroll);
		ViewGroup.LayoutParams parm = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
		hsv.removeAllViews();
		hsv.addView(buildRowConsole(),parm);
	}
	
	private void altUpdate(int trigger){
		//trigger: id of object initiating call
		
		//poll change state
		//pattern name changed?
		String newName = ((EditText)findViewById(R.id.patternName)).getText().toString();
		boolean patternNameChanged = !wip.getName().equals(newName);
		//section name changed?
		String newSecName = ((EditText)findViewById(R.id.sectionName)).getText().toString();
		boolean sectionNameChanged = sip==null?true:!sip.getName().equals(newSecName);
		//row pattern changed?
		//text pattern changed?
		String newTextPattern = ((EditText)findViewById(R.id.rowPattern)).getText().toString();
		boolean patternChanged = rip==null?true:!rip.getPattern().equals(newTextPattern);
		//console pattern changed?
		//section number changed?
		boolean secNumChanged = trigger==R.id.sectionNum?true:false;
		//row number changed?
		boolean rowNumChanged = trigger==R.id.rowNum?true:false;
			
		//save changes
		if(patternNameChanged)
			wip.setName(newName);
			
		if(secNumChanged){
			sectionNameChanged = true;
			rowNumChanged = true;
		}
		
		if(rowNumChanged)
			patternChanged = true;
			
		if(sectionNameChanged){
			//can be triggered indirectly by sectionNum change
			if(sip == null){
				sip = new Section();
				wip.addSection(sip);
			}
			sip.setName(newSecName);
		}
		
		if(patternChanged){
			//can be triggered indirectly by a sectionNum or rowNum change
			//will need changes for console trigger
			if(rip == null){
				rip = new Row();
				sip.addRow(rip);
			}
			if(newTextPattern.equals(""))
				sip.delRow(sip.findRow(rip));
			else
				rip.setPattern(newTextPattern);
		}
		
		//update objects in progress
		NumberPicker secNum = (NumberPicker)findViewById(R.id.sectionNum);
		sip = wip.getSection(secNum.getValue());
		NumberPicker rowNum = (NumberPicker)findViewById(R.id.rowNum);
		rip = sip.getRow(secNumChanged?0:rowNum.getValue());
		
		//update UI
		//pattern name
		((EditText)findViewById(R.id.patternName)).setText(wip.getName());
		//section number can only change manually, does not need attention
		//section name
		((EditText)findViewById(R.id.sectionName)).setText(sip.getName());
		//row number
		if(secNumChanged)
			rowNum.setValue(0);
		//text pattern
		((EditText)findViewById(R.id.rowPattern)).setText(rip==null?"":rip.getPattern());
		//console pattern
		HorizontalScrollView hsv = (HorizontalScrollView)findViewById(R.id.stitchScroll);
		ViewGroup.LayoutParams parm = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
		hsv.removeAllViews();
		hsv.addView(buildRowConsole(),parm);
		//stitch counts
		((TextView)findViewById(R.id.stitchCountCurr)).setText(Integer.toString(rip==null?0:rip.getStitchCount()));
		if(rowNum.getValue() > 0)
			((TextView)findViewById(R.id.stitchCountPrev)).setText(Integer.toString(sip.getRow(rowNum.getValue()-1).getStitchCount()));
		else
			((TextView)findViewById(R.id.stitchCountPrev)).setText(" ");
	}
}
