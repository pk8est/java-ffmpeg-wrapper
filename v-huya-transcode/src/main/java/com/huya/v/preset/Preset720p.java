package com.huya.v.preset;

import com.huya.v.transcode.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/1/10.
 */
@Service
public class Preset720p implements Preset{

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
        builder.addOption("-acodec", "copy");
        builder.addOption("-b:a", "64k");
        builder.addOption("-f", "mpegts");
        builder.addOption("-vf", "scale=-2:720");
        builder.addOption("-threads", "0");
        //builder.addOption("-subq", "5");
        //builder.addOption("-trellis", "1");
        //builder.addOption("-refs", "1");
        //builder.addOption("-coder", "0");
        //builder.addOption("-me_range", "16");
        //builder.addOption("-keyint_min", "25");
        //builder.addOption("-g", "30");
        //builder.addOption("-maxrate", "800k");
        //builder.addOption("-minrate", "800k");
        //builder.addOption("-sc_threshold", "40");
        //builder.addOption("-i_qfactor", "0.71");
        return builder;
    }

}
