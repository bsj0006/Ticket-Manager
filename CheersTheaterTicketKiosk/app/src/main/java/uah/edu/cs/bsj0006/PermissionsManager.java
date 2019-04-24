package uah.edu.cs.bsj0006;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for handling Android app permissions
 */
public class PermissionsManager {

    private Map<Permissions, PermissionListener> listeners = new HashMap<>();

    void requestPermissions(Activity activity, Permissions permission, PermissionListener listener) {
        if (ActivityCompat.checkSelfPermission(activity, permission.getManifestId()) == PackageManager.PERMISSION_GRANTED) {
            listener.onPermissionGranted(permission);
        } else {

            listeners.put(permission, listener);

            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {
                requestPermission(permission, activity);
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle("Permission Needed")
                        .setMessage(permission.getRationaleResString())
                        .setPositiveButton("Ok", (dialog, which) -> requestPermission(permission, activity))
                        .show();
            }
        }
    }

    void handlePermissionResults(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Permissions permission = Permissions.fromRequestCode(requestCode);
        if (permission != null) {
            PermissionListener listener = listeners.get(permission);
            if (listener != null) {
                boolean success = (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                if (success) {
                    listener.onPermissionGranted(permission);
                } else {
                    listener.onPermissionDenied(permission);
                }
            }
        }
    }

    private void requestPermission(Permissions permission, Activity activity) {
        final String[] permissions = new String[]{permission.getManifestId()};
        ActivityCompat.requestPermissions(activity, permissions, permission.getRequestCode());
    }


    interface PermissionListener {
        void onPermissionGranted(Permissions permission);

        void onPermissionDenied(Permissions permission);
    }

    enum Permissions {
        CAMERA(2, Manifest.permission.CAMERA, R.string.CAMERA_RATIONALE);

        private final int requestCode;
        private final String manifestId;

        @StringRes
        private final int rationaleResString;

        Permissions(int requestCode, String manifestId, int rationaleResString) {
            this.requestCode = requestCode;
            this.manifestId = manifestId;
            this.rationaleResString = rationaleResString;
        }

        public int getRequestCode() {
            return requestCode;
        }

        public String getManifestId() {
            return manifestId;
        }

        @StringRes
        public int getRationaleResString() {
            return rationaleResString;
        }

        @Nullable
        public static Permissions fromRequestCode(int code) {
            Permissions permission = null;
            for (Permissions perm : Permissions.values()) {
                if (code == perm.getRequestCode()) {
                    permission = perm;
                }
            }

            return permission;
        }
    }
}
