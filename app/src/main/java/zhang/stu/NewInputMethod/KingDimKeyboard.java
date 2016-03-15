
package zhang.stu.NewInputMethod;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.view.inputmethod.EditorInfo;

public class KingDimKeyboard extends Keyboard {

    private Key mEnterKey;

    static final int KEYCODE_MODE_CHANGE_HANZI = -200;
    static final int KEYCODE_MODE_CHANGE_SETTING = -300;
    static final int KEYCODE_MODE_CHANGE_123 = -400;
    static final int KEYCODE_MODE_CHANGE_ABC = -500;
    static final int KEYCODE_MODE_CHANGE_SYMBOL=-700;
   // static final int KEYCODE_MODE_CHANGE_LABEL = -6001;
    static final int KEYCODE_MODE_CHANGE_bihua = -6000;//lk 转换为旧笔画输入法键盘
    
    static final String Symbol[]={
    	":-D",":-(",":-O",":-p",":-|","^0^",
    	"（⊙o⊙）","→_→","T_T","-_-!","-_-|||","(∩＿∩)",
    	"(^O^)","(x_x)?" ,"=_=凸","(¯(oo)¯)","orz","囧"
    };
    public KingDimKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public KingDimKeyboard(Context context, int layoutTemplateResId, 
            CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }

//键盘在描绘键的时候调用，它是从一个xml资源文件中载入一个键，并且放置在(x,y)坐标处
    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, 
            XmlResourceParser parser) {
        Key key = new KingDimKey(res, parent, x, y, parser);
        if (key.codes[0] == -10) {
            mEnterKey = key;//保存回车键，在下文中根据输入上下文EditorInfo对回车键的label进行改变
        }
        return key;
    }
    
//判断当前的编辑器给定的ime options来为键盘上的Enter键设定合适的label.setImeOptions方法在主程序的onStartInput回调函数中调用。
//传入了EditorInfo.imeOptions类型的options参数。此变量地位与EditorInfo.inputType类似。
     
    void setImeOptions(Resources res, int options) {
        if (mEnterKey == null) {
            return;
        }
        //只要加载了EditorInfo的包，就可以使用其中的常量
        switch (options&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO://这个是go操作，它将用户带入到一个该输入目标的目标的动作。那么确认键将不会有icon，但是有label：GO

                mEnterKey.icon = null;//把图标设为空，并不代表就是空，只是下面的可以代替之
                mEnterKey.label = res.getText(R.string.label_go_key);
            	mEnterKey.iconPreview = res.getDrawable(//lk 设置enter的popup图标
                        R.drawable.keyboard_go_b);
                break;
            case EditorInfo.IME_ACTION_NEXT://这个是next操作，它将用户带入到该应用程序的下一个输入目标中。
            	mEnterKey.iconPreview = res.getDrawable(//lk 设置enter的popup图标
                        R.drawable.keyboard_next_b);
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_next_key);
                break;
            case EditorInfo.IME_ACTION_SEARCH://这个是search操作，它的默认动作就是搜索。比如在URL框中输入的时候，默认的就是search操作，它提供了一个像放大镜一样的icon。
            	mEnterKey.iconPreview = res.getDrawable(//lk 设置enter的popup图标
                        R.drawable.keyboard_search_b);
            	mEnterKey.icon = res.getDrawable(
                        R.drawable.keyboard_search);
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND://这个是send操作，它的默认动作就是发送当前的内容。比如在短消息的内容框里面输入的时候，后面通常就是一个发送操作。它也是只提供一个Label：SEND
            case EditorInfo.IME_ACTION_SEND+EditorInfo.IME_FLAG_NO_ENTER_ACTION:
            	mEnterKey.iconPreview = res.getDrawable(//lk 设置enter的popup图标
                        R.drawable.keyboard_send_b);
                mEnterKey.icon =  null;
                mEnterKey.label = res.getText(R.string.label_send_key);
                break;
            default:
                mEnterKey.iconPreview = res.getDrawable(
                        R.drawable.enter_icon_b);
                mEnterKey.icon = res.getDrawable(
                        R.drawable.enter_icon);
                mEnterKey.label = null;
                break;
        }
    }
    
    @Override
	public List<Key> getKeys()
	{
		// TODO Auto-generated method stub   	   	
		return super.getKeys();
	}
    public Key findKey(int KeyCode)
    {
    	List<Key> kingDIMKeyList;//=new ArrayList<Keyboard.Key>();
    	kingDIMKeyList=getKeys();
    	Keyboard.Key array_element;
    	Key FindKey =	null;
		for (int i = 0; i < kingDIMKeyList.size(); i++)
		{
    		 array_element = kingDIMKeyList.get(i);
    		if(array_element.codes[0]==KeyCode)
    			FindKey=array_element;		
		} 	   	
		return FindKey; 	
    }
    //KingDimKeyboard（Keyboard）内部类KingDimKeyboard.KingDimKey（Keyboard.Key）的构造函数，在createKeyFromXml中调用
    static class KingDimKey extends Keyboard.Key {
        
        public KingDimKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
        }
        
    //用来判断一个坐标是否在该键内,覆盖该方法，可以改变某些键的作用区域
        @Override
        public boolean isInside(int x, int y) {
            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);//将cancel键的作用范围减小了10以防止错误点击
        }
    }

}
