package com.futronictech;

import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;


import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;

public class ModuleFutronic extends ReactContextBaseJavaModule {

    private FPScan mFPScan = null;
    private File SyncDir = null;
    private Handler mHandler = null;
    public static boolean mUsbHostMode = true;

    public static UsbDeviceDataExchangeImpl usb_host_ctx = null;

    public ModuleFutronic(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ModuleFutronic";
    }

    @ReactMethod
    public void checkDeviceFingerprint(Promise promise) {

        if(usb_host_ctx == null){
            HandlerThread mHandlerThread = new HandlerThread("futronictech");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
            usb_host_ctx = new UsbDeviceDataExchangeImpl(getReactApplicationContext() , mHandler);
        }

        if( mFPScan != null )
        {
            FtrScanDemoUsbHostActivity.mStop = true;       
            mFPScan.stop();            
        }

        FtrScanDemoUsbHostActivity.mStop = false;       

        usb_host_ctx.CloseDevice();
        if(usb_host_ctx.OpenDevice(0, true))
        {
            Log.i("FUTRONIC", "Device is open");
            promise.resolve(true);
        }
        else
        {
            if(!usb_host_ctx.IsPendingOpen())
            {
                Log.i("FUTRONIC", "Can not start scan operation.\nCan't open scanner device");
            }
            promise.resolve(false);
        }

    }

    @ReactMethod
    public void stopScan()
    {
        if( mFPScan != null && !FtrScanDemoUsbHostActivity.mStop)
        {
            Log.i("FUTRONIC", "Stoping scan");
            mFPScan.stop();
            mFPScan = null;
	        FtrScanDemoUsbHostActivity.mStop = true;       

        }
    }

    @ReactMethod
    public void enableLFD()
    {
        if ( mFPScan != null )
        {
            Log.i("FUTRONIC", "LFD [ENABLE]");
            mFPScan.enableLFD();
        }
    }

    @ReactMethod
    public void disableLFD()
    {
        if ( mFPScan != null )
        {
            Log.i("FUTRONIC", "LFD [DISABLE]");
            mFPScan.disableLFD();
        }
    }

    @ReactMethod
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(getCurrentActivity(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                //Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(getCurrentActivity(), new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                return false;
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @ReactMethod
    public void StartScan()
    {
        if( mFPScan != null )
        {
            FtrScanDemoUsbHostActivity.mStop = true;       
            mFPScan.stop();            
        }

        FtrScanDemoUsbHostActivity.mStop = false;       

        SyncDir = getReactApplicationContext().getExternalFilesDir(null);
        mFPScan = new FPScan(usb_host_ctx, SyncDir, mHandler);
        Log.i("FUTRONIC", "Starting scan");
        mFPScan.start();
    }

    @ReactMethod
    public void checkDeviceIsOpen(Promise promise)
    {        
        if ( mFPScan != null )
        {
            if ( mFPScan.errCode != 87 )
                promise.resolve(true);
            else
                promise.resolve(false);
        } else 
            promise.resolve(false);
    }

    public void msgToast(String msg) {
        Toast toast = Toast.makeText(getReactApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.show();
    }

 }


