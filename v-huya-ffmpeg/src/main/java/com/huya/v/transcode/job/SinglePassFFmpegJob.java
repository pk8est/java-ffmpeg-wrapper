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
public class SinglePassFFmpegJob extends FFmpegJob{


    final public FFmpegBuilder builder;

    public SinglePassFFmpegJob(FFmpegBuilder builder) throws IOException {
        this(new FFmpeg(), new FFprobe(), builder, null);
    }

    public SinglePassFFmpegJob(FFmpeg ffmpeg, FFmpegBuilder builder) throws IOException {
        this(ffmpeg, new FFprobe(), builder, null);
    }

    public SinglePassFFmpegJob(FFprobe ffprobe, FFmpegBuilder builder) throws IOException {
        this(new FFmpeg(), ffprobe, builder, null);
    }

    public SinglePassFFmpegJob(FFmpeg ffmpeg, FFprobe ffprobe, FFmpegBuilder builder) throws IOException {
        this(ffmpeg, ffprobe, builder, null);
    }

    public SinglePassFFmpegJob(FFmpeg ffmpeg, FFprobe ffprobe, FFmpegBuilder builder, @Nullable ProgressListener listener) {
        super(ffmpeg, ffprobe, listener);

        // Random prefix so multiple runs don't clash
        this.builder = checkNotNull(builder);

        // Build the args now (but throw away the results). This allows the illegal arguments to be
        // caught early, but also allows the ffmpeg command to actually alter the arguments when
        // running.
        //this.builder.build();
    }

    public FFmpegProbeResult getFFprobeResult() throws IOException {
        return  ffprobe.probe(builder.getInputs().get(0));
    }

    public FFmpegProbeResult getFFprobeResult(int index) throws IOException {
        return  ffprobe.probe(builder.getInputs().get(index));
    }

    public void run(){

        state = State.RUNNING;

        try {
            ffmpeg.run(builder, listener);
            state = State.FINISHED;

        } catch (IOException e) {
            state = State.FAILED;
            errorMessage = e.getMessage();
        }
        /*try {
            ffmpeg.run(builder, listener);
            state = State.FINISHED;

        } catch (Throwable t) {
            state = State.FAILED;

            Throwables.throwIfUnchecked(t);
            throw new RuntimeException(t);
        }*/
    }

}
