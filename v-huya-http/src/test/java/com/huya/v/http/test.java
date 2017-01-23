package com.huya.v.http;

import com.huya.v.http.routes.Controller;
import com.huya.v.http.routes.Get;
import com.huya.v.http.util.ResponseCode;
import com.huya.v.transcode.FFmpegExecutor;
import com.huya.v.transcode.builder.FFmpegBuilder;
import com.huya.v.transcode.job.FFmpegJob;
import com.huya.v.transcode.progress.Progress;
import com.huya.v.transcode.progress.ProgressDataListener;
import com.huya.v.transcode.progress.ProgressListener;

import java.io.IOException;

/**
 * Created by Administrator on 2017/1/4.
 */
public class test {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.bind(8081);
        server.accept(new Controller() {
            @Get("/user/{name}")
            public void index(String username, HttpReader reader, final HttpWriter writer) throws IOException {

                writer.writeResponseHeader(HttpServer.VERSION, ResponseCode.OK);
                writer.writeHeader("Accept-Ranges", "bytes");
                writer.writeHeader("Access-Control-Allow-Origin", "*");
                writer.writeHeader("Content-Type", "video/mpeg4");
                writer.writeHeader("Connection", "keep-alive");
                writer.endHeader();


                String input_1 = "C:\\Users\\Administrator\\Desktop\\video2\\encode\\input.mp4";
                String output_1 = "C:\\Users\\Administrator\\Desktop\\video2\\encode\\output1.mp4";
                String output_2 = "C:\\Users\\Administrator\\Desktop\\video2\\encode\\output2.mp4";
                FFmpegExecutor executor = new FFmpegExecutor();
                FFmpegBuilder builder = new FFmpegBuilder();
                builder.addInput(input_1);
                builder.addOption("-f", "mpegts");

                builder.addOutput(new ProgressDataListener() {
                    @Override
                    public void getData(byte[] buf, int len) throws IOException {
                        try {
                            //writer.write(buf, 0, len));
                            writer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                });
                FFmpegJob job = executor.createJob(builder);
                builder.addProgressListener(new ProgressListener() {
                    @Override
                    public void progress(Progress progress, String line) {
                        System.out.println(line);
                    }
                });
                job.run();
                writer.close();
                /*return new HttpHandler(){
                    @Override
                    public void accept(HttpReader reader, HttpWriter writer) throws IOException {
                        writer.writeResponseHeader(HttpServer.VERSION, ResponseCode.OK);
                        writer.writeHeader("Accept-Ranges", "bytes");
                        writer.writeHeader("Access-Control-Allow-Origin", "*");
                        writer.writeHeader("Content-Type", "video/mpeg4");
                        writer.writeHeader("Connection", "keep-alive");
                        writer.endHeader();
                        writer.flush();
                        writer.close();
                    }
                };*/
            }
        });
        server.listen();
    }
}
