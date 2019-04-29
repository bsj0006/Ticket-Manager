package uah.edu.cs.bsj0006;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import uah.edu.cs.bsj0006.ticket.Ticket;
import uah.edu.cs.bsj0006.ticket.TicketHtmlRenderer;
import uah.edu.cs.bsj0006.ticket.TicketParser;

public class ActivityViewModel extends AndroidViewModel {
    private RequestQueue queue;

    private TicketHtmlRenderer renderer;
    private PrinterService printer;

    private String url = null;

    private boolean busy = false;

    public ActivityViewModel(@NonNull Application application) {
        super(application);
        queue = Volley.newRequestQueue(application);
        renderer = new TicketHtmlRenderer(application);
    }

    String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    void setPrinter(PrinterService printer) {
        this.printer = printer;
    }

    synchronized void requestTicketInfo(int ticketId, IRequestListener requestListener) {
        if (!busy) {
            busy = true;
            JsonObjectRequest request = new JsonObjectRequest(url + "/manage/ticket?id=" + ticketId,
                    null,
                    response -> {
                        Log.d("ViewModel", response.toString());
                        if (response.has("errors")) {
                            String msg = "Invalid ticket id";
                            requestListener.onFail(msg);
                            busy = false;
                        } else {
                            try {
                                JSONObject ticketJson = response.getJSONObject("ticket");
                                List<Ticket> tickets = TicketParser.parseTickets(ticketJson);
                                renderer.convertTickets(tickets, webView -> {
                                    printer.printWebView(webView);
                                    busy = false;
                                });
                            } catch (TicketParser.TicketParseException | JSONException e) {
                                String msg = "Invalid ticket";
                                requestListener.onFail(msg);
                                busy = false;
                            }
                        }
                    },
                    error -> {
                        String msg = "Network error while attempting to request info";
                        Log.e("viewmodel", msg);
                        requestListener.onFail(msg);
                        busy = false;
                    });
            queue.add(request);
        }
    }

    interface IRequestListener {
        void onFail(String error);
    }
}
