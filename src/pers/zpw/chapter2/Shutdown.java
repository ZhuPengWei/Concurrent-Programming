package pers.zpw.chapter2;

public class Shutdown {


    public static void main(String[] args) throws InterruptedException {

        Runner one = new Runner();
        Thread countThread = new Thread(one, "CountThread");
        countThread.start();

        // 睡眠一秒，main线程对CountThread进行中断，使CountThread能够感知中断而结束
        Thread.sleep(1000);
        countThread.interrupt();


        Runner two = new Runner();
        countThread = new Thread(two, "CountThread");
        countThread.start();

        // 睡眠1s，main线程对Runner two 进行取消，使CountThread能够感知on为false 而结束
        Thread.sleep(1000);
        two.cancel();

    }


    private static class Runner implements Runnable {
        private long i;

        private volatile boolean on = true;

        @Override
        public void run() {

            while (on && !Thread.currentThread().isInterrupted()) {
                i++;
            }

            System.out.println("Count i = " + i);
        }

        public void cancel() {
            on = false;
        }
    }
}
