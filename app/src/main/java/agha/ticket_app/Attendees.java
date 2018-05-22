package agha.ticket_app;

import java.util.ArrayList;

/**
 * Created by Abdulrhman-Hasan-Agha on 8/6/2017.
 */

public class Attendees {

    // Initiliaze variables for every property
    String date, id, firstName, lastName, buyerName, ticketType, email;

    // ArrayList to store dates of check-ins for every attendee
    ArrayList<String> dateList = new ArrayList<>();

    // ArrayList to store wether a check-in is valid or not
    ArrayList<String> chechinAcceptance = new ArrayList<>();

    // Constructor
    public Attendees(String date, String id, String firstName, String lastName,
                     String buyerName, String ticketType, String email) {
        this.date = date;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.buyerName = buyerName;
        this.ticketType = ticketType;
        this.email = email;
    }

    // **** getter and setter **** //
    public ArrayList<String> getDateList() {
        return dateList;
    }

    public ArrayList<String> getDateListSign() {
        return chechinAcceptance;
    }

    public void setDateList(String date) {
        dateList.add(date);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
