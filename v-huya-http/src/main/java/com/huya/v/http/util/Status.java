package com.huya.v.http.util;

import com.huya.v.http.HttpHandler;
import com.huya.v.http.HttpReader;
import com.huya.v.http.HttpServer;
import com.huya.v.http.HttpWriter;

import java.io.IOException;

/**
 * Status
 *
 * @author Ian Caffey
 * @since 1.0
 */
public class Status {
    private Status() {

    }

    public static HttpHandler ok() {
        return Status.code(ResponseCode.OK);
    }

    public static HttpHandler badRequest() {
        return Status.code(ResponseCode.BAD_REQUEST);
    }

    public static HttpHandler internalServerError() {
        return Status.code(ResponseCode.INTERNAL_SERVER_ERROR);
    }

    public static HttpHandler notFound() {
        return Status.code(ResponseCode.NOT_FOUND);
    }

    public static HttpHandler code(final ResponseCode code) {
        return new HttpHandler(){
            @Override
            public void accept(HttpReader reader, HttpWriter writer) throws IOException {
                writer.writeResponseHeader(HttpServer.VERSION, code);
                writer.endHeader();
                writer.flush();
                writer.close();
            }
        };
        /*
        return (reader, writer) -> {
            writer.writeResponseHeader(HttpServer.VERSION, code);
            writer.endHeader();
            writer.flush();
            writer.close();
        };
        */
    }
}
