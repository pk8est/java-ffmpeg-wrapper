package com.huya.v.transcode.execute;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2016/12/26.
 */
public class RunProcessor implements Processor{
    final static Logger LOG = LoggerFactory.getLogger(RunProcessor.class);

    @Override
    public Process run(List<String> args) throws IOException {
        Preconditions.checkNotNull(args, "Arguments must not be null");
        Preconditions.checkArgument(!args.isEmpty(), "No arguments specified");

        if (LOG.isInfoEnabled()) {
            LOG.info("{}", Joiner.on(" ").join(args));
        }

        ProcessBuilder builder = new ProcessBuilder(args);
        //builder.redirectErrorStream(true);
        return builder.start();
    }
}
