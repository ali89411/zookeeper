package com.ccitsoft.zookpeer.base;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * CuratorAPI== session 超时重新连接  主从选举  分布式计算器  分布式锁
 * 
 * BackgroundCallback 函数  Executor参数  可以调用多线程处理任务
 * @author Administrator
 *
 */
public class CuratorTest {
	
	// 连接地址
    final static String CONN_ADDR = "192.168.88.51,192.168.89.61,192.168.89.184"; 

	public static void main(String[] args) throws Exception{
		//1, 重试策略  ：初始时间为1s。重试10次.有4种实现NTIMES。ONETIMES
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 2);
		//2, 工厂连接
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONN_ADDR).sessionTimeoutMs(1000 * 16).retryPolicy(retryPolicy).build();
		// 3,开启
		cf.start();
		// 4, 创建节点
//		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/s1", "s111".getBytes());
		// 5, 删除节点
//		cf.delete().deletingChildrenIfNeeded().forPath("/super");
		// 6, 修改节点
//		cf.setData().forPath("/super/s1", "s22222".getBytes());
		// 7, 回调函数----BackgroundCallback可以使用多线程处理创建节点
		ExecutorService pool = Executors.newFixedThreadPool(10);
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).inBackground(new BackgroundCallback(){
			@Override
			public void processResult(CuratorFramework cf, CuratorEvent event) throws Exception {
				System.out.println("getResultCode" + event.getResultCode());
				System.out.println("Type" + event.getType());
				System.out.println(Thread.currentThread().getName());
			}
		},pool).forPath("/super/s2","s2新增内容".getBytes());
		// 8, 关闭
		cf.close();
	}
	
}
