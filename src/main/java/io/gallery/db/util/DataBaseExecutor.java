package io.gallery.db.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据库线程池
 */
public class DataBaseExecutor {
    private static ExecutorService cachedThreadPool;

    public static ExecutorService getService() {
        if (cachedThreadPool == null)
            cachedThreadPool = Executors.newCachedThreadPool();
        return cachedThreadPool;
    }
}
