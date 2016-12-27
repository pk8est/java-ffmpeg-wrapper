package com.huya.v.transcode.job;

import com.huya.v.transcode.FFmpeg;
import com.huya.v.transcode.FFprobe;
import com.huya.v.transcode.builder.FFmpegBuilder;
import com.huya.v.transcode.ffprobe.FFmpegProbeResult;
import com.huya.v.transcode.progress.ProgressListener;

import javax.annotation.Nullable;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Administrator on 2016/12/26.
 */
public abstract class FFmpegJob implements Runnable{
    public enum State {
        WAITING, RUNNING, FINISHED, FAILED,
    }

    final FFmpeg ffmpeg;
    final FFprobe ffprobe;
    public FFmpegBuilder builder;
    final ProgressListener listener;

    State state = State.WAITING;

    public FFmpegJob() throws IOException {
        this(new FFmpeg(), new FFprobe(), null);
    }

    public FFmpegJob(FFmpeg ffmpeg) throws IOException {
        this(ffmpeg, new FFprobe(), null);
    }

    public FFmpegJob(FFprobe ffprobe) throws IOException {
        this(new FFmpeg(), ffprobe, null);
    }

    public FFmpegJob(FFmpeg ffmpeg, @Nullable ProgressListener listener) throws IOException {
        this(ffmpeg, new FFprobe(), listener);
    }

    public FFmpegJob(FFprobe ffprobe, @Nullable ProgressListener listener) throws IOException {
        this(new FFmpeg(), ffprobe, listener);
    }

    public FFmpegJob(FFmpeg ffmpeg, FFprobe ffprobe, @Nullable ProgressListener listener) {
        this.ffmpeg = checkNotNull(ffmpeg);
        this.ffprobe = checkNotNull(ffprobe);
        this.listener = listener;
    }

    public FFmpegProbeResult getFFprobeResult() throws IOException {
        return null;
    }

    public State getState() {
        return state;
    }
}
