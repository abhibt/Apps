package com.abhi.abcd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.abhi.abcd.R.array;
import static com.abhi.abcd.R.id;
import static com.abhi.abcd.R.layout;

/*
References- http://www.trivisonno.com/programming/update-android-gui-timer
*/
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "APPS";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private int nCurrentTask = 0;
    // Array of strings...
    ListView simpleList;
    TextView textView;
    //String[] labels;
    private AdView adView;
    TimerTask timerTask;
    private Handler mHandler;
    List<String> where = new ArrayList<String>();
    private Timer timer;
    private Timer timer1;
    static ArrayAdapter<String> arrayAdapter;
    Runnable runOnUiThread;
    //final Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.activity_main);
        simpleList = findViewById(id.simpleListView);


        arrayAdapter = new ArrayAdapter<String>(this, layout.activity_listview, id.textView, where);
        simpleList.setAdapter(arrayAdapter);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.


        adView = findViewById(id.adView1);
        //AdRequest adRequest = new AdRequest.Builder().build();

        AdRequest adRequest = new AdRequest.Builder()
                .build();
        //final .Builder builder = AdRequest.Builder.addTestDevice("0C3B2F16993B787DFF1BDBF88E4B8EDD");
        adView.loadAd(adRequest);

        Spinner spinner = findViewById(id.backup_spinner);
        spinner.setOnItemSelectedListener(this);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                array.Backup_List, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        //spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS}, 1);
            }

        } else {
            /* do nothing */
            /* permission is granted */
        }
        //ShowCustomToast("Getting List of Applications.Please Wait.");
        //Do the fetching in a timer. Otherwise the App may take some time to come up.
        nCurrentTask = 0;

        new Thread(new startMyTimer()).start();
    }

    public void ShowCustomToast(final String szCustom) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            int duration = Toast.LENGTH_SHORT;
            try {
                mHandler = new android.os.Handler();
                Toast toast = Toast.makeText(getApplicationContext(), szCustom, duration);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
                }
            });
    }

    public void ShowSpinnerOnScreen(final boolean bShow) {
                    ProgressBar spinner = (ProgressBar) findViewById(id.progressBar);
                    try {
                        if (bShow == true && spinner != null) {
                            spinner.setVisibility(View.VISIBLE);
                        } else if (spinner != null) {
                            spinner.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

    public void EnableDisableSpinner(final boolean bEnable) {
                                  Spinner spin = (Spinner) findViewById(R.id.backup_spinner);
                                  Button bBackup = (Button) findViewById(id.button);
                                  try {
                                      spin.setEnabled(bEnable);
                                      bBackup.setEnabled(bEnable);
                                  } catch (Exception e) {
                                      e.printStackTrace();
                                  }
                          }

    public void installedApps() {
        Boolean bFound = false;
        String appName;
        String szActivities = null;
        List<PackageInfo> packList = getPackageManager().getInstalledPackages(0);
        Resources res = getResources();
        where.clear();
        //labels = res.getStringArray(R.array.BannedApps);
        for (int i = 0; i < packList.size(); i++) {
            PackageInfo packInfo = packList.get(i);
            if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

                appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();

                System.out.println(appName);
                //Get First Install time
                final long firstInstallTime = packInfo.firstInstallTime;
                // New date object from millis
                Date date = new Date(firstInstallTime);
// formattter
                SimpleDateFormat formatter;
                formatter = new SimpleDateFormat("yyyy-MM-dd",
                        Locale.getDefault());
                //formatter.setTimeZone(TimeZone.getTimeZone(String.valueOf(TimeZone.getDefault())));
// Pass date object
                String formatted = formatter.format(date);
                //appName = appName + "-" + formatted;

                //Get last usage of the app
                /*
                ActivityInfo[] activityInfo = packInfo.activities;
                if (activityInfo != null)
                    szActivities = activityInfo[0].toString();
                appName = appName + szActivities;*/
                where.add(appName);


            }
        }
        //Once all the apps are fetched, then stop the timer task.
        int nSize = where.size();
        String szAppsFound = nSize + " Apps Found.";
        where.add(szAppsFound);

    }

    public int sum(int a, int b) {
        return a + b;
    }

    //Start the timer to get the list of installed apps.
    /*
    public void startMyTimer() {
        /* set a new Timer
        timer = new Timer();
        EnableDisableSpinner(false);
        ShowSpinnerOnScreen(true);

                    public void run() {
            installedApps();
        }
         //schedule the timer, after the first 1000ms the TimerTask will run every 600000ms
        ShowSpinnerOnScreen(false);
        EnableDisableSpinner(true);
        //
    }*/

    private void GetContacts() throws InterruptedException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Contacts permission NOT granted");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            ShowSpinnerOnScreen(true);
            EnableDisableSpinner(false);
//            ShowCustomToast("Getting List of Contact.Please Wait.");
            getContactList();
            //sleep(5000);
            //ShowSpinnerOnScreen(false);
            //EnableDisableSpinner(true);
        }
    }


    public void showToast(View view) {
        //Send email to the entered email address;
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("text/plain");

        final View viewById;
/*        viewById = findViewById(R.id.editTextTextEmailAddress);
        final Intent intent;
        intent = emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{viewById.getContext().toString()});
 */
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App Manager:Backing up List of Apps");
        Intent intent1;
        int nTotalElements = simpleList.getCount();
        String szEmailBody = "";
        for (int i = 0; i < nTotalElements; i++) {
            szEmailBody = szEmailBody + String.format("%d.%s\n", i, simpleList.getItemAtPosition(i));
        }

        intent1 = emailIntent.putExtra(Intent.EXTRA_TEXT, szEmailBody);
        emailIntent.setType("message/rfc822");
        try {
            startActivity(Intent.createChooser(emailIntent,
                    "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(),
                    "No email clients installed.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private Context getActivity() {
        final Context activity = this.getActivity();
        return activity;
    }

    final Runnable myRunnable = new Runnable() {
        public void run() {
            installedApps();
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final MainActivity thisView = this;
        System.out.println("Spinner Activity");
        //Toast.makeText(getApplicationContext(), R.array.Backup_List[0] , Toast.LENGTH_LONG).show();
        System.out.print(position);
        switch (position) {
            //app list
            case 0: //App list
                //stoptimertask();
                ShowCustomToast("Getting List of App List.Please Wait.");
                nCurrentTask = 0;
                new Thread(new startMyTimer()).start();
                break;
            case 3: //SMS
                //stoptimertask();
                // getAllSms();
                // public static final String INBOX = "content://sms/inbox";
// public static final String SENT = "content://sms/sent";
// public static final String DRAFT = "content://sms/draft";
                try {
                    ShowCustomToast("Getting List of SMSs");
                    Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
                    if (cursor == null)
                        if (cursor.moveToFirst()) { // must check the result to prevent exception
                            do {
                                String msgData = "";
                                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                                }
                                // use msgData
                            } while (cursor.moveToNext());
                        } else {
                            // empty box, no SMS
                            ShowCustomToast("Empty List of SMS");
                        }
                } finally {
                    ShowCustomToast("Can't read SMS");
                }

               /*     //startMyTimer();
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        getAllSms(this);
                    }
                }
                finally {
                    ShowCustomToast("Can't read SMS from GetAllSMS");
                }
                Telephony provider = new Telephony(this);

                List<Sms> sms = provider(Telephony.Filter.INBOX).getList();
                for(int i = 0; i <= sms.size() - 1; i++) {
                    Log.d("Address",sms.get(i).address);
                    Log.d("Sms",sms.get(i).body);
                    Log.d("ReceivedDate",""+sms.get(i).receivedDate));
                    Log.d("State",""+sms.get(i).status);
                    Log.d("person",""+sms.get(i).person);
                }*/
                break;
            case 1: //Contact
                ShowSpinnerOnScreen(true);
                EnableDisableSpinner(false);
                nCurrentTask = 1;
                new Thread(new Task()).start();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        System.out.println("Spinner Activity");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ShowSpinnerOnScreen(true);
                    EnableDisableSpinner(false);
                    ShowCustomToast("Getting List of Contact.Please Wait.");
                    getContactList();
                    ShowSpinnerOnScreen(false);
                    EnableDisableSpinner(true);
                } else {
                    where.clear();
                    //Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
                    simpleList.invalidateViews();
                }
                return;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getAllSms(Context context) {

        ContentResolver cr = context.getContentResolver();
        Cursor c = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        }
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    String smsDate = c.getString(c.getColumnIndexOrThrow(Sms.DATE));
                    String number = c.getString(c.getColumnIndexOrThrow(Sms.ADDRESS));
                    String body = c.getString(c.getColumnIndexOrThrow(Sms.BODY));
                    Date dateFormat = new Date(Long.valueOf(smsDate));
                    String type;
                    switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Sms.TYPE)))) {
                        case Sms.MESSAGE_TYPE_INBOX:
                            type = "inbox";
                            break;
                        case Sms.MESSAGE_TYPE_SENT:
                            type = "sent";
                            break;
                        case Sms.MESSAGE_TYPE_OUTBOX:
                            type = "outbox";
                            break;
                        default:
                            break;
                    }


                    c.moveToNext();
                }
            }

            c.close();

        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NewApi")
    public List<String> getAllSms() {
        List<String> lstSms = new ArrayList<String>();
        ContentResolver cr = getContentResolver();

        Cursor c = cr.query(Telephony.Sms.Inbox.CONTENT_URI, // Official
                // CONTENT_URI
                // from docs
                new String[]{Telephony.Sms.Inbox.BODY}, // Select body text
                null, null, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER); // Default
        // sort
        // order);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                lstSms.add(c.getString(0));
                c.moveToNext();
            }
        } else {
            throw new RuntimeException("You have no SMS in Inbox");
        }
        c.close();

        return lstSms;
    }

    public List<String> getSMS() {
        List<String> sms = new ArrayList<String>();

        Cursor cur = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        while (cur != null && cur.moveToNext()) {
            String address = cur.getString(cur.getColumnIndex("address"));
            String body = cur.getString(cur.getColumnIndexOrThrow("body"));
            sms.add("Number: " + address + " .Message: " + body);
        }

        if (cur != null) {
            cur.close();
        }
        return sms;
    }

    private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        where.clear();

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);
                        where.add(String.format("%s,%s", name, phoneNo));
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }

        View simpleList1 = findViewById(id.simpleListView);
        simpleList1.invalidate();
    }

    class Task implements Runnable {
            @Override
            public void run () {
                try {
                    GetContacts();
                    ShowSpinnerOnScreen(false);
                    EnableDisableSpinner(true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }
    class startMyTimer implements Runnable {
        @Override
        public void run() {
            installedApps();
            ShowSpinnerOnScreen(false);
            EnableDisableSpinner(true);
        }
    }
}


//https://developer.android.com/guide/topics/ui/controls/pickers

