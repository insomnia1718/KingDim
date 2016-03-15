package zhang.stu.NewInputMethod.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import zhang.stu.NewInputMethod.R;

/**
 * Created by tao.zhang on 2016/3/10.
 */
public class KeyView extends LinearLayout {
    private TextView key_number;
    private TextView key_word;
    private ImageView key_icon;
    private int code;
    private String input_text;
    private String up;
    private String down;

    public KeyView(Context context) {
        super(context);
        init(context, null);
    }

    public KeyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public KeyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.key_layout, this);
        key_number = (TextView) findViewById(R.id.key_number);
        key_word = (TextView) findViewById(R.id.key_word);
        key_icon = (ImageView) findViewById(R.id.key_icon);
        key_number.setVisibility(View.GONE);
        key_word.setVisibility(GONE);
        key_icon.setVisibility(GONE);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KeyView);
        if (typedArray != null) {
            up = typedArray.getString(R.styleable.KeyView_text_up);
            down = typedArray.getString(R.styleable.KeyView_text_down);
            Drawable icon = typedArray.getDrawable(R.styleable.KeyView_icon);
            input_text = typedArray.getString(R.styleable.KeyView_input_text);
            int text_up_color = typedArray.getColor(R.styleable.KeyView_text_up_color, Color.DKGRAY);
            int text_down_color = typedArray.getColor(R.styleable.KeyView_text_down_color, Color.BLACK);
            int text_up_size = typedArray.getInteger(R.styleable.KeyView_text_up_size, 15);
            int text_down_size = typedArray.getInteger(R.styleable.KeyView_text_down_size, 18);
            code = typedArray.getInteger(R.styleable.KeyView_code, 0);
            typedArray.recycle();
            if (!TextUtils.isEmpty(up)) {
                key_number.setText(up);
                key_number.setVisibility(VISIBLE);
            }
            if (!TextUtils.isEmpty(down)) {
                key_word.setText(down);
                key_word.setVisibility(VISIBLE);
            }
            if (icon != null) {
                key_icon.setImageDrawable(icon);
                key_icon.setVisibility(View.VISIBLE);
            }
            key_number.setTextColor(text_up_color);
            key_word.setTextColor(text_down_color);
            key_number.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_up_size);
            key_word.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_down_size);
            toLowcase();
        }
    }

    public String getInput_text() {
        return input_text;
    }

    public void setInput_text(String input_text){
        this.input_text = input_text;
    }

    public int getCode() {
        return code;
    }

    public void toUpcase() {
        if (down != null) {
            key_word.setText(down.toUpperCase());
            setInput_text(down.toUpperCase());
        }
    }

    public void toLowcase() {
        if (down != null) {
            key_word.setText(down.toLowerCase());
            setInput_text(down.toLowerCase());
        }
    }
}
