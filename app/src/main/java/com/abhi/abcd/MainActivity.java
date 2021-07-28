package com.abhi.abcd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;

import static com.abhi.abcd.R.array;
import static com.abhi.abcd.R.id;
import static com.abhi.abcd.R.layout;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int nCurrentTask = 0;
    // Array of strings...
    ListView simpleList;
    TextView textView;
    private AdView adView;
    private AppListFinder appListFinder;
    private Handler mHandler;
    private Handler mHandlerLooper;
    List<String> where = new ArrayList<String>();
    Handler msghandler;
    static ArrayAdapter<String> arrayAdapter;
    Runnable runOnUiThread;
    private Context getActivity;
    static boolean bRunning = false;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    boolean bActivityRunning = false;
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
        try {

            //Handler, creation of new handler.
            msghandler = new Handler() {
                @SuppressLint("HandlerLeak")
                @Override
                public void handleMessage(Message msg) {
                    String stringToDisplay;
                    Bundle bundle = msg.getData();
                    Integer nState = bundle.getInt("myKey");
                    switch (nState) {
                        case 0:
                            ShowCustomToast(bundle.getInt("StringKey"));
                            ShowSpinnerOnScreen(true);
                            EnableDisableButtonAndList(false);
                            break;
                        case 1:
                        case 3:
                            ShowSpinnerOnScreen(false);
                            EnableDisableButtonAndList(true);
                            break;
                        case 2:
                            ShowCustomToast(bundle.getInt("StringKey"));
                            ShowSpinnerOnScreen(true);
                            EnableDisableButtonAndList(false);
                            break;

                        default:
                            break;
                    }
                    arrayAdapter.notifyDataSetChanged();
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            System.out.println("finally block executed");
        }
        Thread mythread = new Thread(new AppListFinder(getApplicationContext(), where, msghandler));
        mythread.start();
    }

    //Short text messages to be shown in the screen.
    public void ShowCustomToast(final int nResId) {
        final String szCustom = getString(nResId);
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
            finally
            {
                System.out.println("finally block executed");
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
                    finally
                    {
                        System.out.println("finally block executed");
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
                                  finally
                                  {
                                      System.out.println("finally block executed");
                                  }
                          }



    //Send email via email client. First Choose, then send.
    public void showToast(View view) {

        //final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        //emailIntent.setType("text/plain");

        String mailId="a@b";
        Intent emailIntent = new Intent(Intent.ACTION_SEND, Uri.fromParts(
                "mailto", mailId, null));
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("message/rfc822");
        if ( bRunning == false) {
            bRunning = true;
            startActivity(Intent.createChooser(emailIntent, "Send Mail"));

            final View viewById;
/*        viewById = findViewById(R.id.editTextTextEmailAddress);
        final Intent intent;
        intent = emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{viewById.getContext().toString()});
 */
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.bakup_app_list));
            Intent intent1;
            int nTotalElements = simpleList.getCount();
            String szEmailBody = "";
            for (int i = 0; i < nTotalElements; i++) {
                szEmailBody = szEmailBody + String.format("%d.%s\n", i + 1, simpleList.getItemAtPosition(i));
            }

            intent1 = emailIntent.putExtra(Intent.EXTRA_TEXT, szEmailBody);
            emailIntent.setType("message/rfc822");
            try {
                startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_email_app)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(),
                        R.string.no_email_client_installed,
                        Toast.LENGTH_SHORT).show();
            } finally {
                System.out.println("finally block executed");
                bRunning = false;
            }
            bRunning = false;
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
        try {
            System.out.print(position);
            if (bActivityRunning == false) {
                bActivityRunning = true;

                EnableDisableButtonAndList(false);
                switch (position) {
                    //app list
                    case 0: //App list
                        ShowCustomToast(R.string.get_app_list);
                        mythread = new Thread(new AppListFinder(getApplicationContext(), where, msghandler));
                        where.clear();
                        simpleList.invalidate();
                        mythread.start();
                        break;

                    case 1: //Contact
                        PackageManager manager = getPackageManager();
                        String szPackage = getPackageName();
                        int hasPermission = manager.checkPermission("android.permission.READ_CONTACTS", szPackage);
                        //ShowCustomToast("Need permission to get contact list. Please provide.");
                        if (hasPermission == manager.PERMISSION_GRANTED) {
                            ShowCustomToast(R.string.get_contact_list);
                            where.clear();
                            simpleList.invalidate();
                            mythread = new Thread(new ContactListFinder(getApplicationContext(), where, msghandler));
                            mythread.start();
                        } else {
                            //where.clear();
                            //simpleList.invalidate();
                            EnableDisableButtonAndList(true);
                            Spinner spin = (Spinner) findViewById(R.id.backup_spinner);
                            spin.setSelection(0);
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            /*hasPermission = manager.checkPermission("android.permission.READ_CONTACTS", szPackage);
                            if (hasPermission == manager.PERMISSION_GRANTED) {
                                ShowCustomToast(R.string.get_contact_list);
                                where.clear();
                                simpleList.invalidate();
                                mythread = new Thread(new ContactListFinder(getApplicationContext(), where, msghandler));
                                mythread.start();
                            } else {
                                ShowCustomToast(R.string.provide_permissions);
                                where.clear();
                                EnableDisableButtonAndList(true);
                                simpleList.invalidate();
                            }*/
                        }
                        break;
                    default:
                        break;
                }

            }
         bActivityRunning  =false;
        }
        
        finally
        {
            System.out.println("finally block executed");
        }
        //simpleList.invalidate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        System.out.println("Spinner Activity");
    }



 }


//https://developer.android.com/guide/topics/ui/controls/pickers
//https://www.tutorialspoint.com/android/android_loading_spinner.htm
//http://www.java2s.com/Code/Android/UI/UsingThreadandProgressbar.htm
