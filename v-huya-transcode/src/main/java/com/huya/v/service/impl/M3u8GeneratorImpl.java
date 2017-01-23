package com.huya.v.service.impl;

import com.huya.v.service.M3u8Generator;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/10.
 */
@Service
public class M3u8GeneratorImpl implements M3u8Generator {

    @Override
    public void createM3u8File(File inputFile, File outputFile, String preset) throws FileNotFoundException, IOException {
        BufferedReader bf = new BufferedReader(new FileReader(inputFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line = null;
        while ((line=bf.readLine())!=null){
            if(!line.startsWith("#")){
                line = preset + "/" + line;
                /*if(line.indexOf("?") == -1){
                    line = line + "?preset=" + preset;
                }else{
                    line = line + "&preset=" + preset;
                }*/
            }
            bw.write(line);
            bw.newLine();
        }
        bf.close();
        bw.close();
    }

    public void createM3u8File(BufferedReader bf, File outputFile, String preset, String host) throws FileNotFoundException, IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line = null;
        while ((line=bf.readLine())!=null){
            if(!line.startsWith("#") && !"".equals(line)){
                if(line.startsWith("http://")){
                    line = line.replace("http://", host);
                }
                if(line.indexOf("?") == -1){
                    line = line + "?rate=" + preset;
                }else{
                    line = line + "&rate=" + preset;
                }
            }
            bw.write(line);
            bw.newLine();
        }
        bw.close();
    }

    @Override
    public void createM3u8ListFile(String output, List<Map<String, String>> list) throws FileNotFoundException, IOException {
        createM3u8ListFile(new File(output), list);
    }

    @Override
    public void createM3u8ListFile(File outputFile, List<Map<String, String>> list) throws FileNotFoundException, IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        bw.write("#EXTM3U");
        bw.newLine();
        for(Map<String, String> map: list){
            bw.write("#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=" + map.get("bandwidth") + ",RESOLUTION=" + map.get("resolution"));
            bw.newLine();
            bw.write(map.get("filename"));
            bw.newLine();
        }
        bw.close();
    }
}
