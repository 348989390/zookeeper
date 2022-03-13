package com.example.zookeeper.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallBack implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {

    ZooKeeper zk;

    String threadName;

    CountDownLatch cc = new CountDownLatch(1);

    String pathName;

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public  void  tryLock(){
        try {
            System.out.println(threadName+"  create..........");
            // TODO: 2022/3/13  可能存在锁重入的情况，第一个线程和第5000个线程是同一个
            //获取到锁之前，阻塞代码
            zk.create("/lock",threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,this,"asd");
            cc.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public  void  unLock(){
        try {
            zk.delete(pathName,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    //Watcher 监控事件
    @Override
    public void process(WatchedEvent event) {

        //如果A节点挂了，只会通知监控他的B节点，重新判断自己是不是第一个节点能不能向下执行
        //如果X节点挂了，也会通知Y节点，去判定是不是第一个，watch前面的节点。
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                //当前A节点消失了，通知watch A节点的B节点去获取/下的所有子节点，重新判定自己是不是第一个
                zk.getChildren("/",false,this,"asd");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }
    }
    //StringCallBack  回调函数
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if(name!=null){
            System.out.println(threadName+"create node ......"+name);
            pathName = name;
            zk.getChildren("/",false,this,"asd");
        }
    }

    //getChildren  callback
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {

//        System.out.println("lock  locks.......");
//        for (String child : children) {
//            System.out.println(child);
//        }

        Collections.sort(children);
        int i = children.indexOf(pathName.substring(1));

        if(i==0){
            //当前节点是第一个节点,
            System.out.println(threadName+"i am first.....");
            try {
                zk.setData("/",threadName.getBytes(),-1);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cc.countDown();
        }else{
            //监控前一个节点，可能监控 过程中，前一个节点挂了，所以需要回调
            zk.exists("/"+children.get(i-1),this,this,"asd");
        }
    }

    //StatCallBack  回调
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }
}
