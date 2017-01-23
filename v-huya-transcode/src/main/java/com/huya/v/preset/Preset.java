package com.huya.v.preset;

import com.huya.v.transcode.builder.FFmpegBuilder;

/**
 * Created by Administrator on 2017/1/10.
 */
public interface Preset {

    public String getBandwidth();

    public String getResolution();

    public FFmpegBuilder build();

}
