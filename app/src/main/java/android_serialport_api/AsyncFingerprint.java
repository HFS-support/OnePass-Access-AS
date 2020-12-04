package android_serialport_api;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

public class AsyncFingerprint extends Handler {

    private static final int FP_GetImage = 0x01;
    private static final int FP_GenChar = 0x02;
    private static final int FP_Match = 0x03;
    private static final int FP_Search = 0x04;
    private static final int FP_RegModel = 0x05;
    private static final int FP_StoreChar = 0x06;
    private static final int FP_LoadChar = 0x07;
    private static final int FP_UpChar = 0x08;

    private static final int FP_DownChar = 0x09;
    private static final int FP_UpImage = 0x0a;
    private static final int FP_DeleteChar = 0x0c;
    private static final int FP_Empty = 0x0d;
    private static final int FP_Enroll = 0x10;
    private static final int FP_Identify = 0x11;
    private Handler mWorkerThreadHandler;

    private static final int FP_GetImageEX = 0x30;
    private static final int FP_UpImageEX = 0x31;
    private static final int FP_GenCharEX = 0x32;
    private static final int FP_RS422 = 0x33;
    private static final int FP_RS422ex = 0x34;
    private static final int PS_GETIDCARDNO = 0x52;
    /**
     * 响应包和图像数据共40044字节
     */
    private static final int UP_IMAGE_RESPONSE_SIZE = 40044;

    private static final int UP_IMAGEEX_RESPONSE_SIZE = 16521;

    /**
     * 响应包和特征值数据共568字节
     */
    private static final int UP_CHAR_RESPONSE_SIZE = 568;

    /**
     * 缓冲区:下位机返回数据存放地
     */
    private byte[] data = new byte[1024 * 50];

    private byte[] buffer = new byte[1024 * 50];

    protected AsyncFingerprint(Looper looper) {
        createHandler(looper);
    }

    private Handler createHandler(Looper looper) {
        return mWorkerThreadHandler = new WorkerHandler(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case FP_GetImage:
                if (onGetImageListener == null) {
                    return;
                }
                if (msg.arg1 == 0) {
                    onGetImageListener.onGetImageSuccess();
                } else {
                    onGetImageListener.onGetImageFail();
                }
                break;
            case FP_UpImage:
                if (onUpImageListener == null) {
                    return;
                }
                if (msg.obj != null) {
                    onUpImageListener.onUpImageSuccess((byte[]) msg.obj);
                } else {
                    onUpImageListener.onUpImageFail();
                }
                break;
            case FP_GenChar:
                if (onGenCharListener == null) {
                    return;
                } else {
                    if (msg.arg1 == 0) {
                        onGenCharListener.onGenCharSuccess(msg.arg2);
                    } else {
                        onGenCharListener.onGenCharFail();
                    }
                }
                break;
            case FP_RegModel:
                if (onRegModelListener == null) {
                    return;
                } else {
                    if (msg.arg1 == 0) {
                        Log.i("whw", "onRegModelListener");
                        onRegModelListener.onRegModelSuccess();
                    } else {
                        onRegModelListener.onRegModelFail();
                    }
                }
                break;
            case FP_UpChar:
                if (onUpCharListener == null) {
                    return;
                } else {
                    if (msg.obj != null) {
                        onUpCharListener.onUpCharSuccess((byte[]) msg.obj);
                    } else {
                        onUpCharListener.onUpCharFail();
                    }
                }
                break;
            case FP_DownChar:
                if (onDownCharListener == null) {
                    return;
                } else {
                    if (msg.arg1 == 0) {
                        onDownCharListener.onDownCharSuccess();
                    } else {
                        onDownCharListener.onDownCharFail();
                    }
                }
                break;
            case FP_Match:
                if (onMatchListener == null) {
                    return;
                } else {
                    if ((Boolean) msg.obj) {
                        onMatchListener.onMatchSuccess();
                    } else {
                        onMatchListener.onMatchFail();
                    }
                }
                break;
            case FP_StoreChar:
                if (onStoreCharListener == null) {
                    return;
                } else {
                    if (msg.arg1 == 0) {
                        onStoreCharListener.onStoreCharSuccess();
                    } else {
                        onStoreCharListener.onStoreCharFail();
                    }
                }
                break;
            case FP_LoadChar:
                if (onLoadCharListener == null) {
                    return;
                } else {
                    if (msg.arg1 == 0) {
                        onLoadCharListener.onLoadCharSuccess();
                    } else {
                        onLoadCharListener.onLoadCharFail();
                    }
                }
                break;
            case FP_Search:
                if (onSearchListener == null) {
                    return;
                } else {
                    byte[] result = (byte[]) msg.obj;
                    if (result != null) {
                        if (result[9] == 0x00) {
                            short pageId = getShort(result[10], result[11]);
                            short matchScore = getShort(result[12], result[13]);
                            onSearchListener.onSearchSuccess(pageId, matchScore);
                            return;
                        }
                    }
                    onSearchListener.onSearchFail();
                }
                break;
            case FP_DeleteChar:
                if (onDeleteCharListener == null) {
                    return;
                } else {
                    if (msg.arg1 == 0) {
                        onDeleteCharListener.onDeleteCharSuccess();
                    } else {
                        onDeleteCharListener.onDeleteCharFail();
                    }
                }
                break;
            case FP_Empty:
                if (onEmptyListener == null) {
                    return;
                } else {
                    if (msg.arg1 == 0) {
                        onEmptyListener.onEmptySuccess();
                    } else {
                        onEmptyListener.onEmptyFail();
                    }
                }
                break;
            case FP_Enroll:
                if (onEnrollListener == null) {
                    return;
                } else {
                    byte[] result = (byte[]) msg.obj;
                    if (result != null) {
                        if (result[9] == 0x00) {
                            short pageId = getShort(result[10], result[11]);
                            onEnrollListener.onEnrollSuccess(pageId);
                            return;
                        }
                    }
                    onEnrollListener.onEnrollFail();
                }
                break;
            case FP_Identify:
                if (onIdentifyListener == null) {
                    return;
                } else {
                    byte[] result = (byte[]) msg.obj;
                    if (result != null) {
                        if (result[9] == 0x00) {
                            short pageId = getShort(result[10], result[11]);
                            short matchScore = getShort(result[12], result[13]);
                            onIdentifyListener.onIdentifySuccess(pageId, matchScore);
                            return;
                        }
                    }
                    onIdentifyListener.onIdentifyFail();
                }
                break;
            case FP_GetImageEX:
                if (onGetImageExListener == null) {
                    return;
                }
                if (msg.arg1 == 0) {
                    onGetImageExListener.onGetImageExSuccess();
                } else {
                    onGetImageExListener.onGetImageExFail();
                }
                break;
            case FP_UpImageEX:
                if (onUpImageExListener == null) {
                    return;
                }
                if (msg.obj != null) {
                    onUpImageExListener.onUpImageExSuccess((byte[]) msg.obj);
                } else {
                    onUpImageExListener.onUpImageExFail();
                }
                break;
            case FP_GenCharEX:
                if (onGenCharExListener == null) {
                    return;
                } else {
                    if (msg.arg1 == 0) {
                        onGenCharExListener.onGenCharExSuccess(msg.arg2);
                    } else {
                        onGenCharExListener.onGenCharExFail();
                    }
                }
                break;
            case FP_RS422:
                if (onRS485Listener == null) {
                    return;
                } else {
                    if (msg.obj != null) {
                        onRS485Listener.onRS485Success((byte[]) msg.obj);
                    } else {
                        onRS485Listener.onRS485Fail();
                    }
                }
                break;
            case FP_RS422ex:
                if (onRS485exListener == null) {
                    return;
                } else {
                    if (msg.obj != null) {
                        onRS485exListener.onRS485exSuccess((byte[]) msg.obj);
                    } else {
                        onRS485exListener.onRS485exFail();
                    }
                }
                break;
            case PS_GETIDCARDNO:
                if (onPSGetIdCardNOListener == null) {
                    return;
                } else {
                    if (msg.obj != null) {
                        onPSGetIdCardNOListener.onPSGetIdCardNOSuccess((byte[]) msg.obj);
                    } else {
                        onPSGetIdCardNOListener.onPSGetIdCardNOFail();
                    }
                }
                break;
            default:
                break;
        }
    }

