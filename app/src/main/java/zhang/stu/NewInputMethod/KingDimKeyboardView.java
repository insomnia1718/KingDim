

package zhang.stu.NewInputMethod;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.util.AttributeSet;

//keyboard类通常来说就只是一个概念而已，并不能实例出来一个UI,所以需要借助于一个VIEW类来进行绘制。
public class KingDimKeyboardView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;//设了一个无用的键值，等到后面调用

    //当继承View的时候，会有一个含有AttributeSet参数的构造方法,通过此类就可以得到自己定义的xml属性，也可以是android的内置的属性
    public KingDimKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KingDimKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //有长时间按键事件的时候会调用
    @Override
    protected boolean onLongPress(Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);//给键盘监听器发送一个OPTIONS键被按下的事件
            return true;
        } else {
            return super.onLongPress(key);
        }
    }

}
