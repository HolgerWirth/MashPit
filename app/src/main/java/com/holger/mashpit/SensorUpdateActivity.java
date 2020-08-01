package com.holger.mashpit;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.os.ConfigurationCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.holger.mashpit.tools.FTPUpdate;
import com.holger.mashpit.tools.MD5;
import com.holger.mashpit.tools.SensorPublishMQTT;
import com.holger.mashpit.tools.SnackBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SensorUpdateActivity extends AppCompatActivity implements FTPUpdate.OnFTPUpdateListener,SensorPublishMQTT.OnPublishConfiguration {
    private static final String DEBUG_TAG = "SensorUpdateActivity";

    SnackBar snb;
    TextView signatureMD5;
    Button uploadImage;

    String server;
    String alias;
    String IP;
    String localPath;

    Context context;
    String generatedString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_update);

        Toolbar toolbar = findViewById(R.id.sensordev_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView serverId = findViewById(R.id.serverId);
        TextView serverSystem = findViewById(R.id.serverSystem);
        TextView serverIP = findViewById(R.id.serverIP);
        signatureMD5 = findViewById(R.id.signatureMD5);

        IP = getIntent().getStringExtra("IP");
        server = getIntent().getStringExtra("server");
        alias = getIntent().getStringExtra("alias");

        Button selectImage = findViewById(R.id.selectImage);
        uploadImage = findViewById(R.id.uploadImage);

        signatureMD5.setVisibility(View.GONE);
        uploadImage.setVisibility(View.GONE);

        serverId.setEnabled(false);
        serverSystem.setEnabled(false);
        serverIP.setEnabled(false);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Update Sensor");

        final CoordinatorLayout coordinatorLayout = findViewById(R.id.snb_content);
        snb = new SnackBar(coordinatorLayout);

        serverId.setText(getIntent().getStringExtra("server"));
        serverSystem.setText(getIntent().getStringExtra("system"));
        serverIP.setText(getIntent().getStringExtra("IP"));

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBrowse(view);
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpdate();
            }
        });
    }

    public void onBrowse(View view) {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == 1) {
            signatureMD5.setVisibility(View.VISIBLE);
            uploadImage.setVisibility(View.VISIBLE);
            Uri uri = data.getData();
            assert uri != null;
            File path =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            Log.i(DEBUG_TAG, "Image path: " + path.getAbsolutePath());
            File uploadFile = new File(path,displayName(uri));
            localPath=uploadFile.getPath();
            Log.i(DEBUG_TAG, "Image File: " + displayName(uri));

            Locale locale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
            SimpleDateFormat fmtout = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", locale);
            Date df = new java.util.Date(uploadFile.lastModified());

            SpannableStringBuilder infoText= new SpannableStringBuilder();
            SpannableString filename = new SpannableString(uploadFile.getName());
            filename.setSpan(new StyleSpan(Typeface.BOLD), 0, filename.length(), 0);
            infoText.append("Filename: ")
                    .append(filename);
            SpannableString filesize = new SpannableString(uploadFile.length()+ " bytes");
            filesize.setSpan(new StyleSpan(Typeface.BOLD), 0, filesize.length(), 0);
            infoText.append("\nFile size: ")
                    .append(filesize);
            SpannableString lastmodified = new SpannableString(fmtout.format(df));
            lastmodified.setSpan(new StyleSpan(Typeface.BOLD), 0, lastmodified.length(), 0);
            infoText.append("\nLast modified: ")
                    .append(lastmodified);
            SpannableString md5sig = new SpannableString(MD5.calculateMD5(uploadFile));
            md5sig.setSpan(new StyleSpan(Typeface.BOLD), 0, md5sig.length(), 0);
            infoText.append("\nMD5 signature: ")
                    .append(md5sig);
            signatureMD5.setText(infoText);
            signatureMD5.setEnabled(false);
            uploadImage.setEnabled(true);
        }
    }

    private String displayName(Uri uri) {

        Cursor mCursor =
                getApplicationContext().getContentResolver().query(uri, null, null, null, null);
        assert mCursor != null;
        int indexedname = mCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        mCursor.moveToFirst();
        String filename = mCursor.getString(indexedname);
        mCursor.close();
        return filename;
    }

    public void startUpdate() {
        Log.i(DEBUG_TAG, "Starting update of: " + IP);
        int myPass = new Random().nextInt(300000) + 10000;
        generatedString = Integer.toString(myPass);
        final MaterialAlertDialogBuilder alertDialog;
        alertDialog = new MaterialAlertDialogBuilder(this);
        context = this;
        Log.i(DEBUG_TAG, "FTP Upload for user: " + server + " and password: " + generatedString);

        alertDialog.setTitle(getString(R.string.pubConfig));
        alertDialog.setMessage(getString(R.string.updateFTPAlert));
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(DEBUG_TAG, "Clicked on Cancel!");
            }
        });
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(DEBUG_TAG, "Clicked on OK! - OK");
                JSONObject obj = new JSONObject();
                try {
                    obj.put("FTPPWD", generatedString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
                pubMQTT.PublishServerUpdate(server, obj.toString());
            }

        });
        alertDialog.show();
    }

    @Override
    public void FTPCallback(Boolean success) {
        Log.i(DEBUG_TAG, "FTPUpdate success!");
        final MaterialAlertDialogBuilder alertDialog;
        alertDialog = new MaterialAlertDialogBuilder(this);
        alertDialog.setTitle(getString(R.string.updateFTPTitle));
        if(alias.isEmpty()) {
            alertDialog.setMessage(getString(R.string.updateFTPsuccess, server));
        }
        else
        {
            alertDialog.setMessage(getString(R.string.updateFTPsuccess, alias));
        }
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(DEBUG_TAG, "FTP success: Clicked on OK! - OK");
                onBackPressed();
            }
        });
        alertDialog.show();
    }

    @Override
    public void PublishConfigurationCallback(Boolean success, int position) {
        if (success) {
            snb.displayInfo(R.string.pubConfOK);
            new FTPUpdate(context, IP, server, generatedString, localPath);
        } else {
            snb.displayInfo(R.string.pubConfNOK);
        }
    }
}
