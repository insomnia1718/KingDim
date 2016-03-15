package zhang.stu.NewInputMethod;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class CandidateView extends View {

    private static final int OUT_OF_BOUNDS = -1;

    private KingDimInputMethodService mService;//candidateView的宿主类，也就是该view是为什么输入法服务的。
    private List<String> mSuggestions;//建议列表
    private int mSelectedIndex;//用户选择的词的索引
    private int mTouchX = OUT_OF_BOUNDS;
    private Drawable mSelectionHighlight;//描绘选择区域高亮
    private boolean mTypedWordValid;//键入的word是否合法正确
    
    private Rect mBgPadding;//背景填充区域，决定将要在那个部分显示

    private static final int MAX_SUGGESTIONS = 30;
    private static final int SCROLL_PIXELS = 20;
    
    private int[] mWordWidth = new int[MAX_SUGGESTIONS];//候选词的每个词的宽度
    private int[] mWordX = new int[MAX_SUGGESTIONS];//每个候选词的X坐标

    private static final int X_GAP = 10;// 两个候选词之间的间隙
    
    private static final List<String> EMPTY_LIST = new ArrayList<String>();

    //所有关于绘制的信息，比如线条的颜色等
    private int mColorNormal;
    private int mColorRecommended;
    private int mColorOther;
    private int mVerticalPadding;
    private Paint mPaint;
    private boolean mScrolled;
    private int mTargetScrollX;
    
    private int mTotalWidth;
    
    private GestureDetector mGestureDetector;
    
    private PopupWindow mPopupWindow;

    private int t;
    private TextView Candidateview;
    public int wordWidth;
    private int nowWidth=-100;
    View dialogView;
    LayoutInflater inflater;
    private TextView tvv;
    /**
     * Construct a CandidateView for showing suggested words for completion.
     * @param context
     * @param attrs
     */
    public CandidateView(Context context) {
        super(context);
        
        DisplayMetrics dm2 = getResources().getDisplayMetrics();   
        //System.out.println("heigth2 : " + dm2.heightPixels);   
        //System.out.println("width2 : " + dm2.widthPixels); 
        
        mPopupWindow = new PopupWindow(context); 

 
        t = mSelectedIndex;
        mPopupWindow.setHeight(dm2.heightPixels/8);
        mPopupWindow.setWidth(dm2.heightPixels/8);
        Candidateview=new TextView(context);
        Candidateview.setHeight(46);
        Candidateview.setTextColor(Color.BLACK);
        Candidateview.setBackgroundColor(Color.WHITE);
        Candidateview.setWidth(46);
        Candidateview.setGravity(Gravity.CENTER);
        Candidateview.setTextSize(dm2.widthPixels/10);
     

      
        mPopupWindow.setContentView(Candidateview);

        
        //getResouces这个函数用来得到这个应用程序的所有资源,就连android自带的资源也要如此
        mSelectionHighlight = context.getResources().getDrawable(
                android.R.drawable.list_selector_background);
        mSelectionHighlight.setState(new int[] {
                android.R.attr.state_enabled,
                android.R.attr.state_focused,
                android.R.attr.state_window_focused,
                android.R.attr.state_pressed
        });

        Resources r = context.getResources();
        /*
        setBackgroundColor(r.getColor(R.color.candidate_background));
        
        mColorNormal = r.getColor(R.color.candidate_normal);
        mColorRecommended = r.getColor(R.color.candidate_recommended);
        mColorOther = r.getColor(R.color.candidate_other);
        mVerticalPadding = r.getDimensionPixelSize(R.dimen.candidate_vertical_padding);
        */
        setBackgroundColor(Color.WHITE);
        mColorNormal = Color.BLUE;
        mColorRecommended = Color.BLUE;
        mColorOther = Color.BLACK;
        mVerticalPadding = r.getDimensionPixelSize(R.dimen.candidate_vertical_padding);
        
        
        mPaint = new Paint();
        mPaint.setColor(mColorNormal);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_font_height));
        mPaint.setStrokeWidth(0);
        
        //手势监听器重载
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
          	  switch(e2.getAction())
          	   {
            	  case  MotionEvent.ACTION_UP:
            		 
          		  break;
          	   }
        	  Log.i("MSG","onScroll e1.getX() e1.getY()--->"+String.valueOf(e1.getX())+"--"+String.valueOf( e1.getY()));
        	  Log.i("MSG","onScroll e2.getX() e2.getY()--->"+String.valueOf(  e2.getX())+"--"+String.valueOf( e2.getY()));
          	  
                mScrolled = true;
                int sx = getScrollX();//得到滑动开始的横坐标
                Log.i("MSG","onScrollges----->"+String.valueOf(sx));
                sx += distanceX;
                if (sx < 0) {//是否越过了最小的最左边
                    sx = 0;
                }
                if (sx + getWidth() > mTotalWidth) {//看是否超过了最大宽度                
                    sx -= distanceX;
                }
                mTargetScrollX = sx;//记录将要移动到的位置，后面会用到
                scrollTo(sx, getScrollY());
                invalidate();// 使整个View无效，导致调用onDraw重绘
                return true;
            }
        });
        //设置候选View的初始状态
        setHorizontalFadingEdgeEnabled(true);//设置水平边缘淡出效果
        setWillNotDraw(false);//设置这个候选视图是否需要draw自己?如果view子类中重载了onDraw（Canvas）方法，必须调用该函数
        setHorizontalScrollBarEnabled(false);//设置水平滚动条是否显示
        setVerticalScrollBarEnabled(false);//设置垂直滚动条是否显示
    }
    
    /**
     * A connection back to the service to communicate with the text field
     * @param listener
     */
    public void setService(KingDimInputMethodService listener) {
        mService = listener;
    }
    
    @Override
    public int computeHorizontalScrollRange() {//表示这个VIEW的水平滚动区域，返回候选视图的总体宽度。
        return mTotalWidth;
    }

    /*
     * 重载的view类，它主要是在布局阶段被父视图所调用。比如当父视图需要根据其子视图的大小来进行布局时，
     * 就需要回调这个函数来看该view的大小。
     * */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = resolveSize(100, widthMeasureSpec);
        
        // Get the desired height of the icon menu view (last row of items does
        // not have a divider below)
        Rect padding = new Rect();
        //高亮区域除了字以外，剩下的空隙，用getPadding得到
        mSelectionHighlight.getPadding(padding);
        //这里用来计算整个候选条的应有高度，当然，得考虑字体的高度，这也是重载的原因
        final int desiredHeight = ((int)mPaint.getTextSize()) + mVerticalPadding
                + padding.top + padding.bottom;
        
        // Maximum possible width and desired height
        //设置宽高，这是View类的函数。整个候选栏的大小都是由这个语句决定的
        setMeasuredDimension(measuredWidth,
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    /**
     * If the canvas is null, then only touch calculations are performed to pick the target
     * candidate.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas != null) {
            super.onDraw(canvas);
        }
        mTotalWidth = 0;
        if (mSuggestions == null) return;//如果没有候选词就不需要绘制
        
        if (mBgPadding == null) {//初始化背景的填充区域，直接view的背景中得到即可
            mBgPadding = new Rect(0, 0, 0, 0);
            if (getBackground() != null) {
                getBackground().getPadding(mBgPadding);
            }
        }
        int x = 0;//候选词的起始位置
        final int count = mSuggestions.size(); 
        final int height = getHeight();
        final Rect bgPadding = mBgPadding;
        final Paint paint = mPaint;
        final int touchX = mTouchX;
        final int scrollX = getScrollX();
        final boolean scrolled = mScrolled;
        final boolean typedWordValid = mTypedWordValid;
        final int y = (int) (((height - mPaint.getTextSize()) / 2) - mPaint.ascent());

       //添加候选词
        for (int i = 0; i < count; i++) {
            String suggestion = mSuggestions.get(i);
            float textWidth = paint.measureText(suggestion);//获取候选词宽度
            wordWidth = (int) textWidth + X_GAP * 2;//候选词显示宽度是词语宽度加上两倍间隙

            mWordX[i] = x;//保存候选词显示的X坐标
            mWordWidth[i] = wordWidth;//保存每个候选词的显示宽度
            paint.setColor(mColorNormal);//设置候选词的颜色
            if (touchX + scrollX >= x && touchX + scrollX < x + wordWidth && !scrolled) {//判断是否选择了当前词
                if (canvas != null) {
                    canvas.translate(x, 0);
                    mSelectionHighlight.setBounds(0, bgPadding.top, wordWidth, height);//设置高亮绘画区域边界
                    mSelectionHighlight.draw(canvas);//画高亮区域
                    canvas.translate(-x, 0);
                }
                mSelectedIndex = i;//保存被选词的索引
                
                DisplayMetrics dm2 = getResources().getDisplayMetrics();   
                //System.out.println("heigth2 : " + dm2.heightPixels);   
                //System.out.println("width2 : " + dm2.widthPixels);     
            }
                   

            	
             if(mSelectedIndex>=0){
             	if(nowWidth!=mSelectedIndex){                 

             		if(mPopupWindow.isShowing()==true)
             			mPopupWindow.dismiss();
             		CreatePop();
             		
             		mPopupWindow.setWidth(wordWidth+20);
             		nowWidth=mSelectedIndex;
             	}
             	
             	tvv= (TextView) dialogView.findViewById(R.id.contview); 
             	tvv.setText(mSuggestions.get(mSelectedIndex));

                 mPopupWindow.showAtLocation(getRootView().getRootView(), Gravity.CENTER_HORIZONTAL|Gravity.TOP,0, -100);
                 

     	    }      
           if(mScrolled)
           {
           	mPopupWindow.dismiss(); 
           }
          
            if (canvas != null) {
                if ((i == 1 && !typedWordValid) || (i == 0 && typedWordValid)) {
                    paint.setFakeBoldText(true);//第一个候选词设为粗体
                    paint.setColor(mColorRecommended);
                } else if (i != 0) {
                  //  paint.setColor(mColorOther);
                	paint.setColor(mColorRecommended);
                }
                canvas.drawText(suggestion, x + X_GAP, y, paint);//绘制候选词，这里决定了候选词的显示位置
                paint.setColor(mColorOther); 
                canvas.drawLine(x + wordWidth + 0.5f, bgPadding.top,//画候选词之间的分割线 
                        x + wordWidth + 0.5f, height + 1, paint);
                paint.setFakeBoldText(false);//设置回非粗体
            }
            x += wordWidth;
        }
       
        mTotalWidth = x;
      //  判断目标滚动是否是当前的，不是就需要滚动过去。
//        if (mTargetScrollX != getScrollX()) {//每次滑动，都会造成mTargetScrollX改变，因为他在动作监听函数里面赋值
//            scrollToTarget();
//        }
        
    }
    
    private void CreatePop(){
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        dialogView = inflater.inflate(R.layout.popupwindow, null, false);
        dialogView.setBackgroundResource(R.layout.rounded_corners_view);
        mPopupWindow = new PopupWindow(dialogView,60,60,false);
        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.layout.rounded_corners_pop)); 
    }
    
    private void scrollToTarget() {
        int sx = getScrollX();//?Return the left edge of the displayed part of your view, in pixels.
        if (mTargetScrollX > sx) {
            sx += SCROLL_PIXELS;
            if (sx >= mTargetScrollX) {
                sx = mTargetScrollX;
                requestLayout();
            }
        } else {
            sx -= SCROLL_PIXELS;
            if (sx <= mTargetScrollX) {
                sx = mTargetScrollX;
                requestLayout();
            }
        }
        scrollTo(sx, getScrollY());
        invalidate();
    }
    
    public void scrollbyword(int direction){
    	mScrolled = true;
    	switch (direction){
    	case -6:
    		
    		if(this.getScrollX()>=0){
    			scrollBy(-getWidth(),0);
    			//System.out.println("case -6");
        		}
    		if(this.getScrollX()<0){
    			this.scrollTo(0, 0);
    		}
    		
    		break;
    	case -7:
    		if(this.getScrollX()>=0){
    			scrollBy((-wordWidth),0);
    			//System.out.println("case -7");
        		}
    		if(this.getScrollX()<0){
    			this.scrollTo(0, 0);
    		}
    		
    		break;
    	case -8:
    		if(this.getScrollX()<mTotalWidth){
    			scrollBy(getWidth(),0);
    			//System.out.println("case -8");
        		}
    		if(this.getScrollX()+getWidth()>=mTotalWidth){
    			this.scrollTo(mTotalWidth-getWidth(), 0);
    		}
    		break;
    	case -9:
    		System.out.println(getScrollX()+"  "+mTotalWidth);
    		System.out.println(getWidth()+"  "+getRightFadingEdgeStrength());
    		System.out.println(getHeight()+"  "+getLeft()+" "+getWidth());
    		if(this.getScrollX()+getWidth()<mTotalWidth){
    		scrollBy(wordWidth,0);
    		//System.out.println("case -9");
    		}
    		if(this.getScrollX()+getWidth()>=mTotalWidth){
    			this.scrollTo(mTotalWidth-getWidth(), 0);
    		}
    		break;
    		
    	}
    	invalidate();
    	
    }
    
    
    public void setSuggestions(List<String> suggestions, boolean completions,
            boolean typedWordValid) {
    	if(!completions)
    	{
    		mService.setCandidatesViewShown(false);
    	}
        clear();
        if (suggestions != null) {
            mSuggestions = new ArrayList<String>(suggestions);
        }
        mTypedWordValid = typedWordValid;
        scrollTo(0, 0);
        mTargetScrollX = 0;
        // Compute the total width
        onDraw(null);//onDraw的参数为null的时候，他不再执行super里面的onDraw
        invalidate();
        requestLayout();
    }

    public void clear() {
        mSuggestions = EMPTY_LIST;//将候选词数组设为空数组
        mTouchX = OUT_OF_BOUNDS;//将触摸点的横坐标设为-200，表示无触摸
        mSelectedIndex = -1;
        invalidate();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent me) {

        /***
         * 前面定义的GestureDetector.SimpleOnGestureListener，
         * 是GestureDetector.OnGestureListener的派生类
		 * GestureDetector.OnGestureListener使用的时候，这里会返回 true
         * ****/
    	if (mGestureDetector.onTouchEvent(me)) {
            return true;
        }

        int action = me.getAction();
        int x = (int) me.getX();
        int y = (int) me.getY();
        mTouchX = x;//被点击的点的横坐标

        //如果后续出现滑动，又会被前面那个监听器监听到并返回
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mScrolled = false;
            invalidate();
            break;
        case MotionEvent.ACTION_MOVE:
            if (y <= 0) {
                // Fling up!?
                if (mSelectedIndex >= 0) {
                    mService.pickSuggestionManually(mSelectedIndex,mSuggestions);
                    mSelectedIndex = -1;
                }
            }
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
            if (!mScrolled) {
                if (mSelectedIndex >= 0) {
                    mService.pickSuggestionManually(mSelectedIndex,mSuggestions);
                }
            }
            mPopupWindow.dismiss(); 
            invalidate();
            mSelectedIndex = -1;
            removeHighlight();
            requestLayout();
            break;
        }
        return true;
    }
    
    /**
     * For flick through from keyboard, call this method with the x coordinate of the flick 
     * gesture.
     * @param x
     */
    public void takeSuggestionAt(float x) {
        mTouchX = (int) x;
        // To detect candidate
        onDraw(null);
        if (mSelectedIndex >= 0) {
            mService.pickSuggestionManually(mSelectedIndex,mSuggestions);
        }
        invalidate();
    }

    private void removeHighlight() {////取消高亮区域的显示，等待下次生成
        mTouchX = OUT_OF_BOUNDS;
        invalidate();
    }
}

/***
 * 候选词视图。 当用户输入一些字符之后，输入法可能需要提供给用户一些可用的候选词的列表。
 * 这个视图的管理和输入视图不大一样，因为这个视图是非常的短暂的，它只是在有候选词的时候才会被显示。
 * 可以用setCandidatesViewShow来设置是否需要显示这个视图。正是因为这个显示的频繁性，所以它一般不会被销毁，
 * 而且不会改变你的应用程序的视图。
 * **/
