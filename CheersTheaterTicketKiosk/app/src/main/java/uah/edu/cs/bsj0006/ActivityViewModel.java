package uah.edu.cs.bsj0006;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class ActivityViewModel extends AndroidViewModel {
    private RequestQueue queue;

    private TicketHtmlRenderer renderer;
    private PrinterService printer;

    private String url = null;

    private String barcode;
    private CameraSource camera;

    public ActivityViewModel(@NonNull Application application) {
        super(application);
        queue = Volley.newRequestQueue(application);
        renderer = new TicketHtmlRenderer(application);
    }

    public String getBarcode() {
        return barcode;
    }

    String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    public void setPrinter(PrinterService printer) {
        this.printer = printer;
    }

    public void requestTicketInfo(final Context context, int ticketId) {
        JsonObjectRequest request = new JsonObjectRequest(url + "/manage/ticket?id=" + ticketId,
                null,
                response -> {
                    Log.d("ViewModel", response.toString());
                    if (response.has("errors")) {
                        Log.e("Viewmodel", "Failed to get ticket");
                    } else {
                        try {
                            JSONObject ticketJson = response.getJSONObject("ticket");
                            List<Ticket> tickets = TicketParser.parseTickets(ticketJson);
                            for (Ticket ticket : tickets) {
                                Log.d("viewmodel", ticket.getReservedSeat());
                            }
                            renderer.convertTickets(tickets, webView -> printer.printWebView(webView));

                        } catch (JSONException e) {
                            Log.e("Viewmodel", "Unable to parse ticket");
                        } catch (TicketParser.TicketParseException ticketParseExcpetion) {
                            Toast.makeText(getApplication(), "Invalid id", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                error -> Toast.makeText(context, "Failed to get ticket. Network error.", Toast.LENGTH_LONG).show());
        queue.add(request);
    }

    void createBarcodeScanner() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplication())
                .setBarcodeFormats(Barcode.EAN_8)
                .build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                Integer code = null;
                SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                for (int i = 0; i < barcodeSparseArray.size(); i++) {
                    int key = barcodeSparseArray.keyAt(i);
                    Barcode barcode = barcodeSparseArray.get(key);
                    if (barcode.format == Barcode.EAN_8) {
                        Integer value = Integer.getInteger(barcode.rawValue);
                        if (value != null) {
                            code = value;
                            break;
                        }
                    }
                }
                if (code != null) {
                    requestTicketInfo(getApplication(), code);
                }
            }
        });
        camera = new CameraSource.Builder(getApplication(), barcodeDetector)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .build();
    }

    @SuppressLint("MissingPermission")
    public void startScanning() {
        if (camera != null) {
            try {
                camera.start();
            } catch (IOException e) {
                camera = null;
            }
        }
    }

    public void stopScanning() {
        if (camera != null) {
            camera.stop();
        }
    }

    public void onDestroy() {
        if (camera != null) {
            camera.release();
        }
    }
}
