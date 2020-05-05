package com.sbw.auder.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;

import com.bumptech.glide.load.engine.Resource;
import com.sbw.auder.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;

public class DownloadAudio extends AsyncTask<String, String, String> {
    String id,extension;
    Context context;

    public DownloadAudio(Context context) {
        this.context=context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String savedAudioPath = null;
        this.id = strings[1];
        this.extension = strings[2];
        try {
            URL url = new URL(strings[0]);
            URLConnection conexion = url.openConnection();
            conexion.connect();

            // download the file



            File dcimDir = new File(context.getExternalFilesDir(null),DIRECTORY_MUSIC);
            File picsDir = new File(dcimDir, "Nandi");


            boolean success = true;
            if (!picsDir.exists()) {
                success = picsDir.mkdirs();
            }
            if(success){
                File audioFile = new File(picsDir, strings[1]+extension);
                savedAudioPath = audioFile.getAbsolutePath();
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(audioFile);

                byte data[] = new byte[1024];

                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedAudioPath;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(extension.equals(".mp3"))
            TinyDB.setAudioPath(id,s);
        else
            TinyDB.setVideoPath(id,s);
    }
}
