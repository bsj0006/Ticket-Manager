package uah.edu.cs.bsj0006;

import java.util.Calendar;

public class Show {
    private int showID;
    private String showName;
    private Calendar date;
    private String Theater;

    public Show(int showID, String showName, Calendar date, String theater) {
        this.showID = showID;
        this.showName = showName;
        this.date = date;
        Theater = theater;
    }

    public int getShowID() {
        return showID;
    }

    public String getShowName() {
        return showName;
    }

    public Calendar getDate() {
        return date;
    }

    public String getTheater() {
        return Theater;
    }
}
