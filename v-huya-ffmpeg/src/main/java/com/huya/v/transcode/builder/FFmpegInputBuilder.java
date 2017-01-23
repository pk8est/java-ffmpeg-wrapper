package com.huya.v.transcode.builder;

import com.google.common.collect.ImmutableList;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Administrator on 2016/12/23.
 */
public class FFmpegInputBuilder extends CommonBuilder{

    final FFmpegBuilder parent;
    public String filename;
    public URI uri;
    public InputStream stream;
    public boolean pipe = false;
    private InputStream inputStream;

    protected FFmpegInputBuilder(FFmpegBuilder parent, String filename, List<String> options) {
        this.parent = checkNotNull(parent);
        this.filename = filename;
        this.options = options;
    }

    protected FFmpegInputBuilder(FFmpegBuilder parent, InputStream stream, List<String> options) {
        this.parent = checkNotNull(parent);
        this.stream = stream;
        this.options = options;
    }

    public FFmpegInputBuilder(String filename){
        this(new FFmpegBuilder(), filename, new ArrayList());
    }

    public  FFmpegInputBuilder(InputStream stream){
        this(new FFmpegBuilder(), stream, new ArrayList());
    }

    public FFmpegInputBuilder(FFmpegBuilder builder, String filename) {
        this(builder, filename, new ArrayList());
    }

    public FFmpegInputBuilder(FFmpegBuilder builder, InputStream stream) {
        this(builder, stream, new ArrayList());
    }

    public void setPipe(boolean pipe){
        this.pipe = pipe;
    }

    public boolean isPipe(){
        return pipe;
    }

    public List<String> build(FFmpegBuilder parent) {
        checkNotNull(parent);

        ImmutableList.Builder<String> args = new ImmutableList.Builder<String>();

        args.addAll(options);

        if ( filename != null) {
            args.add("-i", filename);
        } else if (!parent.isPipe() && (stream != null)) {
            args.add("-i", "-");
            this.setPipe(true);
        } else if (uri != null) {
            args.add("-i", uri.toString());
        } else {
            assert (false);
        }

        return args.build();
    }

    public InputStream getInputStream() {
        return stream;
    }
}
