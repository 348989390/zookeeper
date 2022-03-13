package com.example.zookeeper.config;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKUtils {

    private static final String address="192.168.187.129:2181,192.168.187.130:2181,192.168.187.131:2181,192.168.187.132:2181/testLock";

    private static DefaultWatch watch = new DefaultWatch();

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static ZooKeeper  getInstance() {

        ZooKeeper zooKeeper = null;
        try {
            watch.setCd(countDownLatch);

            zooKeeper = new ZooKeeper(address,1000,watch);

            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return zooKeeper;
    }
}
