package agha.ticket_app;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.hardware.Camera;
import android.widget.Toast;

import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class QRScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView ;
    String QRresult= "";
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrscanner_activity);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        mScannerView.stopCamera();
        QRresult = result.getText();
        // check internet
        if (!internetConnectionAvailable(2000))
            Toast.makeText(getApplicationContext(),getResources().getString
                            (R.string.connection_error),
                    Toast.LENGTH_LONG).show();
        else new GetTicketValidation().execute();

        // Uncomment this code if you would like to get back to landing page after reading
        // QRcodes
        /*Handler handler = null;
        handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                Intent i = new Intent(QRScanner.this,LandingPage.class);
                startActivity(i);
                finish();
            }
        }, 4000);*/
    }

    private boolean internetConnectionAvailable(int timeOut) {
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                @Override
                public InetAddress call() {
                    try {
                        return InetAddress.getByName("google.com");
                    } catch (UnknownHostException e) {
                        return null;
                    }
                }
            });
            inetAddress = future.get(timeOut, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        }
        return inetAddress!=null && !inetAddress.equals("");
    }

    @Override
    public void onBackPressed() {
        if (!internetConnectionAvailable(2000))
            Toast.makeText(getApplicationContext(),getResources().getString
                            (R.string.connection_error),
                    Toast.LENGTH_LONG).show();
        else {
            mScannerView.stopCamera();
            Intent i = new Intent(this, LandingPage.class);
            startActivity(i);
            finish();
        }
    }

    // Async Inner Class To Check In
    class GetTicketValidation extends AsyncTask<Void,Void,Void> {

        boolean status ;
        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //dialog
            dialog = new Dialog(context);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                url = new URL(
                        "http://www.etathaker.com/tc-api/"+Login.event_key+"/check_in/"+QRresult
                );
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    status = jsonObject.getBoolean("status");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // if status from json is true, show success dialog
            if (status){
                dialog.setContentView(R.layout.ticket_scan_success_alert);
                dialog.show();
            }
            // if status from json is false, show failed dialog
            else{
                dialog.setContentView(R.layout.ticket_scan_failed_alert);
                dialog.show();
            }
            // show dialog for 2 sec
            Handler handler = null;
            handler = new Handler();
            handler.postDelayed(new Runnable(){
                public void run(){
                    dialog.cancel();
                    dialog.dismiss();
                }
            }, 2000);
            // re-reads code after 1 sec
            Handler handlerActivity = null;
            handler = new Handler();
            handler.postDelayed(new Runnable(){
                public void run(){
                    Intent i = new Intent(QRScanner.this,QRScanner.class);
                    startActivity(i);
                }
            }, 1000);

        }
    }
}
