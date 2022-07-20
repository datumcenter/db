package io.gallery.db.util;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompileUtil {
    private String fullClassName;//类全名
    private String sourceCode;
    private Map<String, ByteJavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();//存放编译之后的字节码(key:类全名,value:编译之后输出的字节码)
    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler(); //获取java的编译器
    private DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();//存放编译过程中输出的信息
    private String consoleResult;//控制台输出的内容
    private Object returnResult;//返回结果
    private long compileTime;//编译耗时(单位ms)
    private long runTime;//运行耗时(单位ms)

    public CompileUtil(String sourceCode) {
        this.sourceCode = sourceCode;
        this.fullClassName = getFullClassName(sourceCode);
    }

    /**
     * 编译字符串源代码,编译失败在 diagnosticsCollector 中获取提示信息
     *
     * @return true:编译成功 false:编译失败
     */
    public boolean compiler() {
        long startTime = System.currentTimeMillis();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);//标准的内容管理器,更换成自己的实现，覆盖部分方法
        JavaFileManager javaFileManager = new StringJavaFileManage(standardFileManager);
        JavaFileObject javaFileObject = new StringJavaFileObject(fullClassName, sourceCode);//构造源代码对象
        JavaCompiler.CompilationTask task = compiler.getTask(null, javaFileManager, diagnosticsCollector, null, null, Collections.singletonList(javaFileObject));//获取一个编译任务
        compileTime = System.currentTimeMillis() - startTime;//设置编译耗时
        return task.call();
    }

    /**
     * 执行main方法，重定向System.out.print
     */
    public void run() {
        run("main", new Class[]{String[].class}, new Object[]{new String[]{}});
    }

    /**
     * 执行methodName对应方法，重定向System.out.print
     *
     * @param methodName String
     * @param argsClass  Class[]
     * @param args       Object[]
     */
    public void run(String methodName, Class[] argsClass, Object[] args) {
        PrintStream out = System.out;
        try {
            long startTime = System.currentTimeMillis();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            System.setOut(printStream);
            StringClassLoader scl = new StringClassLoader();
            Class<?> clazz = scl.findClass(fullClassName);
            Method method = clazz.getMethod(methodName, argsClass);
            returnResult = method.invoke(null, args); //调用指定方法
            runTime = System.currentTimeMillis() - startTime;//设置运行耗时
            consoleResult = outputStream.toString(String.valueOf(StandardCharsets.UTF_8));//设置打印输出的内容
        } catch (NoSuchMethodException e) {
            consoleResult = "运行失败：方法不存在！" + e.getMessage();
        } catch (InvocationTargetException e) {
            consoleResult = "运行失败：返回结果访问异常！" + e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            consoleResult = "运行失败：" + e.getMessage();
            e.printStackTrace();
        } finally {
            //还原默认打印的对象
            System.setOut(out);
        }
    }

    /**
     * @return 编译信息(错误 警告)
     */
    public String getCompileMessage() {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic diagnostic : diagnosticsCollector.getDiagnostics()) {
            sb.append(diagnostic.toString()).append("\r\n");
        }
        return sb.toString();
    }


    /**
     * 获取类的全名称
     *
     * @param sourceCode 源码
     * @return 类的全名称
     */
    public static String getFullClassName(String sourceCode) {
        String className = "";
        Pattern pattern = Pattern.compile("package\\s+\\S+\\s*;");
        Matcher matcher = pattern.matcher(sourceCode);
        if (matcher.find()) {
            className = matcher.group().replaceFirst("package", "").replace(";", "").trim() + ".";
        }
        pattern = Pattern.compile("class\\s+\\S+\\s+\\{");
        matcher = pattern.matcher(sourceCode);
        if (matcher.find()) {
            className += matcher.group().replaceFirst("class", "").replace("{", "").trim();
        }
        return className;
    }

    /**
     * 自定义一个字符串的源码对象
     */
    private class StringJavaFileObject extends SimpleJavaFileObject {
        //等待编译的源码字段
        private String contents;

        //java源代码 => StringJavaFileObject对象的时候使用
        public StringJavaFileObject(String className, String contents) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {//字符串源码会调用该方法
            return contents;
        }

    }

    /**
     * 自定义一个编译之后的字节码对象
     */
    private class ByteJavaFileObject extends SimpleJavaFileObject {
        //存放编译后的字节码
        private ByteArrayOutputStream outPutStream;

        public ByteJavaFileObject(String className, Kind kind) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), kind);
        }

        //StringJavaFileManage 编译之后的字节码输出会调用该方法（把字节码输出到outputStream）
        @Override
        public OutputStream openOutputStream() {
            outPutStream = new ByteArrayOutputStream();
            return outPutStream;
        }

        //在类加载器加载的时候需要用到
        public byte[] getCompiledBytes() {
            return outPutStream.toByteArray();
        }
    }

    /**
     * 自定义一个JavaFileManage来控制编译之后字节码的输出位置
     */
    private class StringJavaFileManage extends ForwardingJavaFileManager {
        StringJavaFileManage(JavaFileManager fileManager) {
            super(fileManager);
        }

        //获取输出的文件对象，它表示给定位置处指定类型的指定类。
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            ByteJavaFileObject javaFileObject = new ByteJavaFileObject(className, kind);
            javaFileObjectMap.put(className, javaFileObject);
            return javaFileObject;
        }
    }

    /**
     * 自定义类加载器, 用来加载动态的字节码
     */
    private class StringClassLoader extends ClassLoader {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            ByteJavaFileObject fileObject = javaFileObjectMap.get(name);
            if (fileObject != null) {
                byte[] bytes = fileObject.getCompiledBytes();
                return defineClass(name, bytes, 0, bytes.length);
            }
            try {
                return ClassLoader.getSystemClassLoader().loadClass(name);
            } catch (Exception e) {
                return super.findClass(name);
            }
        }
    }

    public String getConsoleResult() {
        return consoleResult;
    }

    public long getCompileTime() {
        return compileTime;
    }

    public long getRunTime() {
        return runTime;
    }

    public Map<String, ByteJavaFileObject> getJavaFileObjectMap() {
        return javaFileObjectMap;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public Object getReturnResult() {
        return returnResult;
    }

    public void setConsoleResult(String consoleResult) {
        this.consoleResult = consoleResult;
    }
}
