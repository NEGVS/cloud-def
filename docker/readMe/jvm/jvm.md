https://res.cloudinary.com/aaco/image/upload/f_auto/q_auto/v1671096314/NANCY-_4_fdwq5n.jpg
AWK
brian kernighan 
https://blog.csdn.net/weixin_43688483/article/details/106349982

非常好的方向 👍
JVM（Java Virtual Machine）是中高级 Java 面试中**必问核心模块之一**。
下面我帮你整理出一份 **《JVM 面试必问题目清单》** ——
按主题分类、含高频题 + 要点提示，覆盖从基础到调优。

---

## 🧠 一、JVM 基础概念篇

1. **JVM 是什么？它的主要组成部分有哪些？**
   👉 组成：类加载器、运行时数据区、执行引擎、本地接口、垃圾回收系统。

2. **JDK、JRE、JVM 的区别？**
   👉 JDK 包含 JRE；JRE 包含 JVM。开发、运行、执行层级递进。

3. **什么是字节码？为什么 Java 可以跨平台？**
   👉 因为 JVM 解释执行相同的字节码，屏蔽底层 OS 差异。

4. **JVM 是如何加载一个 .class 文件的？**
   👉 经过类加载过程：加载 → 链接（验证、准备、解析）→ 初始化。
   com.ruoyi.demi.stock.stockB.entity.StockOctopus
5. com.ruoyi.demi.stock.stockB.mapper.StockOctopusMapper
5. 
---

## 🧩 二、内存结构篇（Runtime Data Area）

1. **JVM 内存区域有哪些？**
   👉 程序计数器、虚拟机栈、本地方法栈、堆、方法区（元空间）。

2. **堆和栈的区别？**

    * 栈：线程私有、存放局部变量、方法调用栈帧。
    * 堆：所有线程共享，存放对象实例。

3. **方法区和元空间的区别？**
   👉 Java 8 之后用 **Metaspace（元空间）** 替代永久代，存放类元数据。

4. **什么是直接内存（Direct Memory）？为什么会 OOM？**
   👉 NIO 使用的堆外内存，不受 -Xmx 限制，但仍计入系统内存上限。

---

## ♻️ 三、垃圾回收机制（GC）

1. **常见的 GC 算法有哪些？**

    * 标记-清除（Mark-Sweep）
    * 标记-整理（Mark-Compact）
    * 复制算法（Copying）
    * 分代收集（Generational Collection）

2. **Minor GC / Major GC / Full GC 的区别？**
   👉 新生代、小规模回收 → 老年代回收 → 整堆回收。

3. **新生代为什么使用复制算法？**
   👉 大多数对象“朝生夕死”，复制算法效率高，碎片少。

4. **常见的垃圾回收器有哪些？特点？**

   | 回收器              | 代   | 特点              |
      | ---------------- | --- | --------------- |
   | Serial           | 新生代 | 单线程、简单可靠        |
   | Parallel         | 新生代 | 多线程、高吞吐量        |
   | CMS              | 老年代 | 并发收集、低停顿        |
   | G1               | 整堆  | 可预测暂停时间、区域化管理   |
   | ZGC / Shenandoah | 整堆  | 超低延迟 (<10ms 停顿) |

5. **GC 日志怎么看？如何判断发生 Full GC？**
   👉 日志中 `[Full GC ...]`，查看原因、耗时、内存占用。

---mimansa jaiswal

## 🧮 四、类加载机制

1. **类加载过程的几个阶段？**
   👉 加载 → 验证 → 准备 → 解析 → 初始化。

2. **双亲委派模型（Parent Delegation Model）原理？**
   👉 优先交由父加载器加载，防止重复加载与安全问题。

3. **打破双亲委派的场景？**
   👉 热部署（Tomcat）、SPI（ServiceLoader）机制、自定义类加载器。

4. **常见的 ClassLoader 类型？**
   👉 Bootstrap、Extension、AppClassLoader、自定义 ClassLoader。

---

## ⚙️ 五、性能调优与实战

1. **常见 JVM 启动参数有哪些？**

    * 堆内存：`-Xms`、`-Xmx`
    * 元空间：`-XX:MaxMetaspaceSize`
    * GC：`-XX:+UseG1GC`、`-XX:+UseParallelGC`
    * 日志：`-Xlog:gc*`（JDK9+）

2. **如何排查内存泄漏？**
   👉 使用工具：`jmap`, `jstat`, `jvisualvm`, `MAT`, `arthas`。

    * Dump 堆内存 → 分析引用链 → 找出无法释放的对象。

3. **常见 OOM 类型有哪些？**

    * `java.lang.OutOfMemoryError: Java heap space`
    * `Metaspace` / `GC overhead limit exceeded`
    * `Direct buffer memory`
    * `Unable to create new native thread`

4. **JVM 调优思路？**

    * 明确问题：慢 / 卡 / 内存溢出
    * 收集数据：GC 日志、CPU、堆 dump
    * 定位瓶颈：老年代占用高？GC频繁？
    * 调参数：堆大小、GC算法、线程数

5. **如何查看 JVM 实时运行状态？**
   👉 命令：`jps`, `jstat`, `jstack`, `jmap`, `jcmd`

---

## 💡 六、实战类/开放题

1. 你如何选择合适的 GC？
2. 如果系统频繁 Full GC，你的排查思路是什么？
3. 为什么要调节 Eden 与 Survivor 区比例？
4. CMS 回收过程中出现 Concurrent Mode Failure 怎么办？
5. 生产环境如何安全修改 JVM 参数？
6. 谈谈 G1 与 CMS 的区别。
7. 你是否实际调优过 JVM？举例说明效果。

---

## 📘 Bonus：推荐学习路线

1. **理解 JVM 架构与内存模型**
2. **掌握 GC 原理与日志分析**
3. **熟悉性能工具与调优实践**
4. **阅读源码 & JVM 参数实测**

---

是否希望我帮你生成一个：

* ✅「JVM 高频面试题 + 答案详解」PDF（适合打印复习）
* ✅ 或一个「JVM 模拟面试题库（含难度等级 + 答案提示）」？

你想要哪一种版本？
