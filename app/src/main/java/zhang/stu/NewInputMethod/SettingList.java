package zhang.stu.NewInputMethod;
import java.util.ArrayList;  
import java.util.HashMap;  
import android.app.ListActivity;  
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;  
import android.view.View;  
import android.widget.ListView;  
import android.widget.SimpleAdapter;  
//设置菜单的界面，用listactivity来实现
public class SettingList extends ListActivity{  
    /** Called when the activity is first created. */  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.settingoptionlist);  
          
        //生成一个ArrayList类型的变量list  
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();  
        //生成两个HashMap类型的变量map1 ， map2  
        //HashMpa为键值对类型。第一个参数为建，第二个参数为值  
        HashMap<String, String> map1 = new HashMap<String, String>();  
        HashMap<String, String> map2 = new HashMap<String, String>();  
        HashMap<String, String> map3 = new HashMap<String, String>();
        HashMap<String, String> map4 = new HashMap<String, String>();
        //把数据填充到map中。  
        map1.put("optionName", "添加自造词");            
        map2.put("optionName", "汉字读音查询");  
        map3.put("optionName", "帮助文档");
        map4.put("optionName", "输入法更新");
        //把map添加到list中  
        list.add(map1);  
        list.add(map2);  
        list.add(map3);
        list.add(map4);
        //生成一个SimpleAdapter类型的变量来填充数据  
        SimpleAdapter listAdapter = new SimpleAdapter(this, list, R.layout.list, new String[]{"optionName"}, new int[]{R.id.optionName});  
        //设置显示ListView  
        setListAdapter(listAdapter);  
          
    }  
    //重写onListItemClick但是ListView条目事件  
    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {  
        // TODO Auto-generated method stub  
        super.onListItemClick(l, v, position, id);  
        switch(position)
        {
        	case 0:
        		System.out.println("First");
        		Intent in = new Intent();
        		in.setClass(this, SelfMadeWords.class);
        	    this.startActivity(in);
        		break;
        	case 1:
        		System.out.println("Second");   		
        		break;
        	case 2:
        		System.out.println("Third");
        		 Uri uri0=Uri.parse("http://cslab.stu.edu.cn/KingAIMHelp.htm");
				 Intent intent=new Intent(Intent.ACTION_VIEW, uri0);
				 intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
				 startActivity(intent);
        		break;
        	case 3:
        		System.out.println("Last");
        		break;
        	default:
        		break;
        }
        //显示单击条目ID号  
    }  
}
      
