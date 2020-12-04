package com.fgtit.access;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fgtit.access.R;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.utils.ToastUtil;
import com.fgtit.utils.ExtApi;
import com.fgtit.app.ActivityList;
import com.fgtit.app.Fingerprint;
import com.fgtit.app.UserItem;
import com.fgtit.app.UsersList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.fpi.MtGpio;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.scanner.CaptureActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.AsyncFingerprint.OnDownCharListener;
import android_serialport_api.AsyncFingerprint.OnGenCharExListener;
import android_serialport_api.AsyncFingerprint.OnGetImageExListener;
import android_serialport_api.AsyncFingerprint.OnRegModelListener;
import android_serialport_api.AsyncFingerprint.OnSearchListener;
import android_serialport_api.AsyncFingerprint.OnStoreCharListener;
import android_serialport_api.AsyncFingerprint.OnUpCharListener;
import android_serialport_api.AsyncFingerprint.OnUpImageExListener;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortManager;

public class EnrollActivity extends Activity {

    private EditText editText1, editText2, editText3, editText4, editText5, editText6, editText7;
    private ImageView imgPhoto, imgFinger1, imgFinger2, imgFinger3, imgFinger4;

    private byte[] jpgbytes = null;

    private ImageView fpImage;
    private TextView tvFpStatus;
    private Dialog fpDialog;
    private int iPlaceCount = 0;
    private byte[] fpTemp = new byte[512];
    private byte[] fpTemp1=new byte[256];
    private byte[] fpTemp2=new byte[256];
    //NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;

    private TextView tvCardStatus;
    private Dialog cardDialog = null;

    private int iEnrolIndex = 0;
    private int iSelectType = 0;

    UserItem userItem = new UserItem();
    public String CardSN = "";