    protected class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FP_GetImage:
                    int valueGetImage = PSGetImage();
                    Log.d("WorkerHandler", "valueGetImage:" + valueGetImage);
                    AsyncFingerprint.this.obtainMessage(FP_GetImage, valueGetImage, -1)
							.sendToTarget();
                    break;
                case FP_UpImage:
                    byte[] imageData = PSUpImage();
                    AsyncFingerprint.this.obtainMessage(FP_UpImage, imageData).sendToTarget();
                    break;
                case FP_GenChar:
                    int valueGenChar = PSGenChar(msg.arg1);
                    AsyncFingerprint.this.obtainMessage(FP_GenChar, valueGenChar, msg.arg1)
							.sendToTarget();
                    break;
                case FP_RegModel:
                    int valueRegModel = PSRegModel();
                    AsyncFingerprint.this.obtainMessage(FP_RegModel, valueRegModel, -1)
							.sendToTarget();
                    break;
                case FP_UpChar:
                    byte[] charData = PSUpChar();
                    AsyncFingerprint.this.obtainMessage(FP_UpChar, charData).sendToTarget();
                    break;
                case FP_DownChar:
                    int valueDownChar = PSDownChar((byte[]) msg.obj);
                    AsyncFingerprint.this.obtainMessage(FP_DownChar, valueDownChar, -1)
							.sendToTarget();
                    break;
                case FP_Match:
                    boolean valueMatch = PSMatch();
                    AsyncFingerprint.this.obtainMessage(FP_Match, Boolean.valueOf(valueMatch))
							.sendToTarget();
                    break;
                case FP_StoreChar:
                    int valueStoreChar = PSStoreChar(msg.arg1, msg.arg2);
                    AsyncFingerprint.this.obtainMessage(FP_StoreChar, valueStoreChar, -1)
							.sendToTarget();
                    break;
                case FP_LoadChar:
                    int valueLoadChar = PSLoadChar(msg.arg1, msg.arg2);
                    AsyncFingerprint.this.obtainMessage(FP_LoadChar, valueLoadChar, -1)
							.sendToTarget();
                    break;
                case FP_Search:
                    byte[] result = PSSearch(msg.arg1, msg.arg2, (Integer) msg.obj);
                    AsyncFingerprint.this.obtainMessage(FP_Search, result).sendToTarget();
                    break;
                case FP_DeleteChar:
                    int valueDeleteChar = PSDeleteChar((short) msg.arg1, (short) msg.arg2);
                    AsyncFingerprint.this.obtainMessage(FP_DeleteChar, valueDeleteChar, -1)
							.sendToTarget();
                    break;
                case FP_Empty:
                    int valueEmpty = PSEmpty();
                    AsyncFingerprint.this.obtainMessage(FP_Empty, valueEmpty, -1).sendToTarget();
                    break;
                case FP_Enroll:
                    byte[] valueEnroll = PSEnroll();
                    AsyncFingerprint.this.obtainMessage(FP_Enroll, valueEnroll).sendToTarget();
                    break;
                case FP_Identify:
                    byte[] valueIdentify = PSIdentify();
                    AsyncFingerprint.this.obtainMessage(FP_Identify, valueIdentify).sendToTarget();
                    break;
                case FP_GetImageEX:
                    int valueGetImageEx = PSGetImageEx();
                    AsyncFingerprint.this.obtainMessage(FP_GetImageEX, valueGetImageEx, -1)
							.sendToTarget();
                    break;
                case FP_UpImageEX:
                    byte[] imageDataEx = PSUpImageEx();
                    AsyncFingerprint.this.obtainMessage(FP_UpImageEX, imageDataEx).sendToTarget();
                    break;
                case FP_GenCharEX:
                    int valueGenCharEx = PSGenCharEx(msg.arg1);
                    AsyncFingerprint.this.obtainMessage(FP_GenCharEX, valueGenCharEx, msg.arg1)
							.sendToTarget();
                    break;
                case FP_RS422:
                    byte[] valueRS485 = PSRS422((byte[]) msg.obj);
                    AsyncFingerprint.this.obtainMessage(FP_RS422, valueRS485).sendToTarget();
                    break;
                case FP_RS422ex:
                    byte[] valueRS485ex = PSRS422ex((byte[]) msg.obj);
                    AsyncFingerprint.this.obtainMessage(FP_RS422ex, valueRS485ex).sendToTarget();
                    break;
                case PS_GETIDCARDNO:
                    byte[] valueGETIDCARDNO = PSGETIDCARDNO();
                    AsyncFingerprint.this.obtainMessage(PS_GETIDCARDNO, valueGETIDCARDNO)
							.sendToTarget();
                    break;
                default:
                    break;
            }
        }
    }

    private OnGetImageListener onGetImageListener;

    private OnUpImageListener onUpImageListener;

    private OnGenCharListener onGenCharListener;

    private OnRegModelListener onRegModelListener;

    private OnUpCharListener onUpCharListener;

    private OnDownCharListener onDownCharListener;

    private OnMatchListener onMatchListener;

    private OnStoreCharListener onStoreCharListener;

    private OnLoadCharListener onLoadCharListener;

    private OnSearchListener onSearchListener;

    private OnDeleteCharListener onDeleteCharListener;

    private OnEmptyListener onEmptyListener;

    private OnEnrollListener onEnrollListener;

    private OnIdentifyListener onIdentifyListener;

    private OnGetImageExListener onGetImageExListener;
    private OnUpImageExListener onUpImageExListener;
    private OnGenCharExListener onGenCharExListener;
    private OnRS485Listener onRS485Listener;
    private OnRS485exListener onRS485exListener;
    private OnPSGetIdCardNOListener onPSGetIdCardNOListener;

    public void setOnGetImageListener(OnGetImageListener onGetImageListener) {
        this.onGetImageListener = onGetImageListener;
    }

    public void setOnUpImageListener(OnUpImageListener onUpImageListener) {
        this.onUpImageListener = onUpImageListener;
    }

    public void setOnGenCharListener(OnGenCharListener onGenCharListener) {
        this.onGenCharListener = onGenCharListener;
    }

    public void setOnRegModelListener(OnRegModelListener onRegModelListener) {
        this.onRegModelListener = onRegModelListener;
    }

    public void setOnUpCharListener(OnUpCharListener onUpCharListener) {
        this.onUpCharListener = onUpCharListener;
    }

    public void setOnDownCharListener(OnDownCharListener onDownCharListener) {
        this.onDownCharListener = onDownCharListener;
    }

    public void setOnMatchListener(OnMatchListener onMatchListener) {
        this.onMatchListener = onMatchListener;
    }

    public void setOnStoreCharListener(OnStoreCharListener onStoreCharListener) {
        this.onStoreCharListener = onStoreCharListener;
    }

    public void setOnLoadCharListener(OnLoadCharListener onLoadCharListener) {
        this.onLoadCharListener = onLoadCharListener;
    }

    public void setOnSearchListener(OnSearchListener onSearchListener) {
        this.onSearchListener = onSearchListener;
    }

    public void setOnDeleteCharListener(OnDeleteCharListener onDeleteCharListener) {
        this.onDeleteCharListener = onDeleteCharListener;
    }

    public void setOnEmptyListener(OnEmptyListener onEmptyListener) {
        this.onEmptyListener = onEmptyListener;
    }

    public void setOnEnrollListener(OnEnrollListener onEnrollListener) {
        this.onEnrollListener = onEnrollListener;
    }

    public void setOnIdentifyListener(OnIdentifyListener onIdentifyListener) {
        this.onIdentifyListener = onIdentifyListener;
    }

    public void setOnGetImageExListener(OnGetImageExListener onGetImageExListener) {
        this.onGetImageExListener = onGetImageExListener;
    }

    public void setOnUpImageExListener(OnUpImageExListener onUpImageExListener) {
        this.onUpImageExListener = onUpImageExListener;
    }

    public void setOnGenCharExListener(OnGenCharExListener onGenCharExListener) {
        this.onGenCharExListener = onGenCharExListener;
    }

    public void setOnRS485Listener(OnRS485Listener onRS485Listener) {
        this.onRS485Listener = onRS485Listener;
    }

    public void setOnRS485exListener(OnRS485exListener onRS485exListener) {
        this.onRS485exListener = onRS485exListener;
    }

    public void setOnGetIdCardNOListener(OnPSGetIdCardNOListener onPSGetIdCardNOListener) {
        this.onPSGetIdCardNOListener = onPSGetIdCardNOListener;
    }

    public interface OnGetImageListener {
        void onGetImageSuccess();

        void onGetImageFail();
    }

    public interface OnUpImageListener {
        void onUpImageSuccess(byte[] data);

        void onUpImageFail();
    }

    public interface OnGenCharListener {
        void onGenCharSuccess(int bufferId);

        void onGenCharFail();
    }

    public interface OnRegModelListener {
        void onRegModelSuccess();

        void onRegModelFail();
    }

    public interface OnUpCharListener {
        void onUpCharSuccess(byte[] model);

        void onUpCharFail();
    }

    public interface OnDownCharListener {
        void onDownCharSuccess();

        void onDownCharFail();
    }

    public interface OnMatchListener {
        void onMatchSuccess();

        void onMatchFail();
    }

    public interface OnStoreCharListener {
        void onStoreCharSuccess();

        void onStoreCharFail();
    }

    public interface OnLoadCharListener {
        void onLoadCharSuccess();

        void onLoadCharFail();
    }

    public interface OnSearchListener {
        void onSearchSuccess(int pageId, int matchScore);

        void onSearchFail();
    }

    public interface OnDeleteCharListener {
        void onDeleteCharSuccess();

        void onDeleteCharFail();
    }

    public interface OnEmptyListener {
        void onEmptySuccess();

        void onEmptyFail();
    }

    public interface OnEnrollListener {
        void onEnrollSuccess(int pageId);

        void onEnrollFail();
    }

    public interface OnIdentifyListener {
        void onIdentifySuccess(int pageId, int matchScore);

        void onIdentifyFail();
    }

    public interface OnGetImageExListener {
        void onGetImageExSuccess();

        void onGetImageExFail();
    }

    public interface OnUpImageExListener {
        void onUpImageExSuccess(byte[] data);

        void onUpImageExFail();
    }

    public interface OnGenCharExListener {
        void onGenCharExSuccess(int bufferId);

        void onGenCharExFail();
    }

    public interface OnRS485Listener {
        void onRS485Success(byte[] data);

        void onRS485Fail();
    }

    public interface OnRS485exListener {
        void onRS485exSuccess(byte[] data);

        void onRS485exFail();
    }

    public interface OnPSGetIdCardNOListener {
        void onPSGetIdCardNOSuccess(byte[] data);

        void onPSGetIdCardNOFail();
    }

    public void FP_GetImage() {
        mWorkerThreadHandler.sendEmptyMessage(FP_GetImage);
    }

    public void FP_UpImage() {
        mWorkerThreadHandler.sendEmptyMessage(FP_UpImage);
    }

    public void FP_GenChar(int bufferId) {
        mWorkerThreadHandler.obtainMessage(FP_GenChar, bufferId, -1).sendToTarget();
    }

    public void FP_RegModel() {
        mWorkerThreadHandler.sendEmptyMessage(FP_RegModel);
    }

    public void FP_UpChar() {
        mWorkerThreadHandler.sendEmptyMessage(FP_UpChar);
    }

    public void FP_DownChar(byte[] model) {
        mWorkerThreadHandler.obtainMessage(FP_DownChar, model).sendToTarget();
    }

    public void FP_Match() {
        mWorkerThreadHandler.sendEmptyMessage(FP_Match);
    }

    public void FP_StoreChar(int bufferId, int pageId) {
        mWorkerThreadHandler.obtainMessage(FP_StoreChar, bufferId, pageId).sendToTarget();
    }

    public void FP_LoadChar(int bufferId, int pageId) {
        mWorkerThreadHandler.obtainMessage(FP_LoadChar, bufferId, pageId).sendToTarget();
    }

    public void FP_Search(int bufferId, int startPageId, int pageNum) {
        mWorkerThreadHandler.obtainMessage(FP_Search, bufferId, startPageId, pageNum)
				.sendToTarget();
    }

    public void FP_DeleteChar(int pageIDStart, int delNum) {
        mWorkerThreadHandler.obtainMessage(FP_DeleteChar, pageIDStart, delNum).sendToTarget();
    }

    public void FP_Empty() {
        mWorkerThreadHandler.sendEmptyMessage(FP_Empty);
    }

    public void FP_Enroll() {
        mWorkerThreadHandler.sendEmptyMessage(FP_Enroll);
    }

    public void FP_Identify() {
        mWorkerThreadHandler.sendEmptyMessage(FP_Identify);
    }

    public void FP_GetImageEx() {
        mWorkerThreadHandler.sendEmptyMessage(FP_GetImageEX);
    }

    public void FP_UpImageEx() {
        mWorkerThreadHandler.sendEmptyMessage(FP_UpImageEX);
    }

    public void FP_GenCharEx(int bufferId) {
        mWorkerThreadHandler.obtainMessage(FP_GenCharEX, bufferId, -1).sendToTarget();
    }

    public void FP_RS422(byte[] model) {
        mWorkerThreadHandler.obtainMessage(FP_RS422, model).sendToTarget();
        //	mWorkerThreadHandler.sendEmptyMessage(FP_RS422);
    }

    public void FP_RS422ex(byte[] model) {
        mWorkerThreadHandler.obtainMessage(FP_RS422ex, model).sendToTarget();
        //	mWorkerThreadHandler.sendEmptyMessage(FP_RS422);
    }

    public void FP_GetIdCardNO() {
        mWorkerThreadHandler.sendEmptyMessage(PS_GETIDCARDNO);
    }

    /**
     * 录入指纹图像
     *
     * @return 返回值为确认码 确认码=00H表示录入成功 确认码=01H表示收包有错 确认码=02H表示传感器上无手指
     * 确认码=03H表示录入不成功 确认码=-1 表示失败
     */
    private synchronized int PSGetImage() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, 0x01, 0x00, 0x03, 0x01, 0x00, 0x05};
        Log.d("AsyncFingerprint", Base64.encodeToString(command, 0));
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 50);
        Log.d("AsyncFingerprint", Base64.encodeToString(buffer, 0));
        printlog("PSGetImage", length);
        if (length == 12) {
            return buffer[9];
        }
        return -1;
    }

    /**
     * 将ImageBuffer中的指纹图像生成特征文件存于charBuffer1或CharBuffer2中
     *
     * @param bufferId （CharBuffer1:1h,CharBuffer2:2h）
     * @return 返回值为确认码 确认码=00H表示生成特征成功 确认码=01H表示收包有错 确认码=06H表示指纹图像太乱而生不成图像
     * 确认码=07H表示指纹图像正常，但特征点太少而生不成特征 确认码=15H表示图像缓冲区内没有有效原始图而生不成图像 确认码=-1
     * 表示失败
     */
    private synchronized byte PSGenChar(int bufferId) {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 01, (byte) 0x00, (byte) 0x04, (byte) 0x02, (byte) bufferId, (byte)
				0x00, (byte) (0x7 + bufferId)};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 500);
        printlog("PSGenChar", length);
        if (length == 12) {
            return buffer[9];
        }
        return -1;
    }

    /**
     * 合并特征生成模板，将CharBuffer1和CharBuffer2中的特征文件合并生成模板，结果存于CharBuffer1和CharBuffer2
     * 中。
     *
     * @return 返回值为确认码 确认码=00H表示合并成功 确认码=01H表示收包有错 确认码=0aH表示合并失败（两枚指纹不属于同一手指）
     * 确认码=-1 表示失败
     */
    private synchronized byte PSRegModel() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, 0x01, 0x00, 0x03, 0x05, 0x00, 0x09};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        printlog("PSRegModel", length);
        if (length == 12) {
            return buffer[9];
        }
        return -1;
    }

    /**
     * 将CharBuffer中的模板储存到指定的pageId号的flash数据库位置 bufferId:只能为1h或2h
     * pageId：范围为0~1010
     *
     * @return 返回值为确认码 确认码=00H表示储存成功 确认码=01H表示收包有错 确认码=0bH表示PageID超出指纹库范围
     * 确认码=18H表示写FLASH出错 确认码=-1 表示失败
     */
    private synchronized byte PSStoreChar(int bufferId, int pageId) {
        byte[] pageIDArray = short2byte((short) pageId);
        // Log.i("whw", "pageid hex=" + DataUtils.toHexString(pageIDArray));
        int checkSum = 0x01 + 0x00 + 0x06 + 0x06 + bufferId + (pageIDArray[0] & 0xff) +
				(pageIDArray[1] & 0xff);
        byte[] checkSumArray = short2byte((short) checkSum);
        // Log.i("whw",
        // "checkSumArray hex=" + DataUtils.toHexString(checkSumArray)
        // + "    checkSum=" + checkSum);
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x06, (byte) 0x06, (byte) bufferId, (byte)
				pageIDArray[0], (byte) pageIDArray[1], (byte) checkSumArray[0], (byte)
				checkSumArray[1]};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        printlog("PSStoreChar", length);
        if (length == 12) {
            return buffer[9];
        }
        return -1;
    }

    /**
     * 将flash 数据库中指定pageId号的指纹模板读入到模板缓冲区CharBuffer1或CharBuffer2
     * bufferId:只能为1h或2h pageId：范围为0~1023
     *
     * @param bufferId pageId号
     * @return 返回值为确认码 确认码=00H表示读出成功 确认码=01H表示收包有错 确认码=0cH表示读出有错或模板有错
     * 确认码=0BH表示PageID超出指纹库范围 确认码=-1 表示失败
     */
    private synchronized byte PSLoadChar(int bufferId, int pageId) {
        byte[] pageIDArray = short2byte((short) pageId);
        int checkSum = 0x01 + 0x00 + 0x06 + 0x07 + bufferId + (pageIDArray[0] & 0xff) +
				(pageIDArray[1] & 0xff);
        byte[] checkSumArray = short2byte((short) checkSum);
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x06, (byte) 0x07, (byte) bufferId, (byte)
				pageIDArray[0], (byte) pageIDArray[1], (byte) checkSumArray[0], (byte)
				checkSumArray[1]};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        printlog("PSLoadChar", length);
        if (length == 12) {
            return buffer[9];
        }
        return -1;
    }

    private synchronized byte[] PSSearch(int bufferId, int startPageId, int pageNum) {
        byte[] startPageIDArray = short2byte((short) startPageId);
        byte[] pageNumArray = short2byte((short) pageNum);
        int checkSum = 0x01 + 0x00 + 0x08 + 0x04 + bufferId + (startPageIDArray[0] & 0xff) +
				(startPageIDArray[1] & 0xff) + (pageNumArray[0] & 0xff) + (pageNumArray[1] & 0xff);
        byte[] checkSumArray = short2byte((short) checkSum);
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) bufferId, (byte)
				startPageIDArray[0], (byte) startPageIDArray[1], (byte) pageNumArray[0], (byte)
				pageNumArray[1], (byte) checkSumArray[0], (byte) checkSumArray[1]};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        printlog("PSSearch", length);
        if (length == 16) {
            byte[] result = new byte[16];
            System.arraycopy(buffer, 0, result, 0, length);
            return result;
        }
        return null;
    }

    /**
     * 精确比对CharBuffer1与CharBuffer2中的特征文件 注意点:下位机返回的数据里面还有一个得分，当得分大于等于50时，指纹匹配
     *
     * @return true：指纹匹配成功 false：比对失败
     */
    private synchronized boolean PSMatch() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x03, (byte) 0x00, (byte)
				0x07};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        printlog("PSMatch", length);
        if (length == 14) {
            if (buffer[9] == 0x00) {
                return score(buffer[10], buffer[11]);
            }
        }
        return false;
    }

    private synchronized byte[] PSEnroll() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x10, (byte) 0x00, (byte)
				0x14};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 600);
        printlog("PSEnroll", length);
        if (length == 14) {
            byte[] result = new byte[length];
            System.arraycopy(buffer, 0, result, 0, length);
            return result;
        }
        return null;
    }

    /**
     * @return -1:其它错误 -2：没有搜索到 >=0:搜索到的页码 0-1023
     */
    private synchronized byte[] PSIdentify() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x11, (byte) 0x00, (byte)
				0x15};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 600);
        printlog("PSIdentify", length);
        if (length == 16) {
            byte[] result = new byte[length];
            System.arraycopy(buffer, 0, result, 0, length);
            return result;
        }
        return null;
    }

    /**
     * 删除模板
     *
     * @param pageIDStart
     * @param delNum
     * @return
     */
    private synchronized byte PSDeleteChar(short pageIDStart, short delNum) {
        byte[] pageIDArray = short2byte(pageIDStart);
        byte[] delNumArray = short2byte(delNum);
        int checkSum = 0x01 + 0x07 + 0x0c + (pageIDArray[0] & 0xff) + (pageIDArray[1] & 0xff) +
				(delNumArray[0] & 0xff) + (delNumArray[1] & 0xff);
        byte[] checkSumArray = short2byte((short) checkSum);
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x07, (byte) 0x0c, pageIDArray[0],
				pageIDArray[1], delNumArray[0], delNumArray[1], checkSumArray[0],
				checkSumArray[1]};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        printlog("PSDeleteChar", length);
        if (length == 12) {
            return buffer[9];
        }
        return -1;
    }

    private synchronized byte PSEmpty() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x0d, (byte) 0x00, (byte)
				0x11};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        printlog("PSEmpty", length);
        if (length == 12) {
            return buffer[9];
        }
        return -1;
    }

    /**
     * 将特征缓冲区中的特征文件上传给上位机(默认的特征缓冲区为charbuffer1)
     *
     * @return byte[]：长度为512字节成功 否则失败 null:上传特征文件失败
     */
    private synchronized byte[] PSUpChar() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x08, (byte) 0x01, (byte)
				0x00, (byte) 0x0e};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        Log.i("xxx", "Size=" + length);
        printlog2("PSUpChar", length);
        // 响应为12字节，共4个数据包，每个包为139字节，所以返回的总字节数为568字节
        if (length >= UP_CHAR_RESPONSE_SIZE) {
            index = 12;// 数据包的起始下标
            packetNum = 0;
            byte[] packets = new byte[UP_CHAR_RESPONSE_SIZE];
            System.arraycopy(buffer, 0, packets, 0, UP_CHAR_RESPONSE_SIZE);
            return parsePacketData(packets);
        }
        return null;

    }

    /**
     * 上位机下载特征文件到模块的特征缓冲区(默认的缓冲区为CharBuffer2)
     *
     * @param model :指纹的特征文件
     * @return 返回值为确认码 确认码=00H表示下载成功 确认码=-1 表示失败
     */
    private synchronized byte PSDownChar(byte[] model) {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x09, (byte) 0x02, (byte)
				0x00, (byte) 0x10};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        printlog("PSDownChar", length);
        if (length == 12 && buffer[9] == 0x00) {
            sendData(model);
            return 0x00;
        }
        return -1;
    }

    /**
     * 将图像缓冲区的数据上传给上位机
     *
     * @return byte[]的length大小为36k ：一个字节含有两个像素，每个像素占4bits null:上传失败
     */
    private synchronized byte[] PSUpImage() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x0a, (byte) 0x00, (byte)
				0x0e};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        Log.i("whw", "PSUpImage length=" + length);
        if (length >= UP_IMAGE_RESPONSE_SIZE) {
            byte[] packets = new byte[length];
            System.arraycopy(buffer, 0, packets, 0, length);
            index = 12;
            packetNum = 0;
            byte[] data = parsePacketData(packets);
            return getFingerprintImage(data);
        }
        return null;

    }


    /**
     * 录入指纹图像
     *
     * @return 返回值为确认码 确认码=00H表示录入成功 确认码=01H表示收包有错 确认码=02H表示传感器上无手指
     * 确认码=03H表示录入不成功 确认码=-1 表示失败
     */
    private synchronized int PSGetImageEx() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, 0x01, 0x00, 0x03, 0x30, 0x00, 0x34};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 50);
        printlog("PSGetImageEx", length);
        if (length == 12) {
            return buffer[9];
        }
        return -1;
    }

    /**
     * 将ImageBuffer中的指纹图像生成特征文件存于charBuffer1或CharBuffer2中
     *
     * @param bufferId （CharBuffer1:1h,CharBuffer2:2h）
     * @return 返回值为确认码 确认码=00H表示生成特征成功 确认码=01H表示收包有错 确认码=06H表示指纹图像太乱而生不成图像
     * 确认码=07H表示指纹图像正常，但特征点太少而生不成特征 确认码=15H表示图像缓冲区内没有有效原始图而生不成图像 确认码=-1
     * 表示失败
     */
    private synchronized byte PSGenCharEx(int bufferId) {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 01, (byte) 0x00, (byte) 0x04, (byte) 0x32, (byte) bufferId, (byte)
				0x00, (byte) (0x37 + bufferId)};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 500);
        printlog("PSGenCharEx", length);
        if (length == 12) {
            return buffer[9];
        }
        return -1;
    }


    /**
     * 将图像缓冲区的数据上传给上位机
     *
     * @return byte[]的length大小为36k ：一个字节含有两个像素，每个像素占4bits null:上传失败
     */
    private synchronized byte[] PSUpImageEx() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x31, (byte) 0x00, (byte)
				0x35};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 100);
        Log.i("whw", "PSUpImageEx length=" + length);
        if (length >= UP_IMAGEEX_RESPONSE_SIZE) {
            byte[] packets = new byte[length];
            System.arraycopy(buffer, 0, packets, 0, length);
            index = 12;
            packetNum = 0;
            byte[] data = parsePacketData(packets);
            return getFingerprintImageEx(data);
        }
        return null;

    }

    private synchronized byte[] PSRS422(byte[] data) {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x33, (byte) 0x00, (byte)
				0x37};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 500);
        printlog("PSRS422", length);
        if (buffer[9] == 0x00) {
            int sum = getShort(buffer[19], buffer[20]);
            if (sum > 2) {
                byte[] result = new byte[sum - 2];
                System.arraycopy(buffer, 21, result, 0, sum - 2);
            return result;
            }
        }
        return null;


    }

    private synchronized byte[] PSRS422ex(byte[] data) {
        if (data == null) {
            byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff,
					(byte) 0xff, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x34, (byte) 0x00,
					(byte) 0x38};
            sendCommand(command);
            int length = SerialPortManager.getInstance().read(buffer, 500);
            printlog("PSRS422ex", length);
            if (buffer[9] == 0x00) {
                byte[] result = new byte[length];
                System.arraycopy(buffer, 0, result, 0, length);
                return result;
            }
            return null;
        } else {
            //	sendDataex(data);
            sendCommand(data);
//		int length = SerialPortManager.getInstance().read(buffer,12, 500);
/*			printlog("PSRS422ex", length);
		if (buffer[9]== 0x00) {
			byte[] result = new byte[length];
			System.arraycopy(buffer, 0, result, 0, length);
			return result;
		}*/
            return null;
        }

    }

    /**
     * PSGETIDCARDNO读卡
     *
     * @return int
     */
    private synchronized byte[] PSGETIDCARDNO() {
        byte[] command = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
				0xff, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x52, (byte) 0x00, (byte)
				0x56};
        sendCommand(command);
        int length = SerialPortManager.getInstance().read(buffer, 500);
        printlog("PSGETIDCARDNO", length);
        if (buffer[9] == 0x00) {
            byte[] result = new byte[4];
            System.arraycopy(buffer, 10, result, 0, 4);
            return result;
        }
        return null;
    }

    /**
     * 发送指纹模板数据包512字节,分为4次发送，3次数据包，一次结束包
     *
     * @param data
     */
    private void sendData(byte[] data) {
        // 数据包指令头
        byte[] dataPrefix = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0x02, (byte) 0x00, (byte) 0x82};
        // 结束包指令头
        byte[] endPrefix = {(byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0x08, (byte) 0x00, (byte) 0x82};
        byte[] command = new byte[dataPrefix.length + 128 + 2];
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                System.arraycopy(endPrefix, 0, command, 0, endPrefix.length);
            } else {
                System.arraycopy(dataPrefix, 0, command, 0, dataPrefix.length);
            }
            System.arraycopy(data, i * 128, command, dataPrefix.length, 128);
            short sum = 0;
            for (int j = 6; j < command.length - 2; j++) {
                sum += (command[j] & 0xff);
            }
            byte[] size = short2byte(sum);
            command[command.length - 2] = size[0];
            command[command.length - 1] = size[1];
            sendCommand(command);
            SystemClock.sleep(20);
        }
    }

    private int index;// 数据包的起始下标
    private int packetNum;// 数据包的个数

    private byte[] parsePacketData(byte[] packet) {
        int dstPos = 0;
        int packageLength = 0;
        int size = 0;
        do {
            packageLength = getShort(packet[index + 7], packet[index + 8]);
            System.arraycopy(packet, index + 9, data, dstPos, packageLength - 2);
            dstPos += packageLength - 2;// 2是校验和
            packetNum++;
            size += packageLength - 2;
        } while (moveToNext(index + 6, packageLength, packet));
        if (size != 0) {
            byte[] dataPackage = new byte[size];
            Log.i("whw", "**************packetNum=" + packetNum);
            System.arraycopy(data, 0, dataPackage, 0, size);
            return dataPackage;
        }
        return null;
    }

    private boolean moveToNext(int position, int packageLength, byte[] packet) {
        if (packet[position] == 0x02) {
            index += packageLength + 9;
            return true;
        }
        return false;
    }

    public byte[] getFingerprintImage(byte[] data) {
        if (data == null) {
            return null;
        }
        byte[] imageData = new byte[data.length * 2];
        // Log.i("whw", "*****************data.length="+data.length);
        for (int i = 0; i < data.length; i++) {
            imageData[i * 2] = (byte) (data[i] & 0xf0);
            imageData[i * 2 + 1] = (byte) (data[i] << 4 & 0xf0);
        }

        byte[] bmpData = toBmpByte(256, packetNum, imageData);
        return bmpData;
    }

    public byte[] getFingerprintImageEx(byte[] data) {
        if (data == null) {
            return null;
        }
        byte[] imageData = new byte[data.length * 2];
        // Log.i("whw", "*****************data.length="+data.length);
        for (int i = 0; i < data.length; i++) {
            imageData[i * 2] = (byte) (data[i] & 0xf0);
            imageData[i * 2 + 1] = (byte) (data[i] << 4 & 0xf0);
        }

        byte[] bmpData = toBmpByte(152, 200, imageData);
        return bmpData;
    }

    /**
     * 将数据传入内存
     */
    private byte[] toBmpByte(int width, int height, byte[] data) {
        byte[] buffer = null;
        try {
            // // 创建输出流文件对象
            // java.io.FileOutputStream fos = new
            // java.io.FileOutputStream(path);
            // // 创建原始数据输出流对象
            // java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // 给文件头的变量赋值
            int bfType = 0x424d; // 位图文件类型（0—1字节）
            int bfSize = 54 + 1024 + width * height;// bmp文件的大小（2—5字节）
            int bfReserved1 = 0;// 位图文件保留字，必须为0（6-7字节）
            int bfReserved2 = 0;// 位图文件保留字，必须为0（8-9字节）
            int bfOffBits = 54 + 1024;// 文件头开始到位图实际数据之间的字节的偏移量（10-13字节）

            // 输入数据的时候要注意输入的数据在内存中要占几个字节，
            // 然后再选择相应的写入方法，而不是它自己本身的数据类型
            // 输入文件头数据
            dos.writeShort(bfType); // 输入位图文件类型'BM'
            dos.write(changeByte(bfSize), 0, 4); // 输入位图文件大小
            dos.write(changeByte(bfReserved1), 0, 2);// 输入位图文件保留字
            dos.write(changeByte(bfReserved2), 0, 2);// 输入位图文件保留字
            dos.write(changeByte(bfOffBits), 0, 4);// 输入位图文件偏移量

            // 给信息头的变量赋值
            int biSize = 40;// 信息头所需的字节数（14-17字节）
            int biWidth = width;// 位图的宽（18-21字节）
            int biHeight = height;// 位图的高（22-25字节）
            int biPlanes = 1; // 目标设备的级别，必须是1（26-27字节）
            int biBitcount = 8;// 每个像素所需的位数（28-29字节），必须是1位（双色）、4位（16色）、8位（256色）或者24位（真彩色）之一。
            int biCompression = 0;// 位图压缩类型，必须是0（不压缩）（30-33字节）、1（BI_RLEB压缩类型）或2（BI_RLE4压缩类型）之一。
            int biSizeImage = width * height;// 实际位图图像的大小，即整个实际绘制的图像大小（34-37字节）
            int biXPelsPerMeter = 0;// 位图水平分辨率，每米像素数（38-41字节）这个数是系统默认值
            int biYPelsPerMeter = 0;// 位图垂直分辨率，每米像素数（42-45字节）这个数是系统默认值
            int biClrUsed = 256;// 位图实际使用的颜色表中的颜色数（46-49字节），如果为0的话，说明全部使用了
            int biClrImportant = 0;// 位图显示过程中重要的颜色数(50-53字节)，如果为0的话，说明全部重要

            // 因为java是大端存储，那么也就是说同样会大端输出。
            // 但计算机是按小端读取，如果我们不改变多字节数据的顺序的话，那么机器就不能正常读取。
            // 所以首先调用方法将int数据转变为多个byte数据，并且按小端存储的顺序。

            // 输入信息头数据
            dos.write(changeByte(biSize), 0, 4);// 输入信息头数据的总字节数
            dos.write(changeByte(biWidth), 0, 4);// 输入位图的宽
            dos.write(changeByte(biHeight), 0, 4);// 输入位图的高
            dos.write(changeByte(biPlanes), 0, 2);// 输入位图的目标设备级别
            dos.write(changeByte(biBitcount), 0, 2);// 输入每个像素占据的字节数
            dos.write(changeByte(biCompression), 0, 4);// 输入位图的压缩类型
            dos.write(changeByte(biSizeImage), 0, 4);// 输入位图的实际大小
            dos.write(changeByte(biXPelsPerMeter), 0, 4);// 输入位图的水平分辨率
            dos.write(changeByte(biYPelsPerMeter), 0, 4);// 输入位图的垂直分辨率
            dos.write(changeByte(biClrUsed), 0, 4);// 输入位图使用的总颜色数
            dos.write(changeByte(biClrImportant), 0, 4);// 输入位图使用过程中重要的颜色数

            // 构造调色板数据
            byte[] palatte = new byte[1024];
            for (int i = 0; i < 256; i++) {
                palatte[i * 4] = (byte) i;
                palatte[i * 4 + 1] = (byte) i;
                palatte[i * 4 + 2] = (byte) i;
                palatte[i * 4 + 3] = 0;
            }
            dos.write(palatte);

            dos.write(data);
            // 关闭数据的传输
            dos.flush();
            buffer = baos.toByteArray();
            dos.close();
            // fos.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 将一个int数据转为按小端顺序排列的字节数组
     *
     * @param data int数据
     * @return 按小端顺序排列的字节数组
     */
    private byte[] changeByte(int data) {
        byte b4 = (byte) ((data) >> 24);
        byte b3 = (byte) (((data) << 8) >> 24);
        byte b2 = (byte) (((data) << 16) >> 24);
        byte b1 = (byte) (((data) << 24) >> 24);
        byte[] bytes = {b1, b2, b3, b4};
        return bytes;
    }

    private short getShort(byte b1, byte b2) {
        short temp = 0;
        temp |= (b1 & 0xff);
        temp <<= 8;
        temp |= (b2 & 0xff);
        return temp;
    }

    private byte[] short2byte(short s) {
        byte[] size = new byte[2];
        size[1] = (byte) (s & 0xff);
        size[0] = (byte) ((s >> 8) & 0xff);
        return size;
    }

    /**
     * 指纹得分比对,>=50分返回true：比对成功
     *
     * @return
     */
    private boolean score(byte b1, byte b2) {
        byte[] temp = {b1, b2};
        short score = 0;
        score |= (temp[0] & 0xff);
        score <<= 8;
        score |= (temp[1] & 0xff);
        Log.i("whw", "---------------score=" + score);
        return score >= 50;
    }

    private void sendCommand(byte[] command) {
//		Log.i("whw", "sendCommand hex=" + DataUtils.toHexString(command));
        try {
            SerialPortManager.getInstance().write(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printlog(String tag, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(buffer, 0, temp, 0, length);
        Log.i("whw", tag + "=" + toHexString(temp));
    }

    private void printlog2(String tag, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(buffer, 0, temp, 0, length);
        Log.i("xxx", tag + "=" + toHexString(temp));
    }

    public String toHexString(byte[] b) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            buffer.append(toHexString1(b[i]));
        }
        return buffer.toString();
    }

    public String toHexString1(byte b) {
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            return "0" + s;
        } else {
            return s;
        }
    }
}
