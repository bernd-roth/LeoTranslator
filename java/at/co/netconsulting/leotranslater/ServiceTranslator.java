package at.co.netconsulting.leotranslater;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bernd on 15.05.16.
 */
public class ServiceTranslator extends IntentService {

    public ServiceTranslator() {
        super("ServiceTranslator");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("onHandleIntent", intent.getStringExtra("ClipBoardData").toString());
        String language = intent.getStringExtra("Language").toString();
        downloadFile(intent.getStringExtra("ClipBoardData").toString(), language);
        String htmlText = openFile(intent.getStringExtra("ClipBoardData").toString());
        String words = findWord(htmlText, intent.getStringExtra("ClipBoardData").toString());
        createNotification(words, intent.getStringExtra("ClipBoardData").toString());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void createNotification(String words, String wordForTranslation){
        Intent intent = new Intent(this, ServiceTranslator.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        if(words==null) {
            Notification n = new Notification.Builder(this)
                    .setContentTitle("You were looking for " + wordForTranslation)
                    .setContentText("Sorry, but I have not found anything for you")
                    .setSmallIcon(R.drawable.common_full_open_on_phone)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, n);
        } else {
            Notification n = new Notification.Builder(this)
                    .setContentTitle("You were looking for " + wordForTranslation)
                    .setContentText("This is your translation of " + words)
                    .setSmallIcon(R.drawable.common_full_open_on_phone)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, n);
        }
    }

    public String findWord(String word, String wordForTranslation){
        Pattern pattern = Pattern.compile("(var c1Arr = new Array).*");
        Matcher matcher = pattern.matcher(word);
        String firstWord = null;
        while (matcher.find()) {
            firstWord = matcher.group();
        }
        Log.d("Found", "Text that I fonud: "+firstWord);
        if(firstWord == null)
            return null;
        else {
            String[] words = firstWord.split(",");
            Log.d("FirstWord", "First word: " + words[1]);
            return words[1];
        }
    }

    public String openFile(String wordToTranslate){
        String aDataRow = "", aBuffer="";
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/LeoTranslator");
        String fileName = wordToTranslate+".txt";
        File file = new File (myDir, fileName);
        try {
            FileInputStream fIn = new FileInputStream(file);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));

            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            myReader.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }

    public void downloadFile(String wordToTranslate, String language) {
        URL url;

        // get URL content
        try {
            url = new URL("http://www.dict.cc/?s="+wordToTranslate);

            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;

            //save to this filename
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/LeoTranslator");
            if(!myDir.exists() && !myDir.isDirectory()) {
                myDir.mkdirs();
            }
            String fileName = wordToTranslate+".txt";
            File file = new File (myDir, fileName);

            if (!file.exists()) {
                file.createNewFile();
            }

            //use FileWriter to write file
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            while ((inputLine = br.readLine()) != null) {
                bw.write(inputLine);
            }

            bw.close();
            br.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}