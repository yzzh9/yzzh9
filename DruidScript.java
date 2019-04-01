package com.test.btrace.script;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Duration;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeClassName;
import com.sun.btrace.annotations.ProbeMethodName;
import com.sun.btrace.annotations.Self;

import java.lang.reflect.Field;

/**
 * @author yangzaizhong
 * @description
 * @date 2019/4/1 上午9:11
 */
@BTrace
public class DruidScript {

    @OnMethod(clazz = "com.alibaba.druid.pool.DruidDataSource", method = "getConnection", location = @Location(Kind.RETURN))
    public static void run(@Self Object self, long timeWait, @Duration long time, @ProbeClassName String className, @ProbeMethodName String methodName) {
        //activeCount是正在运行的使用连接个数
        Field activeCountField = BTraceUtils.field("com.alibaba.druid.pool.DruidDataSource", "activeCount");
        int activeCount = (Integer) BTraceUtils.get(activeCountField, self);

        //poolingCount是线程池中连接个数，初始化时该值为initialSize，最大为maxActive
        Field poolingCountField = BTraceUtils.field("com.alibaba.druid.pool.DruidDataSource", "poolingCount");
        int poolingCount = (Integer) BTraceUtils.get(poolingCountField, self);

        //连接次数，累加
        Field connectionField = BTraceUtils.field("com.alibaba.druid.pool.DruidDataSource", "connectCount");
        long connectCount = (Long) BTraceUtils.get(connectionField, self);

        BTraceUtils.println("cost time: " + time/1000000 + ",activeCount: " + activeCount + ",poolingCount: " + poolingCount + ",connectCount: " + connectCount);
        /**
         * druid源码：activeCount：活动连接数，poolingCount：线程池中的可用连接数
         *         // 创建连接
         *         // 防止创建超过maxActive数量的连接
         *         if (activeCount + poolingCount >= maxActive) {
         *             empty.await();//会释放当前独占锁
         *         }
         *
         *         //获取链接，并返回
         *         decrementPoolingCount();
         *         holder = connections[poolingCount];
         *         connections[poolingCount] = null;
         */
    }


}
