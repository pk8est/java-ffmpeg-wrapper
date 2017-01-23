package com.huya.v.controller;

import com.huya.v.transcode.FFmpegExecutor;
import com.huya.v.transcode.builder.FFmpegBuilder;
import com.huya.v.transcode.job.FFmpegJob;
import com.huya.v.transcode.progress.Progress;
import com.huya.v.transcode.progress.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.OutputStream;


@Controller
@RequestMapping("/video")
public class VideoController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(VideoController.class);


    @RequestMapping("/**/*.mp4")
    public String index(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Accept-Ranges","bytes");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "video/mpeg4");
        response.setHeader("Connection", "keep-alive");
        //response.setHeader("Content-Disposition", "attachment;fileName=input.mp4");

        try {

            String input = "C:\\Users\\Administrator\\Desktop\\video2\\encode\\input.mp4";
            FFmpegExecutor executor = new FFmpegExecutor();
            final FFmpegBuilder builder = new FFmpegBuilder();
            builder.addInput(input);
            builder.addOption("-f", "mpegts");

            OutputStream os = response.getOutputStream();
            FFmpegJob job = executor.createJob(builder);
            builder.addOutput(os);
            builder.addProgressListener(new ProgressListener() {
                @Override
                public void progress(Progress progress, String line) {
                    //System.out.println(line);
                }
            });
            job.run();
            if(!job.isSuccess()){
                LOG.error(job.getErrorMessage());
            }
            // 这里主要关闭。
            //os.close();

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        return null;
    }


}
