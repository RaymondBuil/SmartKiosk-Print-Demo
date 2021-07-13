package nl.rkeb.kioskprintdemo;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zebra.printconnectintentswrapper.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import nl.rkeb.kioskprintdemo.databinding.ActivityFullscreenBinding;

import static android.content.ContentValues.TAG;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(myDataWedgeBroadcastReceiver);
    }

    //
    // After registering the broadcast receiver, the next step (below) is to define it.
    // Here it's done in the MainActivity.java, but also can be handled by a separate class.
    // The logic of extracting the scanned data and displaying it on the screen
    // is executed in its own method (later in the code). Note the use of the
    // extra keys defined in the strings.xml file.
    //
    private BroadcastReceiver myDataWedgeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();

            //  This is useful for debugging to verify the format of received intents from DataWedge
            //for (String key : b.keySet())
            //{
            //    Log.v(LOG_TAG, key);
            //}

            if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                //  Received a barcode scan
                try {
                    File sdcard = Environment.getExternalStorageDirectory();
                    printerPassthrough(callTemplate(intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type)),intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data))), "Print scanned barcode data using template.zpl");

                } catch (Exception e) {
                    //  Catch if the UI does not exist when we receive the broadcast
                }
            }
        }
    };


    private void askPermission(String permission,int requestCode) {
        if (ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
            // We Don't have permission
            ActivityCompat.requestPermissions(this,new String[]{permission},requestCode);
        }

    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private ActivityFullscreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContent;

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
        binding.dummyButton.setOnTouchListener(mDelayHideTouchListener);

        Button prnCfg = findViewById(R.id.buttonPrnCfg);
        prnCfg.setOnClickListener(v -> {

            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();
            printerPassthrough(getConfigFile(sdcard, "config.txt"), "Config File config.txt");
        });

        Button prnTemplate = findViewById(R.id.buttonPrnTemplate);
        prnTemplate.setOnClickListener(v -> {

            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();
            printerPassthrough(getConfigFile(sdcard, "template.zpl"), "Store template.zpl to printer");
        });

        Button prnLabel1 = findViewById(R.id.buttonPrnLabel1);
        prnLabel1.setOnClickListener(v -> {

            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();
            printerPassthrough(getConfigFile(sdcard, "label1.zpl"), "Print Label 1 from label1.zpl");
        });

        Button prnLabel2 = findViewById(R.id.buttonPrnLabel2);
        prnLabel2.setOnClickListener(v -> {

            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();
            printerPassthrough(getConfigFile(sdcard, "label2.zpl"), "Print Label 2 from label2.zpl");
        });

        askPermission(Manifest.permission.READ_EXTERNAL_STORAGE,1);
        askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,1);

        createConfigAndTemplate("copy_",Environment.getExternalStorageDirectory().getAbsolutePath());

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        registerReceiver(myDataWedgeBroadcastReceiver, filter);

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
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void createConfigAndTemplate(String myFileID, String myFilesDir) {
        try {
            AssetManager am = getAssets();
            String[] list = am.list("");
            String copyToFile;
            for (String s:list) {
                if (s.startsWith(myFileID)) {

                    copyToFile = myFilesDir + "/" + s.replace(myFileID, "");

                    File tempFile = new File(copyToFile);

                    if (!tempFile.exists()) {

                        Log.d(TAG, "Reading asset file " + s);
                        InputStream inStream = am.open(s);
                        int size = inStream.available();
                        byte[] buffer = new byte[size];
                        inStream.read(buffer);
                        inStream.close();

                        Log.d(TAG, "Writting asset file " + copyToFile);
                        FileOutputStream fos = new FileOutputStream(copyToFile);
                        fos.write(buffer);
                        fos.close();
                    } else
                        Log.d(TAG,"No copy - File exists " + copyToFile);
                }
            }
        }
        catch (Exception e) {
            // Better to handle specific exceptions such as IOException etc
            // as this is just a catch-all
            Log.e(TAG,e.getMessage());
        }
    }

    private StringBuilder getConfigFile(File sdcard, String fileName) {

        //Get the text file
        File file = new File(sdcard, fileName);
        StringBuilder fileData = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                fileData.append(line);
                fileData.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Log.e(TAG,"File: " + fileName +" - " + e.getMessage());
        }
        return fileData;
    }

    private StringBuilder callTemplate(String barcodeType, String barcodeContent) {

        //Get the text file
        StringBuilder fileData = new StringBuilder();

        fileData.append("^XA"); fileData.append('\n');
        fileData.append("^XFE:TEMPLATE.ZPL^FS"); fileData.append('\n');
        fileData.append("^FN1^FD" + barcodeType + "^FS"); fileData.append('\n');
        fileData.append("^FN2^FD" + barcodeContent + "^FS"); fileData.append('\n');
        fileData.append("^PQ1"); fileData.append('\n');
        fileData.append("^XZ"); fileData.append('\n');

        return fileData;
    }


    private void myLooper () {
//        if (!myLooperActive) {
//            myLooperActive = true;
//            Looper.prepare();
//        }
    }

    private void myLooperQuit() {
//        if (myLooperActive) {
//            myLooperActive = false;
//            getMainLooper().quitSafely();
//        }
    }

    private void printerPassthrough(StringBuilder prnData, String whichData) {

        Toast.makeText(this, whichData, Toast.LENGTH_LONG).show();

        PCPassthroughPrint passthroughPrint = new PCPassthroughPrint(FullscreenActivity.this);
        PCPassthroughPrintSettings settings = new PCPassthroughPrintSettings() {{
            mPassthroughData = prnData.toString();
            mEnableTimeOutMechanism = true;
        }};

        passthroughPrint.execute(settings, new PCPassthroughPrint.onPassthroughResult() {
            @Override
            public void success(PCPassthroughPrintSettings settings) {
                Log.i(TAG,getCurrentDateTime() + "Succeeded: " + whichData);
            }

            @Override
            public void error(String errorMessage, int resultCode, Bundle resultData, PCPassthroughPrintSettings settings) {
                Log.e(TAG,getCurrentDateTime() + "Error: " + whichData);
                Log.e(TAG,getCurrentDateTime() + errorMessage);
            }

            @Override
            public void timeOut(PCPassthroughPrintSettings settings) {
                Log.w(TAG,getCurrentDateTime() + "Timeout: " + whichData);
            }
        });
    }

    private void printerStatus() {

        PCPrinterStatus printerStatus = new PCPrinterStatus(FullscreenActivity.this);
        PCIntentsBaseSettings settings = new PCIntentsBaseSettings()
        {{
            mCommandId = "printerstatus";
        }};

        printerStatus.execute(settings, new PCPrinterStatus.onPrinterStatusResult() {

            @Override
            public void success(PCIntentsBaseSettings settings, HashMap<String, String> printerStatusMap) {
                Log.i(TAG,getCurrentDateTime() + "Succeeded: Printer Status");
                Log.i(TAG,getCurrentDateTime() + "Printer Status:");
                for (HashMap.Entry<String, String> entry : printerStatusMap.entrySet()) {
                    Log.i(TAG,getCurrentDateTime() + entry.getKey() + " = " + entry.getValue());
                }
            }

            @Override
            public void error(String errorMessage, int resultCode, Bundle resultData, PCIntentsBaseSettings settings) {
                Log.e(TAG,getCurrentDateTime() + "Printer status Error");
                Log.e(TAG,getCurrentDateTime() + errorMessage);
            }

            @Override
            public void timeOut(PCIntentsBaseSettings settings) {
                Log.w(TAG,getCurrentDateTime() + "Timeout: Printer status");
            }
        });
    }

    private void unselectPrinter() {

        PCUnselectPrinter unselectPrinter = new PCUnselectPrinter(FullscreenActivity.this);
        PCIntentsBaseSettings settings = new PCIntentsBaseSettings()
        {{
            mCommandId = "unselectPrinter";
        }};

        unselectPrinter.execute(settings, new PCUnselectPrinter.onUnselectPrinterResult() {
            @Override
            public void success(PCIntentsBaseSettings settings) {
                Log.i(TAG,getCurrentDateTime() + "Unselect Printer succeeded");
            }

            @Override
            public void error(String errorMessage, int resultCode, Bundle resultData, PCIntentsBaseSettings settings) {
                Log.e(TAG,getCurrentDateTime() + "Error while trying to unselect printer: " + errorMessage);
            }

            @Override
            public void timeOut(PCIntentsBaseSettings settings) {
                Log.w(TAG,getCurrentDateTime() + "Timeout while trying to unselect printer");
            }
        });

    }
    private String getCurrentDateTime() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss - ", Locale.getDefault());
        return sdf.format(new Date());
    }


}