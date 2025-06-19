package xCloud.tools.codeGenerate;

import lombok.Data;
import xCloud.util.CodeX;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;


@Data
public class GenerateCode {

    /**
     * 生成的所有后端文件----------------
     */
    /**
     * todo 只需要更改这一个地方
     * 示例：
     * 数据库tableName名字:hro_recruitment_job_transfer，
     * 则命名new_name为：JobTransfer
     */
    final String tableName = "stock_data";
    final String new_name = "stockData";//需要更改，否则为默认命名

    //-----------------------------
    final String vo_name = new_name;
    final String new_name_upper = CodeX.firstToUpper(new_name);
    final String new_name_small = CodeX.firstToSmall(new_name);

    public static void main(String[] args) throws IOException, SQLException {

        //*************************************** 以下禁止更改 ***********************************
        long l = System.currentTimeMillis();
        GenerateCode generateCode = new GenerateCode();


        //读取表结构-默认读取生产数据库
        LinkedHashMap<String, String> columnName_comment_map2 = (LinkedHashMap<String, String>) CodeX.getTableStructure(generateCode.tableName);
        if (columnName_comment_map2.isEmpty()) {
            System.out.println("数据库表不存在，请检查数据库表名是否正确");
            return;
        }
        LinkedHashMap<String, String> columnName_comment_map = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : columnName_comment_map2.entrySet()) {
            if (entry.getKey().contains("remark")) {
                continue;
            }
            columnName_comment_map.put(entry.getKey(), entry.getValue());
            System.out.println(entry.getKey() + "---" + entry.getValue());
        }


        String project_root_dir = System.getProperty("user.dir") + "/andy" + generateCode.new_name_upper + "CodeGeneration/";
        System.out.println("获取当前目录");
        System.out.println(project_root_dir);
        //设置生成文件存储路径 vo.java和mapper.xml
        final String path_write_xml = project_root_dir + "mapper/" + CodeX.firstToUpper(generateCode.new_name) + "Mapper.xml";
        final String path_write_java = project_root_dir + "entity/" + CodeX.firstToUpper(generateCode.new_name) + ".java";
        final String path_write_java1 = project_root_dir + "entity/" + CodeX.firstToSmall(generateCode.new_name);
        final String path_write_test = project_root_dir + "test/" + CodeX.firstToSmall(generateCode.new_name) + "Test.sql";

        CodeX.checkDirIsExist(path_write_java);
        CodeX.checkDirIsExist(path_write_xml);
        CodeX.checkDirIsExist(path_write_java1);

        /**
         * 1-生成vo.java and mapper.xml， comment_flag：备注是否开启，默认开启,true开启、false关闭
         */
        GenerateCodeMethods generateCodeMethods = new GenerateCodeMethods();
        generateCodeMethods.generate_vo_java(columnName_comment_map, generateCode.new_name, path_write_xml, path_write_java, generateCode.tableName, true);
        /**
         * 2-生成mapper.java、service，serviceImpl
         */
        int index = 0;
        String primary_key = "";
        for (Map.Entry<String, String> stringStringEntry : columnName_comment_map.entrySet()) {
            index++;
            if (index == 1) {
                primary_key = stringStringEntry.getKey();
                continue;
            }
        }
        generateCodeMethods.generate_mapper_service(generateCode.new_name, primary_key);
        /**
         * 3-生成测试
         */
        generateCodeMethods.generate_Test(path_write_test, columnName_comment_map);

        System.out.println("恭喜你代码生成完毕");
        System.out.println("文件所在位置：" + project_root_dir);
        System.out.println("共用时:" + (System.currentTimeMillis() - l) / 1000.0 + " 秒");
    }

}
