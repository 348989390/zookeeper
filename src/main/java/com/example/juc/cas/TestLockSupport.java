package com.example.juc.cas;

import java.util.concurrent.locks.LockSupport;

public class TestLockSupport {

    public static void main(String[] args) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    System.out.println(i);
                    if(i==5){
                        LockSupport.park();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("after 8 senconds");

        LockSupport.unpark(thread);

    }
}
