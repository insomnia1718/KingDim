package zhang.stu.NewInputMethod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SettingActivity extends Activity{

	private Button myButton = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		myButton = (Button)findViewById(R.id.myButton);
		myButton.setOnClickListener(new MyButtonListener());
	}
   public class MyButtonListener implements OnClickListener{

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(SettingActivity.this, KingDimInputMethodService.class);//????????????????
		startService(intent);
	}
	   
   }
}
