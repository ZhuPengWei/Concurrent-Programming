# JAVA并发编程总结

让我来揭开并发编程神秘的面纱！

## 目录
- 1、并发编程简介以及带来的挑战
   - <a  target="_blank"  href="https://gitee.com/zhupengwei/Concurrent-Programming/blob/master/src/pers/zpw/chapter1/1.1.md" >1.1、线程是什么并发编程又是什么</a>
   - <a  target="_blank"  href="https://gitee.com/zhupengwei/Concurrent-Programming/blob/master/src/pers/zpw/chapter1/1.2.md" >1.2、上下文切换的挑战 </a>
   - <a  target="_blank"  href="https://gitee.com/zhupengwei/Concurrent-Programming/blob/master/src/pers/zpw/chapter1/1.3.md" > 1.3、死锁 </a>
   - <a  target="_blank"  href="https://gitee.com/zhupengwei/Concurrent-Programming/blob/master/src/pers/zpw/chapter1/1.4.md" >1.4、资源限制的挑战 </a>
   
- 2、并发编程的基础
   - [2.1、线程的简介](#-21-线程的简介-)
      - [2.1.1、为什么要使用多线程？](#211-为什么要使用多线程) 
      - [2.1.2、线程的状态](#212-线程的状态)
      - [2.1.3、Daemon线程](#213-Daemon线程)
   - 2.2、启动和终止线程
      - 2.2.1、启动线程与理解中断的意义
      - 2.2.2、过期的suspend()、resume()和stop()
      - 2.2.3、怎么安全的终止线程
   - 2.3、线程间的通信
   - 2.3.1、volatile和synchronized关键字 
   - 2.3.2、等待/通知机制
   - 2.3.3、等待/通知机制的经典范式  
   - 2.3.4、管道输入/输出流
   - 2.3.5、Thread.join()的使用
   - 2.3.6、ThreadLocal的使用
   

### 2、并发编程的基础
   （本章节大多节选自JAVA并发编程的艺术第四章）
   
   java从诞生开始就明明智的选择了内置对多线程的支持，这使得java语言相比同一时期的其他语言具有明显的优势。线程作为操作系统调度的最小单元，多个线程能够同时执行，这将会显著提升程序的性能，在多核环境中表现得更加的明显。
   但是，过多地创建线程和对线程的不当管理也容易造成问题。
   
####  2.1 线程的简介 


##### 2.1.1 为什么要使用多线程？


正确的使用多线程，总是能够给开发人员带来极大的好处，使用多线程的原因主要有如下的几点：

（1）更多的处理器核心

（2）更快的响应时间

（3）更好的编程模型

现在我来举例一个场景：假如一段代码需要从A系统拿取几十万上百万条数据，耗时4s中。接着，需要从B系统拿取几十万上百万条数据，也耗时4s，
如果不使用多线程的话，无疑是非常的耗时的总共是8s。采用多线程的话可以分别开启2个线程去A，B两个系统获取数据，主线程同步等待这两个线程获取完数据然后进行汇总
这样大大的提升了程序的性能

![](.README_images/e9cf2d09.png)

#### 2.1.2 线程的状态

java线程在运行的生命周期中可能处于表4-1所示的6种不同的状态，在给定的一个时刻

状态名称 | 说明
---|---
NEW | 初始状态，线程被构建
RUNNABLE | 运行状态，java线程将操作系统中的就绪和运行两种状态统称为"运行中"
blocked | 阻塞状态，表示线程阻塞于锁
waiting | 等待状态，表示线程进入等待状态，进入该状态表示当前线程需要等待其他线程作出一定的特定动作
time_waiting | 超出等待状态
terminated | 终止状态，表示当前线程已经执行完毕

下面使用jstack工具，尝试的查看代码运行时的线程信息，实例如下代码所示

```
public class ThreadStateDemo {

    public static void main(String[] args) {

        new Thread(new TimeWaiting(), "TimeWaitingThread").start();
        new Thread(new Waiting(), "Waiting").start();

        // 一个获取锁成功，一个被阻塞
        new Thread(new Blocked(), "Blocked-1").start();
        new Thread(new Blocked(), "Blocked-2").start();
    }


    // 该线程在Blocked.class实例上加锁后，不会释放锁
    static class Blocked implements Runnable {
        @Override
        public void run() {
            synchronized (Blocked.class) {
                while (true) {
                    SleepUtils.second(100);
                }
            }
        }
    }

    // 该线程在Waiting.class实例上等待
    static class Waiting implements Runnable {
        @Override
        public void run() {
            while (true) {
                synchronized (Waiting.class) {
                    try {
                        Waiting.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    // 该线程不断的进行睡眠
    static class TimeWaiting implements Runnable {
        @Override
        public void run() {
            while (true) {
                SleepUtils.second(100);
            }
        }
    }


    // 沉睡工具类
    static class SleepUtils {
        static void second(int second) {
            try {
                Thread.sleep(second * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
```

1、jps

![](.README_images/e4e7d51a.png)

2、 jstack 5438 可以知道输出如下所示。

```
"Blocked-2" #14 prio=5 os_prio=31 tid=0x00007fbdbf81e000 nid=0x5c03 waiting for monitor entry [0x0000700005b94000]
   java.lang.Thread.State: BLOCKED (on object monitor)
        at pers.zpw.chapter2.ThreadStateDemo$Blocked.run(ThreadStateDemo.java:22)
        - waiting to lock <0x000000066ac32b60> (a java.lang.Class for pers.zpw.chapter2.ThreadStateDemo$Blocked)
        at java.lang.Thread.run(Thread.java:748)

"Blocked-1" #13 prio=5 os_prio=31 tid=0x00007fbdc10ab800 nid=0x5b03 waiting on condition [0x0000700005a91000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
        at java.lang.Thread.sleep(Native Method)
        at pers.zpw.chapter2.ThreadStateDemo$SleepUtils.second(ThreadStateDemo.java:60)
        at pers.zpw.chapter2.ThreadStateDemo$Blocked.run(ThreadStateDemo.java:22)
        - locked <0x000000066ac32b60> (a java.lang.Class for pers.zpw.chapter2.ThreadStateDemo$Blocked)
        at java.lang.Thread.run(Thread.java:748)

"Waiting" #12 prio=5 os_prio=31 tid=0x00007fbdc2002800 nid=0xa603 in Object.wait() [0x000070000598e000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x000000066ac2e9a0> (a java.lang.Class for pers.zpw.chapter2.ThreadStateDemo$Waiting)
        at java.lang.Object.wait(Object.java:502)
        at pers.zpw.chapter2.ThreadStateDemo$Waiting.run(ThreadStateDemo.java:35)
        - locked <0x000000066ac2e9a0> (a java.lang.Class for pers.zpw.chapter2.ThreadStateDemo$Waiting)
        at java.lang.Thread.run(Thread.java:748)

"TimeWaitingThread" #11 prio=5 os_prio=31 tid=0x00007fbdbf81d000 nid=0x5803 waiting on condition [0x000070000588b000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
        at java.lang.Thread.sleep(Native Method)
        at pers.zpw.chapter2.ThreadStateDemo$SleepUtils.second(ThreadStateDemo.java:60)
        at pers.zpw.chapter2.ThreadStateDemo$TimeWaiting.run(ThreadStateDemo.java:50)
        at java.lang.Thread.run(Thread.java:748)


```
通过示例，可以了解到线程在自身的生命周期中，并不是固定的处理某个状态，而是随着代码的执行在不同的状态之间进行来回的切换。
java线程状态变迁如图所示

![](.README_images/8d4e95cc.png)


**注意：**

JAVA将操作系统中的运行和就绪两个状态合并称为运行状态。阻塞状态是线程阻塞在进入synchronized关键字修饰
的方法或代码块（获取锁时）的状态，但是阻塞在java.concurrent包中Lock接口的线程状态却是等待的状态，因为Lock接口
对于阻塞的实现均使用了LockSupport类中的相关的方法

#### 2.1.3 Daemon线程

Daemon线程是一种支持形的线程，因为它主要被用作程序中后台调度以及支持性工作。
可以通过Thread.setDaemon(true)将线程设置为Daemon线程。
我们来看这个例子

```
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

```
运行这段代码的话没有任何的输出，main线程启动了DaemonRunner线程之后随着main方法执行完毕而终止，
此时虚拟机中已经没有非Daemon线程了，虚拟机退出，然而DaemonRunner中的finally块并没有执行

注意：

在构建Daemon线程的时候，不能依靠finally块中的内容来确保执行关闭或清理资源的逻辑