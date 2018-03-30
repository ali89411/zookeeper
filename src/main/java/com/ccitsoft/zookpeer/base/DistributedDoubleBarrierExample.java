package com.ccitsoft.zookpeer.base;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 双栅栏允许客户端在计算的开始和结束时同步。当足够的进程加入到双栅栏时，进程开始计算， 当计算完成时，离开栅栏。
 * @author Administrator
 */
public class DistributedDoubleBarrierExample {
	private static final int QTY = 5;
    private static final String PATH = "/examples/doublebarrier";
    // 连接地址
    final static String CONN_ADDR = "192.168.88.51,192.168.89.61,192.168.89.184"; 
    public static void main(String[] args) throws Exception {
    	//1, 重试策略  ：初始时间为1s。重试10次.有4种实现NTIMES。ONETIMES
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 2);
		//2, 工厂连接
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString(CONN_ADDR).sessionTimeoutMs(1000 * 16).retryPolicy(retryPolicy).build();
        client.start();
        ExecutorService service = Executors.newFixedThreadPool(QTY);
        for (int i = 0; i < QTY; ++i) {
            final DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(client, PATH, QTY);
            final int index = i;
            Callable<Void> task = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Thread.sleep((long) (3 * Math.random()));
                    System.out.println("Client #" + index + " enters");
                    // enter方法被调用时，成员被阻塞，直到所有的成员都调用了enter
                    barrier.enter();
                    System.out.println("Client #" + index + " begins");
                    Thread.sleep((long) (3000 * Math.random()));
                    // leave方法被调用时，它也阻塞调用线程， 知道所有的成员都调用了leave。 就像百米赛跑比赛， 发令枪响， 所有的运动员开始跑，等所有的运动员跑过终点线，比赛才结束。
                    barrier.leave();
                    System.out.println("Client #" + index + " left");
                    return null;
                }
            };
            service.submit(task);
        }
        service.shutdown();
        service.awaitTermination(10, TimeUnit.MINUTES);
    }
}
