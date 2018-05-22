package agha.ticket_app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

public class ListView extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Dialog dialog;
    private final Context context = this;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private TextView bytecode_icon;
    private Toolbar mToolbar;
    private SearchView searchView;
    // Original List
    public static ArrayList<Attendees> attendeesList = new ArrayList<>();
    // Copy List
    private ArrayList<Attendees> attendeesListCopy = new ArrayList<>();
    private android.widget.ListView attendeeListView;
    // Adapter for attendeeListView
    private CustomAdapter adapter = new CustomAdapter();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_activity);

        // Initialize bytecode_icon on slideing menu
        bytecode_icon = (TextView) findViewById(R.id.nav_bytecode);

        // *** Initialize toolbar and set it as an action bar
        mToolbar = (Toolbar) findViewById(R.id.listview_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // ***

        // *** Initialize drawerLayout and its toggle
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mDrawerLayout.bringToFront();
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
        // ***

        // *** Initialize navigationView and respond for clicks
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_menu_landing);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                // get id of the item clicked
                int id = item.getItemId();
                switch (id) {

                    // if main is clicked
                    case R.id.nav_menu_main:

                        // check internet first
                        if (!internetConnectionAvailable(2000))
                            Toast.makeText(getApplicationContext(),getResources().getString
                                            (R.string.connection_error),
                                    Toast.LENGTH_LONG).show();
                        else {
                            Intent toHome = new Intent(ListView.this, LandingPage.class);
                            startActivity(toHome);
                        }
                        break;

                    // if attendee list is clicked
                    case R.id.nav_menu_menu:
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    // if logout is clicked (no need to check internet)
                    case R.id.nav_menu_logout:
                        Intent toLogin = new Intent(ListView.this, Login.class);
                        startActivity(toLogin);
                        break;
                }
                return true;
            }
        });

        // Initialize the request to the API
        new GetAttendee().execute();

        // *** Initialize the list view and respond to clicks
        attendeeListView = (android.widget.ListView) findViewById(R.id.listview_listview);
        attendeeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!internetConnectionAvailable(2000))
                    Toast.makeText(getApplicationContext(),getResources().getString
                                    (R.string.connection_error),
                            Toast.LENGTH_LONG).show();
                else {
                    Intent i = new Intent(ListView.this, TicketInfo.class);
                    i.putExtra("TicketType", attendeesListCopy.get(position).getTicketType());
                    i.putExtra("BuyerName", attendeesListCopy.get(position).getBuyerName());
                    i.putExtra("Email", attendeesListCopy.get(position).getEmail());
                    i.putExtra("Date", attendeesListCopy.get(position).getDate());
                    i.putExtra("Id", attendeesListCopy.get(position).getId());
                    startActivity(i);
                }
            }
        });
        // ***
    }

    // Method to check if internet is available
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

    /*
    Uncomment this method if you would like to put a button to scan QR codes in list view
    activity (Check internet first by internetConnectionAvailable method)

    protected void scanQR(View v) {
        Intent i = new Intent(this, QRScanner.class);
        startActivity(i);
    }
    */

    // *** hyper link method for bytecode_button on sliding menu --> bytecode.sa
    protected void hyperLink(View v) {
        Intent hl = new Intent();
        hl.setAction(Intent.ACTION_VIEW);
        hl.addCategory(Intent.CATEGORY_BROWSABLE);
        hl.setData(Uri.parse("http://bytecode.sa/"));
        startActivity(hl);
    }
    // ***

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listview_menu, menu);

        // Initliaze menu item for action search in attendee listview
        MenuItem menuItem = menu.findItem(R.id.action_search);
        // Assign menu item to a search view variable
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // get item id
        int id = item.getItemId();

        if (id == R.id.listView_refresh) {
            // check internet
            if (!internetConnectionAvailable(2000))
                Toast.makeText(getApplicationContext(),getResources().getString
                                (R.string.connection_error),
                        Toast.LENGTH_LONG).show();

            // internet is available
            else {
                attendeesListCopy.clear();
                new GetAttendee().execute();
            }
        }

        // if toggle is clicked
        if (mToggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    // Method for search property
    @Override
    public boolean onQueryTextChange(String newText) {

        // if nothing is typed in search bar, view the original list
        if (newText.isEmpty()) adapter.setFilter(attendeesList);

        else {
            newText = newText.toLowerCase();
            // Initialize filtered list to view
            ArrayList<Attendees> newList = new ArrayList<>();
            // check if newText is contained by each attendee
            for (int i = 0 ; i<attendeesListCopy.size() ; i++){
                String name = attendeesListCopy.get(i).getFullName().toLowerCase();
                if (name.contains(newText)) {
                    newList.add(attendeesListCopy.get(i));
                }
            }
            // send the filtered list to the adapter to view it ;
            adapter.setFilter(newList);
        }
        return true;
    }

    class CustomAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return attendeesListCopy.size();
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
            row = inflater.inflate(R.layout.listview_layout, parent, false);

            TextView name_txt, date_txt, id_txt;

            date_txt = (TextView) row.findViewById(R.id.listview_date);
            id_txt = (TextView) row.findViewById(R.id.listview_id);
            name_txt = (TextView) row.findViewById(R.id.listview_name);

            date_txt.setText(attendeesListCopy.get(position).getDate());
            id_txt.setText(attendeesListCopy.get(position).getId());
            name_txt.setText(attendeesListCopy.get(position).getFirstName() + " " +
                    attendeesListCopy.get(position).getLastName());

            return row;
        }

        public void setFilter(ArrayList<Attendees> newList) {
            attendeesListCopy.clear();
            attendeesListCopy.addAll(newList);
            notifyDataSetChanged();
        }

    }

    // Async Inner Class To Get All Attendee Of The Event
    class GetAttendee extends AsyncTask<Void, Void, Void> {

        String size = "";
        String arrayResult = "";
        URL urlSize, url;
        int AttendSize;
        HttpURLConnection urlConnectionSize = null;
        HttpURLConnection urlConnection = null;

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

            // the first call is to get the total newest number of attendee
            try {
                // Initialize connection to the url
                urlSize = new URL("http://www.etathaker.com/tc-api/" + Login.event_key + "/event_essentials");
                urlConnectionSize = (HttpURLConnection) urlSize.openConnection();

                // Initialze the input stream and read the json file
                InputStream inputSize = urlConnectionSize.getInputStream();
                InputStreamReader readerSize = new InputStreamReader(inputSize);
                int dataSize = readerSize.read();
                while (dataSize != -1) {
                    char current = (char) dataSize;
                    size += current;
                    dataSize = readerSize.read();
                }

                // convert the read information into a json file and get the number of attendee
                JSONObject jsonSize = new JSONObject(size);
                AttendSize = jsonSize.getInt("sold_tickets"); // attendee number

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // The second call is to get the data of each attendee and store it in the list
            try {
                // Initialize the connection to the api
                url = new URL("http://www.etathaker.com/tc-api/" + Login.event_key +
                        "/tickets_info/" + AttendSize + "/1/");
                urlConnection = (HttpURLConnection) url.openConnection();

                // Initialize input stream to read data from api and read it
                InputStream input = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(input);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    arrayResult += current;
                    data = reader.read();
                }

                // convert to json file
                JSONArray jsonArray = new JSONArray(arrayResult);
                attendeesList.clear();
                attendeesListCopy.clear();
                // for each object of the json array create a new attendee and store its data
                for (int i = 0; i < jsonArray.length() - 1; i++) {
                    Attendees attendees;
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject obj = jsonObject.getJSONObject("data");
                    JSONArray custom = obj.getJSONArray("custom_fields");
                    JSONArray innerCustom = custom.getJSONArray(0);
                    String tickettype = innerCustom.get(1).toString();
                    innerCustom = custom.getJSONArray(1);
                    String buyer = innerCustom.get(1).toString();
                    innerCustom = custom.getJSONArray(2);
                    String email = innerCustom.get(1).toString();
                    // if a member is not checked yet, json will have no info for date_checked
                    if (!obj.has("date_checked")) {
                        attendees = new Attendees(
                                "No Date",
                                obj.getString("transaction_id"),
                                obj.getString("buyer_first"),
                                obj.getString("buyer_last"),
                                buyer,
                                tickettype,
                                email
                        );
                    } else {
                        attendees = new Attendees(
                                obj.getString("date_checked"),
                                obj.getString("transaction_id"),
                                obj.getString("buyer_first"),
                                obj.getString("buyer_last"),
                                buyer,
                                tickettype,
                                email
                        );
                    }
                    attendeesList.add(attendees);
                    attendeesListCopy.add(attendees);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // set adapter
            attendeeListView.setAdapter(adapter);
            // hide dialog
            dialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        // check internet first
        if (!internetConnectionAvailable(2000))
            Toast.makeText(getApplicationContext(),getResources().getString
                            (R.string.connection_error),
                    Toast.LENGTH_LONG).show();
        else {
            Intent toHome = new Intent(ListView.this, LandingPage.class);
            startActivity(toHome);
            finish();
        }

    }
}
