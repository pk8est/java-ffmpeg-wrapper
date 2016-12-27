package com.huya.v.transcode;

import com.huya.v.transcode.builder.FFmpegBuilder;
import com.huya.v.transcode.ffprobe.FFmpegFormat;
import com.huya.v.transcode.ffprobe.FFmpegProbeResult;
import com.huya.v.transcode.ffprobe.FFmpegStream;
import com.huya.v.transcode.job.FFmpegJob;
import com.huya.v.transcode.progress.Progress;
import com.huya.v.transcode.progress.ProgressDataListener;
import com.huya.v.transcode.progress.ProgressListener;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/12/26.
 */
public class test {

    static String input_1 = "C:\\Users\\Administrator\\Desktop\\video2\\encode\\input.mp4";
    static String output_1 = "C:\\Users\\Administrator\\Desktop\\video2\\encode\\output1.mp4";
    static String output_2 = "C:\\Users\\Administrator\\Desktop\\video2\\encode\\output2.mp4";

    public static void main(String[] args) throws Exception{
        twoPassFFmpegJob();
        //singlePassFFmpegJob();
        //defaultTranscode();
        //ffprobe();

    }

    public static void twoPassFFmpegJob() throws Exception{
        FFmpegExecutor executor = new FFmpegExecutor();
        FFmpegBuilder builder = new FFmpegBuilder();
        builder.addInput(input_1);
        builder.addOutput(output_1, new String[]{"-f", "mpegts", "-bsf:v", "h264_mp4toannexb", "-b:v", "2000k"});
        builder.addOutput(output_2, new String[]{"-f", "mp4", "-b:v", "200k"});
        FFmpegJob job = executor.createTwoPassJob(builder);
        FFmpegProbeResult result = job.getFFprobeResult();
        System.out.println(result);
        //builder.addOption("-b:v", String.valueOf(result.format.bit_rate));

        builder.addProgressListener(new ProgressListener() {
            @Override
            public void progress(Progress progress, String line) {
                System.out.println(line);
            }
        });
        job.run();
    }

    public static void singlePassFFmpegJob() throws Exception{
        FFmpegExecutor executor = new FFmpegExecutor();
        FFmpegBuilder builder = new FFmpegBuilder();
        builder.addInput(input_1);
        builder.addOutput(output_1, new String[]{"-f", "mpegts", "-bsf:v", "h264_mp4toannexb", "-b:v", "2000k"});
        builder.addOutput(output_2, new String[]{"-f", "mp4", "-b:v", "200k"});
        FFmpegJob job = executor.createJob(builder);
        builder.addProgressListener(new ProgressListener() {
            @Override
            public void progress(Progress progress, String line) {
                System.out.println(line);
            }
        });
        job.run();
    }

    public static void defaultTranscode() throws Exception{
        long starTime=System.currentTimeMillis();
        FFmpeg ffmpeg = new FFmpeg();
        FFmpegBuilder builder = new FFmpegBuilder();
        builder.addInput(input_1);
        builder.addOutput(output_1, new String[]{"-f", "mpegts", "-bsf:v", "h264_mp4toannexb", "-b:v", "2000k"});
        builder.addOutput(output_2, new String[]{"-f", "mp4", "-b:v", "200k"});
        builder.addProgressListener(new ProgressListener() {
            @Override
            public void progress(Progress progress, String line) {
                System.out.println(line);
            }
        });
        ffmpeg.run(builder);
        ffmpeg.runTwoPass(builder);
        System.err.format("run time %dms", System.currentTimeMillis() - starTime);

    }
    public static void defaultTranscode2() throws Exception{

        FFmpeg ffmpeg = new FFmpeg();
        FFmpegBuilder builder = new FFmpegBuilder();
        builder.addInput(input_1);
        builder.addOption("-f", "mpegts");
        //builder.addOption("-c", "copy");
        builder.addOption("-bsf:v", "h264_mp4toannexb");
        OutputStream out = new FileOutputStream(output_1);
        builder.addOutput(out);

        final OutputStream out2 = new BufferedOutputStream(new FileOutputStream(output_2), 4096);
        builder.addOutput(new ProgressDataListener() {
            @Override
            public void getData(byte[] buf, int len) throws IOException {
                out2.write(buf, 0, len);
                if (len < 4096) {
                    out2.flush();
                }
            }

        });
        builder.addProgressListener(new ProgressListener() {
            @Override
            public void progress(Progress progress, String line) {
                //System.out.println(progress);
                System.out.println(line);
            }
        });
        try{
            ffmpeg.run(builder);
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    public static void ffprobe() throws Exception{
        FFprobe ffprobe = new FFprobe();
        FFmpegProbeResult probeResult = ffprobe.probe(input_1);
        System.err.println(probeResult);
        FFmpegFormat format = probeResult.getFormat();
        System.out.format("%nFile: '%s' ; Format: '%s' ; Duration: %.3fs",
                format.filename,
                format.format_long_name,
                format.duration
        );

        FFmpegStream stream = probeResult.getStreams().get(0);
        System.out.format("%nCodec: '%s' ; Width: %dpx ; Height: %dpx",
                stream.codec_long_name,
                stream.width,
                stream.height
        );
    }
}
