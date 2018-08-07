package com.enjoy.james.servlet;

import com.enjoy.james.annotation.*;
import com.enjoy.james.controller.JamesController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zxf
 * @date 2018/8/6 21:33
 */
public class DispatcherServlet extends HttpServlet {

    List<String> classNames = new ArrayList<String>();
    Map<String, Object> beans = new HashMap<String, Object>();
    //存放url地址和要调用的方法
    Map<String, Object> handlerMap = new HashMap<String, Object>();
    //IOC是在项目启动的时候加载
    @Override
    public void init(ServletConfig config) throws ServletException {
        //先得包所有的class类进行收集
        //可以放到properties中
        //扫描到的class放到classNames list中
        scanPackage("com.enjoy");
        //IOC容器map.put(key, instance)，得到了beans
        doInstance();
        for (Map.Entry<String, Object> entry: beans.entrySet()){
            System.out.println(entry.getKey()+":"+entry.getValue());
        }

        //依赖注入
        doAutowired();

        //建立一个URL与method 的映射关系
        UrlMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    /**
     * 所有浏览器发送来的请求都是doPost来接受和处理的
     * */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();// james-mvc/james/query
        String context = req.getContextPath();//得到当前的工程名
        String path = uri.replace(context, "");//切除工程名，得到james/query去对应一个method

        Method method = (Method) handlerMap.get(path);
        //拿到控制层controller
        JamesController instance = (JamesController) beans.get("/"+path.split("/")[1]);
    }

    private void scanPackage(String basePackage){//com.enjoy---->com.enjoy
        //扫描编译好的类路径下的所有类
        URL url = this.getClass().getClassLoader().getResource("/"+basePackage.replace("\\.", "/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);
        String [] filesStr = file.list();
        for (String path: filesStr){
            File filePath = new File(fileStr+path);//com/enjoy/james/controller
            if (filePath.isDirectory()){
                scanPackage(basePackage+"."+path);
            }else {
                //得到了.class文件
                //用一个Map保存Class文件//com.enjoy.james.controller.JamesController.class
                classNames.add(basePackage+"."+filePath.getName());
            }
        }
    }

    public void doInstance(){
        if (classNames.size()==0){
            System.out.println("找不到class类");
            return;
        }
        //遍历所有刚被扫描到的
        for (String className: classNames){
            //得到一个不包括class的犬类路径名
            String cn = className.replace(".class", "");

            try {
                //处理声明在class类上的注解
                Class<?> clazz = Class.forName(cn);//加载类，用来实例化Bean或拿到当前类的所有属性
                if (clazz.isAnnotationPresent(EnjoyController.class)){//判断是否上面声明了controller注解
                    //JamesController
                    Object instance = clazz.newInstance();//创建对象，==new JamesController
                    //ioc map,map.put(key, instance), 拿到那个类上的@RequestMapping注解作为key
                    EnjoyRequestMapping requestMapping = (EnjoyRequestMapping) clazz.getAnnotation(EnjoyRequestMapping.class);
                    String key = requestMapping.value();
                    beans.put(key, instance);
                }else if (clazz.isAnnotationPresent(EnjoyService.class)){
                    //JamesService
                    Object instance = clazz.newInstance();//创建对象
                    EnjoyService service = (EnjoyService) clazz.getAnnotation(EnjoyService.class);
                    String key = service.value();
                    beans.put(key, instance);//service类型的bean放到beans
                }else{
                    continue;
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void doAutowired(){
        if (beans.entrySet().size()==0){
            System.out.println("没有一个类被实例化");
            return;
        }
        //beans遍历
        for(Map.Entry<String, Object> entry: beans.entrySet()){
            Object instance = entry.getValue();//获取bean实例
            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(EnjoyController.class)){
                //哪些属性
                Field[] fields = clazz.getFields();

                for (Field field: fields){
                    if (field.isAnnotationPresent(EnjoyAutowired.class)){
                        EnjoyAutowired auto = field.getAnnotation(EnjoyAutowired.class);
                        String key = auto.value();
                        //加大权限
                        field.setAccessible(true);
                        try {
                            field.set(instance, beans.get(key));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }else {
                        continue;
                    }
                }
            }else {
                continue;
            }
        }
    }

    public void UrlMapping(){
        //和控制层相关，因此拿到控制层的内容
        for (Map.Entry<String, Object> entry: beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(EnjoyController.class)){
                EnjoyRequestMapping requestMapping = clazz.getAnnotation(EnjoyRequestMapping.class);
                String classPath = requestMapping.value();
                //拿到类中的方法
                Method[] methods = clazz.getMethods();
                for (Method method: methods){
                    //判断方法上有没有注解
                    if (method.isAnnotationPresent(EnjoyRequestMapping.class)){
                        //拿到方法上的注解的内容
                        EnjoyRequestMapping url = clazz.getAnnotation(EnjoyRequestMapping.class);
                        String methodPath = url.value();
                        //将两个路径叠加起来放到map中
                        handlerMap.put(classPath+methodPath, method);
                    }else {
                        continue;
                    }
                }
            }
        }
    }

    //这里不用策略模式，VIP才有策略模式
    private static Object[] hand(HttpServletRequest request, HttpServletResponse response, Method method){
        //拿到当前执行方法有哪些参数
        Class<?>[] paramClazzs = method.getParameterTypes();
        //根据参数的个数，new一个参数的数组，将方法里的所有参数赋值到args来
        Object[] args = new Object[paramClazzs.length];
        int args_i = 0;
        int index = 0;
        for (Class<?> paramClazz: paramClazzs){
            if (ServletRequest.class.isAssignableFrom(paramClazz)){
                args[args_i++] = request;
            }
            if (ServletResponse.class.isAssignableFrom(paramClazz)){
                args[args_i++] = response;
            }
            //从0-3判断有没有RequestParam注解，很明显paramClazz为0和1时不是，
            //当2和3时为@RequestParam, 需要解析
            //[@com.enjoy.james.annotation.EnjoyRequestParam(value=name)]
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if (paramAns.length > 0){
                for (Annotation paramAn: paramAns){
                    if (EnjoyRequestParam.class.isAssignableFrom(paramAn.getClass())){
                        EnjoyRequestParam rp = (EnjoyRequestParam)paramAn;
                        //找到注解里的name和age
                        args[args_i++] = request.getParameter(rp.value());
                    }
                }
            }
            index++;
        }
        return args;
    }

}
