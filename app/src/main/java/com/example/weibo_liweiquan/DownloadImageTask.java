package com.example.weibo_liweiquan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView imageView;
    private String imageName;

    public DownloadImageTask(ImageView imageView, String name) {
        this.imageView = imageView;
        this.imageName = name;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String urlDisplay = urls[0];
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlDisplay);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
            Log.i("downloadmine", imageName);
            saveImageToExternalStorage(bitmap,  imageName+".jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        Toast.makeText(imageView.getContext(), "图片下载完成，请相册查看！", Toast.LENGTH_SHORT).show();
    }

    private void saveImageToExternalStorage(Bitmap bitmap, String imageName) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/DCIM");


        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        File file = new File(myDir, imageName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
