package com.huya.v.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class WebExceptionHandler implements HandlerExceptionResolver {

    private static final Logger LOG = LoggerFactory.getLogger(WebExceptionHandler.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {
        StringBuffer url = request.getRequestURL();
        StringBuffer paramValue = new StringBuffer();
        Enumeration e = (Enumeration) request.getParameterNames();
        while (e.hasMoreElements()) {
            String parName = (String) e.nextElement();
            String value = request.getParameter(parName);
            paramValue.append(String.format("[%s:%s]", parName, value));
        }
        LOG.error(String.format("system error url:%s,params:%s", url, paramValue), ex);
        if (ex instanceof BasicBusinessException) {
            Map<String, String> model = new HashMap<String, String>();
            model.put("message", ex.getMessage());
            return new ModelAndView("error/business", model);
        }
        return new ModelAndView("error/error");
    }

}
