package com.futronictech;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

import com.app.R;

public class FtrScanDemoUsbHostActivity extends Activity {
    /** Called when the activity is first created. */
	private static Button mButtonScan;
	private static Button mButtonStop;
	private Button mButtonSave;
	private static TextView mMessage;
	private static TextView mScannerInfo;
	private static ImageView mFingerImage;
	private CheckBox mCheckFrame;
	private CheckBox mCheckLFD;
	private CheckBox mCheckInvertImage;
	private CheckBox mCheckUsbHostMode;
	private CheckBox mCheckNFIQ;
	
    public static boolean mStop = false;
	public static boolean mFrame = true;
	public static boolean mLFD = false;
	public static boolean mInvertImage = false;
    public static boolean mNFIQ = false;
	
    public static final int MESSAGE_SHOW_MSG = 1;
    public static final int MESSAGE_SHOW_SCANNER_INFO = 2;
    public static final int MESSAGE_SHOW_IMAGE = 3;
    public static final int MESSAGE_ERROR = 4;
    public static final int MESSAGE_TRACE = 5;

    public static byte[] mImageFP = null;  
	public static Object mSyncObj= new Object();	
	
    public static int mImageWidth = 0;
    public static int mImageHeight = 0;
	private static int[] mPixels = null;
    public static Bitmap mBitmapFP = null;
	private static Canvas mCanvas = null;
    private static Paint mPaint = null;
	
    private FPScan mFPScan = null;   
    //
    public static boolean mUsbHostMode = true;

    // Intent request codes
    private static final int REQUEST_FILE_FORMAT = 1;
    public static UsbDeviceDataExchangeImpl usb_host_ctx = null;

    private File SyncDir = null;

	public static void InitFingerPictureParameters(int wight, int height)
    {
    	 mImageWidth = wight;
	     mImageHeight = height;
         
         mImageFP = new byte[FtrScanDemoUsbHostActivity.mImageWidth*FtrScanDemoUsbHostActivity.mImageHeight];
	     mPixels = new int[FtrScanDemoUsbHostActivity.mImageWidth*FtrScanDemoUsbHostActivity.mImageHeight];
	  	     	             
	     mBitmapFP = Bitmap.createBitmap(wight, height, Config.RGB_565); 
	     
	     mCanvas = new Canvas(mBitmapFP); 
	     mPaint = new Paint(); 
	        
	     ColorMatrix cm = new ColorMatrix(); 
	     cm.setSaturation(0); 
	     ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm); 
	     mPaint.setColorFilter(f);
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mFrame = true;
        mUsbHostMode = true;
    	mLFD = mInvertImage = false;    	
    	mButtonScan = (Button) findViewById(R.id.btnScan);
        mButtonStop = (Button) findViewById(R.id.btnStop);
        mButtonSave = (Button) findViewById(R.id.btnSave);
        mMessage = (TextView) findViewById(R.id.tvMessage);
        mScannerInfo = (TextView) findViewById(R.id.tvScannerInfo);
        mFingerImage = (ImageView) findViewById(R.id.imageFinger);
        mCheckFrame = (CheckBox) findViewById(R.id.cbFrame);
        mCheckLFD = (CheckBox) findViewById(R.id.cbLFD);
        mCheckInvertImage = (CheckBox) findViewById(R.id.cbInvertImage);
        mCheckUsbHostMode = (CheckBox) findViewById(R.id.cbUsbHostMode);
		mCheckNFIQ = (CheckBox) findViewById(R.id.cbNFIQ);

        usb_host_ctx = new UsbDeviceDataExchangeImpl(this , mHandler);
		SyncDir = this.getExternalFilesDir(null);

		if ( !isStoragePermissionGranted() )
		{
			mButtonScan.setEnabled(false);
		}

