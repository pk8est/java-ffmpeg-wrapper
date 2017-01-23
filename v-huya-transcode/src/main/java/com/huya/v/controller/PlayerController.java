package com.huya.v.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/player")
public class PlayerController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerController.class);


    @RequestMapping("/index")
    public String index(HttpServletRequest request, final HttpServletResponse response) {
        return "player/play";
    }


}
