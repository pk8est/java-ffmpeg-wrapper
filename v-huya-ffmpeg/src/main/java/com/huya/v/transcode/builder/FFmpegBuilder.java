package com.huya.v.transcode.builder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.huya.v.transcode.ffprobe.FFmpegProbeResult;
import com.huya.v.transcode.progress.ProgressDataListener;

import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Administrator on 2016/12/23.
 */
public class FFmpegBuilder extends CommonBuilder {

    public enum Strict {
        VERY, // strictly conform to a older more strict version of the specifications or reference
        // software
        STRICT, // strictly conform to all the things in the specificiations no matter what consequences
        NORMAL, // normal
        UNOFFICAL, // allow unofficial extensions
        EXPERIMENTAL;

        // ffmpeg command line requires these options in lower case
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
    public String path = "ffmpeg";

    /**
     * Log level options: https://ffmpeg.org/ffmpeg.html#Generic-options
     */
    public enum Verbosity {
        QUIET, PANIC, FATAL, ERROR, WARNING, INFO, VERBOSE, DEBUG;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    boolean override = true;
    int pass = 0;
    String pass_directory = "";
    String pass_prefix;
    Verbosity verbosity = Verbosity.INFO;


    // Input settings
    boolean stdout = true;
    boolean pipe = false;
    final List<String> inputs = new ArrayList<>();


    public FFmpegBuilder(){}

    public void setPipe(boolean pipe){
        this.pipe = pipe;
    }

    public boolean isPipe(){
        return pipe;
    }

    public FFmpegBuilder setOverride(boolean override) {
        this.override = override;
        return this;
    }

    public boolean getOverride() {return override;}

    public FFmpegBuilder setVerbosity(Verbosity verbosity) {
        checkNotNull(verbosity);
        this.verbosity = verbosity;
        return this;
    }

    public FFmpegBuilder setPass(int pass) {
        this.pass = pass;
        return this;
    }

    public FFmpegBuilder setPassDirectory(String directory) {
        this.pass_directory = checkNotNull(directory);
        return this;
    }

    public FFmpegBuilder setPassPrefix(String prefix) {
        this.pass_prefix = checkNotNull(prefix);
        return this;
    }

    public boolean isStdout(){
        return stdout;
    }

    public List<String> getInputs(){
        return inputs;
    }


    public FFmpegBuilder setInput(FFmpegProbeResult result) {
        clearInputs();
        return addInput(result);
    }

    public FFmpegBuilder setInput(String filename) {
        clearInputs();
        return addInput(filename);
    }

    public FFmpegBuilder addInput(FFmpegProbeResult result) {
        checkNotNull(result);
        String filename = checkNotNull(result.format).filename;
        inputProbes.put(filename, result);
        return addInput(filename);
    }

    public FFmpegBuilder addInput(String filename) {
        checkNotNull(filename);
        inputs.add(filename);
        return this;
    }

    public FFmpegBuilder addOutput(FFmpegOutputBuilder output) {
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(String filename) {
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, filename);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(String filename, String[] options) {
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, filename, options);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(String filename, List<String> options) {
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, filename, options);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(URI uri) {
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, uri);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(URI uri, String[] options) {
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, uri, options);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(URI uri, List<String> options) {
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, uri, options);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(OutputStream stream) {
        this.stdout = false;
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, stream);
        outputStreams.add(stream);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(OutputStream stream, String[] options) {
        this.stdout = false;
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, stream, options);
        outputStreams.add(stream);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(OutputStream stream, List<String> options) {
        this.stdout = false;
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, stream, options);
        outputStreams.add(stream);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(ProgressDataListener listener) {
        this.stdout = false;
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, listener);
        progressDataListeners.add(listener);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(ProgressDataListener listener, String[] options) {
        this.stdout = false;
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, listener, options);
        progressDataListeners.add(listener);
        outputs.add(output);
        return this;
    }

    public FFmpegBuilder addOutput(ProgressDataListener listener, List<String> options) {
        this.stdout = false;
        FFmpegOutputBuilder output = new FFmpegOutputBuilder(this, listener, options);
        progressDataListeners.add(listener);
        outputs.add(output);
        return this;
    }

    protected void clearInputs() {
        inputs.clear();
        inputProbes.clear();
    }

    public List<String> build() {
        ImmutableList.Builder<String> args = new ImmutableList.Builder<String>();

        Preconditions.checkArgument(!inputs.isEmpty(), "At least one input must be specified");
        Preconditions.checkArgument(!outputs.isEmpty(), "At least one output must be specified");

        args.add(override ? "-y" : "-n");
        args.add("-v", this.verbosity.toString());

        if (progress != null) {
            args.add("-progress", progress.toString());
        }

        for (String input : inputs) {
            args.add("-i", input);
        }

        args.addAll(options);

        if (pass > 0) {
            args.add("-pass", Integer.toString(pass));

            if (pass_prefix != null) {
                args.add("-passlogfile", pass_directory + pass_prefix);
            }
        }


        for (FFmpegOutputBuilder output : this.outputs) {
            args.addAll(output.build(this, pass));
        }

        return args.build();
    }
}
