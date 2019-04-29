package uah.edu.cs.bsj0006.ticket;

public class Ticket {
    private int ticketID;
    private Show show;
    private int userID;
    private int paymentMethodID;
    private String reservedSeat;
    private boolean paid;
    private float price;

    Ticket(int ticketID, Show show, int userID, int paymentMethodID, String reservedSeat,
           boolean paid, float price) {
        this.ticketID = ticketID;
        this.show = show;
        this.userID = userID;
        this.paymentMethodID = paymentMethodID;
        this.reservedSeat = reservedSeat;
        this.paid = paid;
        this.price = price;
    }

    int getTicketID() {
        return ticketID;
    }

    Show getShow() {
        return show;
    }

    int getUserID() {
        return userID;
    }

    int getPaymentMethodID() {
        return paymentMethodID;
    }

    String getReservedSeat() {
        return reservedSeat;
    }

    boolean isPaid() {
        return paid;
    }

    float getPrice() {
        return price;
    }
}
