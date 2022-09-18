package com.abhi.abcd;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import androidx.appcompat.app.AppCompatActivity;

import static android.view.View.OnClickListener;

public class FtpConnector extends AppCompatActivity {
    private Button ftpConnectButton;
    private static final int FTPPort = 21;
    private static String FTPUsername = "Abhi";
    private static String FTPPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_connector);
        Button ftpConnectButton = (Button) findViewById(R.id.ftpConnectButton_ID);
        ftpConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                Toast.makeText(getApplicationContext(),
                        R.string.no_email_client_installed,
                        Toast.LENGTH_SHORT).show();
                //Connect to ftp server.
                ConnectToFTPServer();
            }
            });
    }
    private FTPClient ConnectToFTPServer()
    {
        TextView szIPAddress = findViewById(R.id.editTextFtpServerAddres_ID);
        CharSequence szIPAddressText = szIPAddress.getText();
        //  Reference: https://stackoverflow.com/a/8761268/6667035
        FTPClient ftp = new FTPClient();

            //  Reference: https://stackoverflow.com/a/55950845/6667035
            //https://stackoverflow.com/questions/15895363/android-ftp-server
            //  The argument of `FTPClient.connect` method is hostname, not URL.
        try {
            String szText = "192.168.29.7";
            ftp.connect((String) szText, FTPPort);
        }
        catch (Exception e)
        {
            Log.d("",e.getMessage());
        }
            try {
            boolean status = ftp.login(FTPUsername, FTPPassword);
            if (status)
            {
                ftp.enterLocalPassiveMode();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.sendCommand("OPTS UTF8 ON");
            }
            System.out.println("status : " + ftp.getStatus());
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (SocketException en) {
            en.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ftp;
    }
}