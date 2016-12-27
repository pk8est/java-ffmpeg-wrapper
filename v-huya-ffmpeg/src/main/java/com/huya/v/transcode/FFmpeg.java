package com.huya.v.transcode;

import com.google.common.io.CharStreams;
import com.huya.v.transcode.builder.CommonBuilder;
import com.huya.v.transcode.execute.Processor;
import com.huya.v.transcode.execute.RunProcessor;
import com.huya.v.transcode.progress.*;
import com.sun.istack.internal.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        if (listener != null) {
            try (ProgressParser progressParser = createProgressParser(listener)) {
                progressParser.start();
                builder = builder.addProgress(progressParser.getUri());
                Process process = runProcessor.run(path(builder.build()));
                writerStdout(builder, process);
            }
        } else {
            writerStdout(builder, runProcessor.run(path(builder.build())));
        }

    }

    protected void writerStdout(CommonBuilder builder, Process process) throws IOException{
        try {
            List<OutputStream> outputStreams = builder.getOutputStreams();
            List<ProgressDataListener> progressDataListeners = builder.getProgressDataListeners();
            List<ProgressListener> progressListeners = builder.getProgressListeners();
            if(!progressListeners.isEmpty()){
                callProgressListener(process, progressListeners);
            }
            if(progressDataListeners.isEmpty() && outputStreams.isEmpty()){
                CharStreams.copy(wrapInReader(process), System.out);
            }else{
                InputStream in = process.getInputStream();
                new ProgressStdout(in, progressDataListeners, outputStreams).run();
            }
            throwOnError(process);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            process.destroy();
        }
    }

    private void callProgressListener(Process process, List<ProgressListener> progressListeners) throws IOException {
        InputStream in;
        in = process.getErrorStream();
        for (ProgressListener listener: progressListeners){
            new ProgressReader(in, listener).start();
        }
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