    private Spinner spin1, spin2, spin3, spin4;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);

        this.getActionBar().setDisplayHomeAsUpEnabled(true);

        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        editText4 = (EditText) findViewById(R.id.editText4);
        editText5 = (EditText) findViewById(R.id.editText5);
        editText6 = (EditText) findViewById(R.id.editText6);
        editText7 = (EditText) findViewById(R.id.editText7);

        imgPhoto = (ImageView) findViewById(R.id.imageView4);
        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EnrollActivity.this, CameraExActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", "1");
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }
        });

        imgFinger1 = (ImageView) findViewById(R.id.imageView1);
        imgFinger1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (iSelectType == 0) {
                    FPDialog(1);
                } else {
                    CardDialog(1);
                }
                //Intent intent = new Intent(EnrollActivity.this, CaptureActivity.class);
                //startActivityForResult(intent,0);
            }
        });

        imgFinger2 = (ImageView) findViewById(R.id.imageView2);
        imgFinger2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (iSelectType == 0) {
                    FPDialog(2);
                } else {
                    CardDialog(2);
                }
            }
        });

        imgFinger3 = (ImageView) findViewById(R.id.imageView3);
        imgFinger3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (iSelectType == 0) {
                    FPDialog(3);
                } else {
                    CardDialog(3);
                }
            }
        });
        imgFinger4 = (ImageView) findViewById(R.id.imageView5);
        imgFinger4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Toast.makeText(EnrollActivity.this, "Please place the 125 card", Toast
                        .LENGTH_SHORT).show();
                Fingerprint.getInstance().GetIdCardNO();
            }
        });

        //����
        spin1 = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array
                .us1_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin1.setAdapter(adapter1);
        spin1.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                userItem.usertype = (byte) pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //nothing to do
            }
        });
        spin1.setSelection(1);

        //ʶ������
        spin2 = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array
                .us2_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin2.setAdapter(adapter2);
        spin2.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                if (pos == 0) {
                    imgFinger1.setImageResource(R.drawable.fingerprint);
                    iSelectType = 0;
                } else {
                    imgFinger1.setImageResource(R.drawable.rfid);
                    iSelectType = 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spin2.setSelection(0);

        spin3 = (Spinner) findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array
                .us2_array, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin3.setAdapter(adapter3);
        spin3.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                if (pos == 0) {
                    imgFinger2.setImageResource(R.drawable.fingerprint);
                    iSelectType = 0;
                } else {
                    imgFinger2.setImageResource(R.drawable.rfid);
                    iSelectType = 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spin3.setSelection(0);

        spin4 = (Spinner) findViewById(R.id.spinner4);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this, R.array
                .us2_array, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin4.setAdapter(adapter4);
        spin4.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                if (pos == 0) {
                    imgFinger3.setImageResource(R.drawable.fingerprint);
                    iSelectType = 0;
                } else {
                    imgFinger3.setImageResource(R.drawable.rfid);
                    iSelectType = 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spin4.setSelection(0);
        Fingerprint.getInstance().setHandler(fingerprintHandler);
        //Card
        InitReadCard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1: {
                Bundle bl = data.getExtras();
                String barcode = bl.getString("barcode");
                //editText9.setText(barcode);
            }
            break;
            case 2:
                break;
            case 3: {
                Bundle bl = data.getExtras();
                String id = bl.getString("id");
                Toast.makeText(EnrollActivity.this, "Pictures Finish", Toast.LENGTH_SHORT).show();
                byte[] photo = bl.getByteArray("photo");
                if (photo != null) {
                    try {
                        Matrix matrix = new Matrix();
                        Bitmap bm = BitmapFactory.decodeByteArray(photo, 0, photo.length);
                        matrix.preRotate(-90);
                        Bitmap nbm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
                                matrix, true);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        nbm.compress(Bitmap.CompressFormat.JPEG, 80, out);//��ͼƬѹ��������
                        jpgbytes = out.toByteArray();

                        Bitmap bitmap = BitmapFactory.decodeByteArray(jpgbytes, 0, jpgbytes.length);
                        imgPhoto.setImageBitmap(bitmap);

                        editText6.setText("Enrolled");

                    } catch (Exception e) {
                    }
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.enroll, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            AlertDialog.Builder builder = new Builder(this);
            builder.setTitle("Back");
            builder.setMessage("Data not save, back?");
            //builder.setCancelable(false);
            builder.setPositiveButton("Cancel", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Back", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //SerialPortManager.getInstance().closeSerialPort();
                    EnrollActivity.this.finish();
                }
            });
            builder.create().show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                //SerialPortManager.getInstance().closeSerialPort();
                this.finish();
                return true;
            case R.id.action_save: {
                if (CheckInputData(1)) {
                    userItem.userid = Integer.valueOf(editText1.getText().toString());
                    userItem.username = (editText2.getText().toString());
                    UsersList.getInstance().AppendUser(userItem);
                    if (jpgbytes != null) {
                        File file = new File(Environment.getExternalStorageDirectory() +
                                "/OnePass/data/" + String.valueOf(userItem.userid) + ".jpg");
                        FileOutputStream fos;
                        try {
                            fos = new FileOutputStream(file);
                            fos.write(jpgbytes);
                            fos.close();
                        } catch (Exception e) {
                        }
                    }
                    Toast.makeText(EnrollActivity.this, "Enroled OK!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            return true;
            case R.id.action_make: {
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean CheckInputData(int type) {
        int len = editText1.getText().toString().length();
        if (len <= 0) {
            Toast.makeText(EnrollActivity.this, "Please input ID", Toast.LENGTH_SHORT).show();
            return false;
        }
        len = editText2.getText().toString().length();
        if (len <= 0) {
            Toast.makeText(EnrollActivity.this, "Please input name", Toast.LENGTH_SHORT).show();
            return false;
        }
		/*
		if(mRefSize1<=0){
			Toast.makeText(EnrollActivity.this, "Please Input Template One", Toast.LENGTH_SHORT)
			.show();
			return false;
		}
		if(mRefSize2<=0){
			Toast.makeText(EnrollActivity.this, "Please Input Template Two", Toast.LENGTH_SHORT)
			.show();
			return false;
		}
		if(!iscap){
			Toast.makeText(EnrollActivity.this, "Please Take Photo", Toast.LENGTH_SHORT).show();
			return false;
		}
		*/
        if (type == 1) {
            if (UsersList.getInstance().UserIsExists(Integer.valueOf(editText1.getText().toString
                    ()))) {
                Toast.makeText(EnrollActivity.this, "ID Exists", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    //Fingerprint Enroll
    private void FPDialog(int i) {
        iEnrolIndex = i;
        AlertDialog.Builder builder = new Builder(EnrollActivity.this);
        builder.setTitle("Enroll Fingerprint");
        final LayoutInflater inflater = LayoutInflater.from(EnrollActivity.this);
        View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
        fpImage = (ImageView) vl.findViewById(R.id.imageView1);
        tvFpStatus = (TextView) vl.findViewById(R.id.textview1);
        builder.setView(vl);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Fingerprint.getInstance().Cancel();
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        fpDialog = builder.create();
        fpDialog.setCanceledOnTouchOutside(false);
        fpDialog.show();

        iPlaceCount = 0;
        Fingerprint.getInstance().SetUpImage(true);

        Fingerprint.getInstance().Process();
    }

    @SuppressLint("HandlerLeak")
    private final Handler fingerprintHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Fingerprint.STATE_PLACE:
                    if (tvFpStatus!= null) {
                        tvFpStatus.setText(getString(R.string.txt_fpplace));
                    }
                    break;
                case Fingerprint.STATE_LIFT:
                    break;
                case Fingerprint.STATE_GETIMAGE: {
                    tvFpStatus.setText(getString(R.string.txt_fpdisplay));
                }
                break;
                case Fingerprint.STATE_UPIMAGE: {
                    tvFpStatus.setText(getString(R.string.txt_fpprocess));
                    Bitmap image = BitmapFactory.decodeByteArray((byte[]) msg.obj, 0, ((byte[])
                            (msg.obj)).length);
                    fpImage.setImageBitmap(image);
                }
                break;
                case Fingerprint.STATE_GENDATA: {
                    tvFpStatus.setText(getString(R.string.txt_fpidentify));
                }
                break;
                case Fingerprint.STATE_UPDATA: {
                    iPlaceCount++;
                    if (iPlaceCount >= 2) {
                        System.arraycopy((byte[]) msg.obj, 0, fpTemp2,0, 256);
                        switch (iEnrolIndex) {
                            case 1:
                                if (FPMatch.getInstance().MatchTemplate(fpTemp1,fpTemp2) > 40) {
                                    userItem.enlcon1[0] = 1;
                                    System.arraycopy(fpTemp1, 0, userItem.fp1, 0, 256);
                                    System.arraycopy(fpTemp2, 0, userItem.fp1, 256, 256);
                                    editText3.setText("Enrolled");
                                }else {
                                    editText3.setText("Merger failure");
                                }
                                break;
                            case 2:
                                if (FPMatch.getInstance().MatchTemplate(fpTemp1,fpTemp2) > 40) {
                                    userItem.enlcon2[0] = 1;
                                    System.arraycopy(fpTemp1, 0, userItem.fp2, 0, 256);
                                    System.arraycopy(fpTemp2, 0, userItem.fp2, 256, 256);
                                    editText4.setText("Enrolled");
                                }else {
                                    editText4.setText("Merger failure");
                                }
                                break;
                            case 3:
                                if (FPMatch.getInstance().MatchTemplate(fpTemp1,fpTemp2) > 40) {
                                    userItem.enlcon3[0] = 1;
                                    System.arraycopy(fpTemp1, 0, userItem.fp3, 0, 256);
                                    System.arraycopy(fpTemp2, 0, userItem.fp3, 256, 256);
                                    editText5.setText("Enrolled");
                                }else {
                                    editText5.setText("Merger failure");
                                }
                                break;
                        }
                        Fingerprint.getInstance().Cancel();
                        fpDialog.cancel();
                        fpDialog = null;
                    } else {
                        System.arraycopy((byte[]) msg.obj, 0, fpTemp1,0, 256);
                        Fingerprint.getInstance().Process();
                    }
                }
                break;
                case Fingerprint.STATE_GetCardNOex: {

                    System.arraycopy((byte[]) msg.obj, 0, userItem.enllNO, 0, 4);
                    String text = "";
                    for (int i = 0; i < userItem.enllNO.length; i++) {
                        text = text + Integer.toHexString(userItem.enllNO[i] & 0xFF).toUpperCase();
                    }
                    editText7.setText(text);
                }
                break;
                case Fingerprint.STATE_FAIL:
                    Fingerprint.getInstance().Process();
                    break;
            }
        }
    };

    //Card Enroll
    private void CardDialog(int i) {
        iEnrolIndex = i;
        AlertDialog.Builder builder = new Builder(EnrollActivity.this);
        builder.setTitle("Enroll Card");
        final LayoutInflater inflater = LayoutInflater.from(EnrollActivity.this);
        View vl = inflater.inflate(R.layout.dialog_enrolcard, null);
        tvCardStatus = (TextView) vl.findViewById(R.id.textview1);
        builder.setView(vl);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                cardDialog = null;
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                cardDialog = null;
            }
        });
        cardDialog = builder.create();
        cardDialog.setCanceledOnTouchOutside(false);
        cardDialog.show();
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

    //NFC

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        byte[] sn = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        String cardstr =/*Integer.toString(count)+":"+*/
                Integer.toHexString(sn[0] & 0xFF).toUpperCase() +
                        Integer.toHexString(sn[1] & 0xFF).toUpperCase() +
                        Integer.toHexString(sn[2] & 0xFF).toUpperCase() +
                        Integer.toHexString(sn[3] & 0xFF).toUpperCase();

        if (UsersList.getInstance().FindUserItemByCard(sn) == null) {
            if (cardDialog != null) {
                switch (iEnrolIndex) {
                    case 1:
                        editText3.setText(cardstr);
                        userItem.enlcon1[0] = 2;
                        System.arraycopy(sn, 0, userItem.enlcon1, 1, 4);
                        break;
                    case 2:
                        editText4.setText(cardstr);
                        userItem.enlcon2[0] = 2;
                        System.arraycopy(sn, 0, userItem.enlcon2, 1, 4);
                        break;
                    case 3:
                        editText5.setText(cardstr);
                        userItem.enlcon3[0] = 2;
                        System.arraycopy(sn, 0, userItem.enlcon3, 1, 4);
                        break;
                }
                tvCardStatus.setText(cardstr);
                cardDialog.cancel();
                cardDialog = null;
            }
            CardSN = cardstr;
        } else {
            Toast.makeText(EnrollActivity.this, "Duplicate Card!", Toast.LENGTH_SHORT).show();
        }
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
}
