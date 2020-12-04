package com.fgtit.access;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
//import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import com.fgtit.access.R;

public class DialogResult extends android.app.Dialog{

    Context context;
    View view;
    View backView;
    String message;
    TextView messageTextView;
    String title;
    TextView titleTextView;

    Bitmap image;
    ImageView imageView;

    ListView listView;
    List<String> mData=null;

    Button buttonAccept;
    Button buttonCancel;

    String buttonCancelText;

    View.OnClickListener onAcceptButtonClickListener;
    View.OnClickListener onCancelButtonClickListener;


    public DialogResult(Context context, String title, String message,Bitmap image,List<String> data) {
        super(context, android.R.style.Theme_Translucent);
        this.context = context;// init Context
        this.message = message;
        this.title = title;
        this.image = image;
        this.mData = data;
    }

    public void addCancelButton(String buttonCancelText){
        this.buttonCancelText = buttonCancelText;
    }

    public void addCancelButton(String buttonCancelText, View.OnClickListener onCancelButtonClickListener){
        this.buttonCancelText = buttonCancelText;
        this.onCancelButtonClickListener = onCancelButtonClickListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideBottomUIMenu();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_result);

        view = (LinearLayout)findViewById(R.id.contentDialog);
        backView = (LinearLayout)findViewById(R.id.dialog_rootView);
        backView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getX() < view.getLeft()
                        || event.getX() >view.getRight()
                        || event.getY() > view.getBottom()
                        || event.getY() < view.getTop()) {
                    dismiss();
                }
                return false;
            }
        });

        this.titleTextView = (TextView) findViewById(R.id.title);
        setTitle(title);

        this.messageTextView = (TextView) findViewById(R.id.message);
        setMessage(message);

        this.buttonAccept = (Button) findViewById(R.id.button_accept);
        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(onAcceptButtonClickListener != null)
                    onAcceptButtonClickListener.onClick(v);
            }
        });

        //if(buttonCancelText != null){
            this.buttonCancel = (Button) findViewById(R.id.button_cancel);
            this.buttonCancel.setVisibility(View.VISIBLE);
            this.buttonCancel.setText(buttonCancelText);
            buttonCancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    if(onCancelButtonClickListener != null)
                        onCancelButtonClickListener.onClick(v);
                }
            });
        //}

        this.imageView=(ImageView)findViewById(R.id.imageView1);
        setBitmap(image);

        listView=(ListView) findViewById(R.id.listView1);
        setList(mData);
        
        this.buttonCancel.setVisibility(View.GONE);
        this.buttonAccept.setVisibility(View.GONE);
        
        Resources r = this.getContext().getResources();  
        if(this.mData==null){
        	this.messageTextView.setTextColor(r.getColor(R.color.red));
        }else{
        	this.messageTextView.setTextColor(r.getColor(R.color.green));
        }
    }

    protected void hideBottomUIMenu() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
          View v = this.getWindow().getDecorView();
          v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
          //for new api versions.
          View decorView = getWindow().getDecorView();
          int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
          decorView.setSystemUiVisibility(uiOptions);
        }
        
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
          @Override
          public void onSystemUiVisibilityChange(int visibility) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
          }
        });
      }
    
    public void setBitmap(Bitmap image) {
        this.image = image;
        if(title == null)
            imageView.setVisibility(View.GONE);
        else{
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(image);
        }
    }

    public void setList(List<String> data){
        this.mData=data;
        if(mData==null)
            listView.setVisibility(View.GONE);
        else{
            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(new ArrayAdapter<String>(this.context,android.R.layout.simple_list_item_1,mData));
        }
    }

    @Override
    public void show() {
        super.show();
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_main_show_amination));
        backView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_root_show_amin));
    }

    // GETERS & SETTERS

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        messageTextView.setText(message);
    }

    public TextView getMessageTextView() {
        return messageTextView;
    }

    public void setMessageTextView(TextView messageTextView) {
        this.messageTextView = messageTextView;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if(title == null)
            titleTextView.setVisibility(View.GONE);
        else{
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
            //titleTextView.setTextColor(Color.rgb(0, 128, 0));
        }
    }

    public TextView getTitleTextView() {
        return titleTextView;
    }

    public void setTitleTextView(TextView titleTextView) {
        this.titleTextView = titleTextView;
    }

    public Button getButtonAccept() {
        return buttonAccept;
    }

    public void setButtonAccept(Button buttonAccept) {
        this.buttonAccept = buttonAccept;
    }

    public Button getButtonCancel() {
        return buttonCancel;
    }

    public void setButtonCancel(Button buttonCancel) {
        this.buttonCancel = buttonCancel;
    }

    public void setOnAcceptButtonClickListener(
            View.OnClickListener onAcceptButtonClickListener) {
        this.onAcceptButtonClickListener = onAcceptButtonClickListener;
        if(buttonAccept != null)
            buttonAccept.setOnClickListener(onAcceptButtonClickListener);
    }

    public void setOnCancelButtonClickListener(
            View.OnClickListener onCancelButtonClickListener) {
        this.onCancelButtonClickListener = onCancelButtonClickListener;
        if(buttonCancel != null)
            buttonCancel.setOnClickListener(onCancelButtonClickListener);
    }

    @Override
    public void dismiss() {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_main_hide_amination);
        anim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        DialogResult.super.dismiss();
                    }
                });

            }
        });
        Animation backAnim = AnimationUtils.loadAnimation(context, R.anim.dialog_root_hide_amin);

        view.startAnimation(anim);
        backView.startAnimation(backAnim);
    }



}
