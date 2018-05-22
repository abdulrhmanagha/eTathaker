package agha.ticket_app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by User-Sai on 8/1/2017.
 */

public class SoldTickets extends Fragment {

    static TextView checked_ticket,name ; // name of event

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view1 = inflater.inflate(R.layout.checked_tickets_frag,container,false);
        checked_ticket = (TextView)view1.findViewById(R.id.sold_tickets_txt_sold_tickets);
        name = (TextView)view1.findViewById(R.id.sold_tickets_event_txt);
        return view1;
    }
}
