package com.ccitsoft.zookpeer.base;

import java.io.IOException;
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
 * @author Administrator
 * Watcher 一次性。一直监听,1,使用上下文Watcher 2,重新创建Watcher
 * 事件类型 ：EventType.NodeCreate
 *        EventType.NodeDataChanged
 *        EventType.NodeChildrenChanges
 *        EventType.NodeDeleted
 * 状态类型：keeperSatus.Disconnectd
 *        keeperSatus.sycyDisconnectd
 *        keeperSatus.AuthFailed
 *        keeperSatus.Expired
 */
public class ZookeeperWatcher implements Watcher{

	// 连接地址
	final static String CONN_ADDR = "192.168.88.51,192.168.89.61,192.168.89.184"; 
	// 超时时间
	final static int SESSION_TIME = 5000;
	// 阻塞信号  保证zooKeeper连接成功--异步连接
	final static CountDownLatch countSemaphore = new CountDownLatch(1);
	
	// prent
	final static String prentPath = "/p";
	
	// children
	final static String childrenPath = "/p/c";
	
	static ZooKeeper zooKeeper = null;
	
	public void getConnection(String addr, int sessionTime){
		try {
		  closeConnection();
		  zooKeeper = new ZooKeeper(addr, SESSION_TIME, this);
		  // 加认证
//		  zooKeeper.addAuthInfo(scheme, auth);
		  countSemaphore.await();
		  System.out.println("zk---服务器连接----");
		} catch (IOException e) {
			e.printStackTrace();
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void closeConnection() {
		if (zooKeeper != null) {
			try {
				zooKeeper.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static void main(String[] args) {
		ZookeeperWatcher watcher = new ZookeeperWatcher();
		watcher.getConnection( CONN_ADDR, SESSION_TIME);
		// 手动Watcher
//		zooKeeper.exists(path, watcher);
//		zooKeeper.getData(path, watch, stat)
//		zooKeeper.getChildren(path, watch, stat)
		try {
			zooKeeper.create(prentPath, new String("111").getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			zooKeeper.exists(childrenPath, true);
			zooKeeper.create(childrenPath, new String("111").getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void process(WatchedEvent event) {
		
		System.out.println("event---"+event);
		if(event == null){
			return;
		}
		// 取得zooKeeper状态
		KeeperState keeperState = event.getState();
		// 获取信号状态
		EventType type = event.getType();
//		String path = event.getPath();
		if(KeeperState.SyncConnected == keeperState){
			if(EventType.None == type){
				// 发送信号量,让后序程序继续执行
				countSemaphore.countDown();
				System.out.println("zk 连接成功----");
			}
			else if(EventType.NodeCreated == type){
				System.out.println("父节点---节点创建---!");
			}
			else if(EventType.NodeDataChanged == type){
				System.out.println("父节点---节点修改---!");
			}
			else if(EventType.NodeDeleted == type){
				System.out.println("父节点---节点删除---!");
			}
			else if(EventType.NodeChildrenChanged == type){
				System.out.println("子节点---节点修改---!");
			}
		}
	}

}
