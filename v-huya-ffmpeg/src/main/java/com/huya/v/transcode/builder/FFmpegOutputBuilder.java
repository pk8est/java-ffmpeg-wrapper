package com.huya.v.transcode.builder;

import com.google.common.collect.ImmutableList;
import com.huya.v.transcode.progress.ProgressDataListener;
import org.apache.commons.lang3.SystemUtils;

import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Administrator on 2016/12/23.
 */
public class FFmpegOutputBuilder extends CommonBuilder{

    final FFmpegBuilder parent;
    public String filename;
    public URI uri;
    public OutputStream stream;
    public ProgressDataListener listener;

    final private static String DEVNULL = SystemUtils.IS_OS_WINDOWS ? "NUL" : "/dev/null";

    public FFmpegOutputBuilder() {
        this.parent = null;
    }

    protected FFmpegOutputBuilder(String filename) {
        this( new FFmpegBuilder(), filename);
    }

    protected FFmpegOutputBuilder(String filename, String[] options) {
        this( new FFmpegBuilder(), filename, options);
    }

    protected FFmpegOutputBuilder(String filename, List<String> options) {
        this( new FFmpegBuilder(), filename, options);
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, String filename) {
        this.parent = checkNotNull(parent);
        this.filename = filename;
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, String filename, String[] options) {
        this(parent, filename, Arrays.asList(options));    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, String filename, List<String> options) {
        this.parent = checkNotNull(parent);
        this.filename = filename;
        this.options = options;
    }

    protected FFmpegOutputBuilder(URI uri) {
        this( new FFmpegBuilder(), uri);
    }

    protected FFmpegOutputBuilder(URI uri, String[] options) {
        this( new FFmpegBuilder(), uri, options);
    }

    protected FFmpegOutputBuilder(URI uri, List<String> options) {
        this( new FFmpegBuilder(), uri, options);
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, URI uri) {
        this.parent = checkNotNull(parent);
        this.uri = uri;
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, URI uri, String[] options) {
        this(parent, uri, Arrays.asList(options));
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, URI uri, List<String> options) {
        this.parent = checkNotNull(parent);
        this.uri = uri;
        this.options = options;
    }

    protected FFmpegOutputBuilder(OutputStream stream) {
        this( new FFmpegBuilder(), stream);
    }

    protected FFmpegOutputBuilder(OutputStream stream, String[] options) {
        this( new FFmpegBuilder(), stream, options);
    }

    protected FFmpegOutputBuilder(OutputStream stream, List<String> options) {
        this( new FFmpegBuilder(), stream, options);
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, OutputStream stream) {
        this.parent = checkNotNull(parent);
        this.stream = stream;
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, OutputStream stream, String[] options) {
        this(parent, stream, Arrays.asList(options));
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, OutputStream stream, List<String> options) {
        this.parent = checkNotNull(parent);
        this.stream = stream;
        this.options = options;
    }

    protected FFmpegOutputBuilder(ProgressDataListener listener) {
        this( new FFmpegBuilder(), listener);
    }

    protected FFmpegOutputBuilder(ProgressDataListener listener, List<String> options) {
        this( new FFmpegBuilder(), listener, options);
    }

    protected FFmpegOutputBuilder(ProgressDataListener listener, String[] options) {
        this( new FFmpegBuilder(), listener, options);
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, ProgressDataListener listener) {
        this.parent = checkNotNull(parent);
        this.listener = listener;
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, ProgressDataListener listener, String[] options) {
        this(parent, listener, Arrays.asList(options));
    }

    protected FFmpegOutputBuilder(FFmpegBuilder parent, ProgressDataListener listener, List<String> options) {
        this.parent = checkNotNull(parent);
        this.listener = listener;
        this.options = options;
    }

    public List<String> build(FFmpegBuilder parent, int pass) {
        checkNotNull(parent);

        ImmutableList.Builder<String> args = new ImmutableList.Builder<String>();

        args.addAll(options);

        // Output
        if (pass == 1) {
            args.add("-an");
            args.add(DEVNULL);
        } else if (!parent.isPipe() && filename != null) {
            args.add(filename);
        } else if(!parent.isPipe() && (stream != null || listener != null)){
            args.add("-");
            parent.setPipe(true);
        } else if (uri != null) {
            args.add(uri.toString());
        } else {
            assert (false);
        }

        return args.build();
    }

}
