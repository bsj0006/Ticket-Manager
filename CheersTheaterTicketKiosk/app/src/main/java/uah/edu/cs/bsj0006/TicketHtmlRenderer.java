package uah.edu.cs.bsj0006;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TicketHtmlRenderer {
    private WeakReference<Context> contextWeakReference;
    private List<WebView> pendingRenders;

    TicketHtmlRenderer(Context context) {
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
        String htmlDocument = "<html><body><h1>Show: " + showName + "</h1><p>Ticket ID: " + id + "</p></body></html>";
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        pendingRenders.add(webView);
    }

    interface RenderListener {
        void onRenderFinished(WebView webView);
    }
}
