package com.secnium.iast.core.enhance.plugins.api;

import com.secnium.iast.core.handler.IastClassLoader;
import com.secnium.iast.core.handler.controller.impl.HttpImpl;
import com.secnium.iast.core.handler.models.ApiDataModel;
import com.secnium.iast.core.handler.models.MethodEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.secnium.iast.core.report.ApiReport.sendReport;

/**
 * niuerzhuang@huoxian.cn
 */
public class SpringApplicationImpl {

    private static IastClassLoader iastClassLoader;
    public static Method getAPI;
    public static boolean isSend;

    public static void getWebApplicationContext(MethodEvent event, AtomicInteger invokeIdSequencer) {
        if (!isSend) {
        Object applicationContext = event.returnValue;
        createClassLoader(applicationContext);
        loadApplicationContext();
        List<ApiDataModel> invoke = null;
        String apiList = null;
        try {
            invoke = (List<ApiDataModel>) getAPI.invoke(null, applicationContext);
            apiList = invoke.toString();
            apiList = apiList.replace("=", ":").replace("{", "{\"").replace("}", "\"}").replace(" ", "").replace(":", "\":\"").replace("'", "").replace(",", "\",\"").replace("\"[", "[\"").replace("]\"", "\"]").replace("null", "").replace("}\"", "}").replace("\"{", "{").replace("/{\"", "/{").replace("\"},\"met", "}\",\"met").replace("clazz", "class");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e){
            e.printStackTrace();
        }
        sendReport(apiList);
        isSend = true;
        }
    }

    private static void createClassLoader(Object applicationContext) {
        try {
            if (iastClassLoader == null) {
                if (HttpImpl.IAST_REQUEST_JAR_PACKAGE.exists()) {
                    Class<?> applicationContextClass = applicationContext.getClass();
                    URL[] adapterJar = new URL[]{HttpImpl.IAST_REQUEST_JAR_PACKAGE.toURI().toURL()};
                    iastClassLoader = new IastClassLoader(applicationContextClass.getClassLoader(), adapterJar);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static void loadApplicationContext() {
        if (getAPI == null) {
            try {
                Class<?> proxyClass;
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.servlet.SpringApplicationContext");
                getAPI = proxyClass.getDeclaredMethod("getAPI", Object.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        iastClassLoader.loadClass("org.springframework.context.ApplicationContext");
    }

}