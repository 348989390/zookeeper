package com.example.zookeeper.test;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class Test {


    public static void main(String[] args) throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(1);
        //zk是有session概念的，没有连接池的概念
        //watch 观察  回调
        //watch的注册值发生在 读类型调用， get exsits
        //第一类： new zk的时候，传入的watch,这个watch是session级别的，跟path,node没有关系
        ZooKeeper zk = new ZooKeeper("192.168.187.129:2181,192.168.187.130:2181,192.168.187.131:2181,192.168.187.132:2181", 3000, new Watcher() {
            //watch的回调方法
            @Override
            public void process(WatchedEvent event) {
                Event.KeeperState state = event.getState();
                Event.EventType type = event.getType();
                String path = event.getPath();
                System.out.printf("new zk watch "+event.toString());

                switch (state) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println();
                        System.out.println("SyncConnected connected");
                        countDownLatch.countDown();
                        break;
                    case AuthFailed:
                        break;
                    case ConnectedReadOnly:
                        break;
                    case SaslAuthenticated:
                        break;
                    case Expired:
                        break;
                }

                switch (type) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
                        break;
                    case NodeDataChanged:
                        break;
                    case NodeChildrenChanged:
                        break;
                }
            }
        });
        countDownLatch.await();
        ZooKeeper.States state = zk.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("connecting");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("connected");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }


        String pathName = zk.create("/ooxx", "ceshi".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        Stat stat = new Stat();
        byte[] node = zk.getData("/ooxx", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getdata watch:" + event.toString());
                try {
                    //true default watch  被重新注册， new  zK 的那个watch
                    //this  当前watch
                    zk.getData("/ooxx",this,stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);

        System.out.println("node:"+new String(node));

        // /ooxx路径的值发生变化，会触发回调，
        System.out.println("第一次会触发set节点值的回调吗");
        Stat stat1 = zk.setData("/ooxx", "newData".getBytes(), 0);
        System.out.println("第二次会触发set节点值的回调吗");
        Stat stat2 = zk.setData("/ooxx", "newData1".getBytes(), stat1.getVersion());


        System.out.println("-----------------async  begin------------------");

        //异步
        zk.getData("/ooxx", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("-----------------async  called------------------");
                System.out.println(new String(data));
            }
        },"123321");

        System.out.println("-----------------async  over------------------");

        Thread.sleep(Long.parseLong("222222"));


    }
}
