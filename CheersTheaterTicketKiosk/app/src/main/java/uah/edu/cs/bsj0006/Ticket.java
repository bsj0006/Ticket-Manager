package uah.edu.cs.bsj0006;

public class Ticket {
    private int ticketID;
    private Show show;
    private int userID;
    private int paymentMethodID;
    private String reservedSeat;
    private boolean paid;
    private float price;

    public Ticket(int ticketID, Show show, int userID, int paymentMethodID, String reservedSeat,
                  boolean paid, float price) {
        this.ticketID = ticketID;
        this.show = show;
        this.userID = userID;
        this.paymentMethodID = paymentMethodID;
        this.reservedSeat = reservedSeat;
        this.paid = paid;
        this.price = price;
    }

    public int getTicketID() {
        return ticketID;
    }

    public Show getShow() {
        return show;
    }

    public int getUserID() {
        return userID;
    }

    public int getPaymentMethodID() {
        return paymentMethodID;
    }

    public String getReservedSeat() {
        return reservedSeat;
    }

    public boolean isPaid() {
        return paid;
    }

    public float getPrice() {
        return price;
    }
}
