package com.huya.v.transcode.progress;

import java.io.IOException;

/**
 * Created by Administrator on 2016/12/24.
 */
public interface ProgressDataListener {
    void getData(byte[] buf, int len) throws IOException;
}

