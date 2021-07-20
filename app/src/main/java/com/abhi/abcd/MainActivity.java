package com.abhi.abcd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static com.abhi.abcd.R.array;
import static com.abhi.abcd.R.id;
import static com.abhi.abcd.R.layout;

/*
References- http://www.trivisonno.com/programming/update-android-gui-timer
*/
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int nCurrentTask = 0;
    // Array of strings...
    ListView simpleList;
    TextView textView;
    //String[] labels;
    private AdView adView;
    private AppListFinder appListFinder;
    private Handler mHandler;
    private Handler mHandlerLooper;
    List<String> where = new ArrayList<String>();
    Handler msghandler;
    static ArrayAdapter<String> arrayAdapter;
    Runnable runOnUiThread;
    private Context getActivity;
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

//Do the fetching in a timer. Otherwise the App may take some time to come up.
        nCurrentTask = 0;

        //Handler, creation of new handler.
        msghandler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                String stringToDisplay;
                Bundle bundle = msg.getData();
                Integer nState = bundle.getInt("myKey");
                switch(nState)
                {
                    case 0:
                        stringToDisplay = bundle.getString("myKey");
                        ShowCustomToast(stringToDisplay);
                        ShowSpinnerOnScreen(true);
                        EnableDisableButtonAndList(false);
                        break;
                    case 1:
                    case 3:
                        ShowSpinnerOnScreen(false);
                        EnableDisableButtonAndList(true);
                        break;
                    case 2:
                        stringToDisplay = bundle.getString("myKey");
                        ShowCustomToast(stringToDisplay);
                        ShowSpinnerOnScreen(true);
                        EnableDisableButtonAndList(false);
                        break;

                    default: break;
                }
                arrayAdapter.notifyDataSetChanged();
            }
        };

        Thread mythread = new Thread(new AppListFinder(getApplicationContext(), where, msghandler));
        mythread.start();
    }
    //Short text messages to be shown in the screen.
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
    //Show the rotating waiting icon
    public void ShowSpinnerOnScreen(final boolean bShow) {
        final Context applicationContext = getApplicationContext();
        ProgressBar spinner = (ProgressBar) findViewById(id.progressBar);
                    try {
                        if (bShow == true && spinner != null) {
                            spinner.setVisibility(View.VISIBLE);
                        } else if (spinner != null) {
                            spinner.setVisibility(View.INVISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //setContentView (layout.activity_main);
                }

                //Enable or disable the drop down box and the button.
    public void EnableDisableButtonAndList(final boolean bEnable) {
                                  Spinner spin = (Spinner) findViewById(R.id.backup_spinner);
                                  Button bBackup = (Button) findViewById(id.button);
                                  try {
                                      spin.setEnabled(bEnable);
                                      bBackup.setEnabled(bEnable);
                                  } catch (Exception e) {
                                      e.printStackTrace();
                                  }
                          }




    public void showToast(View view) {
        //Send email to the entered email address;
        //final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        //emailIntent.setType("text/plain");

        String mailId="a@b";
        Intent emailIntent = new Intent(Intent.ACTION_SEND, Uri.fromParts(
                "mailto", mailId, null));
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent, "Send Mail"));

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
                    "Choose only email app..."));
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

    //On drop down box selection.
    @SuppressLint("HandlerLeak")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final MainActivity thisView = this;
        Thread mythread;
        System.out.println("Spinner Activity");
        //Toast.makeText(getApplicationContext(), R.array.Backup_List[0] , Toast.LENGTH_LONG).show();
        System.out.print(position);
        EnableDisableButtonAndList(false);
        switch (position) {
            //app list
            case 0: //App list
                ShowCustomToast("Getting Application List.Please Wait.");
                mythread = new Thread(new AppListFinder(getApplicationContext(),where,msghandler));
                mythread.start();
                break;
            case 3: //SMS
                break;
            case 1: //Contact
                ShowCustomToast("Getting Contact List.Please Wait.");
                mythread = new Thread(new ContactListFinder(getApplicationContext(),where,msghandler));
                mythread.start();
                break;
            default:
                break;
        }
        simpleList.invalidate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        System.out.println("Spinner Activity");
    }



 }


//https://developer.android.com/guide/topics/ui/controls/pickers
//https://www.tutorialspoint.com/android/android_loading_spinner.htm
//http://www.java2s.com/Code/Android/UI/UsingThreadandProgressbar.htm
