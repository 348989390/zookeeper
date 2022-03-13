package com.example.zookeeper.lock;

import com.example.zookeeper.config.ZKUtils;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZKLock {

    private  ZooKeeper zk;

    @Before
    public void conn(){
        zk = ZKUtils.getInstance();
    }

    @After
    public  void close(){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void lock(){

        for (int i = 0; i < 10; i++) {
            new Thread(){
                @Override
                public void run() {
                    WatchCallBack watchCallBack = new WatchCallBack();
                    String threadName = Thread.currentThread().getName();
                    watchCallBack.setThreadName(threadName);

                    watchCallBack.setZk(zk);
                    //抢锁
                    watchCallBack.tryLock();
                    //干活
                    System.out.println(threadName+"  working................");
                    try {
                        //干完活睡1秒，如果去掉这行代码，可能第一个抢到锁的，已经执行完了，节点消失了。
                        //第二个线程此时watch第一个节点，但是第一个节点已经消失了，所以第二个线程watch了一个空的节点，不会有回调，形成死锁了。
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //释放锁
                    watchCallBack.unLock();


                }
            }.start();
        }

        while (true){

        }

    }

}
