package com.enjoy.james.service;

import com.enjoy.james.annotation.EnjoyService;

/**
 * @author zxf
 * @date 2018/8/6 21:26
 */
@EnjoyService("JamesServiceImpl")
public class JamesServiceImpl implements JamesService{
    @Override
    public String query(String name, String age) {
        return "name="+name+", age="+age;
    }
}
