package xCloud.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/7/11 14:34
 * @ClassName CodeX
 */
public class CodeX {
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final long EPOCH = 1288834974657L;  // Twitter 起始时间戳

    /**
     * 雪花算法（简化实现），在分布式环境中，避免纯随机；优先雪花算法。
     *
     * @return Long nextId
     */
    public static Long nextId() {
        long timestamp = System.currentTimeMillis();
        long seq = SEQUENCE.getAndIncrement() % 4096;  // 序列号 0-4095
        return ((timestamp - EPOCH) << 22) | seq;  // 简化：时间戳(41位) + 序列(12位)
    }

    /**
     * 001 读取文件内容，每行存储到list，返回list
     *
     * @param path
     */
    public List<String> readFileLines(final String path) {

        List<String> list = new ArrayList<>();
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            System.out.println("读取文件");

            //read file
            try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    list.add(line);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("不是文件");
            return null;
        }
        System.out.println("read over=============,all lines :" + list.size());
        return list;
    }
}
