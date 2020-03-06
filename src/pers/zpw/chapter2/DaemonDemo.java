package pers.zpw.chapter2;

public class DaemonDemo {

    public static void main(String[] args) throws InterruptedException {

        Thread thread = new Thread(new DaemonRunner(), "DaemonRunner");
        thread.setDaemon(true);
        thread.start();

    }

    static class DaemonRunner implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("最后执行的语句");
            }
        }
    }
}
