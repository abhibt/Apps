package com.abhi.abcd;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class AppListFinder implements Runnable {
    Context currContext;
    List<String> where;
    AppListFinder(Context cuContext, List<String> whereList)
    {
        currContext = cuContext;
        where = whereList;
    }

    public void run(){
        Boolean bFound = false;
        String appName;
        String szActivities = null;
        List<PackageInfo> packList = currContext.getPackageManager().getInstalledPackages(0);

        where.clear();

        for (int i = 0; i < packList.size(); i++) {
            PackageInfo packInfo = packList.get(i);
            if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

                appName = packInfo.applicationInfo.loadLabel(currContext.getPackageManager()).toString();

                System.out.println(appName);
                //Get First Install time
                final long firstInstallTime = packInfo.firstInstallTime;
                // New date object from millis
                Date date = new Date(firstInstallTime);

                // formattter
                SimpleDateFormat formatter;
                formatter = new SimpleDateFormat("yyyy-MM-dd",
                        Locale.getDefault());
               // Pass date object
                String formatted = formatter.format(date);
                where.add(appName);
            }
        }
        //Once all the apps are fetched, then stop the timer task.
        int nSize = where.size();
        String szAppsFound = nSize + " Apps Found.";
        where.add(szAppsFound);
    }
}
