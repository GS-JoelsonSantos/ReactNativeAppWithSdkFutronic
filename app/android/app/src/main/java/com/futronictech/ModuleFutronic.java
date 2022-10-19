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
     public void checkFingerprint() {
         HandlerThread mHandlerThread = new HandlerThread("futronictech");
         mHandlerThread.start();
         mHandler = new Handler(mHandlerThread.getLooper());
         usb_host_ctx = new UsbDeviceDataExchangeImpl(getReactApplicationContext() , mHandler);

         if (isStoragePermissionGranted())
         {
             Log.i("FUTRONIC", "Permission is granted");

             usb_host_ctx.CloseDevice();
             if(usb_host_ctx.OpenDevice(0, true))
             {
                 StartScan();
             }
             else
             {
                 msgToast("Permission is revoked");
                 if(!usb_host_ctx.IsPendingOpen())
                 {
                     Log.i("FUTRONIC", "Can not start scan operation.\nCan't open scanner device");
                 }
             }
         }else{
             msgToast("Permission is revoked");
             Log.i("FUTRONIC", "Permission is revoked");
         }
     }

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

    private boolean StartScan()
    {
         SyncDir = getReactApplicationContext().getExternalFilesDir(null);
         mFPScan = new FPScan(usb_host_ctx, SyncDir, mHandler);
         Log.i("FUTRONIC", "Starting scan");
         mFPScan.start();
         return true;
     }

    public void msgToast(String msg) {
        Toast toast = Toast.makeText(getReactApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.show();
    }

 }


