package agha.ticket_app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by User-Sai on 8/1/2017.
 */

public class Tickets extends Fragment {


    static TextView ticket,name ;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.sold_tickets_frag,container,false);
        ticket = (TextView)view1.findViewById(R.id.tickets_txt_sold_tickets);
        name = (TextView)view1.findViewById(R.id.tickets_event_txt);
        return view1;
    }
}
