package com.ccitsoft.zookpeer.base;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

public class ZkCliTest {

	public static void main(String[] args) {
       ZkClient client = new ZkClient(new ZkConnection("192.168.88.51:2181"));
       // 可以递归创建节点。但无法设置具体的值value
       client.createPersistent("/super/li", true);
       client.writeData("/super/li", "ali");
       // 递归删除
//       client.deleteRecursive("/super");
       // 取得
       List<String> children = client.getChildren("/super");
       for (String str : children) {
    	  String rp = "/super/"+str;
    	  String readData = client.readData(rp);
		  System.out.println("节点为:"+str+"," +"内容为:"+readData);
	   }
       client.close();
	}

}
