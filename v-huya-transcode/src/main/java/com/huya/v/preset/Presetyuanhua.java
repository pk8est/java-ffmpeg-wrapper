package com.huya.v.preset;

import com.huya.v.transcode.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/1/10.
 */
@Service
public class Presetyuanhua implements Preset{

    @Override
    public String getBandwidth() {
        return "21705000";
    }

    @Override
    public String getResolution() {
        return "1920x1080";
    }

    @Override
    public FFmpegBuilder build() {
        return null;
    }

}
