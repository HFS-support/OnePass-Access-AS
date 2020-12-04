package android_serialport_api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.content.SharedPreferences;
import android.fpi.MtGpio;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.zyapi.CommonApi;

public class SerialPortManager {
	/**
	 * 串口设备路径
	 */
	private static String PATH ="";
	/**
	 * 串口波特率
	 */
	private static final int BAUDRATE = 460800;

	final String GPIO_DEV = "/sys/GPIO/GPIO13/value";
	final byte[] UP = { '1' };
	final byte[] DOWN = { '0' };

	private static SerialPortManager mSerialPortManager = new SerialPortManager();

	private SerialPort mSerialPort = null;

	private boolean isOpen;

	private boolean firstOpen = false;


	private OutputStream mOutputStream;

	private InputStream mInputStream;

	private byte[] mBuffer = new byte[100 * 1024];

	private int mCurrentSize = 0;

	private Looper looper;

	private HandlerThread ht;

	private ReadThread mReadThread;

	private String sModel="";
	

	/**
	 * 每调用一次就返回一个新的实例对象。
	 *
	 * @return
	 */
	public AsyncFingerprint getNewAsyncFingerprint() {
		if (!isOpen) {
			try {
				openSerialPort();
				isOpen = true;
			} catch (InvalidParameterException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new AsyncFingerprint(looper);
	}

	public SerialPortManager() {
		sModel=getDeviceModel();
	}

	/**
	 * 获取该类的实例对象，为单例
	 *
	 * @return
	 */
	public static SerialPortManager getInstance() {
		return mSerialPortManager;
	}

	/**
	 * 判断串口是否打开
	 *
	 * @return true：打开 false：未打开
	 */
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * 判断串口是否是第一次打开，如果是第一次，需要加延时，可以让身份证模块进行初始化。
	 * @return
	 */
	public boolean isFirstOpen() {
		return firstOpen;
	}

	public void setFirstOpen(boolean firstOpen) {
		this.firstOpen = firstOpen;
	}

	private void createWorkThread() {
		ht = new HandlerThread("workerThread");
		ht.start();
		looper = ht.getLooper();
	}

	/**
	 * 打开串口，如果需要读取身份证和指纹信息，必须先打开串口，调用此方法
	 *
	 * @throws SecurityException
	 * @throws IOException
	 * @throws InvalidParameterException
	 */
	public void openSerialPort() throws SecurityException, IOException,
			InvalidParameterException {
		if (mSerialPort == null) {
			// 上电
			setUpGpio();

			/* Open the serial port */
			mSerialPort = new SerialPort(new File(PATH), BAUDRATE, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
			mReadThread = new ReadThread();
			mReadThread.start();
			isOpen = true;
			createWorkThread();
			firstOpen = true;
		}
	}

	/**
	 * 关闭串口，如果不需要读取指纹或身份证信息时，就关闭串口(可以节约电池电量)，建议程序退出时关闭
	 */
	public void closeSerialPort() {
		if (ht != null) {
			ht.quit();
		}
		ht = null;
		if (mReadThread != null)
			mReadThread.interrupt();
		mReadThread = null;
		try {
			// 断电
			setDownGpio();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (mSerialPort != null) {
			try {
				mOutputStream.close();
				mInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mSerialPort.close();
			mSerialPort = null;
		}
		isOpen = false;
		mCurrentSize = 0;
	}

	protected synchronized int read(byte buffer[], int waittime) {
		/**
		 * 超时时间（ms）
		 */
		int time = 4000;
		int sleepTime = 50;
		int length = time / sleepTime;
		boolean shutDown = false;
		int[] readDataLength = new int[3];
		for (int i = 0; i < length; i++) {
			if (mCurrentSize == 0) {
				SystemClock.sleep(sleepTime);
				continue;
			} else {
				break;
			}
		}

		if (mCurrentSize > 0) {
			while (!shutDown) {
				SystemClock.sleep(sleepTime);
				readDataLength[0] = readDataLength[1];
				readDataLength[1] = readDataLength[2];
				readDataLength[2] = mCurrentSize;
				Log.i("whw", "read2    mCurrentSize=" + mCurrentSize);
				if (readDataLength[0] == readDataLength[1]
						&& readDataLength[1] == readDataLength[2]) {
					shutDown = true;
				}
			}
			if (mCurrentSize <= buffer.length) {
				System.arraycopy(mBuffer, 0, buffer, 0, mCurrentSize);
			}
		}
		return mCurrentSize;
	}

	protected synchronized void write(byte[] data) throws IOException {
		mCurrentSize = 0;
		mOutputStream.write(data);
	}

	public void setUpGpio() throws IOException {

		MtGpio.getInstance().FPPowerSwitch(true);

		PATH = "/dev/ttyMT1";
	}

	public void setDownGpio() throws IOException {

		MtGpio.getInstance().FPPowerSwitch(false);

	}

	private class ReadThread extends Thread {

		@Override
		public void run() {
			while (!isInterrupted()) {
				int length = 0;
				try {
					byte[] buffer = new byte[100];
					if (mInputStream == null)
						return;
					length = mInputStream.read(buffer);
					if (length > 0) {
						System.arraycopy(buffer, 0, mBuffer, mCurrentSize,
								length);
						mCurrentSize += length;
					}
					Log.i("whw", "mCurrentSize=" + mCurrentSize + "  length="
							+ length);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	public boolean WriteIoFile(String strValue,String Path){
		File file;
		FileOutputStream outstream;
		try{
			file = new File(Path);
			outstream = new FileOutputStream(file);
			outstream.write(strValue.getBytes());
			outstream.close();
		} catch(FileNotFoundException e){
			return false;
		} catch(IOException e){
			return false;
		}
		return true;
	}

	public void IoControl(boolean bOpen){
		//String GPIO_DIR ="/sys/devices/soc.0/scan_se955.69/";
		String GPIO_DIR ="/sys/devices/soc.0/scan_se955.71/";
		String[] GPIO_FILE={"start_scan",
				"power_status"};
		if(bOpen){
			WriteIoFile("on",GPIO_DIR+GPIO_FILE[0]);
			WriteIoFile("on",GPIO_DIR+GPIO_FILE[1]);
		}else{
			WriteIoFile("off",GPIO_DIR+GPIO_FILE[0]);
			WriteIoFile("off",GPIO_DIR+GPIO_FILE[1]);
		}
	}

	@SuppressWarnings("static-access")
	public String getDeviceModel(){
		Build bd = new Build();
		String model = bd.MODEL;
		//android.os.Build.MODEL   
		//android.os.Build.VERSION.RELEASE 
		return model;
	}
}
