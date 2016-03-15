package zhang.stu.NewInputMethod;
 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.app.ExpandableListActivity;
import android.content.ContentResolver;  
import android.content.Context;  
import android.database.Cursor;    
import android.net.Uri;  
import android.os.Bundle;  
import android.provider.ContactsContract.CommonDataKinds.Phone;  
import android.provider.ContactsContract.CommonDataKinds.Photo;  
import android.text.TextUtils;  
import android.view.View;  
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;


//利用一个Cursor型的 phoneCursor对象把联系人的信息读出来，再用一个ExpandableListActivity对象来显示
public class ContactsActivity extends ExpandableListActivity  {  
	 Context mContext = null;  
	  
	    /**获取库Phone表字段**/  
	    private static final String[] PHONES_PROJECTION = new String[] {  
	        Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };  
	     
	    /**联系人显示名称**/  
	    private static final int PHONES_DISPLAY_NAME_INDEX = 0;  
	      
	    private static final int PHONES_NUMBER_INDEX = 1;  
	    
	    /**联系人名称**/  

	   ArrayList<HashMap<String, String>>  mContactsName = new ArrayList<HashMap<String,String>>();  
	   
	   /*************************************************************************/
	   List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
	   List<Map<String, String>> child1 = new ArrayList<Map<String, String>>();
	   List<Map<String, String>> child2 = new ArrayList<Map<String, String>>();
	   /**********************************************************************/
	   // ListView mListView = null;  
	  
	    @Override  
	    public void onCreate(Bundle savedInstanceState) {  
	    mContext = this;  
	   
        super.onCreate(savedInstanceState);  
	    setContentView(R.layout.contactnamelist); 
	    /*************************************************************************/
	 
        //创建两个一级条目标题
        Map<String, String> group1 = new HashMap<String, String>();
        group1.put("group", "  手机联系人");
        Map<String, String> group2 = new HashMap<String, String>();
        group2.put("group", "  SIM卡联系人");
        groups.add(group1);
        groups.add(group2);
	    /***********************************************************************/
	    getPhoneContacts();  
	    getSIMContacts();
       
	    List<List<Map<String, String>>> childs = new ArrayList<List<Map<String, String>>>();
	    childs.add(child1);
	    childs.add(child2);
	 //   SimpleAdapter listAdapter = new SimpleAdapter(this, mContactsName, R.layout.contactslist, new String[]{"contactsName"}, new int[]{R.id.contactsName});  
        //设置显示ListView  
    //    setListAdapter(listAdapter);      
        
	    SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this, groups, R.layout.group, new String[] { "group" },
                new int[] { R.id.group }, childs, R.layout.childs,
                new String[] { "contactsName" }, new int[] { R.id.child });
        setListAdapter(adapter);
 
	    }  
	  
	    /**得到手机通讯录联系人信息**/  
	    private void getPhoneContacts() {  
	    ContentResolver resolver = mContext.getContentResolver();  
	    HashMap<String, String> maps = new HashMap<String, String>();
	    // 获取手机联系人  
	    Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);  
        maps.put("contactsName", "添加所有手机联系人到自造词词库");
        child1.add(maps); 
	    
	    if (phoneCursor != null) {  
	        while (phoneCursor.moveToNext()) {  
	  
	        //得到手机号码  
	        String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
	        //当手机号码为空的或者为空字段 跳过当前循环  
	        if (TextUtils.isEmpty(phoneNumber))  
	            continue;  
	          
	        //得到联系人名称  
	        String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);  
	        maps = new HashMap<String, String>(); 
	        maps.put("contactsName", contactName);
	        child1.add(maps);  
	        }  	  
	        phoneCursor.close();  
	    }  
	    }  
	      
	    /**得到手机SIM卡联系人人信息**/  
	    private void getSIMContacts() {  
	    ContentResolver resolver = mContext.getContentResolver();  
	    // 获取SIM卡联系人  
	    Uri uri = Uri.parse("content://icc/adn");  
	    Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,  
	        null);  
	    HashMap<String, String> maps = new HashMap<String, String>();
	    maps.put("contactsName", "添加所有SIM卡联系人到自造词词库");
        child2.add(maps); 
	    if (phoneCursor != null) {  
	        while (phoneCursor.moveToNext()) {  
	  
	        // 得到手机号码  
	        String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
	        // 当手机号码为空的或者为空字段 跳过当前循环  
	        if (TextUtils.isEmpty(phoneNumber))  
	            continue;  
	        // 得到联系人名称  
	        String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);  
	        
	        //SIM卡中没有联系人头像  
	        maps = new HashMap<String, String>(); 
	        maps.put("contactsName", contactName);
	        child2.add(maps);  
	        }  	  
	        phoneCursor.close();  
	    }  
	    }

	/*	@Override
		protected void onListItemClick(ListView l, View v, int position, long id) {
			// TODO Auto-generated method stub
			if(position == 0)
			{
				;
			}
			else
			{
				
				String temp =  mContactsName.get(position).values().toString();
				String tmp = temp.substring(1, temp.length()-1);
			        *//*******************************************************//*
					Charset cst = Charset.forName("gbk");
					ByteBuffer buffer = cst.encode(tmp);
					byte[] bytes = buffer.array();
					System.out.println(tmp);
					Toast.makeText(ContactsActivity.this, temp, Toast.LENGTH_LONG).show();
					for(int i = 0;i<bytes.length;i++)
					{
						System.out.printf("%x",bytes[i]);
					}		
					System.out.println();
					*//***************************************************//*		
			}
			super.onListItemClick(l, v, position, id);
		} */
	    @Override
		public boolean setSelectedChild(int groupPosition, int childPosition,
				boolean shouldExpandGroup) {
			// TODO Auto-generated method stub
			return super.setSelectedChild(groupPosition, childPosition, shouldExpandGroup);
		}

		@Override
		public void setSelectedGroup(int groupPosition) {
			// TODO Auto-generated method stub
			super.setSelectedGroup(groupPosition);
		}

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			return super.onChildClick(parent, v, groupPosition, childPosition, id);
		}
	    
	    
	}  
