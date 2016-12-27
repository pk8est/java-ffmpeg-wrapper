package com.huya.v.transcode;

import com.huya.v.transcode.builder.FFmpegBuilder;
import com.huya.v.transcode.job.FFmpegJob;
import com.huya.v.transcode.job.SinglePassFFmpegJob;
import com.huya.v.transcode.job.TwoPassFFmpegJob;
import com.huya.v.transcode.progress.ProgressListener;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Administrator on 2016/12/26.
 */
public class FFmpegExecutor {
    final FFmpeg ffmpeg;
    final FFprobe ffprobe;

    public FFmpegExecutor() throws IOException {
        this(new FFmpeg(), new FFprobe());
    }

    public FFmpegExecutor(FFmpeg ffmpeg) throws IOException {
        this(ffmpeg, new FFprobe());
    }

    public FFmpegExecutor(FFmpeg ffmpeg, FFprobe ffprobe) {
        this.ffmpeg = checkNotNull(ffmpeg);
        this.ffprobe = checkNotNull(ffprobe);
    }

    public FFmpegJob createJob(FFmpegBuilder builder) {
        return new SinglePassFFmpegJob(ffmpeg, ffprobe, builder, null);
    }

    public FFmpegJob createJob(FFmpegBuilder builder, ProgressListener listener) {
        return new SinglePassFFmpegJob(ffmpeg, ffprobe, builder, listener);
    }

    /**
     * Creates a two pass job, which will execute FFmpeg twice to produce a better quality output.
     * More info: https://trac.ffmpeg.org/wiki/x264EncodingGuide#twopass
     *
     * @param builder The FFmpegBuilder
     * @return A new two-pass FFmpegJob
     */
    public FFmpegJob createTwoPassJob(FFmpegBuilder builder) {
        return new TwoPassFFmpegJob(ffmpeg, ffprobe, builder, null);
    }
}
