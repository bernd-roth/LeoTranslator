package at.co.netconsulting.leotranslater;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private RadioButton whichLangugage;
    private TextView textView;
    private boolean inwifisettings;
    private String firstClipData, output, response;
    private ProgressDialog mProgressDialog;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        verifyStoragePermissions(this);

        RadioGroup rg = (RadioGroup) findViewById(R.id.language);
        if (rg != null) {
            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    whichLangugage = (RadioButton) findViewById(selectedId);
                    String language = whichLangugage.getText().toString();

                    if(language.equals("German to English") && isNetworkAvailableA()){
                        final ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            myClipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                                @Override
                                public void onPrimaryClipChanged() {
                                    ClipData cp = myClipboard.getPrimaryClip();
                                    if (cp.getItemCount() > 0) {
                                        ClipData.Item item = cp.getItemAt(0);
                                        if (item == null) {
                                            Toast.makeText(getApplicationContext(), "Item is null", Toast.LENGTH_LONG).show();
                                        } else {
                                            if (item != null) {
                                                String text = item.coerceToText(getBaseContext()).toString();
                                                Toast.makeText(getApplicationContext(), "Sie suchen nach dem Wort: " + text, Toast.LENGTH_LONG).show();
                                                Intent msgIntent = new Intent(SettingsActivity.this, ServiceTranslator.class);
                                                msgIntent.putExtra("ClipBoardData", text);
                                                msgIntent.putExtra("Language", "German");
                                                startService(msgIntent);
                                            }
                                        }
                                    }
                                }
                            });
                    }else if(language.equals("English to German") && isNetworkAvailableA()){
                        final ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        myClipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                            @Override
                            public void onPrimaryClipChanged() {
                                ClipData cp = myClipboard.getPrimaryClip();
                                if (cp.getItemCount() > 0) {
                                    ClipData.Item item = cp.getItemAt(0);
                                    if (item == null) {
                                        Toast.makeText(getApplicationContext(), "Item is null", Toast.LENGTH_LONG).show();
                                    } else {
                                        if (item != null) {
                                            String text = item.coerceToText(getBaseContext()).toString();
                                            Toast.makeText(getApplicationContext(), "Sie suchen nach dem Wort: " + text, Toast.LENGTH_LONG).show();
                                            Intent msgIntent = new Intent(SettingsActivity.this, ServiceTranslator.class);
                                            msgIntent.putExtra("ClipBoardData", text);
                                            msgIntent.putExtra("Language", "English");
                                            startService(msgIntent);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public boolean isNetworkAvailableA() {
        boolean isAvailable = true;
        if (!isNetworkAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle("Alert");
            builder.setMessage("Turn on Wifi or GSM, and choose language again, please");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    inwifisettings = true;
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return true;
    }

    public String readFile(String filename) {
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (inwifisettings & hasFocus) {
            RadioGroup rg = (RadioGroup) findViewById(R.id.language);
            int checkedId = rg.getCheckedRadioButtonId();
            handleSwitch(checkedId);
            inwifisettings = false;
        }
    }

    public void handleSwitch(int checkedId) {
//        switch(checkedId)
//        {
//            case R.id.englishtogerman:
//                isRunning = isServiceAlreadyRunning(ServiceTranslator.class);
//                if(isRunning) {
//                    stopService(new Intent(getApplicationContext(), ServiceTranslator.class));
//                    startService(new Intent(getApplicationContext(), ServiceTranslator.class));
//                }else
//                    startService(new Intent(getApplicationContext(), ServiceTranslator.class));
//                break;
//            case R.id.germantoenglish:
//                isRunning = isServiceAlreadyRunning(ServiceTranslator.class);
//                if(isRunning) {
//                    stopService(new Intent(getApplicationContext(), ServiceTranslator.class));
//                    startService(new Intent(getApplicationContext(), ServiceTranslator.class));
//                }else {
//                    startService(new Intent(getApplicationContext(), ServiceTranslator.class));
//                }
//                break;
//        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void GermanToEnglish(View view) {
    }

    public void EnglishToGerman(View view) {
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}