package pers.zpw.chapter1;


// 验证多线程执行的速度
public class ConcurrencyTest {

    // 执行次数
    public static final long count = 1000000000;

    public static void main(String[] args) throws InterruptedException {
        concurrency();
        serial();
    }

    // 单线程速度验证
    private static void concurrency() throws InterruptedException {
        long start = System.currentTimeMillis();

        Thread thread = new Thread(() -> {
            int a = 0;
            for (long i = 0; i < count; i++) {
                a += 5;
            }
        });
        thread.start();

        int b = 0;
        for (long i = 0; i < count; i++) {
            b--;
        }
        thread.join();

        long time = System.currentTimeMillis() - start;
        System.out.println("concurrency：time = " + time + "ms ；b= " + b);
    }

    // 多线程速度验证
    private static void serial() {
        long start = System.currentTimeMillis();
        int a = 0;
        for (int i = 0; i < count; i++) {
            a += 5;
        }

        int b = 0;
        for (int i = 0; i < count; i++) {
            b--;
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("serial：time = " + time + "ms ；b= " + b + "； a=" + a);
    }

}
