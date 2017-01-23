package com.huya.v.transcode.progress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/25.
 */
public class ProgressStdout {

    private int size = 4096;
    private InputStream in;
    private List<ProgressDataReader> progressDataReaders = new ArrayList<>();

    public ProgressStdout(InputStream in, List<ProgressDataListener> listeners, List<OutputStream> outputStreams){
        this.in = in;
        initProgressDataListener(listeners);
        initOutputStreams(outputStreams);
    }

    public void initProgressDataListener(List<ProgressDataListener> listeners){
        for (ProgressDataListener listener: listeners){
            progressDataReaders.add(new ProgressDataReader(listener, size));
        }
    }

    public void initOutputStreams(List<OutputStream> outputStreams){
        for (OutputStream outputStream: outputStreams){
            progressDataReaders.add(new ProgressDataReader(outputStream, size));
        }
    }

    public void setSize(int size){
        this.size = size;
    }

    public void run() throws IOException {
        byte[] buf = new byte[size];
        int len = in.read(buf);
        while (len != -1) {
            dispatch(buf, len);
            len = in.read(buf);
        }
    }

    protected void dispatch(byte[] buf, int len) throws IOException {
        if(progressDataReaders.size() > 0){
            for (ProgressDataReader progressDataReader: progressDataReaders){
                progressDataReader.run(buf, len);
            }
        }
    }
}
