package com.example.zookeeper.config;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {
    ZooKeeper zk ;
    MyConf conf;

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setConf(MyConf conf) {
        this.conf = conf;
    }

    public MyConf getConf() {
        return conf;
    }

    @Override
    // exists 方法的回调函数，data为取到的节点内容
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        if(data!=null){
            String s = new String(data);
            conf.setConf(s);
            //取到数据了，释放锁，让方法aWait()继续向下执行
            System.out.println("exists 方法回调。。。"+"有数据，data:"+new String(data));
            countDownLatch.countDown();
        }else{
            System.out.println("exists 方法回调。。。"+"没有数据");
        }
    }

    @Override
    //
    public void processResult(int rc, String path, Object ctx, Stat stat) {

        if(stat!=null){
            try {
                System.out.println("执行getData 获取数据。。。");
                zk.getData("/AppConf",this,this,"asd");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process(WatchedEvent event) {

        switch (event.getType()) {
            case None:
                System.out.println("没有节点.................");
                break;
            case NodeCreated:
                //节点创建后，回调方法没取到数据的话，还在阻塞中，需要获取数据，并释放锁
                //执行获取数据的方法，执行完，触发回调，释放锁
                System.out.println("节点创建，NodeCreated，，，执行getData 获取数据。。。");
                zk.getData("/AppConf",this,this,"asd");
                break;
            case NodeDeleted:
                System.out.println("删除节点.................");
                //清空配置
                conf.setConf("");
                //重新加锁，阻塞线程，等节点创建数据后再释放
                countDownLatch = new CountDownLatch(1);
//                zk.getData("/AppConf",this,this,"asd");
                break;
            case NodeDataChanged:
                //节点值修改时，获取数据，并更新打印
                System.out.println("节点值修改，NodeDataChanged，，，执行getData 获取数据。。。");
                zk.getData("/AppConf",this,this,"asd");
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    public void aWait(){

        zk.exists("/AppConf", this,this,"");

        try {
            //exists 方法是异步的，需要取到数据后，再向下执行
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
