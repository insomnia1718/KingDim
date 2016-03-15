package zhang.stu.NewInputMethod;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
//字造词的界面
public class SelfMadeWords extends Activity {
	
	private MyEditText editT1;//MyEditText是自己定义的继承自EditText的一个类，里面有自定义的拖动选择文本的功能和自定义的下拉菜单
	private Button bnt1;
	private Button bnt2;
	private Button bnt3;
	private Button bnt4;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selfmadewords);
      //  this.registerForContextMenu(findViewById(R.id.editT1));
        editT1 = (MyEditText)findViewById(R.id.editT1);
        this.registerForContextMenu(editT1);		
		bnt1 = (Button)findViewById(R.id.button1);
		bnt2 = (Button)findViewById(R.id.button2);
		bnt3 = (Button)findViewById(R.id.button3);	
		bnt4 = (Button)findViewById(R.id.button4);	
		
		bnt1.setOnClickListener(new ButtonOnClickListener1());
		bnt2.setOnClickListener(new ButtonOnClickListener2());
		bnt3.setOnClickListener(new ButtonOnClickListener3());		
		bnt4.setOnClickListener(new ButtonOnClickListener4());		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(200,200,200,"添加到自造词词库");			
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId()==200)
		{		
			 String  temp = null;
			if(editT1.isEqual())
			{
	        temp = editT1.getText().toString();
	        /*******************************************************/
			Charset cst = Charset.forName("gbk");
			ByteBuffer buffer = cst.encode(temp);
			byte[] bytes = buffer.array();
			System.out.println(temp);
			writeFileData("SelfMadeWords",temp+"\r\n");
			for(int i = 0;i<bytes.length;i++)
			{
				System.out.printf("%x",bytes[i]);
			}		
			System.out.println();
			/***************************************************/			
				if(temp.trim().length()==0)//当没选择或者没输入自造词的时候，用一个Toast来提示用户。
				{
					Toast.makeText(SelfMadeWords.this, "没有选中要加入的自造词!", Toast.LENGTH_LONG).show();
				}
			}
			else{
				editT1.getSeleted();
			}
			return true;
		}
		else
		{
		   return super.onContextItemSelected(item);
		}
	}
	
	 class ButtonOnClickListener1 implements OnClickListener {
		 
		public void onClick(View v) {
			String temp = editT1.getText().toString();
		
			// TODO Auto-generated method stub
			if(editT1.isEqual())
			{
				if(temp.trim().length()==0)
				{
					Toast.makeText(SelfMadeWords.this, "没有选中要加入的自造词!", Toast.LENGTH_LONG).show();
				}
				else{				
					System.out.println(temp);
					writeFileData("SelfMadeWords",temp+"\r\n");//写入自造词，以回车键结尾。
					/*******************************************************/
					Charset cst = Charset.forName("gbk");//利用Charset对象实现unicode向gbk的转化。
					ByteBuffer buffer = cst.encode(temp);
					byte[] bytes = buffer.array();
					for(int i = 0;i<bytes.length;i++)
					{
						System.out.printf("%x",bytes[i]);
					}
					System.out.println();
					/***************************************************/
				}
			}
			else
			{
				editT1.getSeleted();
			}
		}		
	}
	 
	 class ButtonOnClickListener2 implements OnClickListener {
		 
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ClipboardManager cm =(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
				String temp = cm.getText().toString();		//利用一个ClipboardManager对象获取剪切板上的内容
				if(cm==null || temp==null || temp.trim().length()==0)
				{
				   Toast.makeText(SelfMadeWords.this, "剪切板中没有选定的内容!", Toast.LENGTH_LONG).show();
				}
				else{
					System.out.println(temp);
					writeFileData("SelfMadeWords",temp+"\r\n");
					/*******************************************************/
					Charset cst = Charset.forName("gbk");
					ByteBuffer buffer = cst.encode(temp);
					byte[] bytes = buffer.array();
					for(int i = 0;i<bytes.length;i++)
					{
						System.out.printf("%x",bytes[i]);
					}
					System.out.println();
					/***************************************************/

				}
			}		
	}
	 
	 class ButtonOnClickListener3 implements OnClickListener {
		 //跳到另一个activity，从联系人中导入自造词
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent2 = new Intent();
        		intent2.setClass(SelfMadeWords.this, ContactsActivity.class);
        		startActivity(intent2);   
			}		
	}
	 class ButtonOnClickListener4 implements OnClickListener {
		 
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent2 = new Intent();
     		intent2.setClass(SelfMadeWords.this,LastestInputWordsList.class);
     		startActivity(intent2);   
			}		
	}

	 public void writeFileData(String fileName,String message){ 
	       try{ 
	        FileOutputStream fout =openFileOutput(fileName, MODE_APPEND);//以追加形式写入
	        byte [] bytes = message.getBytes(); 
	        fout.write(bytes); 
	         fout.close(); 
	        } 
	       catch(Exception e){ 
	        e.printStackTrace(); 
	       } 
	   }
}
