package agha.ticket_app;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

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

public class LandingPage extends AppCompatActivity{

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private TextView bc_icon ;
    private ZXingScannerView mScannerView ;
    private final String key = "";
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page_activity);

        // Define Button for Byte_Code_Image in the slide menu
        bc_icon = (TextView)findViewById(R.id.nav_bytecode);

        // Define Toolbar and set it as an action_bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // *** Define DrawerLayout and the toggle to open and close the slide menu
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        mToggle.setHomeAsUpIndicator(R.drawable.ic_menu);
        // ***

        // *** Define toggle with its listener to open and close slide menu
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


        // *** Define the slide menu (NavigationView)
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_menu_landing);
        navigationView.setItemIconTintList(null);
        // Define the Listener of the menu to accept clicking on menu items (main-list-logout)
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {

                    // if main Clicked
                    case R.id.nav_menu_main:
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    // if list Clicked
                    case R.id.nav_menu_menu:

                        // Check connection before calling ListView Activity
                        if (!internetConnectionAvailable(2000))
                            Toast.makeText(getApplicationContext(),getResources().getString
                                            (R.string.connection_error),
                                    Toast.LENGTH_LONG).show();
                        else {
                            Intent toList = new Intent(LandingPage.this, ListView.class);
                            startActivity(toList);
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        }
                        break;

                    // if logout Clicked - no need for checking connection
                    case R.id.nav_menu_logout:
                        Intent toLogin = new Intent(LandingPage.this, Login.class);
                        startActivity(toLogin);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                return true ;
            }
        });
        // **** End of Navigation Bar definition

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Define tabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Define FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.landing_page_fab);
        // **** end of navigation bar and toolbar

        // Get Tickets Info (From API)
        new GetTicketsTask().execute();

    }

    protected void scanQR(View v) {

        // check if there is a permission for using CAMERA
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        // if there is no permission, go to settings
        if (result != PackageManager.PERMISSION_GRANTED) {

            // if no permission- show a simple toast
            Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.Toast_permission),
                    Toast.LENGTH_LONG).show();

            // settings activity
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        // check internet before launching QR activity
        if (!internetConnectionAvailable(2000))
            Toast.makeText(getApplicationContext(),getResources().getString
                            (R.string.connection_error),
                    Toast.LENGTH_LONG).show();

        // Launch QR Activity
        else {
            Intent i = new Intent(this, QRScanner.class);
            startActivity(i);
        }
    }

    // method to check internet - it calculates the ping between your connection and
    // google.com (popular website). If no result is given, it returns false
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

    // hyper link for ByteCode icon on slide menu --> bytecode.sa
    protected void hyperLink(View v){
        Intent hl = new Intent();
        hl.setAction(Intent.ACTION_VIEW);
        hl.addCategory(Intent.CATEGORY_BROWSABLE);
        hl.setData(Uri.parse("http://bytecode.sa/"));
        startActivity(hl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_landing_page,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get Item ID
        int id = item.getItemId();

        // if toggle is clicked
        if (mToggle.onOptionsItemSelected(item)) return true ;

        // if refresh is clicked
        if (id == R.id.refresh) {

            // check internet
            if (!internetConnectionAvailable(2000))
                Toast.makeText(getApplicationContext(),getResources().getString
                                (R.string.connection_error),
                        Toast.LENGTH_LONG).show();

            // connection is avaliable - refresh and get new info of tickets
            else new GetTicketsTask().execute();
        }

        return super.onOptionsItemSelected(item);
    }

    // Method for any Tabbed Layout
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                    Tickets tab1 = new Tickets();
                    return tab1;
                case 1:
                    SoldTickets tab2 = new SoldTickets();
                    return tab2;
                default: return null ;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // set title for each tab view
            switch (position) {
                case 0:
                    return getResources().getString(R.string.landing_page_FragSold_sold);
                case 1:
                    return getResources().getString(R.string.landing_page_FragCheck_checkin);
            }
            return null;
        }
    }

    // Async Inner Class To Get Tickets
    class GetTicketsTask extends AsyncTask<Void,Void,Void> {

        String event_name,sold_tickets,checked_tickets ;
        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //show dialog
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.loading_tickets_alert);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                // initialize connection
                url = new URL("http://www.etathaker.com/tc-api/"+Login.event_key+"/event_essentials");
                urlConnection = (HttpURLConnection) url.openConnection();

                // initialize input stream to read JSON file
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                // read JSON file
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                try {
                    // cast the string into JSON file format
                    JSONObject jsonObject = new JSONObject(result);

                    // get values from the JSON
                    event_name = jsonObject.getString("event_name");
                    sold_tickets = jsonObject.getString("sold_tickets");
                    checked_tickets = jsonObject.getString("checked_tickets");


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

            // set the values brought from JSON on landing page widgets
            SoldTickets.name.setText(event_name);
            Tickets.name.setText(event_name);
            Tickets.ticket.setText(sold_tickets);
            SoldTickets.checked_ticket.setText(checked_tickets);

            // hide dialog
            dialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {

        // When back is pressed - You cannot go back to Login layout, you will be sent out
        // of the application
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        startActivity(intent);
        finish();
        System.exit(0);
    }

}
