package xCloud.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/7/11 14:34
 * @ClassName CodeX
 */
public class CodeX {

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
