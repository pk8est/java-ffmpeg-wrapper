package com.huya.v.transcode.progress;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/12/25.
 */
public class ProgressStdin implements Runnable{

    private int size = 4096;
    private InputStream in;
    private OutputStream out;

    public ProgressStdin(InputStream in, OutputStream out){
        this.in = in;
        this.out = new BufferedOutputStream(out, size);
    }

    public void setSize(int size){
        this.size = size;
    }

    public void run() {
        byte[] buf = new byte[size];
        try {
            int len = in.read(buf);
            while (len != -1) {
                out.write(buf, 0 , len);
                len = in.read(buf);
            }
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
