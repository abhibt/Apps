package com.abhi.abcd;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

class ContactListFinder implements Runnable {
    private static final String TAG = "APPS";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    Context currContext;
    List<String> where;
    Handler mScreenHandler;
    Handler mLocalHandler;
    //Prevent multiple runs from happening
    static boolean bRunning = false;
    ContactListFinder(Context cuContext, List<String> whereList, Handler mhandler)
    {
        currContext = cuContext;
        where = whereList;
        mScreenHandler = mhandler;
    }
    @Override
    public void run() {

        Message msg =mScreenHandler.obtainMessage();
        Bundle bundle = new Bundle();
        if (bRunning == false) //Prevent multiple runs from happening
        {
            bRunning = true;
            String dateString = "Getting contact list, please Wait.";
            bundle.putString("myKey", dateString);
            bundle.putInt("myKey", 0);
            msg.setData(bundle);
            mScreenHandler.sendMessage(msg);
            try {
                GetContacts();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg1 = mScreenHandler.obtainMessage();
            Bundle bundle1 = new Bundle();
            bundle1.putInt("myKey", 1);
            msg1.setData(bundle1);
            mScreenHandler.sendMessage(msg1);
            bRunning = false;
        }

    }

    private void GetContacts() throws InterruptedException {
        if (ContextCompat.checkSelfPermission(currContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Contacts permission NOT granted");
            ActivityCompat.requestPermissions((Activity) currContext,
                    new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            getContactList();
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContactList();
                } else {
                    where.clear();
                }
                return;
            }
        }
    }

    private void getContactList() {
        ContentResolver cr = currContext.getContentResolver();
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
    }
}
