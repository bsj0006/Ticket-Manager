package uah.edu.cs.bsj0006;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;

public class PrinterService {
    private PrintManager printManager;

    public PrinterService(Context context) {
        printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
    }

    public void printWebView(WebView webView) {
        String jobName = "Theater Ticket Kiosk Ticket";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }
}
