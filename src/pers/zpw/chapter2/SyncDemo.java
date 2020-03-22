package pers.zpw.chapter2;

import java.util.ArrayList;
import java.util.List;

public class SyncDemo {

    private int count;

    public int getCount() {
        return count;
    }

    private final Object lock;

    public SyncDemo() {
        this.count = 0;
        this.lock = new Object();
    }

    public void add() {
        for (int i = 0; i < 100; i++) {
            count++;
        }

    }

    public void syncAdd() {
        synchronized (lock) {
            for (int i = 0; i < 100; i++) {
                count++;
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        while (true) {

            SyncDemo syncDemo = new SyncDemo();
            List<Thread> threadList = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                threadList.add(new Thread(syncDemo::syncAdd, "thread" + i));
            }
            for (Thread thread : threadList) {
                thread.start();
            }
            for (Thread thread : threadList) {
                thread.join();
            }
            System.out.println(syncDemo.getCount());

            if (syncDemo.getCount() != 10000) {
                break;
            }
        }
    }
}
