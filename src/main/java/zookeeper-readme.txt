1,zookeeper采用zab原子消息广播协议的协议作为其数据一致性的核心算法。
      节点： Leader选举算法采用了Paxos协议。
      角色：Leader,follow ObServer(Watcher监控Leader-follow)
      集群内部     server.0=Ip:2888:3888    2888端口为Leader选举端口    3888为服务通信端口
         server.1=Ip:2888:3888
         server.2=Ip:2888:3888
2,zoo.cfg  tickTime 服务器与客户端心跳间隔,默认为2000ms
           clientPort 客户端端口  默认2181
           initLimit 客户端初始化时忍受多少个心跳时间间隔，(默认是10个间隔)若总时间长度:tickTime*10,未响应则连接失败
           synLimit Leader 与 follow间心跳间隔(默认5个间隔),时间总长tickTime*5
3,原生API
  zooKeeper.create();// PERSISTENT(持久化) PERSISTENT_SEQUENTIAL(持久化顺序)
  zooKeeper.getData();// EPHEMERAL(临时节点) EPHEMERAL_SEQUENTIAL(临时节点顺序) 
  Watcher:实现Watcher接口,重新起process方法
  Watcher 一次性。一直监听,1,使用上下文Watcher 2,重新创建Watcher
 * 事件类型 ：EventType.NodeCreate
 *        EventType.NodeDataChanged
 *        EventType.NodeChildrenChanges
 *        EventType.NodeDeleted
 * 状态类型：keeperSatus.Disconnectd
 *        keeperSatus.sycyDisconnectd
 *        keeperSatus.AuthFailed
 *        keeperSatus.Expired
      其Watcher是一次性,其zooKeeper.getData(),zooKeeper.getChildren(),exit();方法包括是否Watcher参数。
4,ZkCli--API
  client.createPersistent();可以递归创建。删除节点信息。和原生比较方便操作节点信息。
  ZkCliWatcher:提供2个监听接口,具体用法参照ZkCliWatcher
	 1./**
	  * IZkChildListener-----super/已经子节点  新增  删除 操作。不监控其修改（数据变更）操作
	  */
	 2.
	  /**
	  * IZkDataListener-----super/监控其修改（数据变更）操作
	  */
4,Curator--API
      流式操作API提供。重试策略设计----连接重试机制
  //1, 重试策略  ：初始时间为1s。重试10次.有4种实现NTIMES。ONETIMES
  CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONN_ADDR).sessionTimeoutMs(1000 * 16).retryPolicy(retryPolicy).build();
      包含其基本操作优化：create,delete等方法提供回调函数inBackground传入线程池用法。提高了初始化时 创建节点效率。
  CuratorWatcher其设计,完全区别原生和zkCli。原理内存缓冲其节点数据。
  // true 是否缓冲节点数据--------
  PathChildrenCache cache = new PathChildrenCache(cf, "/super", true);
  // 缓冲监听模式也是很多种
  cache.start(StartMode.POST_INITIALIZED_EVENT);
  ExecutorService executor = Executors.newFixedThreadPool(10);
  // 缓冲监听  PathChildrenCacheListener---无法监听根节点
  cache.getListenable().addListener(new PathChildrenCacheListener(){
		@Override
		public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
			event.getType();// 操作类型。包括子节点新增。删除。修改
			System.out.println("event.getType()---------" +  event.getType());
			System.out.println("event.getData()---------" +  event.getData().getPath());
		}
   }, executor);
5,Curator : InterProcessLock 分布式锁类提供。依照"super 创建锁"
	InterProcessLock lock = new InterProcessMutex(cf, "/super");
	lock.acquire();
	lock.release();
  
6,Curator :DistributedAtomicInteger 分布式计数器类提供。依照某个节点统计其节点变化。可以做网站访问量
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
7,栅栏Barrier用法  DistributedBarrier   用法参照DistributedBarrierExample.java
               DistributedDoubleBarrier  用法参照DistributedDoubleBarrier.java
  http://ifeve.com/zookeeper-barrier/
  
8,Curator:重复注册。。。。server1 与server2  ---假如server1挂掉。。zookeeper重复注册---挂掉后保存着未注册的服务。(从跟节点/s1/s2递归监听)
  
           