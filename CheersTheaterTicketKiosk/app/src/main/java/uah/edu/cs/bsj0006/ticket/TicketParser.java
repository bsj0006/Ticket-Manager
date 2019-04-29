package uah.edu.cs.bsj0006.ticket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class TicketParser {

    private static final String CONCERT_HALL = "VBS Concert Hall";
    private static final String PLAYHOUSE = "VBC Playhouse";
    private static final String ERROR = "Unable to parse message";

    public static List<Ticket> parseTickets(JSONObject jsonObject) throws TicketParseException {
        List<Ticket> tickets = new ArrayList<>();
        try {
            int ticketID = jsonObject.getInt("ticketID");
            int ticketCount = jsonObject.getInt("numberOfSeats");
            int showID = jsonObject.getInt("showID");
            Show show = getShow(showID);
            if (show == null) {
                throw new TicketParseException();
            }
            int userID = jsonObject.getInt("userID");
            int paymentMethod = jsonObject.getInt("paymentMethodID");
            String reservedSeats = jsonObject.getString("reservedSeats");
            int paid = jsonObject.getInt("paid");
            int price = jsonObject.getInt("totalPrice");

            for (int i = 0; i < ticketCount; i++) {
                int index = i * 8;
                String seat = reservedSeats.substring(index, index + 8);
                Ticket ticket = new Ticket(ticketID, show, userID, paymentMethod, seat, paid == 1, price);
                tickets.add(ticket);
            }

        } catch (JSONException e) {
            throw new TicketParseException();
        }

        return tickets;
    }

    private static Show getShow(int showID) {
        Show show = null;
        Calendar date;
        switch (showID) {
            case 1:
                date = new GregorianCalendar(2019, 6, 3);
                show = new Show(showID, "Phantom Of The Opera", date, CONCERT_HALL);
                break;
            case 2:
                date = new GregorianCalendar(2019, 6, 4);
                show = new Show(showID, "Phantom Of The Opera", date, CONCERT_HALL);
                break;
            case 3:
                date = new GregorianCalendar(2019, 6, 5);
                show = new Show(showID, "Huntsville Symphony Organization Presents Bach", date, CONCERT_HALL);
                break;
            case 4:
                date = new GregorianCalendar(2019, 6, 4);
                show = new Show(showID, "To Kill A Mockingbird", date, PLAYHOUSE);
                break;
            case 5:
                date = new GregorianCalendar(2019, 6, 5);
                show = new Show(showID, "UAH Choir Performance", date, PLAYHOUSE);
                break;
            case 6:
                date = new GregorianCalendar(2019, 6, 5);
                show = new Show(showID, "Grateful Dead Cover Band", date, PLAYHOUSE);
                break;
        }
        return show;
    }

    public static class TicketParseException extends Exception {
        TicketParseException() {
            super(ERROR);
        }
    }
}
