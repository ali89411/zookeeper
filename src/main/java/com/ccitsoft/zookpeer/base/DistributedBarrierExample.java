package com.ccitsoft.zookpeer.base;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 这个例子创建了controlBarrier来设置栅栏和移除栅栏。 我们创建了5个线程，在此Barrier上等待。 最后移除栅栏后所有的线程才继续执行。
 * @author Administrator
 *
 */
public class DistributedBarrierExample {
	private static final int QTY = 5;
    private static final String PATH = "/examples/barrier";
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
            // 总线controlBarrier控制其removeBarrier操作
            DistributedBarrier controlBarrier = new DistributedBarrier(client, PATH);
            // 首先你需要设置栅栏，它将阻塞在它上面等待的线程
            controlBarrier.setBarrier(	);

            for (int i = 0; i < QTY; ++i) {
                final DistributedBarrier barrier = new DistributedBarrier(client, PATH);
                final int index = i;
                Callable<Void> task = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        Thread.sleep((long) (3 * Math.random()));
                        System.out.println("Client #" + index + " waits on Barrier");
                        // 然后需要阻塞的线程调用“方法等待放行条件:
                        barrier.waitOnBarrier();
                        System.out.println("Client #" + index + " begins");
                        return null;
                    }
                };
                service.submit(task);
            }
            Thread.sleep(10000);
            System.out.println("all Barrier instances should wait the condition");
            // 当条件满足时，移除栅栏，所有等待的线程将继续执行：
            controlBarrier.removeBarrier();
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);
    }
}
