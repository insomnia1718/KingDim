package zhang.stu.NewInputMethod;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import  android.widget.EditText;
import  android.content.Context;  
import  android.text.Layout;  
import  android.text.Selection;  
import  android.util.AttributeSet;
import  android.util.Log;  
import  android.view.MotionEvent;  

 
/**  
 * @author 
 */   
public   class  MyEditText  extends  EditText {  
    private   int  off;  //字符串的偏移值   
    private   int curOff;
    private String TAG;
    public  MyEditText(Context context) {  
        super (context);  
      //  initialize();  
    }  
    public MyEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public MyEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

   /* private   void  initialize() {  
        setGravity(Gravity.TOP);  
        setBackgroundColor(Color.WHITE);  
    }  */
 
    /*
    @Override   
    public   boolean  getDefaultEditable() {  
        return   false ;  
    }  */
 
 
     public   boolean  onTouchEvent(MotionEvent event) {  
    	super.onTouchEvent(event);
        int  action = event.getAction();  
        Layout layout = getLayout();  
        int  line =  0 ;  
        switch (action) {  
        case  MotionEvent.ACTION_DOWN:  
            line = layout.getLineForVertical(getScrollY()+ (int )event.getY());          
            off = layout.getOffsetForHorizontal(line, (int )event.getX());  
            Selection.setSelection(getEditableText(), off);  
            Log.d(TAG, "down");
            break ;  
        case  MotionEvent.ACTION_MOVE:  
        case  MotionEvent.ACTION_UP:  
            line = layout.getLineForVertical(getScrollY()+(int )event.getY());   
            curOff = layout.getOffsetForHorizontal(line, ( int )event.getX());              
            Selection.setSelection(getEditableText(), off, curOff);    
            Log.d(TAG, "up");
            break ;  
        }  
        System.out.println("off = "+ off +" curOff = "+curOff);
        return   true ;  
    }  
     
    public  void getSeleted()
     {
    	 String word ;
         if(curOff<=off){
         	word = this.getText().toString().substring(curOff, off);
         }
         else
         {
         	word = this.getText().toString().substring(off, curOff);
         }
         /*******************************************************/
			Charset cst = Charset.forName("gbk");
			ByteBuffer buffer = cst.encode(word);
			byte[] bytes = buffer.array();
			for(int i = 0;i<bytes.length;i++)
			{
				System.out.printf("%x",bytes[i]);
			}		
		/***************************************************/	
         System.out.println("off="+off+"  "+"curOff="+curOff);
         System.out.println(word);
     }
    public boolean isEqual()
    {
    	if(off==curOff)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
}
