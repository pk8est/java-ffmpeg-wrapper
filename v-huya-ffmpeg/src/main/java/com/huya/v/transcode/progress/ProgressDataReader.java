package com.huya.v.transcode.progress;

import java.io.*;

/**
 * Created by Administrator on 2016/12/24.
 */
public class ProgressDataReader {

    private int size = 4096;
    private OutputStream out;
    private ProgressDataListener listener;

    public ProgressDataReader(ProgressDataListener listener, int size){
        this.size = size;
        this.listener = listener;
    }

    public ProgressDataReader(OutputStream stream, int size){
        this.size = size;
        this.out = new BufferedOutputStream(stream, size);
    }

    public void run(byte[] buf, int len) throws IOException {
        if(listener != null){
            writeListener(buf, len);
        }
        if(out != null){
            writeOut(buf, len);
        }
    }

    public void writeListener(byte[] buf, int len) throws IOException {
        listener.getData(buf, len);
    }

    public void writeOut(byte[] buf, int len) throws IOException {
        out.write(buf, 0, len);
        if(len < size){
            out.flush();
        }
    }

}
