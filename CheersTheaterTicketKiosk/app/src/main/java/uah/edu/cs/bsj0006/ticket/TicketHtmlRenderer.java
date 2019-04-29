package uah.edu.cs.bsj0006.ticket;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TicketHtmlRenderer {
    private WeakReference<Context> contextWeakReference;
    private List<WebView> pendingRenders;

    public TicketHtmlRenderer(Context context) {
        contextWeakReference = new WeakReference<>(context);
        pendingRenders = new ArrayList<>();
    }

    public void convertTickets(List<Ticket> tickets, final RenderListener listener) {

        // Create a WebView object specifically for printing
        WebView webView = new WebView(contextWeakReference.get());
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                listener.onRenderFinished(view);
                pendingRenders.remove(view);
            }
        });

        // Generate an HTML document on the fly:
        String showName = tickets.get(0).getShow().getShowName();
        String id = String.valueOf(tickets.get(0).getTicketID());
        boolean paid = tickets.get(0).isPaid();
        String price = String.valueOf(tickets.get(0).getPrice());
        StringBuilder htmlDocument = new StringBuilder();
        for (int i = 0; i < tickets.size(); i++) {
            htmlDocument.append("<html><body>");
            if(i > 0)
            {
                htmlDocument.append("<style>.dotted {border: 1px dotted #000000; border-style: none none dotted; color: #fff; background-color: #fff; }</style>" +
                        "<hr class='dotted'/>");
            }
            htmlDocument.append("<h1>").append(showName).append("</h1>").append("<p>Ticket ID: ").append(id).append("</p>").append("<p>Seat: ").append(tickets.get(i).getReservedSeat()).append("</p>").append("<p>Paid Status: <input type=\"checkbox\"");
            if (paid) {
                htmlDocument.append(" checked");
            }
            htmlDocument.append(">  Price: $").append(price).append("</p>").append("</body></html>");
        }
        webView.loadDataWithBaseURL(null, htmlDocument.toString(), "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        pendingRenders.add(webView);
    }

    public interface RenderListener {
        void onRenderFinished(WebView webView);
    }
}
