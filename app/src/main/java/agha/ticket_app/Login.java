package agha.ticket_app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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

public class Login extends AppCompatActivity {

    private final Context context = this;
    private Button login_btn;
    private EditText event_key_edittext;
    private TextView error_msg,internet_msg ;
    public static String event_key ;
    private CheckBox checkBox ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Initialize sharePrefrences to store password if (remember me chack box is checked)
        SharedPreferences sharedPreferences = Login.this.getSharedPreferences(
                "LOGIN_INFO",MODE_PRIVATE
        );

        // Initialize varaibles
        login_btn = (Button) findViewById(R.id.login_button);
        event_key_edittext = (EditText) findViewById(R.id.login_edit_password);
        error_msg = (TextView) findViewById(R.id.login_error_txt);
        internet_msg = (TextView) findViewById(R.id.login_internet_txt);
        checkBox = (CheckBox) findViewById(R.id.login_checkbox);

        // get info of the sharedprefrences
        event_key_edittext.setText(sharedPreferences.getString("PASSWORD",""));
        checkBox.setSelected(sharedPreferences.getBoolean("CHECKBOX_VALUE",true));

    }

    // Login button onClick method
    protected void login(View v) throws IOException, InterruptedException {
        if (checkBox.isChecked()){
            // save password
            SharedPreferences sharedPreferences = Login.this.getSharedPreferences(
                    "LOGIN_INFO",MODE_PRIVATE
            );
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("PASSWORD",event_key_edittext.getText().toString());
            editor.putBoolean("CHECKBOX_VALUE",checkBox.isChecked());
            editor.commit();
        }
        else{
            // clear sharedprefrences
            SharedPreferences sharedPreferences = Login.this.getSharedPreferences(
                    "LOGIN_INFO",MODE_PRIVATE
            );
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
        }
        // get the key
        event_key = event_key_edittext.getText().toString();
        // check internet before checking with the api
        if (internetConnectionAvailable(2000)) new VerifyLoginTask().execute();
        // if no internet, show msg
        else internet_msg.setVisibility(View.VISIBLE);

    }

    public static String getKey(){
        return event_key;
    }

    // Method to check internet
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
        // close app
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        startActivity(intent);
        finish();
        System.exit(0);
    }

    // Async Inner Class To Verify Event ID
    class VerifyLoginTask extends AsyncTask<Void,Void,Void>{

        boolean Event_Key ;
        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //show dialog
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.verify_key_alert);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                // Initialize connection to the api
                url = new URL("http://www.etathaker.com/tc-api/"+event_key+"/check_credentials");
                urlConnection = (HttpURLConnection) url.openConnection();

                // Initialize input stream to read data from api and read it
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                // convert to json and get Pass value
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    Event_Key = jsonObject.getBoolean("pass");// get validity of the key
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
            // verify the key from the API
            if (Event_Key) {
                // if true .. dismiss dialog and go to the next activity
                dialog.dismiss();

                Intent i = new Intent(Login.this, LandingPage.class);
                startActivity(i);
            }
            else { // if false .. show the error msg
                internet_msg.setVisibility(View.INVISIBLE);
                error_msg.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        }
    }

}
