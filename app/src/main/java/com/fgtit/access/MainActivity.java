package com.fgtit.access;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.fpi.MtGpio;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.fgtit.access.R;
import com.fgtit.app.ActivityList;
import com.fgtit.app.DeviceConfig;
import com.fgtit.app.Fingerprint;
import com.fgtit.app.LocatorServer;
import com.fgtit.app.LogsList;
import com.fgtit.app.SocketServer;
import com.fgtit.app.TempVals;
import com.fgtit.app.UserItem;
import com.fgtit.app.UsersList;
import com.fgtit.fpcore.FPMatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

public class MainActivity extends Activity {
    private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 45000;
    private static final int AUTO_IMAGE_DELAY_MILLIS = 10;

    private View topLineLayout;

    private ImageSwitcher imageSwitcher;
    private Timer picTimer;
    private TimerTask picTask;
    private Handler picHandler;
    private int ipicIndex = 0;
    private int ipicCount = 0;
    private int iShowCount = 0;
    private boolean bLoadExt = false;
    private ArrayList<String> picList = new ArrayList<String>();

    private DialogResult fpDialog = null;
    private Timer AutoCloseTimer;
    private TimerTask AutoCloseTask;
    private Handler AutoCloseHandler;
    private int AutoCloseCount = 0;
    private PowerManager.WakeLock wakeLock;
    private TextView tvDate, tvTime;
    private int mDay = -1;

    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;
    private Bitmap fpImage;
    private TextView tvFpStatus;

    private SoundPool soundPool;
    private int soundId;
    private boolean soundflag = false;

    private Handler handler = new Handler();

    //NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View
                    .SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View
                    .SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            android.app.ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                //actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
            topLineLayout.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    public static final int ETHER_IFACE_ATATE_DOWN = 0;
    public static final int ETHER_IFACE_ATATE_UP = 1;
    public static final String ETHERNET_IFACE_STATE_CHANGED_ACTION = "android.net.ethernet" +
            ".ETHERNET_IFACE_STATE_CHANGED";
    private final BroadcastReceiver ETHERNETReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ETHERNET_IFACE_STATE_CHANGED_ACTION.equals(action)) {
                if (intent.getIntExtra("ethernet_iface_state", ETHER_IFACE_ATATE_UP) ==
                        ETHER_IFACE_ATATE_UP) {
                    SocketServer.getInstance().Stop();
                    SystemClock.sleep(500);
                    SocketServer.getInstance().setHandler(handler);
                    SocketServer.getInstance().Start();
                    //     Toast.makeText(getApplicationContext(), "Ethernet is work", Toast
                    // .LENGTH_SHORT).show();
                } else {
                    // 	  Toast.makeText(getApplicationContext(), "Ethernet is Notwork", Toast
                    // .LENGTH_SHORT).show();

                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {//
                // 这个监听wifi的打开与关闭，与wifi的连接无关
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        SocketServer.getInstance().Stop();
                        Toast.makeText(getApplicationContext(), "Wifi is Disabled", Toast
                                .LENGTH_SHORT).show();
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        SocketServer.getInstance().Stop();
                        Toast.makeText(getApplicationContext(), "Wifi is Disabling", Toast
                                .LENGTH_SHORT).show();
                        break;
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager
                        .EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    State state = networkInfo.getState();
                    boolean isConnected = state == State.CONNECTED;// 当然，这边可以更精确的确定状态
                    if (isConnected) {
                        SocketServer.getInstance().Stop();
                        SystemClock.sleep(500);
                        SocketServer.getInstance().setHandler(handler);
                        SocketServer.getInstance().Start();
                        //	 Toast.makeText(getApplicationContext(), "wifi is work", Toast
                        // .LENGTH_SHORT).show();
                    } else {

                    }
                }
            }
        }
    };

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);*/
        setContentView(R.layout.activity_main);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        topLineLayout = findViewById(R.id.topLineLayout);

        imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);

        tvDate = (TextView) findViewById(R.id.textView1);
        tvTime = (TextView) findViewById(R.id.textView2);
        tvFpStatus = (TextView) findViewById(R.id.textView3);
        ShowDateTime();

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.dummy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ActivityList.getInstance().AutoResultStart();
                //StatusBarDisable(false);

                Fingerprint.getInstance().Cancel();
                Fingerprint.getInstance().setHandler(null);

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        ActivityList.getInstance().setMainContext(this);
        ActivityList.getInstance().CreateDir();
        ActivityList.getInstance().LoadConfig();
        ActivityList.getInstance().SetAutoResult();
        UsersList.getInstance().LoadAll();
        LogsList.getInstance().Init();

        InitImageSwitcher();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "sc");
        wakeLock.acquire();

        FPMatch.getInstance().InitMatch();

        InitReadCard();
        Fingerprint.getInstance().setContext(this);
        Fingerprint.getInstance().setHandler(fingerprintHandler);
        Fingerprint.getInstance().Open();
        Fingerprint.getInstance().Process();

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(this, R.raw.dong, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundflag = true;
            }
        });
        SocketServer.getInstance().setContext(this);
        LocatorServer.getInstance().setContext(this);
        LocatorServer.getInstance().setHandler(handler);
        LocatorServer.getInstance().startUdpServer();
        setFpIoState(true);
        //     StatusBarDisable(true);
        //      setNavigationBarState(true);
