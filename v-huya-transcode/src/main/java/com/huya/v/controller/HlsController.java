package com.huya.v.controller;

import com.huya.v.preset.Preset;
import com.huya.v.service.M3u8Generator;
import com.huya.v.transcode.FFmpegExecutor;
import com.huya.v.transcode.builder.FFmpegBuilder;
import com.huya.v.transcode.job.FFmpegJob;
import com.huya.v.transcode.progress.Progress;
import com.huya.v.transcode.progress.ProgressListener;
import com.huya.v.util.EncodeUtil;
import com.huya.v.util.HttpResponsePipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/hls")
public class HlsController extends BaseController {

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

    private static final Logger LOG = LoggerFactory.getLogger(HlsController.class);
    private static HttpResponsePipe httpResponsePipe = new HttpResponsePipe();

    @Resource
    private M3u8Generator m3u8Generator;

    @RequestMapping(value="/**/{filename}/{presetName}.m3u8", method= RequestMethod.GET)
    public void index(@PathVariable String filename,
                      @PathVariable String presetName,
                      HttpServletRequest request,
                      HttpServletResponse response
    ) throws IOException {
        StringBuilder path = new StringBuilder();
        String[] pathInfo = request.getRequestURI().split("/");
        for (int i=pathInfo.length-3; i>1; i--){
            path.insert(0, pathInfo[i] + File.separator);
        }

        String m3u8Path = HLS_M3U8_DIR + path.toString() + filename + "-" + presetName + ".m3u8";
        String rawM3u8Path = HLS_M3U8_RAW_DIR + path.toString() + filename + ".m3u8";
        //System.out.println(m3u8Path);
        //System.out.println(rawM3u8Path);
        try {
            File file=new File(m3u8Path);
            if(file.exists()){
                setM3u8Header(response);
                setContentLength(file, response);
                httpResponsePipe.copy(file, response);
                return;
            }
            if(!EncodeUtil.isDefaultPreSets(presetName, HLS_DEFAULT_PRESET)){
                response.sendError(404);
                return;
            }

            File rawFile = new File(rawM3u8Path);
            if(!rawFile.exists()){
                response.sendError(404);
                return;
            }

            m3u8Generator.createM3u8File(rawFile, file, presetName);
            setM3u8Header(response);
            setContentLength(file, response);
            httpResponsePipe.copy(file, response);
        }catch (Exception e){
            e.printStackTrace();
            response.sendError(404);
        }

    }

    @ResponseBody
    @RequestMapping(value="/**/{filename}.m3u8.json", method= RequestMethod.GET)
    public Map list(@PathVariable String filename,
                       HttpServletRequest request,
                       HttpServletResponse response
    ) throws IOException {
        Map result = new HashMap();
        result.put("code", -1);
        result.put("msg", "error");
        StringBuilder path = new StringBuilder();
        String[] pathInfo = request.getRequestURI().split("/");
        for (int i=pathInfo.length-2; i>1; i--){
            path.insert(0, pathInfo[i] + File.separator);
        }
        String basePath = path.toString();
        String rawM3u8Path = HLS_M3U8_DIR + basePath + filename + ".m3u8";
        String host = HLS_HOST + "hls/" + basePath.replace("\\", "/");
        try{
            File rawFile = new File(rawM3u8Path);
            if(!rawFile.exists()){
                return result;
            }
            Map<String, String> m3u8List = new HashMap<>();
            Map<String, Preset> presetMap = EncodeUtil.getPreSets(HLS_DEFAULT_PRESET);
            List<Map<String, String>> m3u8EncdoeList = new ArrayList<>();

            for (Map.Entry<String, Preset> entry : presetMap.entrySet()) {
                Map<String, String> m3u8EncodeConf = new HashMap();
                String presetName = entry.getKey();
                Preset preset = entry.getValue();
                String m3u8Path = HLS_M3U8_DIR + basePath + filename + "-" + presetName + ".m3u8";
                m3u8List.put(presetName, host + filename + "/" + presetName + ".m3u8");
                File m3u8File = new File(m3u8Path);
                m3u8Generator.createM3u8File(rawFile, m3u8File, presetName);
                m3u8EncodeConf.put("preset", presetName);
                m3u8EncodeConf.put("bandwidth", preset.getBandwidth());
                m3u8EncodeConf.put("resolution", preset.getResolution());
                m3u8EncodeConf.put("filename", presetName + ".m3u8");
                m3u8EncdoeList.add(m3u8EncodeConf);
            }
            String index = host + filename + "/list.m3u8";
            String m3u8ListName = HLS_M3U8_DIR + basePath + filename + "-list.m3u8";
            m3u8Generator.createM3u8ListFile(m3u8ListName, m3u8EncdoeList);
            result.put("code", 1);
            result.put("msg", "success");
            result.put("index", index);
            result.put("list", m3u8List);
            return result;

        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value="/**/{presetName}/{filename}.ts", method= RequestMethod.GET)
    public void encode(@PathVariable String presetName,
                       @PathVariable String filename,
                       HttpServletRequest request,
                       HttpServletResponse response
    ) throws IOException {
        StringBuilder path = new StringBuilder();
        String[] pathInfo = request.getRequestURI().split("/");
        int len = pathInfo.length;
        for (int i=len-4; i>1; i--){
            path.insert(0, pathInfo[i] + File.separator);
        }

        String tsPath = HLS_TS_DIR + path.toString() + filename + "-" + presetName + ".ts";
        String rawTsPath = HLS_TS_RAW_DIR + path.toString() + filename + ".ts";

        try {
            if(!EncodeUtil.isDefaultPreSets(presetName, HLS_DEFAULT_PRESET)){
                response.sendError(404);
                return;
            }
            Preset preset = EncodeUtil.getPreSet(presetName);
            if(preset == null){
                response.sendError(404);
                return;
            }

            File file=new File(tsPath);
            if(file.exists()){
                setTsHeader(response);
                httpResponsePipe.copy(file, response);
                return;
            }
            File rawFile = new File(rawTsPath);
            if(!rawFile.exists()){
                response.sendError(404);
                return;
            }
            setTsHeader(response);
            FFmpegBuilder builder = preset.build();
            if(builder == null){
                setTsHeader(response);
                httpResponsePipe.copy(rawFile, response);
                return;
            }
            builder.addInput(rawTsPath);
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
            if(!job.isSuccess()){
                System.err.println(job.getErrorMessage());
                LOG.error(job.getErrorMessage());
            }
        }catch (Exception e){
            e.printStackTrace();
            response.sendError(404);
        }

    }


    protected static void setHeader(HttpServletResponse response){
        response.setHeader("Accept-Ranges","bytes");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
    }

    protected static void setM3u8Header(HttpServletResponse response){
        setHeader(response);
        response.setContentType("application/x-mpegURL");
    }

    protected static void setTsHeader(HttpServletResponse response){
        setHeader(response);
        response.setContentType("video/mp2t");
    }

    protected static void setContentLength(File file,HttpServletResponse response){
        response.setContentLength((int)file.length());
    }


}
