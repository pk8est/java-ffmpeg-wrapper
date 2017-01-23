package com.huya.v.util;

import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by Administrator on 2017/1/10.
 */
public class HttpResponsePipe {

    public static void copy(String file, HttpServletResponse response) throws IOException {
        copy(new File(file), response);
    }

    public static void copy(File file, HttpServletResponse response) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        FileCopyUtils.copy(inputStream, response.getOutputStream());
    }

}
