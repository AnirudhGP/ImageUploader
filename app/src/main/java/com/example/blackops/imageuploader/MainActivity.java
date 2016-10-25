package com.example.blackops.imageuploader;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import org.json.JSONException;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static android.R.attr.action;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private int REQUEST_CAMERA = 0;
    private int SELECT_FILE = 1;
    private String pictureImagePath = "";
    ArrayList<String> uriList;
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
                storeImages();
            }
        });
        uriList = new ArrayList<>();

        /*uriList.add("url1");
        uriList.add("url2");
        PendingNote note = new PendingNote("111", uriList, 0, "haha", "desc");
        NotesDatabaseHelper notesDatabaseHelper = new NotesDatabaseHelper(getApplicationContext());
        notesDatabaseHelper.deletePendingNote("111");
        try {
            notesDatabaseHelper.insertPendingNote(note);
            Log.d(TAG, "inserted");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PendingNote newNote = notesDatabaseHelper.getNextPendingNote();
        Log.d(TAG, "Next note : " + newNote.getCourseId() + " " + newNote.getNoOfPagesUploaded() ) ;
        notesDatabaseHelper.incrementPageNumberOfPendingNote(newNote.getCourseId());
        newNote = notesDatabaseHelper.getNextPendingNote();
        Log.d(TAG, "Incremented note : " + newNote.getCourseId() + " " + newNote.getNoOfPagesUploaded() ) ;
        */

        Button gallery = (Button) findViewById(R.id.gallery);
        Button camera = (Button) findViewById(R.id.camera);

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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";

        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File photoFile = new File(pictureImagePath);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photoFile));
            Bundle data = new Bundle();
            startActivityForResult(takePictureIntent, REQUEST_CAMERA, data);
        }
    }

    private void storeImages() {
        Log.d(TAG, "storing " + uriList.size() + " images");
        PendingNote note = new PendingNote("111", uriList, 0, "haha", "desc");
        NotesDatabaseHelper notesDatabaseHelper = new NotesDatabaseHelper(getApplicationContext());
        notesDatabaseHelper.deletePendingNote("111");
        try {
            notesDatabaseHelper.insertPendingNote(note);
            Log.d(TAG, "inserted");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startService(new Intent(this, UploadService.class));
    }

    private int action;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
                action = 0;
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
                action = 1;
            }
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        if (data.getData() != null) {
            Uri path =  data.getData();
            uriList.add(path.toString());
            /*try {
                uriList.add(getFilePath(MainActivity.this,path));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }*/
            Log.d(TAG, "uri from gallery single = " + path.toString());
        }
        else
        {
            ClipData clipData = data.getClipData();
            for (int i=0; i<clipData.getItemCount();i++) {
                Uri uripath = clipData.getItemAt(i).getUri();
                uriList.add(uripath.toString());
                /*try {
                    uriList.add(getFilePath(MainActivity.this,uripath));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }*/
                Log.d(TAG, "uri from gallery multiple = " + uripath.toString());
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        File imgFile = new File(pictureImagePath);
        uriList.add(Uri.fromFile(imgFile).toString());
        // uriList.add(pictureImagePath);
        Log.d(TAG, "uri from camera = " + Uri.fromFile(imgFile).toString());
    }


    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
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
