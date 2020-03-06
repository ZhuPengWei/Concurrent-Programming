package pers.zpw.chapter1;

public class HelloWorldDemo {

    public static void main(String[] args) {

        System.out.println("Hello World, 你好啊 全世界");

        new Thread(() -> {
            System.out.println("开启了一个线程");
            System.out.println("我不是主线程序，干了一些其他事情");
        }).start();


        // 主线程干了一些其他的事情

    }
}
