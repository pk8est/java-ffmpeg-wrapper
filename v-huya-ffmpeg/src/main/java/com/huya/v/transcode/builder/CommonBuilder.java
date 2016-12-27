package com.huya.v.transcode.builder;

import com.huya.v.transcode.ffprobe.FFmpegProbeResult;
import com.huya.v.transcode.progress.ProgressDataListener;
import com.huya.v.transcode.progress.ProgressListener;

import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Administrator on 2016/12/26.
 */
public class CommonBuilder {

    URI progress;
    List<String> options = new ArrayList<>();
    final List<ProgressListener> progressListeners = new ArrayList<>();
    final List<FFmpegOutputBuilder> outputs = new ArrayList<>();
    final List<OutputStream> outputStreams = new ArrayList<>();
    final List<ProgressDataListener> progressDataListeners = new ArrayList<>();
    final Map<String, FFmpegProbeResult> inputProbes = new TreeMap<>();

    public CommonBuilder addOption(String opt){
        if(opt.startsWith("-")){
            options.add(opt);
        }else{
            options.add("-" + opt);
        }
        return this;
    }

    public CommonBuilder addOption(String opt, String value){
        if(opt.startsWith("-")){
            options.add(opt);
        }else{
            options.add("-" + opt);
        }
        options.add(value);
        return this;
    }

    public CommonBuilder addOption(List<String> opts){
        for (String opt : opts) {
            options.add(opt);
        }
        return this;
    }

    public CommonBuilder addProgress(URI uri) {
        this.progress = checkNotNull(uri);
        return this;
    }

    public CommonBuilder addProgressListener(ProgressListener listener){
        this.progressListeners.add(listener);
        return this;
    }

    public List<OutputStream> getOutputStreams(){
        return outputStreams;
    }

    public List<ProgressDataListener> getProgressDataListeners() {
        return progressDataListeners;
    }

    public List<ProgressListener> getProgressListeners() {
        return progressListeners;
    }

    public List<String> build() {
        return null;
    }

}
