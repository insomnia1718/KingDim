package zhang.stu.NewInputMethod.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import zhang.stu.NewInputMethod.R;

/**
 * Created by tao.zhang on 2016/3/16.
 */
public class CandidateLayout extends LinearLayout implements View.OnClickListener {
    private ImageView can_left;
    private ImageView can_right;
    private LinearLayout can_content;
    private Context context;
    private List<String> suggestions;
    private int pageIndex = 0;
    private int pageCount = 1;
    private OnChooseSuggestionListener listener;

    public interface OnChooseSuggestionListener{
        void onChoose(String suggestion);
    }

    public void setOnChooseSuggestionListener(OnChooseSuggestionListener listener){
        this.listener = listener;
    }

    public CandidateLayout(Context context) {
        super(context);
        init(context);
    }

    public CandidateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CandidateLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        View.inflate(context, R.layout.cand_layout, this);
        can_left = (ImageView) findViewById(R.id.can_left);
        can_right = (ImageView) findViewById(R.id.can_right);
        can_content = (LinearLayout) findViewById(R.id.can_content);
        can_left.setOnClickListener(this);
        can_right.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == can_left) {
            if (pageIndex == 0 || pageCount == 1) return;
            else {
                pageIndex--;
                updateCanView();
            }
        }else if(v == can_right){
            if(pageCount == 1 || pageIndex == pageCount -1) return;
            else{
                pageIndex++;
                updateCanView();
            }
        }else{
            if(v.getTag()!=null) {
                if(listener!=null) {
                    listener.onChoose((String) v.getTag());
                }
            }
        }
    }

    private void updateCanView(){
        can_content.removeAllViews();
        Log.d("count",String.valueOf(pageCount));
        Log.d("index", String.valueOf(pageIndex));
        if(pageIndex == pageCount - 1){
            for (int i = pageIndex*5;i < suggestions.size();i++){
                can_content.addView(getTextView(suggestions.get(i)));
            }
        }else {
            for (int i = pageIndex * 5; i < pageIndex * 5 + 5; i++) {
                can_content.addView(getTextView(suggestions.get(i)));
            }
        }
    }

    public void setSuggestions(List<String> suggestions) {
        if(suggestions == null || suggestions.size() == 0) return;
        this.suggestions = suggestions;
        this.pageIndex = 0;
        this.pageCount = 1;
        if (can_content != null) {
            can_content.removeAllViews();
        }
        if (suggestions.size() <= 5) {
            for (String suggestion : suggestions) {
                can_content.addView(getTextView(suggestion));
            }
        } else {
            pageCount = (int) Math.ceil((double) suggestions.size()/5);
            updateCanView();
        }
    }

    private TextView getTextView(String text) {
        TextView textView = new TextView(context);
        textView.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        textView.setTag(text);
        textView.setText(text);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);
        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.key_bg_selector));
        textView.setOnClickListener(this);
        return textView;
    }

    private int dpToPx(int dp) {
        final float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
