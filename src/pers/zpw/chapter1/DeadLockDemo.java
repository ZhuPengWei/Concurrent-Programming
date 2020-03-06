package pers.zpw.chapter1;

// 死锁例子
public class DeadLockDemo {

    public static final String A = "a";
    public static final String B = "b";

    public static void main(String[] args) {

        deadLock();

    }

    private static void deadLock() {

        new Thread(() -> {
            synchronized (A) {
                System.out.println("线程1 获取到A锁");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (B) {
                    System.out.println("线程1 获取到B锁");
                }
            }
        }).start();

        new Thread(() -> {
            synchronized (B) {
                System.out.println("线程2 获取到B锁");
                synchronized (A) {
                    System.out.println("线程2 获取到A锁");
                }
            }
        }).start();
    }

}
