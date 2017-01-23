package com.huya.v.transcode;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.huya.v.transcode.builder.FFmpegBuilder;
import com.huya.v.transcode.builder.FFmpegInputBuilder;
import com.huya.v.transcode.execute.Processor;
import com.huya.v.transcode.execute.RunProcessor;
import com.huya.v.transcode.ffprobe.FFmpegProbeResult;
import com.huya.v.transcode.io.LoggingFilterReader;
import com.huya.v.transcode.util.FFmpegUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by Administrator on 2016/12/26.
 */
public class FFprobe extends FFcommon{
    public String path = "ffprobe";
    final static Logger LOG = LoggerFactory.getLogger(FFprobe.class);
    final static String FFPROBE = "ffprobe";
    final static String DEFAULT_PATH = MoreObjects.firstNonNull(System.getenv("FFPROBE"), FFPROBE);
    final static Gson gson = FFmpegUtils.getGson();

    public FFprobe() throws IOException {
        this(DEFAULT_PATH, new RunProcessor());
    }

    public FFprobe(@Nonnull Processor runFunction) throws IOException {
        this(DEFAULT_PATH, runFunction);
    }

    public FFprobe(@Nonnull String path) throws IOException {
        this(path, new RunProcessor());
    }

    public FFprobe(@Nonnull String path, @Nonnull Processor runFunction) {
        super(path, runFunction);
    }

    public FFmpegProbeResult probe(String mediaPath) throws IOException {
        return probe(new FFmpegInputBuilder(mediaPath), null);
    }

    public FFmpegProbeResult probe(FFmpegInputBuilder input) throws IOException {
        return probe(input, null);
    }

    public FFmpegProbeResult probe(FFmpegInputBuilder input, @Nullable String userAgent) throws IOException {
        checkIfFFprobe();
        ImmutableList.Builder<String> args = new ImmutableList.Builder<String>();
        args.add(path).add("-v", "quiet");

        if (userAgent != null) {
            args.add("-user-agent", userAgent);
        }

        args.add("-print_format", "json")
            .add("-show_error")
            .add("-show_format")
            .add("-show_streams")
            .add("-threads", "1")
            .addAll(input.build(new FFmpegBuilder()));

        Process p = runProcessor.run(args.build());
        try {
            if(input.isPipe()){
                readStdin(input.getInputStream(), p);
            }
            Reader reader = wrapInReader(p);
            if (LOG.isDebugEnabled()) {
                reader = new LoggingFilterReader(reader, LOG);
            }

            FFmpegProbeResult result = gson.fromJson(reader, FFmpegProbeResult.class);

            throwOnError(p);

            if (result == null) {
                throw new IllegalStateException("Gson returned null, which shouldn't happen :(");
            }

            return result;

        } finally {
            p.destroy();
        }

    }

    private void checkIfFFprobe() throws IllegalArgumentException, IOException {
        if (!isFFprobe()) {
            throw new IllegalArgumentException("This binary '" +  path  + "' is not a supported version of ffprobe");
        }
    }

    public boolean isFFprobe() throws IOException {
        return version().startsWith("ffprobe");
    }
}
