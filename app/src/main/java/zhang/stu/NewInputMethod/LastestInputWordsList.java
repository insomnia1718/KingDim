package zhang.stu.NewInputMethod;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;
//记录最近输入的汉字，用于自造词
public class LastestInputWordsList extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lastest_input_words_list);  
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();  
		String lastestWords = KingDimInputMethodService.lastestInputWords.toString().trim();
		int i = 0;
		String temp = null;
		for(i=lastestWords.length()-2;i>-1;i--)
		{   
			HashMap<String, String> map = new HashMap<String, String>(); 
			temp = lastestWords.substring(i,lastestWords.length());
			map.put("LastestWords", temp);
			list.add(map);
		}
	    SimpleAdapter listAdapter = new SimpleAdapter(this, list, R.layout.lastestwordslist, new String[]{"LastestWords"}, new int[]{R.id.LastestWords});  
	        //设置显示ListView  
	    setListAdapter(listAdapter);  	          
	}
   
}
