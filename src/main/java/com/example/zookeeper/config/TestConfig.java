package com.example.zookeeper.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestConfig {

    ZooKeeper zooKeeper;
    @Before
    public void conn(){
        zooKeeper = ZKUtils.getInstance();



    }

    @After
    public void close(){

    }

    @Test
    public void getConf(){

        WatchCallBack watchCallBack = new WatchCallBack();
        MyConf myConf = new MyConf();
        watchCallBack.setZk(zooKeeper);
        watchCallBack.setConf(myConf);

        //1.节点不存在
        //2.节点存在
        watchCallBack.aWait();

        while (true){
            if(myConf.getConf().equals("")){
                System.out.println("配置丢了。。。。。。");
                //再次重新阻塞，等待节点被创建，然后赋值。
                watchCallBack.aWait();
            }else{
                System.out.println(myConf.getConf());
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
