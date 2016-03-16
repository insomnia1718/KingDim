

package zhang.stu.NewInputMethod;

//import android.content.res.Resources.NotFoundException;

import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
//import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import zhang.stu.NewInputMethod.widget.CandidateLayout;
import zhang.stu.NewInputMethod.widget.KeyView;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class KingDimInputMethodService extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener, View.OnClickListener {
    static final boolean DEBUG = false;
    public static int Mode = 0;//?????????? ????????0?????????Σ?1??????,?????0??????????????
    public static int LastMode = 0;
    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;//????????????????????????

    //    private KeyboardView mInputView;//????View???????????????KingDimKeyboardView
    private CandidateView mCandidateView;//?????????????????
//    private CompletionInfo[] mCompletions;

    private StringBuilder mComposing = new StringBuilder(); //??????????replacement of StringBuffer for non-concurrent use,default capacity is 16
    private boolean mPredictionOn;//???????????????
    private boolean mCompletionOn;//????auto????????????????
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;

    private KingDimKeyboard mSymbolsKeyboard;//???????1
    private KingDimKeyboard mSymbolsShiftedKeyboard;//???????2
    //private KingDimKeyboard mStrokesKeyboard;//??????? lk
    private KingDimKeyboard mQwertyKeyboard;//???????
    private KingDimKeyboard mNewyinxing_firstKeyboard;//???????????????
    private KingDimKeyboard mNewyinxing_secondKeyboard;//?????????????
    private KingDimKeyboard mNewyinxing_bihuaKeyboard;//????????????
    private KingDimKeyboard mSymbolicExpressionKeyboard;
    private KingDimKeyboard mCurKeyboard;//???????
    private String mWordSeparators;//????????????ж?????
    private static KingDimEngine kimCode;
    private LinearLayout linearLayout;
    private WindowManager mWindowManager;
    private LinearLayout main_input_view;
    private RelativeLayout funcLayout;
    private CandidateLayout canView;
    private LinearLayout yinmu_layout;
    private LinearLayout yunmu_layout;
    private LinearLayout bihua_layout;
    private Key ModeKey;
    private View pre_view;
    private View cur_view;
    private boolean shiftUp = false;
    public static StringBuilder lastestInputWords = new StringBuilder("      ");//?????????????????????,??????6???????

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    //??UI??????????
    @Override
    public void onCreate() {
        super.onCreate();
        InputStream is = getResources().openRawResource(R.raw.lib1zbz);
        InputStream InitCache = getResources().openRawResource(R.raw.initcache);
        InputStream InitCacheOrder = getResources().openRawResource(R.raw.initcacheorder);
        kimCode = new KingDimEngine(is, InitCache, InitCacheOrder);
        //getResources??contextWrapper??????????contextWrapper??InputMethodService???????
        mWordSeparators = getResources().getString(R.string.word_separators);
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    //????????????????????????????UI??????????
    @Override
    public void onInitializeInterface() {////?????????????????findViewById??????????????滹??
        //System.out.println("onInitializeInterface()");
        if (mNewyinxing_firstKeyboard != null) {//???????÷??????????
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();//???????????
            if (displayWidth == mLastDisplayWidth) return;//???δ???????????
            mLastDisplayWidth = displayWidth;//???????????
        }
        //??????????????????????
        // mNewyinxing_firstKeyboard = new KingDimKeyboard(this, R.xml.strokes);
        mSymbolsKeyboard = new KingDimKeyboard(this, R.xml.symbols);
        mSymbolsShiftedKeyboard = new KingDimKeyboard(this, R.xml.symbols_shift);
        mQwertyKeyboard = new KingDimKeyboard(this, R.xml.qwerty);
        mNewyinxing_firstKeyboard = new KingDimKeyboard(this, R.xml.newyinxing_first);
        mNewyinxing_secondKeyboard = new KingDimKeyboard(this, R.xml.newyinxing_second);
        mNewyinxing_bihuaKeyboard = new KingDimKeyboard(this, R.xml.newyinxing_bihua);
        mSymbolicExpressionKeyboard = new KingDimKeyboard(this, R.xml.symbolicexpression);
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    //????????????????????????????????????????????????????
    @Override
    public View onCreateInputView() {
        //System.out.println("onCreateInputView()");
        linearLayout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.input, null);
//        mInputView = (KeyboardView)linearLayout.findViewById(R.id.keyboard);
//        mInputView = new KeyboardView()
//        mInputView.setOnKeyboardActionListener(this);//?????????
//        mInputView.setKeyboard(mNewyinxing_firstKeyboard);
        ImageView settingBtn = (ImageView) linearLayout.findViewById(R.id.setting_btn);
        ImageView downBtn = (ImageView) linearLayout.findViewById(R.id.down_btn);
        settingBtn.setOnClickListener(this);
        downBtn.setOnClickListener(this);
        setListener(linearLayout);
        main_input_view = (LinearLayout) linearLayout.findViewById(R.id.main_input_view);
        canView = (CandidateLayout)linearLayout.findViewById(R.id.canView);
        funcLayout = (RelativeLayout)linearLayout.findViewById(R.id.layout_func);
        yinmu_layout = (LinearLayout)linearLayout.findViewById(R.id.layout_first_9);
        yunmu_layout = (LinearLayout)linearLayout.findViewById(R.id.layout_second_9);
        canView.setOnChooseSuggestionListener(new CandidateLayout.OnChooseSuggestionListener() {
            @Override
            public void onChoose(String suggestion) {
                commitText(suggestion);
            }
        });
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        return linearLayout;
    }

    private void setListener(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof KeyView) {
                v.setOnClickListener(this);
            } else if (v instanceof ViewGroup) {
                setListener((ViewGroup)v);
            }
        }
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override
    public View onCreateCandidatesView() {
        //System.out.println("onCreateCandidatesView()");
        mCandidateView = new CandidateView(this);//InputMethodService??Context????????
        mCandidateView.setService(this);
        return mCandidateView;
    }

    //????????????????????
    @Override
    public View onCreateExtractTextView() {
        // TODO Auto-generated method stub
        return super.onCreateExtractTextView();
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    /**
     * ??????????????????????????????????????????????????????????????????????????????棬?????????????????????
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        //System.out.println("onStartInput()");
        super.onStartInput(attribute, restarting);

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);//?????????????????????????????????????????л???????????????????????????????????????
        updateCandidates();
        Mode = 0;// ??????????????????????? 
        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = false;//???????????
        mCompletionOn = false;//?????????????????????
//        mCompletions = null;

        // We are now going to initialize our state based on the type of
        // text being edited.
        /******inputType???????????м????????????????????????inputType?????????????е?????????????????????????????
         * ????TYPE_MASK_CLASS????ó???????????????????????????????????????????CLASS,???????????TYPE_MASK_VARIATION??λ????????????
         * ????VARIATION?????
         * *******/
        //System.out.println("select keyboard");
        System.out.println(getCurrentInputEditorInfo().toString());
        getInputTypeToSet(attribute);//????text?????????????
        /*switch (attribute.inputType&EditorInfo.TYPE_MASK_CLASS) {
        
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = mSymbolsKeyboard;//?????????????????????????????
                //System.out.println("select datetimekeyboard");
                break;
                
            case EditorInfo.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = mSymbolsKeyboard;
                //System.out.println("select phonekeyboard");
                break;
                
            case EditorInfo.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                mCurKeyboard = mStrokesKeyboard;
                //System.out.println("select strokeskeyboard");
                mPredictionOn = true;//????????????
                
                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType &  EditorInfo.TYPE_MASK_VARIATION;
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;//?????????????????
                }
                
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_URI
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = false;//????????????????????????????
                }
                
               //????????????????????????????????????????????????????//
                if ((attribute.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = false;
                    mCompletionOn = isFullscreenMode();
                }
                
                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;
                
            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mStrokesKeyboard;
                updateShiftKeyState(attribute);//????????????д??
        }*/

        // Update the label on the enter key, depending on what the application
        // says it will do.
        //lk ???????????mcurrkeyboard??enter????lbael ???????????label????????

        //mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);//??????????????????????????label
        mSymbolsKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mSymbolsShiftedKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mQwertyKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mNewyinxing_firstKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mNewyinxing_secondKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mNewyinxing_bihuaKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mSymbolicExpressionKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    //????????л??????,?????????????reset????
    @Override
    public void onFinishInput() {
        System.out.println("onFinishInput()");
        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();

        // kimCode.saveCache();
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        //?????????????????н???????????????????????????????????ó??????????????????????ó????????????
        setCandidatesViewShown(false);

        mCurKeyboard = mNewyinxing_firstKeyboard;
//        if (mInputView != null) {
//            mInputView.closing();
//        }
    }

    //???????????????????????????????????????????????????,
    //???????????????????????л????????????????????仯
    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        //System.out.println("onStartInputView()");
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        //System.out.println("setting keyboard...");
        // //System.out.println("select keyboard");
        // System.out.println(getCurrentInputEditorInfo().toString());
        // System.out.println(attribute.inputType+"88880000"+"   "+attribute.toString());
        //System.out.println(EditorInfo.TYPE_MASK_CLASS);
        getInputTypeToSet(attribute);//????text?????????????


//        mInputView.setKeyboard(mCurKeyboard);//???????
//        mInputView.closing();
        //System.out.println("done!");


        //??????? ??????
        //System.out.println("default   dsf");

        System.out.println(EditorInfo.TYPE_CLASS_PHONE);
        System.out.println(attribute.inputType);
        System.out.println(InputType.TYPE_CLASS_NUMBER);
        System.out.println(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        System.out.println(InputType.TYPE_CLASS_PHONE);
        System.out.println(InputType.TYPE_CLASS_NUMBER);
        //??????? ??????

        //lk ???????????mcurrkeyboard??enter????lbael ???????????label????????

        //mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);//??????????????????????????label
        mSymbolsKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mSymbolsShiftedKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mQwertyKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mNewyinxing_firstKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mNewyinxing_secondKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mNewyinxing_bihuaKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        mSymbolicExpressionKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    @Override
    public void onBindInput() {
        // TODO Auto-generated method stub
        //System.out.println("onBindInput()");
        super.onBindInput();
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    //?????????????????????????????????á?????????????????е???????????????????????????????
    //????????????????????????λ??????????????????????????????????
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);//???????
            updateCandidates();//????????
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();//????????????????????????????????
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
   /* 
    * ?????????????????????????????????????棬????????????????????????????
    * ???????????????????????????????Щ???????飬????????????????????????
    * ?????Щ?????????????????档
    */
    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
//            mCompletions = completions;//???????????????????????????
            if (completions == null) {//??к????
                setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    /*???????????????????á??????????????????д????????????????shift???????????????
     * ???????????????????????shift????alt??????????????????unicode?????????????????
     * ???????ж???????????dead??????????????????????????????
     * ??????????????????????????????????????????????????????????
     * ?????????OnKeyDown?????
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);//????long???mMetaState????????????meta??????
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));//???????????????????0.????mMetaState???????????unicode???????????????????
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);//???????
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

//        boolean dead = false;//dead=true?????????ж????????

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {//????c??????????????????????????
//            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() - 1);//?????????????????????????
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length() - 1);
            }
        }

        onKey(c, null);

        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    //?????????????
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
//                if (event.getRepeatCount() == 0 && mInputView != null) {
//                    if (mInputView.handleBack()) {//?????????????????????
//                        return true;
//                    }
//                }
                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);//onkey?????е????????????????????
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;

//            case -500:
//            	mCurKeyboard = mQwertyKeyboard;
//            	break;

            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState() & KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    //??????????????
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }

    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    /*
     * ?????????ж?????????????????????????????????????????????????????????????????????Сд???????????????????
     * */
//    private void updateShiftKeyState(EditorInfo attr) {
//    	//System.out.println("updateShiftKeyState");
//        if (attr != null
//                && mInputView != null ){//lk && mStrokesKeyboard == mInputView.getKeyboard()) {
//            int caps = 0;
//            EditorInfo ei = getCurrentInputEditorInfo();//????????????????????
//            if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
//                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
//            }
//            mInputView.setShifted(mCapsLock || caps != 0);
//        }
//    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {//?ж?????????
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {//?????????????
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    // Implementation of KeyboardViewListener
   /*
    * ??????????????????ú????У????????????????в????????????????????????????????????????????,
    * ???????????????????????????????????????????????????????????????shift??????cancel????????
    * ??????????????????????????????????????????л????????????????????????????
    * */
    public void onKey(int primaryCode, int[] keyCodes) {

        if (isWordSeparator(primaryCode)) {//???涨??????
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);//??????????????????????????????д??
//            updateShiftKeyState(getCurrentInputEditorInfo());//????????????λ??????????Сд??
        }
//        else if (primaryCode == -600)  {
//            
//        }
        else if (primaryCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
        } else if (primaryCode == KeyEvent.KEYCODE_DPAD_UP) {
            keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
        } else if (primaryCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
        } else if (primaryCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
//            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
//            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
//            handleClose();
            return;
        } else if (primaryCode == KingDimKeyboardView.KEYCODE_OPTIONS) {
            // Show a menu or somethin'
        }
//		  else if (primaryCode == KingDimKeyboard.KEYCODE_MODE_CHANGE_123
//		      && mInputView != null) {
//				 getCurrentInputConnection().commitText("",0);
//				 mComposing.setLength(0);
//				 updateCandidates();
//			     mCurKeyboard=mSymbolsKeyboard;// ???
//			     if(Mode == 0 || Mode ==1)//??е???????????????????????????????LastMode
//			     {
//			       LastMode = Mode;
//			     }
//			     Mode = 3;
//		         mInputView.setKeyboard(mCurKeyboard);//  ??????
//		         mSymbolsKeyboard.setShifted(false);
//		}
//		  else if (primaryCode == KingDimKeyboard.KEYCODE_MODE_CHANGE_ABC
//			      && mInputView != null) {
//				 getCurrentInputConnection().commitText("",0);
//				 mComposing.setLength(0);
//				 updateCandidates();
//			     mCurKeyboard=mQwertyKeyboard;
//			     if(Mode == 0 || Mode ==1)//??е???????????????????????????????LastMode
//			     {
//			       LastMode = Mode;
//			     }
//			     Mode = 4;
//			     mInputView.setKeyboard(mCurKeyboard);//  ??????
//			}
//		  else if(primaryCode == KingDimKeyboard.KEYCODE_MODE_CHANGE_SYMBOL
//			      && mInputView != null)
//		  {
//			     getCurrentInputConnection().commitText("",0);
//				 mComposing.setLength(0);
//				 updateCandidates();
//				 Mode = 5;
//			     mCurKeyboard=mSymbolicExpressionKeyboard;
//			     mInputView.setKeyboard(mCurKeyboard);//  ??????
//		  }
//		  else if (primaryCode == KingDimKeyboard.KEYCODE_MODE_CHANGE_HANZI
//			      && mInputView != null) {
//				 getCurrentInputConnection().commitText("",0);
//				 mComposing.setLength(0);
//				 updateCandidates();
//                if(LastMode == 0)
//                 {
//                    	Mode = 0;
//                    	changeYxKeyboardTo(mNewyinxing_firstKeyboard);
//                  }
//
//				 else if(LastMode == 1)
//				 {
//					 Mode = 1;
//					 changeYxKeyboardTo(mNewyinxing_bihuaKeyboard);
//				 }
//			     mNewyinxing_firstKeyboard.setShifted(false);
//			}
//		  else if (primaryCode == KingDimKeyboard.KEYCODE_MODE_CHANGE_bihua
//			      && mInputView != null) {
//			//  ?????????????? ????????
//                 getCurrentInputConnection().commitText("",0);
//         		 mComposing.setLength(0);
//       			 updateCandidates();
//       			 if(Mode == 0)
//       			 {
//       		      Mode = 1;
//       		      changeYxKeyboardTo(mNewyinxing_bihuaKeyboard);
//       			 }
//       			 else if(Mode == 1)
//       			 {
//       			  Mode = 0;
//       			  changeYxKeyboardTo(mNewyinxing_firstKeyboard);
//       			 }
//       			 LastMode = Mode;
//        		 mNewyinxing_firstKeyboard.setShifted(false);
//			}
//		  else if (primaryCode == KingDimKeyboard.KEYCODE_MODE_CHANGE_SETTING
//			      && mInputView != null) {
////				 getCurrentInputConnection().commitText("",0);
////				 mComposing.setLength(0);
////				 updateCandidates();
//				/* Uri uri0=Uri.parse("http://cslab.stu.edu.cn/KingAIMHelp.htm");
//					 Intent intent=new Intent(Intent.ACTION_VIEW, uri0);
//					 intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
//					 startActivity(intent);*/
//			  //System.out.println("setting Activity has not been done!");//Setting Activity is not ready!
//			  Intent intent = new Intent(this,SettingList.class);
//			  //intent.setClass(this, SettingList.class);
//			  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			  startActivity(intent);
//			}
//
        else if (primaryCode == -6) {
//			  getCurrentInputConnection().
            if (mComposing.length() > 0) {
//				 getCurrentInputConnection().commitText("",0);
//				 mComposing.setLength(0);
                mCandidateView.scrollbyword(-6);
                //updateCandidates();
//			  keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
                //System.out.println("up                 ");
            } else {

                keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
            }
        } else if (primaryCode == -7) {
            if (mComposing.length() > 0) {
//					 getCurrentInputConnection().commitText("",0);
//					 mComposing.setLength(0);
//					 
//					updateCandidates();
                mCandidateView.scrollbyword(-7);
//				  keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
                //System.out.println("left1                ");
            } else {
//			  getCurrentInputConnection().

//			  System.out.println(getCurrentInputConnection().toString().length());
//			  getCurrentInputConnection().setSelection(2,2);
//			  //System.out.println("r10ight                 ");
                keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
                //System.out.println("left                 ");
            }
        } else if (primaryCode == -8) {
            if (mComposing.length() > 0) {

                mCandidateView.scrollbyword(-8);
                //System.out.println("down1                 ");
            } else {

                keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
                //System.out.println("down                 ");
            }

        } else if (primaryCode == -9) {
            if (mComposing.length() > 0) {
//					 getCurrentInputConnection().commitText("",0);
//					 mComposing.setLength(0);
//					 
//					updateCandidates();
                //mCandidateView.invalidate();
                mCandidateView.scrollbyword(-9);

//				  keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
                //System.out.println("right1                 ");
            } else {
//			  int leftint=getCurrentInputConnection().getTextBeforeCursor(5000, 0).toString().length();
//			  int rightint=getCurrentInputConnection().getTextAfterCursor(1000, 0).toString().length();
//			  int numint=leftint+rightint;
//			  if(leftint>0)
//			  getCurrentInputConnection().setSelection(leftint-1,leftint-1);
                keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
//			  keyDownUp(KeyEvent.KEYCODE_);
                //System.out.println("RIGHT                 ");
            }


        } else if (primaryCode == -10) {//lk ?????????????,xml?е????10????????????-10
            //System.out.println("here is enter.");
            System.out.println(getCurrentInputEditorInfo().imeOptions);
            System.out.println("Finding!");
            int aId = getCurrentInputEditorInfo().inputType;
            if (getCurrentInputEditorInfo().imeOptions == EditorInfo.IME_ACTION_NEXT) {
                if ((aId == EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE + EditorInfo.TYPE_TEXT_VARIATION_FILTER)
                        | (aId == EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE)) {
                    keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
                    //System.out.println("press two keydown!");
                }
                keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
                System.out.println(getCurrentInputEditorInfo().imeOptions + "     1");
            } else

                keyDownUp(KeyEvent.KEYCODE_ENTER);

        } else if ((primaryCode >= -1058) && (primaryCode <= -1042))
            /*try {
                new KingDimEngine(this).test(primaryCode);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

        {
//			  List<String> suggestions = new ArrayList<String>();
            mComposing.append((char) (-primaryCode - 1000));//???????????????????mComposing???????
            getCurrentInputConnection().setComposingText(mComposing, 1);//???????????????mComposing

//	          try {
            List<String> suggestions = kimCode.getCandidates(mComposing);
//			} catch (NotFoundException e) {
            // TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}          
            updateCandidates();//???????б?
            setSuggestions(suggestions, true, true);
        } else if ((primaryCode >= 1000) && (primaryCode <= 1017)) {
//			  handleSymbolicExpression(primaryCode);
        } else {
//            handleCharacter(primaryCode, keyCodes);//???????????????????????????????Щ??????????
        }

        //System.out.println("firstKeyboar888d");
        //????????ж?????????????????  lk
        System.out.println(mComposing.length());

        if (Mode == 0) {//?????????????????????????ε????????????л?
            //System.out.println("firstKeyboard");
            if (mComposing.length() == 0) {
                //System.out.println("tomNewyinxing_firstKeyboard");
                changeYxKeyboardTo(mNewyinxing_firstKeyboard);
            } else if (mComposing.length() == 1) {
                //System.out.println("mNewyinxing_firstKeyboard");;
                changeYxKeyboardTo(mNewyinxing_secondKeyboard);
            } else if (mComposing.length() == 2) {
                //System.out.println("mNewyinxing_secondKeyboard");
                changeYxKeyboardTo(mNewyinxing_bihuaKeyboard);
            } else {
                //System.out.println("mNewyinxing_bihuaKeyboard");
                changeYxKeyboardTo(mNewyinxing_bihuaKeyboard);
            }
        } else if (Mode == 1) {  //???????????6???7???????????????????????
            if (mComposing.length() == 0) {
                changeYxKeyboardTo(mNewyinxing_bihuaKeyboard);
            } else if (mComposing.length() == 5) {
                changeYxKeyboardTo(mNewyinxing_firstKeyboard);
            } else if (mComposing.length() == 6) {
                changeYxKeyboardTo(mNewyinxing_secondKeyboard);
            } else {
                changeYxKeyboardTo(mNewyinxing_bihuaKeyboard);
            }
        } else {
            ;
        }
    }

    //??????????
    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
//        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    /*
     * ???????????????????????????????????????????????????????????????
     * ??????????У??????????????????????????????????????????????
     * */
    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateKeyboard(){
        if(mComposing.length() == 0){
            showKeyboardType(0);
        }else if(mComposing.length() == 1){
            showKeyboardType(1);
        }else if(mComposing.length() >= 2){
            showKeyboardType(2);
        }
    }

    //???ú??????????????????????????cnadidatevView?????????????
    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
//        if (suggestions != null && suggestions.size() > 0) {
//            setCandidatesViewShown(true);//?ú????????
//        } else if (isExtractViewShown()) {
//            setCandidatesViewShown(true);
//        }
//        if (mCandidateView != null) {
//            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
//        }
        if(canView!=null) {
            canView.setSuggestions(suggestions);
        }
    }

    //?????????????????????????????????????????????????????????????????????
    //??????????????????????????????????
//    private void handleBackspace() {
//        final int length = mComposing.length();
//        if (length > 1) {
//            mComposing.delete(length - 1, length);
//            getCurrentInputConnection().setComposingText(mComposing, 1);
//        	Keyboard currentKeyboard = mInputView.getKeyboard();
//        	if(currentKeyboard != mQwertyKeyboard)
//        	{
//	          List<String> suggestions = kimCode.getCandidates(mComposing);
//	          updateCandidates();//???????б?
//			  setSuggestions(suggestions, true, true);
//        	}
//        } else if (length > 0) {
//            mComposing.setLength(0);
//            getCurrentInputConnection().commitText("", 0);
//            updateCandidates();
//        } else {
//            keyDownUp(KeyEvent.KEYCODE_DEL);
//        }
////        updateShiftKeyState(getCurrentInputEditorInfo());
//    }

//    private void handleShift() {
//        if (mInputView == null) {
//            return;
//        }
//
//        Keyboard currentKeyboard = mInputView.getKeyboard();
//        if (mQwertyKeyboard == currentKeyboard) {
//            // Alphabet keyboard
//            checkToggleCapsLock();//????????????????????????л???
//            mInputView.setShifted(mCapsLock || !mInputView.isShifted());//??????
//        } else if (currentKeyboard == mSymbolsKeyboard) {
//            mSymbolsKeyboard.setShifted(true);
//            mInputView.setKeyboard(mSymbolsShiftedKeyboard);
//            mSymbolsShiftedKeyboard.setShifted(true);
//        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
//            //??ν??setShift,??????????????????Сд???仯,?????????android:code=-1???????????Ч??
//        	mSymbolsShiftedKeyboard.setShifted(false);
//            mInputView.setKeyboard(mSymbolsKeyboard);//???????????shift????(???????????????)
//            mSymbolsKeyboard.setShifted(false);
//        }
//    }
//
//    //??????????????????????????д??????????????????????????????????????????У?
//    //???????????????????????????
//    private void handleCharacter(int primaryCode, int[] keyCodes) {
//        if (isInputViewShown()) {
//            if (mInputView.isShifted()) {
//                primaryCode = Character.toUpperCase(primaryCode);//??????????д
//            }
//        }
//        if (isAlphabet(primaryCode) && mPredictionOn) {//?????????????????????????????
//          //  mComposing.append((char) primaryCode);//???????????????????mComposing???????
//          //  getCurrentInputConnection().setComposingText(mComposing, 1);//???????????????mComposing
//        	getCurrentInputConnection().commitText(String.valueOf((char)primaryCode),1 );
//            updateShiftKeyState(getCurrentInputEditorInfo());//???????????????????????????д
////          updateCandidates();//???????б?
//        } else {
//            getCurrentInputConnection().commitText(
//                    String.valueOf((char) primaryCode), 1);//???????????????????????????λ?????
//        }
//    }
//    private void handleSymbolicExpression(int primaryCode)
//    {
//    	 getCurrentInputConnection().commitText(
//                KingDimKeyboard.Symbol[primaryCode-1000], KingDimKeyboard.Symbol[primaryCode-1000].length() );
//    }
//    //??????
//    private void handleClose() {
//        commitTyped(getCurrentInputConnection());
//        requestHideSelf(0);//?????????????
//        mInputView.closing();
//    }

    //???????л?????????
    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {//?????????????л?
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char) code));//??????????????????????Щ???????
    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0, null);
    }

    //??????????????????????????????????????????????????????????????????
    public void pickSuggestionManually(int index, List<String> mSuggestions) {
        /*if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {*/
           /* CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);*/
        //??lastestInputWords???????????5?????,???lastestInputWords??StringBuilder??????????????????????
        lastestInputWords = lastestInputWords.append(mSuggestions.get(index));
        lastestInputWords.delete(0, lastestInputWords.length() - 5);
        mComposing.setLength(0);    //????????
        getCurrentInputConnection().commitText(mSuggestions.get(index), 1);
        if (mCandidateView != null) {
            mCandidateView.clear();
        }
//            updateShiftKeyState(getCurrentInputEditorInfo());
        kimCode.updateCache(mSuggestions.get(index).toString());//???????????
        //commitTyped(getCurrentInputConnection());
      /*  } else if (mComposing.length() > 0) {
            // If we were generating candidate suggestions for the current
            // text, we would commit one of them here.  But for this sample,
            // we will just commit the current text.
            commitTyped(getCurrentInputConnection());
        }*/

        //lk ???  ?????????? ?????????????????????
        //System.out.println("input the end");
        if (Mode == 0) {
            changeYxKeyboardTo(mNewyinxing_firstKeyboard);
        } else if (Mode == 1) {
            changeYxKeyboardTo(mNewyinxing_bihuaKeyboard);
        } else {
            ;
        }
    }

    public void swipeRight() {//??????????????????????????????
        if (mCompletionOn) {
            pickDefaultCandidate();
        }
    }

    public void swipeLeft() {
//        handleBackspace();
    }

    public void swipeDown() {
//        handleClose();
    }

    public void swipeUp() {
    }

    public void onPress(int primaryCode) {
    }

    public void onRelease(int primaryCode) {
    }

    @Override
    public boolean onEvaluateInputViewShown() {
        // TODO Auto-generated method stub
        //System.out.println("onEvaluateInputViewShown");
        return super.onEvaluateInputViewShown();
    }

    @Override
    public void updateInputViewShown() {
        // TODO Auto-generated method stub
        //System.out.println("updateInputViewShown");
        super.updateInputViewShown();
    }

    /**
     * ???????????????????????????????????
     * design by lk
     */
    public void changeYxKeyboardTo(KingDimKeyboard temp) {
        mCurKeyboard = temp;
        if (Mode == 0) {
            ModeKey = mCurKeyboard.findKey(-6000);
            ModeKey.icon = getResources().getDrawable(R.drawable.yin);
        } else if (Mode == 1) {
            ModeKey = mCurKeyboard.findKey(-6000);
            ModeKey.icon = getResources().getDrawable(R.drawable.strokes_icon_b);
        }
//		mInputView.setKeyboard(mCurKeyboard);
    }

    /**
     * ????text?????????????
     * design by lk
     */
    public void getInputTypeToSet(EditorInfo attribute) {
        switch (attribute.inputType) {

            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_NUMBER + EditorInfo.TYPE_NUMBER_FLAG_DECIMAL:
            case EditorInfo.TYPE_CLASS_NUMBER + EditorInfo.TYPE_NUMBER_FLAG_SIGNED:
                Mode = 3;
                mCurKeyboard = mSymbolsKeyboard;
                //System.out.println("number    ");
                break;
            //lk ???????number????
            case EditorInfo.TYPE_CLASS_DATETIME:
            case EditorInfo.TYPE_CLASS_DATETIME + EditorInfo.TYPE_DATETIME_VARIATION_DATE:
            case EditorInfo.TYPE_CLASS_DATETIME + EditorInfo.TYPE_DATETIME_VARIATION_TIME:
                Mode = 3;
                mCurKeyboard = mSymbolsKeyboard;
                //System.out.println("date and time     ");
                break;
            // lk ?????????????? date time datetime
            case EditorInfo.TYPE_CLASS_TEXT:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_IME_MULTI_LINE:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS:


            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_FILTER:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_LONG_MESSAGE:

            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_PHONETIC:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE:


            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT:
                mPredictionOn = true;//????????????
                Mode = 0;
                mCurKeyboard = mNewyinxing_firstKeyboard;
//        	updateShiftKeyState(attribute);
                //System.out.println("text     ");
                break;

            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
                Mode = 4;
                mCurKeyboard = mQwertyKeyboard;
//        	updateShiftKeyState(attribute);
                //System.out.println("password     "+EditorInfo.TYPE_MASK_VARIATION);
                break;
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_SUBJECT:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_URI:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_URI + EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE:
                Mode = 4;
                mCurKeyboard = mQwertyKeyboard;
//        	updateShiftKeyState(attribute);
                //System.out.println("email address and uri     "+EditorInfo.TYPE_MASK_VARIATION);
                break;
            //lk ??????????text?????????password???? ?????????????????????????????
            //text??????в???????????????????????????password???????????????
            //????????password?????????????????????
            case EditorInfo.TYPE_CLASS_PHONE:
            case EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE + EditorInfo.TYPE_TEXT_VARIATION_FILTER:
                //?????????????????????????????3??????????
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                Mode = 3;
                mCurKeyboard = mSymbolsKeyboard;//?????????????????????????????
                //System.out.println("this is a phone select datetimekeyboard");
                System.out.println(EditorInfo.TYPE_CLASS_PHONE);
                break;
            //lk ???????phone????
            // Phones will also default to the symbols keyboard, though
            // often you will want to have a dedicated phone keyboard.
        /*case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD+1:
            // Phones will also default to the symbols keyboard, though
            // often you will want to have a dedicated phone keyboard.
            mCurKeyboard = mQwertyKeyboard;
            //System.out.println("select password");
            
            break;*/

            // Phones will also default to the symbols keyboard, though
            // often you will want to have a dedicated phone keyboard.

        /*case EditorInfo.TYPE_CLASS_TEXT:
            // This is general text editing.  We will default to the
            // normal alphabetic keyboard, and assume that we should
            // be doing predictive text (showing candidates as the
            // user types).
            mCurKeyboard = mStrokesKeyboard;
            //System.out.println("select strokeskeyboard");
            mPredictionOn = true;//????????????
            
            // We now look for a few special variations of text that will
            // modify our behavior.
            int variation = attribute.inputType &  EditorInfo.TYPE_MASK_VARIATION;
            if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                    variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Do not display predictions / what the user is typing
                // when they are entering a password.
                mPredictionOn = false;//?????????????????
            }
            
            if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
                    || variation == EditorInfo.TYPE_TEXT_VARIATION_URI
                    || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
                // Our predictions are not useful for e-mail addresses
                // or URIs.
                mPredictionOn = false;//????????????????????????????
            }
            
           //????????????????????????????????????????????????????//
            if ((attribute.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                // If this is an auto-complete text view, then our predictions
                // will not be shown and instead we will allow the editor
                // to supply their own.  We only show the editor's
                // candidates when in fullscreen mode, otherwise relying
                // own it displaying its own UI.
                mPredictionOn = false;
                mCompletionOn = isFullscreenMode();
            }
            
            // We also want to look at the current state of the editor
            // to decide whether our alphabetic keyboard should start out
            // shifted.
            updateShiftKeyState(attribute);
            break;
          */
            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                Mode = 0;
                mCurKeyboard = mNewyinxing_firstKeyboard;
//            updateShiftKeyState(attribute);//????????????д??

        }
    }

    private void commitText(String text) {
        getCurrentInputConnection().commitText(text, 0);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof KeyView) {
            int code = ((KeyView) v).getCode();
            if (code >= 44401 && code <= 44426) {
                commitText(((KeyView) v).getInput_text());
                return;
            }
            if((code >= -1058) && (code <= -1042)){
                mComposing.append((char) (-code-1000));//把当前输入的一个字符添加到mComposing字符串中
                getCurrentInputConnection().setComposingText(mComposing, 1);//在输入目标中也显示mComposing
                List<String> suggestions = kimCode.getCandidates(mComposing);
                updateCandidates();//更新候选列表
                setSuggestions(suggestions, true, true);
                updateKeyboard();
            }
            switch (code) {
                case 10000:
                    commitText("，");
                    break;
                case 10001:
                    commitText("。");
                    break;
                case 10002:
                    commitText("？");
                    break;
                case 10003:
                    commitText("！");
                    break;
                case 10004:
                    replaceEnInput();
                    break;
                case 10005:
                    getCurrentInputConnection().deleteSurroundingText(1, 0);
                    break;
                case 10006:
                    break;
                case 10007:
                    break;
                case 10008:
                    commitText("\n");
                    break;
                case 10009:
                    break;
                case 44427:
                    doEnShift(cur_view, !shiftUp);
                    break;
                case 44428:
                    getCurrentInputConnection().deleteSurroundingText(1, 0);
                    break;
                case 10010:
                    commitText(" ");
                    break;
                case 44429:
                    doReturnView();
                    break;
                case 44430:
                    break;
                case 44431:
                    commitText(".");
                    break;
                case 44432:
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE);
                    break;
                case 44433:
                    break;
                case 44434:
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
                    break;
            }
        }
        int id = v.getId();
        switch (id) {
            case R.id.setting_btn:
                break;
            case R.id.down_btn:
                requestHideSelf(0);
                break;
        }
    }

    private void doEnShift(View view,boolean up) {
        shiftUp = up;
        LinearLayout cur = (LinearLayout) view;
        for (int i = 0; i < cur.getChildCount(); i++) {
            View v = cur.getChildAt(i);
            if (v instanceof KeyView) {
                if(((KeyView) v).getCode() == 44427){
                    if(up){
                        v.setBackgroundColor(Color.DKGRAY);
                    }else{
                        v.setBackgroundColor(Color.WHITE);
                    }
                }
                if (((KeyView) v).getCode() >= 44401 && ((KeyView) v).getCode() <= 44426) {
                    if(up) {
                        ((KeyView) v).toUpcase();
                    }else{
                        ((KeyView) v).toLowcase();
                    }
                }
            }else if(v instanceof ViewGroup){
                doEnShift(v,up);
            }
        }
    }

    private void showKeyboardType(int type){
        //0 == 音母键盘，1 == 韵母键盘， 2 == 笔画键盘
        LayoutInflater inflater = getLayoutInflater();
        switch (type){
            case 0:
                yunmu_layout.setVisibility(View.GONE);
                yinmu_layout.setVisibility(View.VISIBLE);
                break;
            case 1:
                yinmu_layout.setVisibility(View.GONE);
                yunmu_layout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void replaceEnInput() {
        View enInputView = getLayoutInflater().inflate(R.layout.input_qwer, null);
        replaceView(main_input_view, enInputView);
    }

    private void replaceView(View currentView, View newView) {
        pre_view = currentView;
        cur_view = newView;
        ViewGroup parent = (ViewGroup) currentView.getParent();
        int index = parent.indexOfChild(currentView);
        parent.removeView(currentView);
        parent.addView(newView, index);
        if(cur_view instanceof ViewGroup) {
            setListener((ViewGroup) cur_view);
        }
    }

    private void doReturnView() {
        replaceView(cur_view, pre_view);
    }

//	@Override
//	public boolean onEvaluateFullscreenMode() {
//		// TODO Auto-generated method stub
//		return true;
//	}
}

/**
 * ???????????????????????????????????????????????????????????
 * ??????????????????????????????е???????????????onCreateInputView?????????????????
 * ????????????ò???????????????????????????????????onEvaluateInputViewShow??????????????
 * ???????????????????????????????????????????????????
 * ?????????updateInputViewShown?????????????
 **/
