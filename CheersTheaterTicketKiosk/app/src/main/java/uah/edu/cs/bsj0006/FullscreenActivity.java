package uah.edu.cs.bsj0006;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    private ActivityViewModel viewModel;

    private PermissionsManager permissionsManager;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

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
                viewModel.requestTicketInfo(FullscreenActivity.this, num);
            } catch (NumberFormatException e) {
                Toast.makeText(FullscreenActivity.this, "Please enter a valid number", Toast.LENGTH_LONG).show();
            }

        });

        permissionsManager.requestPermissions(this, PermissionsManager.Permissions.CAMERA, new PermissionsManager.PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionsManager.Permissions permission) {
                viewModel.createBarcodeScanner();
            }

            @Override
            public void onPermissionDenied(PermissionsManager.Permissions permission) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.stopScanning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.onDestroy();
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
}
