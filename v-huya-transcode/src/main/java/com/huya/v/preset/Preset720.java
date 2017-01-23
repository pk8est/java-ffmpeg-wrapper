package com.huya.v.preset;

import com.huya.v.transcode.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/1/10.
 */
@Service
public class Preset720 implements Preset{

    @Override
    public String getBandwidth() {
        return "9600000";
    }

    @Override
    public String getResolution() {
        return "1280x720";
    }

    @Override
    public FFmpegBuilder build() {
        FFmpegBuilder builder = new FFmpegBuilder();
        builder.addOption("-loglevel", "error");
        builder.addOption("-nostats");
        builder.addOption("-copyts");
        builder.addOption("-vcodec", "libx264"); //h264_qsv,hevc_qsv,libx264
        builder.addOption("-b:v", "800k");
        builder.addOption("-acodec", "copy");  //libmp3lame
        builder.addOption("-b:a", "64k");
        builder.addOption("-ar", "44100");
        builder.addOption("-f", "mpegts");
        builder.addOption("-r", "25");
        builder.addOption("-pix_fmt", "yuv420p");
        builder.addOption("-profile:v", "high");
        builder.addOption("-level", "4.1");
        builder.addOption("-f", "mpegts");
        builder.addOption("-vf", "scale=-2:720");
        builder.addOption("-subq", "5");
        builder.addOption("-trellis", "1");
        builder.addOption("-refs", "1");
        builder.addOption("-coder", "0");
        builder.addOption("-me_range", "16");
        builder.addOption("-keyint_min", "25");
        builder.addOption("-g", "30");
        builder.addOption("-threads", "1");
        builder.addOption("-sc_threshold", "40");
        builder.addOption("-i_qfactor", "0.71");
        builder.addOption("-flags", "+loop");
        builder.addOption("-cmp", "+chroma");
        builder.addOption("-partitions", "+parti4x4+partp8x8+partb8x8");
        builder.addOption("-rc_eq", "'blurCplx^(1-qComp)'");
        /*
        //这几个选择项开启了画面有时会变得非常差
        builder.addOption("-qcomp", "0.6");
        builder.addOption("-qmin", "10");
        builder.addOption("-qmin", "51");
        builder.addOption("-qdiff", "4");
        */
        builder.addOption("-async", "2");
        builder.addOption("-x264opts", "colorprim=bt709:transfer=bt709:colormatrix=bt709:deblock=-1,1:open_gop=1");
        builder.addOption("-tune", "ssim");
        //builder.addOption("preset", "slow");
        return builder;
    }

}
