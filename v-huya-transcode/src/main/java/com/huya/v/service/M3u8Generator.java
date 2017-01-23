package com.huya.v.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/10.
 */
public interface M3u8Generator {

    public void createM3u8File(File inputFile, File outputFile, String preset) throws FileNotFoundException, IOException;

    public void createM3u8File(BufferedReader br, File outputFile, String preset, String host) throws FileNotFoundException, IOException;

    public void createM3u8ListFile(String output, List<Map<String, String>> list) throws FileNotFoundException, IOException;

    public void createM3u8ListFile(File outputFile, List<Map<String, String>> list) throws FileNotFoundException, IOException;
}
