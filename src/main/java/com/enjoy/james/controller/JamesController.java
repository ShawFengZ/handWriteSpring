package com.enjoy.james.controller;

import com.enjoy.james.annotation.EnjoyAutowired;
import com.enjoy.james.annotation.EnjoyController;
import com.enjoy.james.annotation.EnjoyRequestMapping;
import com.enjoy.james.annotation.EnjoyRequestParam;
import com.enjoy.james.service.JamesService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zxf
 * @date 2018/8/6 21:24
 */
@EnjoyController
@EnjoyRequestMapping("/james")
public class JamesController {
    @EnjoyAutowired("JamesServiceImpl")
    private JamesService jamesService;

    @EnjoyRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @EnjoyRequestParam("name") String name, @EnjoyRequestParam("age")String age){
        PrintWriter pw = null;
        try {
            pw = response.getWriter();
            String result = jamesService.query(name, age);
            pw.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
