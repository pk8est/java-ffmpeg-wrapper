package com.huya.v.transcode.progress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by Administrator on 2016/12/24.
 */
public class ProgressReader extends Thread {

    private InputStream in;
    private ProgressListener listener;

    public ProgressReader(InputStream in, ProgressListener listener){
        this.in = in;
        this.listener = listener;
    }

    @Override
    public void run() {
        BufferedReader inBuf = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        Progress p = new Progress();
        try {
            while ((line = inBuf.readLine()) != null) {
                p.parseLineByStdout(line);
                listener.progress(p, line);
                p = new Progress();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
