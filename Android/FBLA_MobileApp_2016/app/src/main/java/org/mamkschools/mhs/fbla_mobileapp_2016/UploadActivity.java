package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONObject;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Commands;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Constants;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.SecureAPI;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Debug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.location.SimpleLocation;

public class UploadActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICTURE_REQUEST_CODE = 1;
    public static ScrollView mUploadForm;
    public static ProgressBar mProgressView;
    private static SimpleLocation simpleLocation;
    private Button getPic;
    private Button uploadBtn;
    private EditText editTitleText;
    private ImageView picPrev;
    private Uri outputFileUri;
    private AsyncTask uploadTask;
    private ByteArrayOutputStream picOutputStream;

    private static final int PERMISSION_REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String [] permsToEnable = getDisabledPermissions();
        if(permsToEnable.length != 0){
            ActivityCompat.requestPermissions(this,
                    permsToEnable,
                    PERMISSION_REQUEST_CODE);
        }

        simpleLocation = new SimpleLocation(getApplicationContext());
        getPic = (Button) (findViewById(R.id.getPic));
        getPic.setOnClickListener(this);
        editTitleText = (EditText) findViewById(R.id.editTitleText);
        picPrev = (ImageView) findViewById(R.id.uploadImage);
        uploadBtn = (Button) findViewById(R.id.uploadNow);
        uploadBtn.setOnClickListener(this);
        mUploadForm = (ScrollView) findViewById(R.id.uploadForm);
        mProgressView = (ProgressBar) findViewById(R.id.uploadProgress);
        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), "Network is not availible. Please connect to the internet", Toast.LENGTH_LONG).show();
            finish();
        }

        openImageIntent();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(uploadTask != null && !uploadTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            uploadTask.cancel(true);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void openImageIntent() {

        File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //noinspection ResultOfMethodCallIgnored
        filePath.mkdirs();
        String fname = "img_" + System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(filePath, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, PICTURE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICTURE_REQUEST_CODE) {
            final boolean isCamera;
            if (data == null) {
                isCamera = true;
            } else {
                final String action = data.getAction();
                isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
            }



            if (!isCamera) {
                outputFileUri = data.getData();
            }



            try {
                Bitmap b;
                {
                    byte[] imageBytes = getBytes(getContentResolver().openInputStream(outputFileUri));
                    b = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                }
                picOutputStream = new ByteArrayOutputStream();
                int maxDim = Math.max(b.getWidth(), b.getHeight());
                b = Bitmap.createScaledBitmap(b,map(b.getWidth(),0,maxDim, 0, 4096), map(b.getHeight(), 0, maxDim, 0, 4096), false);
                b.compress(Bitmap.CompressFormat.JPEG, 70, picOutputStream);
                picPrev.setImageBitmap(b);

            } catch (IOException | NullPointerException e) {
                if(Debug.DEBUG_MODE) {
                    e.printStackTrace();
                }

            } catch (OutOfMemoryError outOfMemoryError){
                if(Debug.DEBUG_MODE){
                    outOfMemoryError.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Out of memory, please increase emmulator ram or close other apps", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getPic:
                openImageIntent();
                break;
            case R.id.uploadNow:
                uploadImage();
                break;
            default:
                //Do nothing
        }

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mUploadForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mUploadForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mUploadForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mUploadForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private static class PicUploadParams {
        public Map<String, String> paramMap = new HashMap<String, String>();
        public Map<String, ByteArrayOutputStream> pics = new HashMap<String, ByteArrayOutputStream>();
    }

    private class PicUpload extends AsyncTask<PicUploadParams, Void, Boolean> {
        SecureAPI picture = SecureAPI.getInstance(getApplicationContext());

        @Override
        protected Boolean doInBackground(PicUploadParams... params) {
            try {
                JSONObject response = picture.HTTPSPOSTMULTI(Commands.Post.POSTPIC +
                                Commands.AUTHCODE_BASE + Constants.AUTHCODE,
                        params[0].paramMap, params[0].pics, getApplicationContext());
                if (response.getString("status").equals("success")) {
                    return true;
                } else if (response.getString("status").equals("error")) {
                    return false;
                } else {
                    throw new Exception("Impossible status");
                }
            } catch (Exception e) {
                if (Debug.DEBUG_MODE) {
                    Debug.log(e.getMessage());
                    e.printStackTrace();
                }
                return false;
            }
        }



        @Override
        protected void onPostExecute(Boolean success) {
            showProgress(false);
            if (success) {
                Debug.log("Upload worked");
                finish();
            } else {
                Debug.log("Upload failed");
                Toast.makeText(getApplicationContext(), "Upload failed, please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
    private int map(int x, int in_min, int in_max, int out_min, int out_max){
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public String [] getDisabledPermissions(){
        String [] permissions = {Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ArrayList<String> disabledPerms = new ArrayList<String>();
        for (String perm: permissions) {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED){
                disabledPerms.add(perm);
            }
        }
        return disabledPerms.toArray(new String[disabledPerms.size()]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                } else {

                    // permission denied, boo!
                    // must open settings, as these permissions are critical

                    Toast.makeText(getApplicationContext(), "Please enable location permissions", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public void uploadImage(){
        if(uploadTask != null){
            uploadTask.cancel(true);
        }
        PicUploadParams uploadPic = new PicUploadParams();
        uploadPic.pics.put("picture", picOutputStream);
        if(editTitleText.getText().toString() == null || editTitleText.getText().toString().equals("")){
            editTitleText.setError("Title is required");
            editTitleText.requestFocus();
            return;
        }
        uploadPic.paramMap.put("title" , editTitleText.getText().toString());

        Constants.LATITUDE = simpleLocation.getLatitude();
        Constants.LONGITUDE = simpleLocation.getLongitude();

        Debug.log("lat" + Constants.LATITUDE + "lon" + Constants.LONGITUDE);

        uploadPic.paramMap.put("geolong", "" + Constants.LONGITUDE);
        uploadPic.paramMap.put("geolat", "" + Constants.LATITUDE);
        showProgress(true);
        uploadTask = new PicUpload().execute(uploadPic);
    }
}
