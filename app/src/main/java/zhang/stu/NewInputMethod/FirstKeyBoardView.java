package zhang.stu.NewInputMethod;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tao.zhang on 2016/3/10.
 */
public class FirstKeyBoardView extends KeyboardView {
    public FirstKeyBoardView(Context context){
        this(context,null);
    }
    public FirstKeyBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FirstKeyBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context,AttributeSet attrs){
        View.inflate(context,R.layout.input,null);

    }
}
