/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

public class ImagePicker extends CordovaPlugin {
    public static String TAG = "ImagePicker";
    private static final String[] PERMISSIONS_STORAGE = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE_STORAGE = 103;
    private CallbackContext callbackContext;
    private JSONObject params;
    private Intent intent;

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "execute action =" + action + ",args =" + args);
        this.callbackContext = callbackContext;
        this.params = args.getJSONObject(0);
        if (action.equals("getPictures")) {
            intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
            int max = 20;
            int desiredWidth = 0;
            int desiredHeight = 0;
            int quality = 100;
            if (this.params.has("maximumImagesCount")) {
                max = this.params.getInt("maximumImagesCount");
            }
            if (this.params.has("width")) {
                desiredWidth = this.params.getInt("width");
            }
            if (this.params.has("height")) {
                desiredHeight = this.params.getInt("height");
            }
            if (this.params.has("quality")) {
                quality = this.params.getInt("quality");
            }
            intent.putExtra("MAX_IMAGES", max);
            intent.putExtra("WIDTH", desiredWidth);
            intent.putExtra("HEIGHT", desiredHeight);
            intent.putExtra("QUALITY", quality);
            if (this.cordova != null) {
                if (cordova.hasPermission(PERMISSIONS_STORAGE[0]) && cordova.hasPermission(PERMISSIONS_STORAGE[1])) {
                    Log.d(TAG, "execute hasPermission true");
                    this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
                } else {
                    cordova.requestPermissions(this, REQUEST_CODE_STORAGE, PERMISSIONS_STORAGE);
                }
            }
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult resultCode =" + resultCode + ",data =" + data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
            JSONArray res = new JSONArray(fileNames);
            this.callbackContext.success(res);
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            String error = data.getStringExtra("ERRORMESSAGE");
            this.callbackContext.error(error);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            JSONArray res = new JSONArray();
            this.callbackContext.success(res);
        } else {
            this.callbackContext.error("No images selected");
        }
    }
    @Override
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) throws JSONException {
        Log.d(TAG, "onRequestPermissionResult");
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_STORAGE:
                if (hasAllPermissionsGranted(grantResults)) {
                    Log.d(TAG, "onRequestPermissionResult hasAllPermissionsGranted");
                    this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
                }
                break;
            default:
                break;
        }
    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}
