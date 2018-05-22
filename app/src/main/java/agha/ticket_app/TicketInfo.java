package agha.ticket_app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.ListView;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TicketInfo extends AppCompatActivity {

    final Context context = this;
    android.widget.ListView lv_info, lv_checkin;
    ArrayList<String> lvValues = new ArrayList<>();
    String[] lvHeadings = new String[3];

    private TextView date;
    private RelativeLayout lay;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private TextView bc_icon;
    private ArrayList<String> dates = new ArrayList<>();
    private ArrayList<String> acceptance = new ArrayList<>();
    private Bundle bundle;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ticket_info_activity);

        bundle = getIntent().getExtras();

        lvValues.add(bundle.getString("TicketType"));
        lvValues.add(bundle.getString("BuyerName"));
        lvValues.add(bundle.getString("Email"));
        id = bundle.getString("Id");

        date = (TextView) findViewById(R.id.ticketinfo_date);
        date.setText(bundle.getString("Date"));

        lv_info = (ListView) findViewById(R.id.ticketinfo_listview);
        lv_info.setAdapter(new CustomAddapter1());

        lv_checkin = (ListView) findViewById(R.id.ticketinfo_checkin_listview);
        new GetDates().execute();

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.checkin_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bc_icon = (TextView) findViewById(R.id.nav_bytecode);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        mToggle.setHomeAsUpIndicator(R.drawable.ic_menu);

        mToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_menu_landing);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_menu_main:
                        if (!internetConnectionAvailable(2000))
                            Toast.makeText(getApplicationContext(), getResources().getString
                                            (R.string.connection_error),
                                    Toast.LENGTH_LONG).show();
                        else {
                            Intent toHome = new Intent(TicketInfo.this, LandingPage.class);
                            startActivity(toHome);
                        }
                        break;
                    case R.id.nav_menu_menu:
                        if (!internetConnectionAvailable(2000))
                            Toast.makeText(getApplicationContext(), getResources().getString
                                            (R.string.connection_error),
                                    Toast.LENGTH_LONG).show();
                        else {
                            Intent toList = new Intent(TicketInfo.this, agha.ticket_app.ListView.class);
                            startActivity(toList);
                        }
                        break;
                    case R.id.nav_menu_logout:
                        Intent toLogin = new Intent(TicketInfo.this, Login.class);
                        startActivity(toLogin);
                        break;
                }
                return true;
            }
        });

    }

    protected void hyperLink(View v) {
        Intent hl = new Intent();
        hl.setAction(Intent.ACTION_VIEW);
        hl.addCategory(Intent.CATEGORY_BROWSABLE);
        hl.setData(Uri.parse("http://bytecode.sa/"));
        startActivity(hl);
    }

    protected void scanQR(View v) {
        Intent i = new Intent(this, QRScanner.class);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mToggle.onOptionsItemSelected(item)) return true;

        return super.onOptionsItemSelected(item);
    }

    class CustomAddapter1 extends BaseAdapter {

        @Override
        public int getCount() {
            return lvHeadings.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.listview_ticketinfo_layout, parent, false);

            TextView heading, value;

            heading = (TextView) row.findViewById(R.id.ticketinfo_txt1);
            value = (TextView) row.findViewById(R.id.ticketinfo_txt2);

            lvHeadings = getResources().getStringArray(R.array.ticketinfo);

            heading.setText(lvHeadings[position]);
            value.setText(lvValues.get(position));

            return row;
        }
    }

    class CustomAddapter2 extends BaseAdapter {

        @Override
        public int getCount() {
            return dates.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.listview_ticketinfo_checkin, parent, false);

            TextView date;
            ImageView checkin;

            date = (TextView) row.findViewById(R.id.checkin_date);
            checkin = (ImageView) row.findViewById(R.id.checkin_sign_image);

            date.setText(dates.get(position));
            if (acceptance.get(position).equals("Pass")) {
                date.setTextColor(Color.parseColor("#02c910"));
                checkin.setImageDrawable(getResources().getDrawable(R.drawable.greentick));
            } else checkin.setImageDrawable(getResources().getDrawable(R.drawable.redx));

            return row;
        }
    }

    // Async Inner Class To Get Tickets
    class GetDates extends AsyncTask<Void, Void, Void> {


        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //show dialog
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.loading_tickets_info_alert);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                url = new URL
                        ("http://www.etathaker.com/tc-api/" + Login.event_key + "/ticket_checkins/" + id);
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
                    JSONArray jsonArray = new JSONArray(result);
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        JSONObject jsonCheckIn = jsonObject.getJSONObject("data");
                        dates.add(jsonCheckIn.getString("date_checked"));
                        if (jsonCheckIn.getString("status").equals("Pass")) acceptance.add("Pass");
                        else acceptance.add("Failed");
                    }
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
            lv_checkin.setAdapter(new CustomAddapter2());
            dialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (!internetConnectionAvailable(2000))
            Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.connection_error),
                    Toast.LENGTH_LONG).show();
        else {
            Intent i = new Intent(this, agha.ticket_app.ListView.class);
            startActivity(i);
            finish();
        }
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
        return inetAddress != null && !inetAddress.equals("");
    }
}