/*        IntentFilter filter = new IntentFilter();
        filter.addAction(ETHERNET_IFACE_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(ETHERNETReceiver,filter);*/
        ActivityList.getInstance().addActivity(this);
    }


    private void setFpIoState(boolean isOn) {
        int state = 0;
        if (isOn) {
            state = 1;
        } else {
            state = 0;
        }
        Intent i = new Intent("ismart.intent.action.fingerPrint_control");
        i.putExtra("state", state);
        sendBroadcast(i);
    }

    private void setNavigationBarState(boolean isHide) {
        Intent intent = new Intent("ismart.intent.action_hide_navigationview");
        intent.putExtra("hide", isHide);
        sendBroadcast(intent);
    }

    public void StatusBarDisable(boolean isdisable) {
        Intent i = new Intent("ismart.intent.action_lock_panelbar");
        i.putExtra("state", isdisable);
        sendBroadcast(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 0:
                Fingerprint.getInstance().setHandler(fingerprintHandler);
                Fingerprint.getInstance().Process();
                break;
            case 1:
            case 2:
                Fingerprint.getInstance().setHandler(fingerprintHandler);
                Fingerprint.getInstance().Process();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        soundPool.release();
        soundPool = null;
        Fingerprint.getInstance().Close();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, null);
    }

    public void InitReadCard() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Device does not support NFC!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Enable the NFC function in the system settings!", Toast
                    .LENGTH_SHORT).show();
            finish();
            return;
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags
                (Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED), new
                IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED), new IntentFilter(NfcAdapter
                .ACTION_TAG_DISCOVERED)};
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        byte[] sn = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);

        UserItem ui = UsersList.getInstance().FindUserItemByCard(sn);
        if (ui != null) {
            fpImage = BitmapFactory.decodeResource(getResources(), R.drawable.rfid);

            List<String> mData = new ArrayList<String>();
            mData.add(ui.username);
            mData.add(String.valueOf(ui.userid));
            if (ui.usertype == 1) mData.add("Administrator");
            else mData.add("General");
            LogsList.getInstance().Append(ui.userid, ui.username, 0, 0);

            ActivityList.getInstance().OpenDoor();

            ShowInfo("Match Result", "Pass", fpImage, mData);
        } else {
            fpImage = BitmapFactory.decodeResource(getResources(), R.drawable.rfid);
            ShowInfo("Match Result", "Fail", fpImage, null);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            //Fingerprint.getInstance().Close();
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void InitImageSwitcher() {
        ipicCount = 3;
        bLoadExt = false;
        picList.clear();
        String sDir = Environment.getExternalStorageDirectory() + "/OnePass/logo";
        String keyword = ".jpg";
        File filepath = new File(sDir);
        File[] files = filepath.listFiles();
        if (files.length > 0) {
            for (File file : files) {
                if (!(file.isDirectory())) {
                    try {
                        if (file.getName().indexOf(keyword) > -1 || file.getName().indexOf
                                (keyword.toUpperCase()) > -1) {
                            picList.add(sDir + "/" + file.getName());
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        if (picList.size() > 0) {
            bLoadExt = true;
            ipicCount = picList.size();
        }

        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView i = new ImageView(mContentView.getContext());
                if (bLoadExt) {
                    i.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    i.setLayoutParams(new ImageSwitcher.LayoutParams(WindowManager.LayoutParams
                            .MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
                } else {
                    i.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                return i;
            }
        });
        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.alphia_in));
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_out));

        if (bLoadExt) {
            try {
                //FileInputStream fis = new FileInputStream(picList.get(ipicIndex));
                //Bitmap bitmap  = BitmapFactory.decodeStream(fis);
                //Drawable drawable =new BitmapDrawable(bitmap);
                //imageSwitcher.setImageDrawable(drawable);
                imageSwitcher.setImageURI(Uri.parse(picList.get(ipicIndex)));
            } catch (Exception e) {
            }
        } else {
            imageSwitcher.setImageResource(R.drawable.demo2);
        }
        PicTimerStart();
    }

    public void PicTimerStart() {
        picTimer = new Timer();
        picHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                iShowCount++;
                if (iShowCount > AUTO_IMAGE_DELAY_MILLIS) {
                    iShowCount = 0;
                    ipicIndex++;
                    if (ipicIndex >= ipicCount) ipicIndex = 0;
                    if (bLoadExt) {
                        imageSwitcher.setImageURI(Uri.parse(picList.get(ipicIndex)));
                    } else {
                        switch (ipicIndex) {
                            case 0:
                                imageSwitcher.setImageResource(R.drawable.demo2);
                                break;
                            case 1:
                                imageSwitcher.setImageResource(R.drawable.demo3);
                                break;
                            case 2:
                                imageSwitcher.setImageResource(R.drawable.demo1);
                                break;
                        }
                    }
                    ShowDateTime();
                }
                super.handleMessage(msg);
            }
        };
        picTask = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Message message = new Message();
                message.what = 1;
                picHandler.sendMessage(message);
            }
        };
        picTimer.schedule(picTask, 1000, 1000);
    }

    public void PicTimerStop() {
        if (picTimer != null) {
            picTimer.cancel();
            picTimer = null;
            picTask.cancel();
            picTask = null;
        }
    }


    private void ShowDateTime() {
        /*
        Time t=new Time(); // or Time t=new Time("GMT+8");
        t.setToNow();
        int year = t.year;
        int month = t.month+1;
        int date = t.monthDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        */
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getDefault());
        int day = c.get(Calendar.DAY_OF_MONTH);
        if (mDay != day) {
            mDay = day;
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int weekd = c.get(Calendar.DAY_OF_WEEK);
            int weeky = c.get(Calendar.WEEK_OF_YEAR) + 1;
            String mWay = "";
            switch (weekd) {
                case 1:
                    mWay = getString(R.string.txt_week1);
                    break;
                case 2:
                    mWay = getString(R.string.txt_week2);
                    break;
                case 3:
                    mWay = getString(R.string.txt_week3);
                    break;
                case 4:
                    mWay = getString(R.string.txt_week4);
                    break;
                case 5:
                    mWay = getString(R.string.txt_week5);
                    break;
                case 6:
                    mWay = getString(R.string.txt_week6);
                    break;
                case 7:
                    mWay = getString(R.string.txt_week7);
                    break;
            }
            tvDate.setText(String.format("%d-%02d-%02d %s %02d", year, month, day, mWay, weeky));
        }
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        tvTime.setText(String.format("%02d : %02d", hour, minute));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
    }

    private void hide() {
        // Hide UI first
        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        topLineLayout.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        topLineLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void ShowInfo(String title, String message, Bitmap image, List<String> listdata) {
        if (fpDialog != null) {
            fpDialog.dismiss();
            fpDialog = null;
        }

        if (fpDialog == null) {

            fpDialog = new DialogResult(MainActivity.this, title, message, image, listdata);
            fpDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fpDialog.dismiss();
                    fpDialog = null;
                }
            });
            fpDialog.setOnCancelButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fpDialog.dismiss();
                    fpDialog = null;
                }
            });
            fpDialog.show();
        } else {
            fpDialog.show();
        }
        AutoCloseCount = 0;
        AutoCloseStart();
    }

    public void AutoCloseStart() {
        if (AutoCloseTimer != null) return;
        AutoCloseTimer = new Timer();
        AutoCloseHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                AutoCloseCount++;
                if (AutoCloseCount >= 3) {
                    AutoCloseCount = 0;
                    AutoCloseStop();
                    if (fpDialog != null) {
                        fpDialog.dismiss();
                        fpDialog = null;
                    }
                }
                super.handleMessage(msg);
            }
        };
        AutoCloseTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                AutoCloseHandler.sendMessage(message);
            }
        };
        AutoCloseTimer.schedule(AutoCloseTask, 1000, 1000);
    }

    public void AutoCloseStop() {
        if (AutoCloseTimer != null) {
            AutoCloseTimer.cancel();
            AutoCloseTimer = null;
            AutoCloseTask.cancel();
            AutoCloseTask = null;
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler fingerprintHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Fingerprint.STATE_PLACE:
                    tvFpStatus.setText(getString(R.string.txt_fpplace));
                    break;
                case Fingerprint.STATE_LIFT:
                    break;
                case Fingerprint.STATE_GETIMAGE: {
                    iShowCount = 0;
                    show();
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    soundPool.play(soundId, 1.0f, 1.0f, 1, 1, 1.0f);
                    tvFpStatus.setText(getString(R.string.txt_fpdisplay));
                }
                break;
                case Fingerprint.STATE_UPIMAGE: {
                    tvFpStatus.setText(getString(R.string.txt_fpprocess));
                    fpImage = BitmapFactory.decodeByteArray((byte[]) msg.obj, 0, ((byte[]) (msg
                            .obj)).length);
                    //fpImage.setImageBitmap(image);
                }
                break;
                case Fingerprint.STATE_GENDATA: {
                    tvFpStatus.setText(getString(R.string.txt_fpidentify));
                }
                break;
                case Fingerprint.STATE_UPDATA: {
                    UserItem ui = UsersList.getInstance().FindUserItemByFP((byte[]) msg.obj);
                    if (ui != null) {
                        if (!Fingerprint.getInstance().IsUpImage())
                            fpImage = BitmapFactory.decodeResource(getResources(), R.drawable
                                    .finger);

                        List<String> mData = new ArrayList<String>();
                        mData.add(ui.username);
                        mData.add(String.valueOf(ui.userid));
                        if (ui.usertype == 1) mData.add("Administrator");
                        else mData.add("General");
                        LogsList.getInstance().Append(ui.userid, ui.username, 0, 0);

                        ActivityList.getInstance().OpenDoor();

                        ShowInfo("Match Result", "Pass", fpImage, mData);
                    } else {
                        if (!Fingerprint.getInstance().IsUpImage())
                            fpImage = BitmapFactory.decodeResource(getResources(), R.drawable
                                    .finger);
                        ShowInfo("Match Result", "Fail", fpImage, null);
                    }
                    TimerStart();
                }
                break;
                case Fingerprint.STATE_RS485: {
                }
                break;
                case Fingerprint.STATE_GetCardNOex: {
                    UserItem ui = UsersList.getInstance().FindUserItemByCardex((byte[]) msg.obj);
                    if (ui != null) {
                        if (!Fingerprint.getInstance().IsUpImage())
                            fpImage = BitmapFactory.decodeResource(getResources(), R.drawable
                                    .finger);

                        List<String> mData = new ArrayList<String>();
                        mData.add(ui.username);
                        mData.add(String.valueOf(ui.userid));
                        if (ui.usertype == 1) mData.add("Administrator");
                        else mData.add("General");
                        LogsList.getInstance().Append(ui.userid, ui.username, 0, 0);

                        ActivityList.getInstance().OpenDoor();

                        ShowInfo("Match Result", "Pass", fpImage, mData);
                    } else {
                        if (!Fingerprint.getInstance().IsUpImage())
                            fpImage = BitmapFactory.decodeResource(getResources(), R.drawable.finger);
                        ShowInfo("Match Result", "Fail", fpImage, null);
                    }
                    TimerStart();
                }
                break;
                case Fingerprint.STATE_FAIL:
                    Fingerprint.getInstance().Process();
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    public void TimerStart() {
        if (startTimer == null) {
            startTimer = new Timer();
            startHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    TimeStop();
                    Fingerprint.getInstance().Process();
                }
            };
            startTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    startHandler.sendMessage(message);
                }
            };
            startTimer.schedule(startTask, 1000, 1000);
        }
    }

    public void TimeStop() {
        if (startTimer != null) {
            startTimer.cancel();
            startTimer = null;
            startTask.cancel();
            startTask = null;
        }
    }

}
