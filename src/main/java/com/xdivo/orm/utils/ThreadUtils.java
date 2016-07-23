package com.xdivo.orm.utils;

import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * 线程池工具
 * Created by liujunjie on 16-7-19.
 */
public class ThreadUtils {

    private static Logger log = Logger.getLogger(ThreadUtils.class);

    private static Executor executor = null;

    //线程池缓冲队列
    private static BlockingQueue<Runnable> workQueue = null;

    //当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中
    private static int QUEUESIZE = 1000;

    private static int COREPOOLSIZE = 100;

    private static int MAXPOOLSIZE = 100;

    static {
        log.info("初始化线程池对象");
        if(workQueue  == null){
            workQueue = new ArrayBlockingQueue<Runnable>(QUEUESIZE);
        }
        if(executor == null){
            executor = new ThreadPoolExecutor(COREPOOLSIZE, MAXPOOLSIZE, 60, TimeUnit.SECONDS, workQueue);
        }
    }

    /**
     * 初始化线程池
     * @param coreSize
     * @param maxPoolSize
     * @param queueSize
     */
    public static void init(int coreSize, int maxPoolSize, int queueSize) {
        if(0 != coreSize ){
            COREPOOLSIZE = coreSize;
        }
        if(0 != maxPoolSize) {
            MAXPOOLSIZE = maxPoolSize;
        }
        if(0 != queueSize) {
            QUEUESIZE = queueSize;
        }
    }

    public static void execute(Runnable runnable) {
        if (QUEUESIZE - workQueue.size() < 100) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        executor.execute(runnable);
    }
}
