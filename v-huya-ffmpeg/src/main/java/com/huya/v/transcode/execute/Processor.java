package com.huya.v.transcode.execute;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2016/12/26.
 */
public interface Processor {

        Process run(List<String> args) throws IOException;

}
