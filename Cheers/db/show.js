/**
 * Creates a new ticket manager. This interfaces with the database to manage tickets.
 *
 * @param database An instance of the database.
 * @constructor
 */
module.exports.ShowManager = function (database) {
    let db = database;

    this.getReservedTickets = function (showID,callback) {

        db.query_show_by_id(showID,function (err, rows) {
            if (!err) {
                let show = new Show(rows.showID, rows.startDate, rows.endDate,
                    rows.time, rows.theaterID, rows.seatsTaken, rows.productionID);
                callback(show)
            } else {
                console.error(err);
                callback(null);
            }
        });
    }

    this.updateReservedTickets = function (showID,seatsTaken) {
        db.update_show_seats_taken(showID,seatsTaken);
    }

};

/**
 * Creates a new ticket object.
 *
 * @param ShowID
 * @param StartDate
 * @param EndDate
 * @param Time
 * @param TheaterID
 * @param SeatsTaken
 * @param ProductionID
 * @constructor
 */
let Show = function (ShowID, StartDate, EndDate, Time, TheaterID, SeatsTaken, ProductionID) {
    this.ShowID = ShowID;
    this.StartDate = StartDate;
    this.EndDate = EndDate;
    this.Time = Time;
    this.TheaterID = TheaterID;
    this.SeatsTaken = SeatsTaken;
    this.ProductionID = ProductionID;
};

module.exports.Show = Show;