package com.huya.v.transcode;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.huya.v.transcode.execute.Processor;
import com.huya.v.transcode.execute.RunProcessor;
import com.huya.v.transcode.util.ProcessUtils;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Administrator on 2016/12/23.
 */
public class FFcommon {

    public String path;
    public final Processor runProcessor;
    String version = null;

    public FFcommon(@Nonnull String path) {
        this(path, new RunProcessor());
    }

    protected FFcommon(@Nonnull String path, @Nonnull Processor runFunction) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(path));
        this.runProcessor = checkNotNull(runFunction);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public synchronized @Nonnull String version() throws IOException {
        if (this.version == null) {
            Process p = runProcessor.run(ImmutableList.of(path, "-version"));
            try {
                BufferedReader r = wrapInReader(p);
                this.version = r.readLine();
                CharStreams.copy(r, CharStreams.nullWriter()); // Throw away rest of the output

                throwOnError(p);
            } finally {
                p.destroy();
            }
        }
        return version;
    }

    protected BufferedReader wrapInReader(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
    }

    protected void throwOnError(Process p) throws IOException {
        try {
            // TODO In java 8 use waitFor(long timeout, TimeUnit unit)
            if (ProcessUtils.waitForWithTimeout(p, 1, TimeUnit.SECONDS) != 0) {
                // TODO Parse the error
                throw new IOException(path + " returned non-zero exit status. Check stdout.");
            }
        } catch (TimeoutException e) {
            throw new IOException("Timed out waiting for " + path + " to finish.");
        }
    }

    public List<String> path(List<String> args) throws IOException {
        return ImmutableList.<String>builder().add(path).addAll(args).build();
    }

    public void run(List<String> args) throws IOException {

        Process process = runProcessor.run(path(args));

        try {
            // TODO Move the copy onto a thread, so that FFmpegProgressListener can be on this thread.

            // Now block reading ffmpeg's stdout. We are effectively throwing away the output.
            CharStreams.copy(wrapInReader(process), System.out); // TODO Should I be outputting to stdout?

            throwOnError(process);

        } finally {
            process.destroy();
        }
    }

}
