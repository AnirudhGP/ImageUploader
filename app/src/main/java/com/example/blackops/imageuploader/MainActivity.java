package com.example.blackops.imageuploader;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private int SELECT_FILE = 1;
    private int CAPTURE_IMAGES_FROM_CAMERA = 2;
    private int image_count_before = 0;
    public static ArrayList<String> uriList;

    public static NotificationManager notificationManager;
    public static NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NoteDetailsActivity.class);
                intent.putStringArrayListExtra("uriList", uriList);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                uriList.clear();
            }
        });
        uriList = new ArrayList<>();

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);

        Intent intent = new Intent(this, NotificationService.class);
        intent.setAction(Constants.ACTION_PAUSE);
        PendingIntent pauseIntent = PendingIntent.getService(this, 0,
                intent, 0);
        Intent intent1 = new Intent(this, NotificationService.class);
        intent1.setAction(Constants.ACTION_RESUME);
        PendingIntent resumeIntent = PendingIntent.getService(this, 0,
                intent1, 0);

        builder.setContentTitle("Notes Uploader")
                .setContentText("Uploading notes ")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .addAction(0, "Pause", pauseIntent)
                .addAction(0, "Resume", resumeIntent);

        Button gallery = (Button) findViewById(R.id.btnSelectPhoto);
        Button camera = (Button) findViewById(R.id.btnCamera);

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryIntent();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraIntent();
            }
        });


    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);

    }

    private void cameraIntent()
    {
        Cursor cursor = loadCursor();
        image_count_before = cursor.getCount();
        cursor.close();
        Intent cameraIntent = new Intent(
                MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        List<ResolveInfo> activities = getPackageManager()
                .queryIntentActivities(cameraIntent, 0);
        if (activities.size() > 0)
            startActivityForResult(cameraIntent, CAPTURE_IMAGES_FROM_CAMERA);
        else
            Toast.makeText(this, "No Camera application", Toast.LENGTH_SHORT)
                    .show();
    }


    public Cursor loadCursor() {
        final String[] columns = { MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_ADDED;
        return getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy);
    }

    public String[] getImagePaths(Cursor cursor, int startPosition) {
        int size = cursor.getCount() - startPosition;
        if (size <= 0)
            return null;
        String[] paths = new String[size];
        int dataColumnIndex = cursor
                .getColumnIndex(MediaStore.Images.Media.DATA);
        for (int i = startPosition; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            paths[i - startPosition] = cursor.getString(dataColumnIndex);
        }
        return paths;
    }

    private void exitingCamera() {
        Cursor cursor = loadCursor();
        String[] paths = getImagePaths(cursor, image_count_before);
        cursor.close();
        getUris(paths);
    }

    private void getUris(String[] paths) {
        for (int i = 0; i < paths.length; i++) {
            uriList.add(Uri.fromFile(new File(paths[i])).toString());
            Log.d(TAG, "uri from camera = " + Uri.fromFile(new File(paths[i])).toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGES_FROM_CAMERA) {
            exitingCamera();
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        if (data.getData() != null) {
            Uri path =  data.getData();
            uriList.add(path.toString());
            Log.d(TAG, "uri from gallery single = " + path.toString());
        }
        else
        {
            ClipData clipData = data.getClipData();
            for (int i=0; i<clipData.getItemCount();i++) {
                Uri uripath = clipData.getItemAt(i).getUri();
                uriList.add(uripath.toString());
                Log.d(TAG, "uri from gallery multiple = " + uripath.toString());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}