package io.gallery.db.util;

import io.gallery.db.exception.DataBaseDataBindingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import javax.script.*;
import java.io.IOException;

public class JsUtil {
    private static final Log logger = LogFactory.getLog(JsUtil.class);
    private static ScriptEngine engine;

    public static ScriptEngine getEngine() {
        if (engine == null) {
            engine = new ScriptEngineManager().getEngineByName("js");
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("polyglot.js.allowAllAccess", true);
        }
        return engine;
    }

    public static Object run(String code) {
        return run(code, null);
    }

    public static Object run(String code, String name) {
        Object result = null;
        if (DBT.isNotNull(code)) {
            code = code.replaceAll("DataBaseUtil\\.", "io.gallery.db.util.DataBaseUtil.");
            //logger.info(code);
            code = code.replaceAll("DataBaseTools\\.", "io.gallery.db.util.DataBaseTools.");
            //logger.info(code);
            code = code.replaceAll("DataSourceUtil\\.", "io.gallery.db.util.DataSourceUtil.");
            //logger.info(code);
            code = code.replaceAll("DataBaseSpringUtil\\.", "io.gallery.db.util.DataBaseSpringUtil.");
            //logger.info(code);
            code = code.replaceAll("DSUtil\\.", "io.gallery.db.util.DSUtil.");
            //logger.info(code);
            code = code.replaceAll("DS\\.", "io.gallery.db.util.DataSourceUtil.");
            //logger.info(code);
            code = code.replaceAll("DB\\.", "io.gallery.db.util.DataBaseUtil.");
            //logger.info(code);
            code = code.replaceAll("DBT\\.", "io.gallery.db.util.DataBaseTools.");
            code = code.replaceAll("Maps\\.", "io.gallery.db.util.Maps.");
            code = code.replaceAll("ExportType\\.", "io.gallery.db.bean.");
            //logger.info("code:" + code);
            try {
                Source source = Source.newBuilder("js", code, name).build();
                result = Context.newBuilder().allowAllAccess(true).build().eval(source).toString();
            } catch (IOException e) {
                throw new DataBaseDataBindingException("运行js错误", e);
            }
        }
        return result;
    }

    public static void test() {
        final ScriptEngineManager mgr = new ScriptEngineManager();
        for (ScriptEngineFactory fac : mgr.getEngineFactories()) {
            System.out.printf("EngineName: %s, EngineVersion: (%s), LanguageName:%s, LanguageVersion(%s), THREADING: %s%n",
                    fac.getEngineName(),
                    fac.getEngineVersion(),
                    fac.getLanguageName(),
                    fac.getLanguageVersion(),
                    fac.getParameter("THREADING"));
        }
    }
}
