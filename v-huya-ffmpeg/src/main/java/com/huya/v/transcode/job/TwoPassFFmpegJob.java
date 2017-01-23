package com.huya.v.transcode.job;

import com.huya.v.transcode.FFmpeg;
import com.huya.v.transcode.FFprobe;
import com.huya.v.transcode.builder.FFmpegBuilder;
import com.huya.v.transcode.ffprobe.FFmpegProbeResult;
import com.huya.v.transcode.progress.ProgressListener;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Administrator on 2016/12/26.
 */
public class TwoPassFFmpegJob extends FFmpegJob {

    final String passlogPrefix;
    final FFmpegBuilder builder;

    public TwoPassFFmpegJob(FFmpegBuilder builder) throws IOException {
        this(new FFmpeg(), new FFprobe(), builder, null);
    }

    public TwoPassFFmpegJob(FFmpeg ffmpeg, FFmpegBuilder builder) throws IOException {
        this(ffmpeg, new FFprobe(), builder, null);
    }

    public TwoPassFFmpegJob(FFprobe ffprobe, FFmpegBuilder builder) throws IOException {
        this(new FFmpeg(), ffprobe, builder, null);
    }

    public TwoPassFFmpegJob(FFmpeg ffmpeg, FFprobe ffprobe, FFmpegBuilder builder) throws IOException {
        this(ffmpeg, ffprobe, builder, null);
    }

    public TwoPassFFmpegJob(FFmpeg ffmpeg, FFprobe ffprobe, FFmpegBuilder builder, @Nullable ProgressListener listener) {
        super(ffmpeg, ffprobe, listener);

        // Random prefix so multiple runs don't clash
        this.passlogPrefix = UUID.randomUUID().toString();
        this.builder = checkNotNull(builder).setPassPrefix(passlogPrefix);

        // Build the args now (but throw away the results). This allows the illegal arguments to be
        // caught early, but also allows the ffmpeg command to actually alter the arguments when
        // running.
        //this.builder.setPass(1).build();
        this.builder.setPass(1);
    }

    protected void deletePassLog() throws IOException {
        final Path cwd = Paths.get("");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cwd, passlogPrefix + "*.log*")) {
            for (Path p : stream) {
                Files.deleteIfExists(p);
            }
        }
    }

    public FFmpegProbeResult getFFprobeResult() throws IOException {
        return  ffprobe.probe(builder.getInputs().get(0));
    }

    public FFmpegProbeResult getFFprobeResult(int index) throws IOException {
        return  ffprobe.probe(builder.getInputs().get(index));
    }

    public void run() {
        state = State.RUNNING;

        try {
            try {
                // Two pass
                final boolean override = builder.getOverride();

                FFmpegBuilder b1 = builder.setPass(1).setOverride(true);
                ffmpeg.run(b1, listener);

                FFmpegBuilder b2 = builder.setPass(2).setOverride(override);
                ffmpeg.run(b2, listener);

            } finally {
                deletePassLog();
            }
            state = State.FINISHED;

        } catch (IOException e) {
            state = State.FAILED;
            errorMessage = e.getMessage();
        }
    }

}
