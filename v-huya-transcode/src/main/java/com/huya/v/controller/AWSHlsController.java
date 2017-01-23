package com.huya.v.controller;

import com.github.kevinsawicki.http.HttpRequest;
import com.huya.v.preset.Preset;
import com.huya.v.service.M3u8Generator;
import com.huya.v.transcode.FFmpegExecutor;
import com.huya.v.transcode.builder.FFmpegBuilder;
import com.huya.v.transcode.job.FFmpegJob;
import com.huya.v.transcode.progress.Progress;
import com.huya.v.transcode.progress.ProgressListener;
import com.huya.v.util.EncodeUtil;
import com.huya.v.util.HttpResponsePipe;
import com.huya.v.util.MD5;
import com.huya.v.util.PropertyFactory.PropertiesHolder;
import com.sun.jndi.toolkit.url.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;


@Controller
public class AWSHlsController extends BaseController {

    @Value("${HLS_M3U8_DIR}")
    private String HLS_M3U8_DIR;

    @Value("${HLS_M3U8_RAW_DIR}")
    private String HLS_M3U8_RAW_DIR;

    @Value("${HLS_TS_DIR}")
    private String HLS_TS_DIR;

    @Value("${HLS_TS_RAW_DIR}")
    private String HLS_TS_RAW_DIR;

    @Value("${HLS_DEFAULT_PRESET}")
    private String HLS_DEFAULT_PRESET;

    @Value("${HLS_HOST}")
    private String HLS_HOST;

    private static final Logger LOG = LoggerFactory.getLogger(AWSHlsController.class);
    private static HttpResponsePipe httpResponsePipe = new HttpResponsePipe();

    @Resource
    private M3u8Generator m3u8Generator;

    @RequestMapping(value = "/**/*.m3u8", method = RequestMethod.GET)
    public void getM3u8(@RequestParam(defaultValue = "720p") String rate,
                      HttpServletRequest request,
                      HttpServletResponse response
    ) throws IOException {
        String filePath = getFilePath(request);
        String remoteFile = getRemoteFileAddr(filePath);
        String localFile = getLocalFileAddr(filePath, rate);

        try {
            File file = new File(localFile);
            if (file.exists()) {
                setM3u8Header(response);
                setContentLength(file, response);
                httpResponsePipe.copy(file, response);
                return;
            }
            if (!EncodeUtil.isDefaultPreSets(rate, HLS_DEFAULT_PRESET)) {
                response.sendError(404);
                return;
            }
            HttpRequest req = HttpRequest.get(remoteFile);
            if (!req.ok()) {
                response.sendError(404);
                return;
            }
            m3u8Generator.createM3u8File(req.bufferedReader(), file, rate, getHost(request));
            setM3u8Header(response);
            setContentLength(file, response);
            httpResponsePipe.copy(file, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(404);
        }
    }

    @RequestMapping(value = "/**/*.ts", method = RequestMethod.GET)
    public void getTs(@RequestParam(defaultValue = "720p") String rate,
                      HttpServletRequest request,
                      HttpServletResponse response
    ) throws IOException {
        String remoteFile = getRemoteFileAddr(getFilePath(request));
        try {
            if (!EncodeUtil.isDefaultPreSets(rate, HLS_DEFAULT_PRESET)) {
                response.sendError(404);
                return;
            }
            Preset preset = EncodeUtil.getPreSet(rate);
            if(preset == null){
                response.sendError(404);
                return;
            }
            HttpRequest req = HttpRequest.get(remoteFile);
            if (!req.ok()) {
                response.sendError(404);
                return;
            }

            setTsHeader(response);
            FFmpegBuilder builder = preset.build();
            if(builder == null){
                setTsHeader(response);
                req.receive(response.getOutputStream());
                return;
            }
            builder.addInput(remoteFile);
            System.out.println(remoteFile);
            builder.addOutput(response.getOutputStream());
            builder.addProgressListener(new ProgressListener() {
                @Override
                public void progress(Progress progress, String line) {
                    System.err.println(line);
                }
            });

            FFmpegExecutor executor = new FFmpegExecutor();
            FFmpegJob job = executor.createJob(builder);
            job.run();
            LOG.info(job.getCommand().toString());
            if(!job.isSuccess()){
                System.err.println(job.getErrorMessage());
                LOG.error(job.getErrorMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(404);
        }
    }


    protected void setHeader(HttpServletResponse response) {
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
    }

    protected void setM3u8Header(HttpServletResponse response) {
        setHeader(response);
        response.setContentType("application/x-mpegURL");
    }

    protected void setTsHeader(HttpServletResponse response) {
        setHeader(response);
        response.setContentType("video/mp2t");
    }

    protected void setContentLength(File file, HttpServletResponse response) {
        response.setContentLength((int) file.length());
    }

    protected String getRemoteFileAddr(String filePath) {
        return "http://" + filePath;
    }

    protected String getLocalFileAddr(String filePath, String rate) {
        return HLS_M3U8_DIR + new MD5().get(filePath) + "-" + rate + ".m3u8";
    }

    protected String getFilePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.substring(path.indexOf("/") == 0 ? 1 : 0);
    }

    protected String getHost(HttpServletRequest request) throws MalformedURLException {
        Uri uri = new Uri(request.getRequestURL().toString());
        int port = uri.getPort();
        return PropertiesHolder.get("HLS_HOST", uri.getHost() + (port == -1 ? "" : ":"+port));
    }
}