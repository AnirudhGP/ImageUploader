package com.example.blackops.imageuploader;

import android.app.IntentService;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Created by aniru on 14-10-2016.
 */

public class UploadService extends IntentService {
    private static final String TAG = "UploadService";

    public UploadService() {
        super("UploadService");
    }

    public UploadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotesDatabaseHelper notesDatabaseHelper = new NotesDatabaseHelper(
                this.getApplicationContext());
        PendingNote note = notesDatabaseHelper.getNextPendingNote();
        if (note == null) {
            Log.d(TAG, "No more notes to be uploaded");
            stopSelf();
        }
        Log.d(TAG, "courseId = " + note.getCourseId());
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("courseId", note.getCourseId())
                .addFormDataPart("profileId", "")
                .addFormDataPart("date","")
                .addFormDataPart("title", note.getTitle())
                .addFormDataPart("notesDesc", note.getNotesDesc());
        File file;
        int pos = note.getNoOfPagesUploaded() + 1;
        ArrayList<String> uriList = note.getUriList();
        Bitmap original = null;
        try {
            InputStream fileInputStream = getContentResolver().openInputStream(Uri.parse(uriList.get(pos-1)));
            original = BitmapFactory.decodeStream(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        file = new File(getFilesDir() + "/temp" + pos + ".jpeg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int size = original.getRowBytes() * original.getHeight();
        Log.i("sw32size", size + "");
        if (size > 10000000)
            original.compress(Bitmap.CompressFormat.JPEG, 40, out);
        else
            original.compress(Bitmap.CompressFormat.JPEG, 80, out);
        body.addFormDataPart("file", "test.jpg", RequestBody.create(MediaType.parse("image/*"), file));

        RequestBody requestBody = body.build();
        Request request = new Request.Builder()
                .url("https://uploadnotes-2016.appspot.com/imgweb")
                .post(requestBody)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Response res = response;
            ResponseBody responseBody = null;
            if (res != null) {
                responseBody = res.body();
            }
            String jsonresponse = null;
            if (responseBody != null) {
                jsonresponse = responseBody.string();
            } else {
               Log.d(TAG, "Upload failed");
            }

            if(jsonresponse!=null) {
                JSONObject jsonObject = new JSONObject(jsonresponse);
                Log.d(TAG, jsonObject.getString("url"));
                notesDatabaseHelper.incrementPageNumberOfPendingNote(note.getCourseId());
                if (note.getNoOfPagesUploaded() + 1 == note.getUriList().size()) {
                    notesDatabaseHelper.deletePendingNote(note.getCourseId());
                }
            }
        }  catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}