        mButtonScan.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {	        		
	        		if( mFPScan != null )
	        		{
	        			mStop = true;
	        			mFPScan.stop();
	        			
	        		}
	        		mStop = false;
	        		if(mUsbHostMode)
	        		{
		        		usb_host_ctx.CloseDevice();
		        		if(usb_host_ctx.OpenDevice(0, true))
		                {
		        			if( StartScan() )
			        		{
			        			mButtonScan.setEnabled(false);
			        	        mButtonSave.setEnabled(false);
			        	        mCheckUsbHostMode.setEnabled(false);
			        	        mButtonStop.setEnabled(true);
			        		}	
		                }
		            	else
		            	{
		            		if(!usb_host_ctx.IsPendingOpen())
		            		{
		            			mMessage.setText("Can not start scan operation.\nCan't open scanner device");
		            		}
		            	}    
	        		}
	        		else
	        		{
	        			if( StartScan() )
		        		{
		        			mButtonScan.setEnabled(false);
		        	        mButtonSave.setEnabled(false);
		        	        mCheckUsbHostMode.setEnabled(false);
		        	        mButtonStop.setEnabled(true);
		        		}	
	        		}
        		}
        });
        
        mButtonStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	        		mStop = true;	       
	        		if( mFPScan != null )
	        		{
	        			mFPScan.stop();
	        			mFPScan = null;
       			
	        		}	        		
	        		mButtonScan.setEnabled(true);
	        		mButtonSave.setEnabled(true);
	        		mCheckUsbHostMode.setEnabled(true);
	        		mButtonStop.setEnabled(false);	        		
        		}
        });
        
        mButtonSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {   
            	if( mImageFP != null)
            		SaveImage();
            }
        });
        

        mCheckFrame.setOnCheckedChangeListener(new OnCheckedChangeListener() { 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
			if (buttonView.isChecked()) 
				mFrame = true;
			else 
			{
				mFrame = false;
				mCheckLFD.setChecked(false);
				mLFD = false;
			}
			} 
        });


        mCheckLFD.setOnCheckedChangeListener(new OnCheckedChangeListener() { 
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
			if (buttonView.isChecked()) 
				mLFD = true;
			else 
				mLFD = false;
			} 
        });
        
        mCheckInvertImage.setOnCheckedChangeListener(new OnCheckedChangeListener() { 
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
			if (buttonView.isChecked()) 
				mInvertImage = true;
			else 
				mInvertImage = false;
			} 
        });

        mCheckUsbHostMode.setOnCheckedChangeListener(new OnCheckedChangeListener() { 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
			if (buttonView.isChecked()) 
				mUsbHostMode = true;
			else 
				mUsbHostMode = false;
			} 
        });
		
		mCheckNFIQ.setOnCheckedChangeListener(new OnCheckedChangeListener() { 			
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
 			if (buttonView.isChecked()) 
 				mNFIQ = true;
 			else 
 				mNFIQ = false;
 			} 
         });
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		mStop = true;	       
		if( mFPScan != null )
		{
			mFPScan.stop();
			mFPScan = null;
		}
		usb_host_ctx.CloseDevice();
		usb_host_ctx.Destroy();
		usb_host_ctx = null;
    }

	public boolean isStoragePermissionGranted() {
		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				//Log.v(TAG,"Permission is granted");
				return true;
			} else {
				//Log.v(TAG,"Permission is revoked");
				ActivityCompat.requestPermissions(this, new String[]{
						Manifest.permission.READ_EXTERNAL_STORAGE,
						Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
				return false;
			}
		}
		else { //permission is automatically granted on sdk<23 upon installation
			//Log.v(TAG,"Permission is granted");
			return true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if( requestCode == 1) {
			Log.v("FtrScanDemoUsbHost","Permission: "+permissions[0]+ "was "+grantResults[0]);
			Log.v("FtrScanDemoUsbHost","Permission: "+permissions[1]+ "was "+grantResults[1]);
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//resume tasks needing this permission
				mButtonScan.setEnabled(true);
			}
		}
	}

	private boolean StartScan()
    {
		mFPScan = new FPScan(usb_host_ctx, SyncDir, mHandler);
		mFPScan.start();		
		return true;
    }
    
    private void SaveImage()
    {
	    Intent serverIntent = new Intent(this, SelectFileFormatActivity.class);
	    startActivityForResult(serverIntent, REQUEST_FILE_FORMAT);
    }
    
    static void SaveImageByFileFormat(String fileFormat, String fileName)
    {
 	   	if( fileFormat.compareTo("WSQ") == 0 )	//save wsq file
    	{    	
    		Scanner devScan = new Scanner();
    		boolean bRet;
    		if( mUsbHostMode )
    			bRet = devScan.OpenDeviceOnInterfaceUsbHost(usb_host_ctx);
    		else
    			bRet = devScan.OpenDevice();
    		if( !bRet )
    		{
				Log.i("FUTRONIC",devScan.GetErrorMessage());
                return;    			
    		}
    		byte[] wsqImg = new byte[mImageWidth*mImageHeight];
    		long hDevice = devScan.GetDeviceHandle();
    		ftrWsqAndroidHelper wsqHelper = new ftrWsqAndroidHelper();
    		if( wsqHelper.ConvertRawToWsq(hDevice, mImageWidth, mImageHeight, 2.25f, mImageFP, wsqImg) )
    		{  			
    	        File file = new File(fileName);                
    	        try { 
    	            FileOutputStream out = new FileOutputStream(file);                    
    	            out.write(wsqImg, 0, wsqHelper.mWSQ_size);	// save the wsq_size bytes data to file
    	            out.close();
					Log.i("FUTRONIC","Image is saved as " + fileName);
    	         } catch (Exception e) {
					Log.i("FUTRONIC","Exception in saving file");
    	         }     			
    		}
    		else
				Log.i("FUTRONIC","Failed to convert the image!");
    		if( mUsbHostMode )
    			devScan.CloseDeviceUsbHost();
    		else
    			devScan.CloseDevice();
    		return;
    	}
    	// 0 - save bitmap file 
        File file = new File(fileName);                
        try { 
            FileOutputStream out = new FileOutputStream(file);                    
            //mBitmapFP.compress(Bitmap.CompressFormat.PNG, 90, out);
            MyBitmapFile fileBMP = new MyBitmapFile(mImageWidth, mImageHeight, mImageFP);
            out.write(fileBMP.toBytes());
            out.close();
			Log.i("FUTRONIC","Image is saved as " + fileName);
         } catch (Exception e) {
			Log.i("FUTRONIC","Exception in saving file");
         } 
    }
    
    // The Handler that gets information back from the FPScan
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SHOW_MSG:            	
            	String showMsg = (String) msg.obj;
                mMessage.setText(showMsg);
                break;
            case MESSAGE_SHOW_SCANNER_INFO:            	
            	String showInfo = (String) msg.obj;
                mScannerInfo.setText(showInfo);
                break;
            case MESSAGE_SHOW_IMAGE:
            	ShowBitmap();
                break;              
            case MESSAGE_ERROR:
           		//mFPScan = null;
            	mButtonScan.setEnabled(true);
            	mCheckUsbHostMode.setEnabled(true);
            	mButtonStop.setEnabled(false);
            	break;
            case UsbDeviceDataExchangeImpl.MESSAGE_ALLOW_DEVICE:
            	if(usb_host_ctx.ValidateContext())
            	{
            		if( StartScan() )
	        		{
	        			mButtonScan.setEnabled(false);
	        	        mButtonSave.setEnabled(false);
	        	        mCheckUsbHostMode.setEnabled(false);
	        	        mButtonStop.setEnabled(true);
	        		}	
            	}
            	else
            		mMessage.setText("Can't open scanner device");
            	break;           
	        case UsbDeviceDataExchangeImpl.MESSAGE_DENY_DEVICE:
            	mMessage.setText("User deny scanner device");
            	break;
            }
        }
    };
    
    private static void ShowBitmap()
    {
    	for( int i=0; i<mImageWidth * mImageHeight; i++)
    	{
    		mPixels[i] = Color.rgb(mImageFP[i],mImageFP[i],mImageFP[i]);
    	}
     	                  
        mCanvas.drawBitmap(mPixels, 0, mImageWidth, 0, 0,  mImageWidth, mImageHeight, false, mPaint);
        
        mFingerImage.setImageBitmap(mBitmapFP);
        mFingerImage.invalidate();
        
        synchronized (mSyncObj) 
        {
         	mSyncObj.notifyAll();
        }
    }        

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
         case REQUEST_FILE_FORMAT:
			 if (resultCode == Activity.RESULT_OK) {
			     // Get the file format
				 String[] extraString = data.getExtras().getStringArray(SelectFileFormatActivity.EXTRA_FILE_FORMAT);
				 String fileFormat = extraString[0];
				 String fileName = extraString[1];
				 SaveImageByFileFormat(fileFormat, fileName);
             }
			 else
				 mMessage.setText("Cancelled!");
             break;            
        }
    }

}

