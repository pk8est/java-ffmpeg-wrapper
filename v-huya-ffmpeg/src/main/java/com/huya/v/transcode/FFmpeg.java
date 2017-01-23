package com.huya.v.transcode;

import com.huya.v.transcode.builder.CommonBuilder;
import com.huya.v.transcode.execute.Processor;
import com.huya.v.transcode.execute.RunProcessor;
import com.huya.v.transcode.progress.ProgressListener;
import com.huya.v.transcode.progress.ProgressParser;
import com.huya.v.transcode.progress.TcpProgressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Administrator on 2016/12/23.
 */
public class FFmpeg extends FFcommon{

    public String path = "ffmpeg";
    public final static String FFMPEG = "ffmpeg";
    public final static String DEFAULT_PATH = firstNonNull(System.getenv("FFMPEG"), FFMPEG);
    private static final Logger LOG = LoggerFactory.getLogger(FFmpeg.class);

    public FFmpeg() throws IOException {
        this(DEFAULT_PATH, new RunProcessor());
    }

    public FFmpeg(@Nonnull Processor runProcessor) throws IOException {
        this(DEFAULT_PATH, runProcessor);
    }

    public FFmpeg(@Nonnull String path) throws IOException {
        this(path, new RunProcessor());
    }

    public FFmpeg(@Nonnull String path, @Nonnull Processor runProcessor) throws IOException {
        super(path, runProcessor);
        version();
    }

    public void run(List<String> args) throws IOException {
        super.run(args);
    }

    public void run(CommonBuilder builder) throws IOException {
        run(builder, null);
    }

    public void runTwoPass(CommonBuilder builder) throws IOException {
        run(builder, null);
    }


    public void run(CommonBuilder builder, @Nullable ProgressListener listener) throws IOException {
        checkNotNull(builder);
        checkIfFFmpeg();
        command = path(builder.build());
        Process process = runProcessor.run(command);
        if (listener != null) {
            try (ProgressParser progressParser = createProgressParser(listener)) {
                progressParser.start();
                builder.addProgress(progressParser.getUri());
            }
        }
        writerStdin(builder, process);
        writerStdout(builder, process);

    }

    protected ProgressParser createProgressParser(ProgressListener listener) throws IOException {
        // TODO In future create the best kind for this OS, unix socket, named pipe, or TCP.
        try {
            // Default to TCP because it is supported across all OSes, and is better than UDP because it
            // provides good properties such as in-order packets, reliability, error checking, etc.
            return new TcpProgressParser(checkNotNull(listener));
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    public boolean isFFmpeg() throws IOException {
        return version().startsWith("ffmpeg");
    }

    private void checkIfFFmpeg() throws IllegalArgumentException, IOException {
        if (!isFFmpeg()) {
            throw new IllegalArgumentException("This binary '" + path
                    + "' is not a supported version of ffmpeg");
        }
    }

}
