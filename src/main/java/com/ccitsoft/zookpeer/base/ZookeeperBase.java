package com.ccitsoft.zookpeer.base;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
/**
 * @author obServer
 *
 */
public class ZookeeperBase {

	
	// 连接地址
	final static String CONN_ADDR = "192.168.88.51,192.168.89.61,192.168.89.184"; 
	// 超时时间
	final static int SESSION_TIME = 5000;
	// 阻塞信号  保证zooKeeper连接成功
	final static CountDownLatch countSemaphore = new CountDownLatch(1);
	
	public static void main(String[] args) {

		try {
			ZooKeeper zooKeeper = new ZooKeeper(CONN_ADDR, SESSION_TIME, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					// 取得zooKeeper状态
					KeeperState keeperState = event.getState();
					// 获取信号状态
					EventType type = event.getType();
					if(KeeperState.SyncConnected == keeperState){
						if(EventType.None == type){
							// 发送信号量,让后序程序继续执行
							countSemaphore.countDown();
							System.out.println("zk 建立连接----");
						}
					}
				}
			});
			// 阻塞
			countSemaphore.await();
		    // 创建父节点Ids.OPEN_ACL_UNSAFE安全认证
			// PERSISTENT 持久化  PERSISTENT_SEQUENTIAL 顺序节点  EPHEMERAL临时节点(本次回话有效)
//			String create = zooKeeper.create("/ali", new String("ali-cheshi").getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//			System.out.println(create);
//			// 创建子节点----本次回话结束 临时值消失--分布式锁------数据存在内存中
			zooKeeper.create("/ali/ali111", new String("ali111").getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			
			// 获取值
			byte[] data = zooKeeper.getData("/ali",false, null);
			System.out.println(new String(data));
			// 
			List<String> children = zooKeeper.getChildren("/ali", false);
			for (String str : children) {
				System.out.println("str-----------------------"+str);
				System.out.println(new String(zooKeeper.getData("/ali/"+str, false, null)));
			}
			
			System.out.println(zooKeeper);
			zooKeeper.close();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (InterruptedException e) {
			e.printStackTrace();
		}catch (KeeperException e) {
			e.printStackTrace();
		}
		
	}

}
