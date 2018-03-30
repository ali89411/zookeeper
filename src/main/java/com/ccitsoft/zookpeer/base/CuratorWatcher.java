package com.ccitsoft.zookpeer.base;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

/**
 * CuratorAPI== 
 * CuratorWatcher --- 利用自己Cache缓冲监控节点
 * InterProcessLock----根据节点创建分布式锁
 * 
 * @author Administrator
 *
 */
public class CuratorWatcher {
	
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
		cf.create().forPath("/super","init".getBytes());
		// true 是否缓冲节点数据--------
		PathChildrenCache cache = new PathChildrenCache(cf, "/super", true);
		// 4,初始化进行 缓冲监听event
		cache.start(StartMode.POST_INITIALIZED_EVENT);
		ExecutorService executor = Executors.newFixedThreadPool(10);
		// 5,缓冲监听
		cache.getListenable().addListener(new PathChildrenCacheListener(){
			@Override
			public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
				event.getType();
				System.out.println("event.getType()---------" +  event.getType());
				System.out.println("event.getData()---------" +  event.getData().getPath());
			}
		}, executor);
		// 4, 创建节点
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/super/s1", "s111".getBytes());
		// 6, 修改节点
		cf.setData().forPath("/super/s1", "s22222".getBytes());
		// 5, 删除节点
		cf.delete().deletingChildrenIfNeeded().forPath("/super");
		// 依照/super创建------分布式锁
		InterProcessLock lock = new InterProcessMutex(cf, "/super");
		lock.acquire();
		lock.release();
		
		// 分布式计数器
		DistributedAtomicInteger atomicInteger = new DistributedAtomicInteger(cf, "/super", new RetryNTimes(3, 1000));
		// 设置一个值
		atomicInteger.forceSet(0);
		AtomicValue<Integer> atomicValue = atomicInteger.get();
		// 
		atomicInteger.increment();
		atomicValue.getStats(); // 状态
		atomicValue.postValue(); // 最新值
		atomicValue.preValue();// 原始值
		// 8, 关闭
		cf.close();
	}
	
}
