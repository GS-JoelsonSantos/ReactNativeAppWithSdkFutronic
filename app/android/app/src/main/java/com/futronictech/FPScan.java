package com.futronictech;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;

public class FPScan {
    private final Handler mHandler;
    private ScanThread mScanThread;
    private UsbDeviceDataExchangeImpl ctx = null;
    private File mDirSync;

    public FPScan(UsbDeviceDataExchangeImpl context, File dirSync, Handler handler ) {
        mHandler = handler;
        ctx = context;
		mDirSync = dirSync;
    }

    public synchronized void start() {
        if (mScanThread == null) {
        	mScanThread = new ScanThread();
        	mScanThread.start();
        }
    }
    
    public synchronized void stop() {
        if (mScanThread != null) {mScanThread.cancel(); mScanThread = null;}
    }
    
    private class ScanThread extends Thread {
    	private boolean bGetInfo;
    	private Scanner devScan = null;
    	private String strInfo;
    	private int mask, flag;
    	private int errCode;
    	private boolean bRet;
		private int nNfiq = 0;
    	
        public ScanThread() {
        	bGetInfo=false;
        	devScan = new Scanner();
        	// set the GlobalSyncDir to 'getExternalFilesDir'
			// *** Returns the absolute path to the directory on the primary shared/external storage device where the application can place persistent files it owns. *** //
			if( !devScan.SetGlobalSyncDir(mDirSync.toString()) )
			{
				mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_SHOW_MSG, -1, -1, devScan.GetErrorMessage()).sendToTarget();
				mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_ERROR).sendToTarget();
				devScan = null;
				return;
			}
        	/******************************************
        	// By default, a directory of "/mnt/sdcard/Android/" is necessary for libftrScanAPI.so to work properly
        	// in case you want to change it, you can set it by calling the below function
        	String SyncDir =  "/mnt/sdcard/test/";  // YOUR DIRECTORY
        	if( !devScan.SetGlobalSyncDir(SyncDir) )
        	{
                mHandler.obtainMessage(FtrScanDemoActivity.MESSAGE_SHOW_MSG, -1, -1, devScan.GetErrorMessage()).sendToTarget();
                mHandler.obtainMessage(FtrScanDemoActivity.MESSAGE_ERROR).sendToTarget();
           	    devScan = null;
	            return;
        	}
        	*******************************************/
        }

        public void run() {
            while (!FtrScanDemoUsbHostActivity.mStop) 
            {
            	if(!bGetInfo)
            	{
            		Log.i("FUTRONIC", "Run fp scan");
            		boolean bRet;
         	        if( FtrScanDemoUsbHostActivity.mUsbHostMode )
         	        	bRet = devScan.OpenDeviceOnInterfaceUsbHost(ctx);
         	        else
         	        	bRet = devScan.OpenDevice();
                    if( !bRet )
                    {
                        //Toast.makeText(this, strInfo, Toast.LENGTH_LONG).show();
                    	if(FtrScanDemoUsbHostActivity.mUsbHostMode)
                    		ctx.CloseDevice();
                        mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_SHOW_MSG, -1, -1, devScan.GetErrorMessage()).sendToTarget();
                        mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_ERROR).sendToTarget();
                        return;
                    }
            		
            		if( !devScan.GetImageSize() )
	    	        {
	    	        	mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_SHOW_MSG, -1, -1, devScan.GetErrorMessage()).sendToTarget();
	    	        	if( FtrScanDemoUsbHostActivity.mUsbHostMode )
	    	        		devScan.CloseDeviceUsbHost();
	    	        	else
	    	        		devScan.CloseDevice();
                        mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_ERROR).sendToTarget();
	    	            return;
	    	        }
					
	    	        FtrScanDemoUsbHostActivity.InitFingerPictureParameters(devScan.GetImageWidth(), devScan.GetImaegHeight());
					
	    	        strInfo = devScan.GetVersionInfo();
    	        	mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_SHOW_SCANNER_INFO, -1, -1, strInfo).sendToTarget();
	    	        bGetInfo = true;
             	}
                //set options
                flag = 0;
                mask = devScan.FTR_OPTIONS_DETECT_FAKE_FINGER | devScan.FTR_OPTIONS_INVERT_IMAGE;
                if(FtrScanDemoUsbHostActivity.mLFD)
                	flag |= devScan.FTR_OPTIONS_DETECT_FAKE_FINGER;
                if( FtrScanDemoUsbHostActivity.mInvertImage)
                	flag |= devScan.FTR_OPTIONS_INVERT_IMAGE;                
                if( !devScan.SetOptions(mask, flag) )
    	        	mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_SHOW_MSG, -1, -1, devScan.GetErrorMessage()).sendToTarget();
                // get frame / image2
                long lT1 = SystemClock.uptimeMillis();
                if( FtrScanDemoUsbHostActivity.mFrame )
                	bRet = devScan.GetFrame(FtrScanDemoUsbHostActivity.mImageFP);
                else
                	bRet = devScan.GetImage2(4,FtrScanDemoUsbHostActivity.mImageFP);
                if( !bRet )
                {
                	mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_SHOW_MSG, -1, -1, devScan.GetErrorMessage()).sendToTarget();
                	errCode = devScan.GetErrorCode();
                	if( errCode != devScan.FTR_ERROR_EMPTY_FRAME && errCode != devScan.FTR_ERROR_MOVABLE_FINGER &&  errCode != devScan.FTR_ERROR_NO_FRAME )
                	{
	    	        	if( FtrScanDemoUsbHostActivity.mUsbHostMode )
	    	        		devScan.CloseDeviceUsbHost();
	    	        	else
	    	        		devScan.CloseDevice();
                        mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_ERROR).sendToTarget();
	    	            return;                		
                	}    	        	
                }
                else
                {
					if( FtrScanDemoUsbHostActivity.mNFIQ )
                	{
	                	if( devScan.GetNfiqFromImage(FtrScanDemoUsbHostActivity.mImageFP, FtrScanDemoUsbHostActivity.mImageWidth, FtrScanDemoUsbHostActivity.mImageHeight))
	                		nNfiq = devScan.GetNIFQValue();
                	}				
                	if( FtrScanDemoUsbHostActivity.mFrame ) 
                		strInfo = String.format("OK. GetFrame time is %d(ms)",  SystemClock.uptimeMillis()-lT1);
                	else
                		strInfo = String.format("OK. GetImage2 time is %d(ms)",  SystemClock.uptimeMillis()-lT1);
                	if( FtrScanDemoUsbHostActivity.mNFIQ )
                	{
                		strInfo = strInfo + String.format("NFIQ=%d", nNfiq);
                	}						
                	mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_SHOW_MSG, -1, -1, strInfo ).sendToTarget();
                }
				synchronized (FtrScanDemoUsbHostActivity.mSyncObj) 
                {
					//show image
					mHandler.obtainMessage(FtrScanDemoUsbHostActivity.MESSAGE_SHOW_IMAGE).sendToTarget();
					try {
						FtrScanDemoUsbHostActivity.mSyncObj.wait(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
            }
            //close device
        	if( FtrScanDemoUsbHostActivity.mUsbHostMode )
        		devScan.CloseDeviceUsbHost();
        	else
        		devScan.CloseDevice();          
        }

        public void cancel() {
        	FtrScanDemoUsbHostActivity.mStop = true;        	        	       	        	
        	try {
        		synchronized (FtrScanDemoUsbHostActivity.mSyncObj) 
		        {
        			FtrScanDemoUsbHostActivity.mSyncObj.notifyAll();
		        }        		
        		this.join();	
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       	      	           	
        }
    }    
}
