package com.ccitsoft.zookpeer.base;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
/**
 * subscribeChildChanges
 * @author Administrator
 *
 */
public class ZkCliWatcher {
	
	
	public static void main(String[] args) {
		 ZkClient client = new ZkClient(new ZkConnection("192.168.88.51:2181"));
		 /**
		  * IZkChildListener-----super/已经子节点  新增  删除 操作。不监控其修改（数据变更）操作
		  */
		 client.subscribeChildChanges("/super", new IZkChildListener(){
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				System.out.println("parentPath" + parentPath);
				System.out.println("currentChilds" + currentChilds);
			}
		 });
		 
		 /**
		  * IZkDataListener-----super/监控其修改（数据变更）操作
		  */
		client.subscribeDataChanges("/super", new IZkDataListener() {
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				System.out.println("变更数据节点为"+dataPath + "---" +"Object" + data);
			}

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				System.out.println("删除数据节点为"+dataPath);
			}

		});
		 
		 // 可以递归创建节点。但无法设置具体的值value
       client.createPersistent("/super");
       client.createPersistent("/super/c1", "1111");
       client.createPersistent("/super/c2", "2222");
       
       client.writeData("/super/c1", "3333");
	   client.close();
		 
	}
}
