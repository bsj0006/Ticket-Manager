package uah.edu.cs.bsj0006;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import uah.edu.cs.bsj0006.camera.CameraSourcePreview;
import uah.edu.cs.bsj0006.camera.GraphicOverlay;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity  implements BarcodeGraphicTracker.BarcodeUpdateListener {

    private ActivityViewModel viewModel;

    private PermissionsManager permissionsManager;
    private SharedPreferences preferences;

    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private com.google.android.gms.vision.CameraSource mCameraSource;

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    private static final String TAG = "Barcode-reader";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        View layout = findViewById(R.id.layout_main);
        layout.setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY | SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);

        permissionsManager = new PermissionsManager();

        preferences = getSharedPreferences("APP", Context.MODE_PRIVATE);

        viewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);
        viewModel.setPrinter(new PrinterService(this));

        if (viewModel.getUrl() == null) {
            String url = preferences.getString("REMOTE", null);
            if (url == null) {
                createUrlDialog();
            } else {
                viewModel.setUrl(url);
            }
        }
        final EditText ticketIdEditor = findViewById(R.id.barcode_editor);
        Button printButton = findViewById(R.id.print_button);
        printButton.setOnClickListener(v -> {
            String numString = ticketIdEditor.getText().toString().trim();
            if (numString.equals("")) {
                Toast.makeText(FullscreenActivity.this, "Please enter a valid number", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                int num = Integer.parseInt(numString);
                viewModel.requestTicketInfo(num, error -> {
                    Toast.makeText(FullscreenActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            } catch (NumberFormatException e) {
                Toast.makeText(FullscreenActivity.this, "Please enter a valid number", Toast.LENGTH_LONG).show();
            }

        });

        permissionsManager.requestPermissions(this, PermissionsManager.Permissions.CAMERA, new PermissionsManager.PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionsManager.Permissions permission) {
                createCameraSource();
            }

            @Override
            public void onPermissionDenied(PermissionsManager.Permissions permission) {
                finish();
            }
        });
    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.EAN_8)
                .build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, this);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        com.google.android.gms.vision.CameraSource.Builder builder = new com.google.android.gms.vision.CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f);

        // make sure that auto focus is an available option
        builder = builder.setAutoFocusEnabled(true);

        mCameraSource = builder
                .build();
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * <p>
     * showing a "Snackbar" message of why the permission is needed then
     * <p>
     * sending the request.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.handlePermissionResults(requestCode, permissions, grantResults);
    }

    private void createUrlDialog() {
        final EditText textEditor = new EditText(this);
        textEditor.setHint("http://127.0.0.1");
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Set URL")
                .setView(textEditor)
                .setPositiveButton("Ok", (dialog, which) -> {
                    String value = textEditor.getText().toString().trim();
                    if (!value.equals("")) {
                        preferences.edit().putString("REMOTE", value).apply();
                        viewModel.setUrl(value);
                    }
                });
        builder.show();
    }


    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }


    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dialog =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dialog.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void onBarcodeDetected(Barcode barcode) {
        Log.d(TAG, "Barcode detected");
        if (barcode.format == Barcode.EAN_8) {
            int value = Integer.parseInt(barcode.rawValue.substring(0, 7));
            Log.d(TAG, "Barcode value is " + value);
            viewModel.requestTicketInfo(value, error -> {
                Toast.makeText(FullscreenActivity.this, error, Toast.LENGTH_SHORT).show();
            });
        }
    }
}
