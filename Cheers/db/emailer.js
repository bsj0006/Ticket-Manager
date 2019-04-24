const nodemailer = require('nodemailer');
const JsBarcode = require('jsbarcode');
const { createCanvas } = require("canvas");


var transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 465,
    auth: {
        user: 'cheers.tickets@gmail.com',
        pass: 'BIGpassword~'
    }
});

function notify(emails) {
    //console.log(emails);
    var mailOptions = {
        from: '"Cheers Squad" <cheers.tickets@gmail.com>', // sender address
        to: emails,
        subject: 'YOUR SEASON TICKET RENEWAL REMINDER',
        text: 'Buy it now! Please contact us at cheers.tickets@gmail.com. \n\nCheers, \nCheers Squad'
    };
    //Send the email
    transporter.sendMail(mailOptions, function (error, info) {
        if (error) {
            console.log(error);
        } else {
            console.log('Email sent: ' + info.response);
        }

    });
}

module.exports.notify_ticket = function (ticketID, show_id, email) {
    var JsBarcode = require('jsbarcode');
    let ticketIDString = ticketID.toString();
    while(ticketIDString.length < 7)
    {
        ticketIDString = "0" + ticketIDString;
    }
    var canvas = createCanvas(200, 150);
    JsBarcode(canvas)
        .EAN8(ticketIDString, {fontSize: 18, textMargin: 0})
        .render();
    let imgBuffer = canvas.toBuffer();

    var mailOptions = {
        from: '"Cheers Squad" <cheers.tickets@gmail.com>', // sender address
        to: email,
        subject: 'Ticket Purchase Confirmation <ID: ' + ticketIDString + '>',
        html: '<p>We are emailing you to confirm the purchase of ticket ' + ticketIDString + '</p><br><img src="cid:unique@bsj0006.com"/>',
        attachments: [{filename: 'code.png', content: imgBuffer, cid: 'unique@bsj0006.com'}]
    };
    //Send the email
    transporter.sendMail(mailOptions, function (error, info) {
        if (error) {
            console.log(error);
        } else {
            console.log('Email sent: ' + info.response);
        }

    });
};

module.exports.notify = notify;










