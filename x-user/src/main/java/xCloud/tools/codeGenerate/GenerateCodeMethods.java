package xCloud.tools.codeGenerate;


import com.baomidou.mybatisplus.annotation.IdType;
import xCloud.tools.CodeX;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GenerateCodeMethods {
    public final static String enter = "\n";
    public final static String tab = "\t";

    /**
     * 1-根据bankDao\DaoImpl\service\serviceImpl生成XXXDao\DaoImpl\service\serviceImpl
     */
    public static void generate_mapper_service(final String new_name, String primary_key) throws IOException {
        GenerateCode generateCode = new GenerateCode();
        String project_root_dir = System.getProperty("user.dir") + "/andy" + generateCode.new_name_upper + "CodeGeneration/";
        final String path_write_mapper = project_root_dir + "mapper/" + generateCode.new_name_upper + "Mapper.java";
        final String path_write_service = project_root_dir + "service/" + generateCode.new_name_upper + "Service.java";
        final String path_write_service_impl = project_root_dir + "service/" + generateCode.new_name_upper + "ServiceImpl.java";
        final String path_write_controller = project_root_dir + "controller/" + generateCode.new_name_upper + "Controller.java";
        /**
         * 1-生成mapper.java
         */
        CodeX.writeFile(path_write_mapper, getMapper_JavaContent(new_name), false);
        /**
         * 2-生成 service.java
         */
        CodeX.writeFile(path_write_service, getServiceContent(new_name), false);
        /**
         * 3-生成 serviceImpl.java
         */
        CodeX.writeFile(path_write_service_impl, getServiceImplContent(new_name, primary_key), false);
        /**
         * 4-生成controller.java
         */
        CodeX.writeFile(path_write_controller, getControllerContent(new_name), false);
    }

    /**
     * 2-该方法由generate_vo_java专门使用，生成xml文件
     *
     * @param path_write  xx
     * @param list_first  xx
     * @param primary_key xx xx
     * @throws IOException xx
     */
    public static void generate_vo_xml(final LinkedHashMap<String, String> columnName_comment_map, final List<String> list_first, final String path_write, final String primary_key,
                                       final String dataBase) throws IOException {

        /**
         * 4-bufferedWriter_xml
         */
        FileOutputStream fileOutputStream_xml = new FileOutputStream(path_write);
        OutputStreamWriter outputStreamWriter_xml = new OutputStreamWriter(fileOutputStream_xml);
        BufferedWriter bufferedWriter_xml = new BufferedWriter(outputStreamWriter_xml);

        bufferedWriter_xml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"yyyyymapper\">");

        bufferedWriter_xml.write("\n<resultMap id=\"result\" type=\"xxxxxxvo\" >");
        bufferedWriter_xml.write(enter);
        /**
         * 1-resultMap----------
         */
        for (String li : list_first) {
            bufferedWriter_xml.write(" \t\t    <result property=\"");
            bufferedWriter_xml.write(li);
            bufferedWriter_xml.write("\" column=\"");
            bufferedWriter_xml.write(li);
            bufferedWriter_xml.write("\"/>" + enter);
        }
        bufferedWriter_xml.write(tab);
        bufferedWriter_xml.write("</resultMap>");
        /**
         * 2-Base_Column_List------
         */
        bufferedWriter_xml.write(enter + enter + tab);
        bufferedWriter_xml.write(" <sql id=\"" + "Base_Column_List\">");
        bufferedWriter_xml.write(enter);
        bufferedWriter_xml.write(tab + tab);
        int count2 = 0;
        for (String f : list_first) {
            count2++;
            if (!f.contains(primary_key)) {
                if (count2 == list_first.size()) {
                    bufferedWriter_xml.write(f);
                } else {
                    bufferedWriter_xml.write(f + ", ");
                }
            }
        }
        bufferedWriter_xml.write(enter);
        bufferedWriter_xml.write(tab);
        bufferedWriter_xml.write(" </sql>");
        bufferedWriter_xml.write(enter);

        /**
         * 3-Base_Column_List_For_Join--------
         */
        bufferedWriter_xml.write(enter);
        bufferedWriter_xml.write(tab);
        //Base_Column_List_For_Join
        bufferedWriter_xml.write("<sql id=\"" + "Base_Column_List_For_Join\">");
        bufferedWriter_xml.write(enter);
        bufferedWriter_xml.write(tab + tab);

        int count = 0;
        //跳过主键
        for (String f : list_first) {
            count++;
            if (!f.contains(primary_key)) {
                if (count == list_first.size()) {
                    bufferedWriter_xml.write("a." + f);
                } else {
                    bufferedWriter_xml.write("a." + f + ", ");
                }
            }
        }
        bufferedWriter_xml.write(enter + tab);
        bufferedWriter_xml.write("</sql>");
        bufferedWriter_xml.write(enter + enter);
        GenerateCode generateCode = new GenerateCode();

        /**
         * 0 --------sql--select--count(*)--------------
         */
//        bufferedWriter_xml.write("\t<select id=\"count" + generateCode.new_name_upper + "VOsByCondition\" parameterType=\"" + generateCode.new_name_small + "\" resultType=\"int\">\n" + "\t\tSELECT \n\t\t\t COUNT(*) \n" + "\t\tFROM \n\t\t\t " + dataBase + " a\n" + "\t\tWHERE \n\t\t\ta.deleted = 1 \n");
//        for (String fff : list_first) {
//            if (fff.equals("deleted")) {
//                continue;
//            }
//            if (fff.equals("status")) {
//                bufferedWriter_xml.write("\t\t<if test=\"status != null and status != ''\">\n" + "\t\t\tAND (a.status in (${status}) or 0 = #{status})\n" + "\t\t</if>\n");
//                continue;
//            }
//            bufferedWriter_xml.write("\t\t<if test=\"" + fff + " != null and " + fff + " != ''\">\n" + "\t\tAND a." + fff + " = #{" + fff + "}\n" + "\t\t</if>" + enter);
//        }
//        bufferedWriter_xml.write("\t\t<if test=\"keywordsSQL != null and keywordsSQL != ''\">\n" + "\t\t\tAND ${keywordsSQL}\n" + "\t\t</if>\t" + enter);
//        bufferedWriter_xml.write("\n\t</select>");
//


        /**
         * 1------insert---输出：VALUES (#{accountId}, #{corpId}, #{cityId}, #{nameZH},...  );
         */
        String xxxxxxvo = "xxxxxxvo";
        bufferedWriter_xml.write("\t<!--1.新增-->" + enter);
        bufferedWriter_xml.write("\t<insert id=\"insert" + generateCode.new_name_upper + "\" parameterType=\"" + xxxxxxvo + "\" useGeneratedKeys=\"true\" keyProperty=\"" + primary_key + "\">");
        bufferedWriter_xml.write("\n\t\tINSERT INTO \n\t\t\t" + dataBase + " (<include refid=\"" + "Base_Column_List\"/>) \n");
        bufferedWriter_xml.write("\t\tVALUES \n\t\t\t(");
        int count7 = 0;
        for (String fff : list_first) {
            count7++;
            if (fff.equals(primary_key)) {
                continue;
            }
            if (fff.equals("deleted")) {
                bufferedWriter_xml.write("1, ");
                continue;
            }
            bufferedWriter_xml.write("#{");
            bufferedWriter_xml.write(fff);
            if (count7 == list_first.size()) {
                bufferedWriter_xml.write("}");
            } else {
                bufferedWriter_xml.write("}, ");
            }
        }
        bufferedWriter_xml.write(")");
        bufferedWriter_xml.write(enter);
        bufferedWriter_xml.write("\t</insert>\n\n");


        /**
         * 1.1----批量--insert---输出：VALUES (#{accountId}, #{corpId}, #{cityId}, #{nameZH},...  );
         */
        bufferedWriter_xml.write("\t<!--1.1 批量新增-->" + enter);
        bufferedWriter_xml.write("\t<insert id=\"insert" + generateCode.new_name_upper + "List" + "\" parameterType=\"" + "java.util.List" + "\" useGeneratedKeys=\"true\" >");
        bufferedWriter_xml.write("\n\t\tINSERT INTO \n\t\t\t" + dataBase + " (<include refid=\"" + "Base_Column_List\"/>) \n");
        bufferedWriter_xml.write("\t\tVALUES \n\t\t\t");
        bufferedWriter_xml.write("<foreach collection=\"list\" item=\"x\" index=\"index\" separator=\",\">");
        int count_insert_2 = 0;
        bufferedWriter_xml.write("(");
        for (String fff : list_first) {
            count_insert_2++;
            if (fff.equals(primary_key)) {
                continue;
            }
            if (fff.equals("deleted")) {
                bufferedWriter_xml.write("1, ");
                continue;
            }
            bufferedWriter_xml.write("#{x.");
            bufferedWriter_xml.write(fff);
            if (count_insert_2 == list_first.size()) {
                bufferedWriter_xml.write("}");
            } else {
                bufferedWriter_xml.write("}, ");
            }
        }
        bufferedWriter_xml.write(")");
        bufferedWriter_xml.write(enter + "\t\t");
        bufferedWriter_xml.write("</foreach>");

        bufferedWriter_xml.write("\t</insert>\n\n");


        /**
         * 2----delete---------
         */
        bufferedWriter_xml.write("\t<!--2.删除-->");
        bufferedWriter_xml.write("\n\t<delete id=\"delete" + generateCode.new_name_upper + "\" parameterType=\"" + xxxxxxvo + "\">" + enter);
        bufferedWriter_xml.write("\t\tUPDATE \n\t\t\t" + dataBase + " \n\t\tSET \n\t\t\t deleted = 2, modifyBy = #{modifyBy}, modifyDate = Now()\n\t\tWHERE\n\t\t\t" + primary_key + " = #{" + primary_key + "}" + enter);
        bufferedWriter_xml.write("\t</delete>\n" + enter);


        /**
         * 3====================update===================
         */


        //bufferedWriter_xml.write( " [8].------SQL---输出： SET cityId          = #{cityId}------------------");
        bufferedWriter_xml.write("\t<!--3.修改-->" + enter);
        bufferedWriter_xml.write("\t<update id=\"update" + generateCode.new_name_upper + "\" parameterType=\"" + xxxxxxvo + "\">  " + enter + tab + tab);
        bufferedWriter_xml.write("UPDATE\n\t\t\t" + dataBase + "\n \t\t<set>\n");
        for (String fff : list_first) {
            if (fff.contains(primary_key)) {
                continue;
            }
//            if (fff.contains("modifyDate")) {
//                bufferedWriter_xml.write("modifyDate = Now()");
//                continue;
//            }
            bufferedWriter_xml.write("\t\t\t<if test=\"" + fff + " != null\">\n" +
                    "                " + fff + " = #{" + fff + "},\n" +
                    "            </if>\n");
        }
        bufferedWriter_xml.write("\n\t\t</set>");

        //      add ,insert "+primary_key+"
        bufferedWriter_xml.write("\t\tWHERE \n\t\t\t" + primary_key + " = #{" + primary_key + "}  \t\t\n" + "  \t</update>  \t" + enter);


        /**
         * 4.select all
         */
        bufferedWriter_xml.write("\n\t<!--4.查询-->" + enter);
        bufferedWriter_xml.write("\t<select id=\"select" + generateCode.new_name_upper + "\" parameterType=\"" + xxxxxxvo + "\" resultMap=\"result\">\n" + "\t\tSELECT \n\t\t\ta." + primary_key + ", <include refid=\"" + "Base_Column_List_For_Join\"/> \n" + "\t\tFROM \n\t\t\t" + dataBase + " a\n\t\tWHERE \n\t\t\ta.deleted = 1");
        bufferedWriter_xml.write(enter);


        for (String fff : list_first) {
            if (fff.equals("deleted")) {
                continue;
            }
//            if (fff.equals("status")) {
//                bufferedWriter_xml.write("\t\t<if test=\"status != null\">\n"
//                        + "\t\t\tAND (a.status in (${status}) or 0 = #{status})\n" + "\t\t</if>\n");
//                continue;
//            }
//            and " + fff + " != ''
//            todo
            String type_value = columnName_comment_map.get(fff);
            String type = "";
            if (type_value.contains("@")) {
                type = type_value.split("@")[1];
            } else {
                type = type_value;
            }
            //只有字符串才加空判断
            if (type.toLowerCase().contains("string")) {
                bufferedWriter_xml.write("\t\t<if test=\"entity." + fff + " != null and entity." + fff + " != ''\">\n"
                        + "\t\t\tAND a." + fff + " = #{entity." + fff + "}\n" + "\t\t</if>\n");
            } else {
                bufferedWriter_xml.write("\t\t<if test=\"entity." + fff + " != null\">\n"
                        + "\t\t\tAND a." + fff + " = #{entity." + fff + "}\n" + "\t\t</if>\n");
            }
        }
//        bufferedWriter_xml.write("\t\t<if test=\"keywordsSQL != null and keywordsSQL != ''\">\n" + "\t\t\tAND ${keywordsSQL}\n" +
//                "\t\t</if>\n" + "\t\t<if test=\"sortColumn != null and sortColumn != ''\">\n" + "\t\t\tORDER BY ${sortColumn} \n" +
//                "\t\t\t<if test=\"sortOrder != null and sortOrder != ''\">\n" + "\t\t\t\t${sortOrder}\n" + "\t\t\t</if>\n" + "\t\t</if>");
        bufferedWriter_xml.write("\n\t</select>\n\n");


        bufferedWriter_xml.write("</mapper>");

        bufferedWriter_xml.close();
        outputStreamWriter_xml.close();
        fileOutputStream_xml.close();

    }

    /**
     * 3-直接生成xxx.java,xxxVO.java,xxxDTO.java,xxxMapper.xml
     *
     * @param new_name        xx
     * @param path_write_xml  xx
     * @param path_write_java xx
     * @param comment_flag    //是否写字段的备注,默认显示
     * @throws IOException xx
     */
    public static void generate_vo_java(final LinkedHashMap<String, String> columnName_comment_map, final String new_name, final String path_write_xml,
                                        final String path_write_java, String dataBase, boolean comment_flag) throws IOException {
        //开始写入
        FileOutputStream fileOutputStream = new FileOutputStream(path_write_java);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        GenerateCode generateCode = new GenerateCode();
        bufferedWriter.write("import com.baomidou.mybatisplus.annotation.IdType;\n" +
                "import com.baomidou.mybatisplus.annotation.TableField;\n" +
                "import com.baomidou.mybatisplus.annotation.TableId;\n" +
                "import com.baomidou.mybatisplus.annotation.TableName;\n" +
                "import com.fasterxml.jackson.annotation.JsonInclude;\n" +
                "import io.swagger.v3.oas.annotations.media.Schema;\n" +
                "import lombok.AllArgsConstructor;\n" +
                "import lombok.Data;\n" +
                "import lombok.NoArgsConstructor;\n" +
                "\n" +
                "import java.io.Serial;\n" +
                "import java.io.Serializable;\n" +
                "import java.util.Date;" +
                "\n@NoArgsConstructor\n" +
                "@AllArgsConstructor\n" +
                "@JsonInclude(JsonInclude.Include.NON_EMPTY)\n" +
                "@TableName(value =\"" + generateCode.tableName + "\")\n" +
                "@Schema(name = \"" + generateCode.vo_name + "\", description = \"" + generateCode.vo_name + "\")\n" +
                "@Data\n" +
                "public class " + generateCode.new_name_upper + " implements Serializable {");

        bufferedWriter.write("\n\t@Serial\n" +
                "\t@TableField(exist = false)\n" +
                "\tprivate static final long serialVersionUID = 1L;" + enter);

        /**
         * 1- 生成字段-备注 private String xxx;
         */
        //到底的desc为止的字段
        List<String> list_vo = new ArrayList<>();
        //所有字段
        List<String> list_vo_all = new ArrayList<>();
        columnName_comment_map.forEach((columnName, comment) -> {
            list_vo_all.add(columnName);
        });
        int index223 = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> columnName_comment : columnName_comment_map.entrySet()) {
            index223++;
            list_vo.add(columnName_comment.getKey());
            if (index223 == 1) {
                bufferedWriter.write("\n\t@TableId(value=\"" + columnName_comment.getKey() + "\"+type = IdType.AUTO)" + enter);
            }
            String descriptionValue = "";
            String columnType = "";
            //如果包含@说明既有描述，也有类型，否则只有类型
            if (columnName_comment.getValue().contains("@")) {
                String[] split = columnName_comment.getValue().split("@");
                descriptionValue = split[0];
                columnType = split[1];
            } else {
                //如果没有备注就用该字段名
                descriptionValue = columnName_comment.getKey();
                columnType = columnName_comment.getValue();
            }
            String ssss1 = "\t@Schema(name = \"" + columnName_comment.getKey() + "\",description = \"" + descriptionValue + "\")\n";
            bufferedWriter.write(ssss1);
//            ⚠️ MyBatis-Plus 提示你：因为这个字段是主键（用了 @TableId），所以 @TableField 注解不会起作用。-- 为什么？
            //在 MyBatis-Plus 中：
            //@TableId 专门用来标记主键字段
            //@TableField 用来配置普通字段的属性（如数据库字段名、是否自动填充、是否存在等）
            //当一个字段被标记为 @TableId，它的元信息（如字段名、自动增长策略等）由 @TableId 控制，不会再读取 @TableField 的信息
//            bufferedWriter.write("\t@TableField(\"" + columnName_comment.getKey() + "\")" + enter);
            String ssss2 = "\tprivate " + columnType + " " + columnName_comment.getKey() + ";" + enter + enter;
            bufferedWriter.write(ssss2);
            stringBuilder.append(ssss1);
            stringBuilder.append(ssss2);
        }
        bufferedWriter.write("}");

        final String primary_key = list_vo.get(0);
        /**
         * 生成xml文件
         */
        generate_vo_xml(columnName_comment_map, list_vo_all, path_write_xml, primary_key, dataBase);

        String path_VO = path_write_java.substring(0, path_write_java.lastIndexOf("/")) + "/" + generateCode.new_name_upper + "VO.java";
        String path_DTO = path_write_java.substring(0, path_write_java.lastIndexOf("/")) + "/" + generateCode.new_name_upper + "DTO.java";
//        stringBuilder
        generate_VO(path_VO, stringBuilder);
        generate_DTO(path_DTO, stringBuilder);
        bufferedWriter.close();
        outputStreamWriter.close();
        fileOutputStream.close();
    }

    public static void generate_VO(String path_VO, StringBuilder stringBuilder) {
        CodeX.checkDirIsExist(path_VO);
        GenerateCode generateCode = new GenerateCode();
        StringBuilder stringBuilder1 = new StringBuilder();


        stringBuilder1.append("\n" +
                "import io.swagger.v3.oas.annotations.media.Schema;\n" +
                "import lombok.Data;\n" +
                "import java.util.Date;\n@Data\n" +
                "@Schema(name = \"" + generateCode.new_name_upper + "VO\", description = \"商保方案明细-参数集合对象\")\n" +
                "public class " + generateCode.new_name_upper + "VO  extends PagerModel {");
        stringBuilder1.append(stringBuilder);
        stringBuilder1.append("\n" + "}");
        CodeX.writeFile(path_VO, stringBuilder1.toString(), true);
    }

    public static void generate_DTO(String path_DTO, StringBuilder stringBuilder) {
        CodeX.checkDirIsExist(path_DTO);
        GenerateCode generateCode = new GenerateCode();

        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append("\n" +
                "import io.swagger.v3.oas.annotations.media.Schema;\n" +
                "import lombok.Data;\n" +
                "import java.util.Date;\n@Data\n" +
                "@Schema(name = \"" + generateCode.new_name_upper + "DTO\", description = \"用于页面搜索条件\")\n" +
                "public class " + generateCode.new_name_upper + "DTO extends PagerModel {");
        stringBuilder1.append(stringBuilder);
        stringBuilder1.append("}");
        CodeX.writeFile(path_DTO, stringBuilder1.toString(), true);
    }


    public static String getControllerContent(final String new_name) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        GenerateCode generateCode = new GenerateCode();

        stringBuilder.append("import cn.hutool.json.JSONUtil;\n" +
                "import io.swagger.v3.oas.annotations.Operation;\n" +
                "import io.swagger.v3.oas.annotations.Parameter;\n" +
                "import io.swagger.v3.oas.annotations.media.Content;\n" +
                "import io.swagger.v3.oas.annotations.media.Schema;\n" +
                "import io.swagger.v3.oas.annotations.responses.ApiResponse;\n" +
                "import io.swagger.v3.oas.annotations.tags.Tag;\n" +
                "import jakarta.annotation.Resource;\nimport com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n" +
                "import jakarta.servlet.http.HttpServletResponse;\n" +
                "import lombok.extern.slf4j.Slf4j;\n" +
                "import org.springframework.web.bind.annotation.GetMapping;\n" +
                "import org.springframework.web.bind.annotation.PostMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestBody;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestParam;\n" +
                "import org.springframework.web.bind.annotation.RestController;\n" +
                "import org.springframework.web.multipart.MultipartFile;\n" +
                "import xCloud.entity.ResultEntity;\n/**\n" +
                " * @Description Example接口\n" +
                " * @Author Andy Fan\n" +
                " * @Date " + CodeX.getNowTime() + "\n" +
                " * @ClassName ExampleController\n" +
                " */\n" +
                "@RestController\n" +
                "@RequestMapping(\"biz/example\")\n" +
                "@Slf4j\n" +
                "@Tag(name = \"Example接口\", description = \"Example接口\")\n" +
                "public class ExampleController {");

        stringBuilder.append("\n\n\t@Resource" +
                "\n\tprivate ExampleService exampleService;\n" +
                "\n" +
                "    /**\n" +
                "     * 1-增加\n" +
                "     *\n" +
                "     * @param dto 前端请求dto\n" +
                "     * @return 查询结果\n" +
                "     */\n" +
                "    @Operation(summary = \"新增\")\n" +
                "    @ApiResponse(responseCode = \"200\", description = \"新增成功\", content = @Content(schema = @Schema(implementation = Example.class)))\n" +
                "    @PostMapping(\"/add\")\n" +
                "    public ResultEntity<Example> add(@RequestBody ExampleDTO dto) {\n" +
                "        log.info(\"新增数据参数：{}\", JSONUtil.toJsonStr(dto));\n" +
                "        return exampleService.add(dto);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 2-删除\n" +
                "     *\n" +
                "     * @param dto 前端请求dto\n" +
                "     * @return 查询结果\n" +
                "     */\n" +
                "    @Operation(summary = \"删除\", description = \"删除\")\n" +
                "    @ApiResponse(responseCode = \"200\", description = \"删除成功\", content = @Content(schema = @Schema(implementation = Example.class)))\n" +
                "    @PostMapping(\"/delete\")\n" +
                "    public ResultEntity<Example> delete(@RequestBody ExampleDTO dto) {\n" +
                "        log.info(\"更新数据参数：{}\", JSONUtil.toJsonStr(dto));\n" +
                "        return exampleService.delete(dto);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 3-修改\n" +
                "     *\n" +
                "     * @param dto 前端请求VO\n" +
                "     * @return 查询结果\n" +
                "     */\n" +
                "    @Operation(summary = \"更新接口\", description = \"更新数据\")\n" +
                "    @ApiResponse(responseCode = \"200\", description = \"更新成功\", content = @Content(schema = @Schema(implementation = Example.class)))\n" +
                "    @PostMapping(\"/update\")\n" +
                "    public ResultEntity<Example> update(@RequestBody ExampleDTO dto) {\n" +
                "        log.info(\"更新数据参数：{}\", JSONUtil.toJsonStr(dto));\n" +
                "        return exampleService.update(dto);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 4-查询-列表\n" +
                "     *\n" +
                "     * @param dto 列表搜索\n" +
                "     * @return 列表\n" +
                "     */\n" +
                "    @Operation(summary = \"获取列表\")\n" +
                "    @ApiResponse(responseCode = \"200\", description = \"获取成功\", content = @Content(schema = @Schema(implementation = Example.class)))\n" +
                "    @PostMapping(value = \"/list\")\n" +
                "    public ResultEntity<Page<Example>> list(@RequestBody ExampleDTO dto) {\n" +
                "        log.info(\"列表查询参数：{}\", JSONUtil.toJsonStr(dto));\n" +
                "        return exampleService.list(dto);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "     * 4.1-查询-详情\n" +
                "     *\n" +
                "     * @param dto \n" +
                "     * @return 基本\n" +
                "     */\n" +
                "    @Parameter(name = \"exampleId\", description = \"Id\", required = true)\n" +
                "    @Operation(summary = \"列表-点击详情\", description = \"详情数据\")\n" +
                "    @ApiResponse(responseCode = \"200\", description = \"详情\", content = @Content(schema = @Schema(implementation = Example.class)))\n" +
                "    @PostMapping(\"/detail\")\n" +
                "    public ResultEntity<ExampleVO> detail(@RequestBody ExampleDTO dto) throws Exception {\n" +
                "        log.info(\"查询详情参数：{}\", JSONUtil.toJsonStr(dto));\n" +
                "        return exampleService.detail(dto);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 5-导入\n" +
                "     *\n" +
                "     * @param multipartFile 文件流\n" +
                "     * @param response      响应流\n" +
                "     */\n" +
                "    @Operation(summary = \"导入\")\n" +
                "    @Parameter(name = \"uploadFile\", description = \"导入文件\", required = true)\n" +
                "    @ApiResponse(responseCode = \"200\", description = \"导入成功\")\n" +
                "    @PostMapping(\"importFile\")\n" +
                "    public void importFile(@RequestParam(\"uploadFile\") MultipartFile multipartFile, HttpServletResponse response) throws Exception {\n" +
                "        exampleService.importFile(multipartFile, \"userId\", response);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 5.1-下载导入模板\n" +
                "     */\n" +
                "    @Operation(summary = \"模版下载接口\", description = \"模版下载接口\")\n" +
                "    @ApiResponse(responseCode = \"200\", description = \"模版下载接口\")\n" +
                "    @GetMapping(\"/downloadTemplate\")\n" +
                "    public void downloadTemplate(HttpServletResponse response) throws Exception {\n" +
                "        exampleService.downloadTemplate(response);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 6-导出\n" +
                "     */\n" +
                "    @Operation(summary = \"导出\", description = \"导出\")\n" +
                "    @PostMapping(\"/export\")\n" +
                "    public void export(@RequestBody ExampleDTO dto, HttpServletResponse response) throws Exception {\n" +
                "        log.info(\"导出的查询参数：{}\", JSONUtil.toJsonStr(dto));\n" +
                "        exampleService.exportFile(dto, response);\n" +
                "    }");
        stringBuilder.append("}");
        return stringBuilder.toString().replace("Example", generateCode.new_name_upper).replace("example", generateCode.new_name_small);
    }

    public static String getServiceContent(final String new_name) throws IOException {
        GenerateCode generateCode = new GenerateCode();
        StringBuilder sb_service = new StringBuilder();
        sb_service.append("import java.util.Map;\n/**\n" +
                " * @author AndyFan\n" +
                " * @description 针对表【" + generateCode.tableName + "】的数据库操作Service\n" +
                " * @createDate " + CodeX.getNowTime() +
                " */\n" +
                "public interface " + generateCode.new_name_upper + "Service extends IService< " + generateCode.new_name_upper + " >\n" +
                "{");
        sb_service.append("/**\n" +
                "     * 1-新增\n" +
                "     *\n" +
                "     * @param dto dto\n" +
                "     * @return 成功条数\n" +
                "     */\n" +
                "    ResultEntity<Example> add(ExampleDTO dto);\n" +
                "\n" +
                "    /**\n" +
                "     * 2-删除\n" +
                "     *\n" +
                "     * @param dto dto\n" +
                "     * @return 成功条数\n" +
                "     */\n" +
                "    ResultEntity<Example> delete(ExampleDTO dto);\n" +
                "\n" +
                "    /**\n" +
                "     * 3-更新\n" +
                "     *\n" +
                "     * @param dto dto\n" +
                "     * @return 成功条数\n" +
                "     */\n" +
                "    ResultEntity<Example> update(ExampleDTO dto);\n" +
                "\n" +
                "    /**\n" +
                "     * 4-查询-列表\n" +
                "     *\n" +
                "     * @param dto 列表搜索\n" +
                "     * @return 列表\n" +
                "     */\n" +
                "    ResultEntity<Page<Example>> list(ExampleDTO dto);\n" +
                "\n" +
                "    /**\n" +
                "     * 4.1-查询-详情\n" +
                "     *\n" +
                "     * @param dto \n" +
                "     * @return 基本信息\n" +
                "     */\n" +
                "    ResultEntity<ExampleVO>  detail(ExampleDTO dto);\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "     * 5-导入\n" +
                "     *\n" +
                "     * @param multipartFile 文件流\n" +
                "     * @param userId        用户id\n" +
                "     * @param response      响应流\n" +
                "     */\n" +
                "    void importFile(MultipartFile multipartFile, String userId, HttpServletResponse response) throws IOException;\n" +
                "\n" +
                "    /**\n" +
                "     * 5.1-下载导入模板\n" +
                "     */\n" +
                "    void downloadTemplate(HttpServletResponse response) throws Exception;\n" +
                "\n" +
                "    /**\n" +
                "     * 6-导出\n" +
                "     *\n" +
                "     * @param dto  搜索条件\n" +
                "     */\n" +
                "    void exportFile(ExampleDTO dto, HttpServletResponse response) throws Exception;\n");

        sb_service.append("\n}");
        return sb_service.toString().replace("Example", generateCode.new_name_upper).replace("example", generateCode.new_name_small);
    }

    public static String getServiceImplContent(final String new_name, String primary_key) throws IOException {
        GenerateCode generateCode = new GenerateCode();
        StringBuilder sb_service_impl = new StringBuilder();
        sb_service_impl.append("" +
                "import jakarta.annotation.Resource;/**\n" +
                " * @author andy_mac\n" +
                " * @description 针对表【" + generateCode.tableName + "】的数据库操作Service实现\n" +
                " * @createDate " + CodeX.getNowTime() +
                " */\n" +
                "@Service\n" +
                "public class " + generateCode.new_name_upper + "ServiceImpl extends ServiceImpl< " + generateCode.new_name_upper + "Mapper, " + generateCode.new_name_upper + " > implements " + generateCode.new_name_upper + "Service\n" +
                "{\n");

        sb_service_impl.append("\n@Resource\n\tprivate ExampleMapper exampleMapper;\n");
        sb_service_impl.append("/**\n" +
                "     * 1-新增\n" +
                "     *\n" +
                "     * @param dto dto\n" +
                "     * @return  ResultEntity\n" +
                "     */\n" +
                "    @Override\n" +
                "    public ResultEntity<Example> add(ExampleDTO dto) {\n" +
                "        if (ObjectUtil.isEmpty(dto)) {\n" +
                "            return ResultEntity.error(\"新增失败，参数为空\");\n" +
                "        }\n" +
                "        //dto-->entity\n" +
                "        Example example = new Example();\n" +
                "        BeanUtils.copyProperties(dto, example);\n" +
                "        int count = exampleMapper.insertExample(example);\n" +
                "        if (count > 0) {\n" +
                "            return ResultEntity.success(example);\n" +
                "        } else {\n" +
                "            return ResultEntity.error(\"新增失败\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 2-删除\n" +
                "     *\n" +
                "     * @param dto dto\n" +
                "     * @return  ResultEntity\n" +
                "     */\n" +
                "    @Override\n" +
                "    public ResultEntity<Example> delete(ExampleDTO dto) {\n" +
                "        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getDetailId())) {\n" +
                "            return ResultEntity.error(\"删除失败，参数为空\");\n" +
                "        }\n" +
                "        //dto-->entity\n" +
                "        Example example = new Example();\n" +
                "        BeanUtils.copyProperties(dto, example);\n" +
                "        int count = exampleMapper.deleteExample(example);\n" +
                "        if (count > 0) {\n" +
                "            return ResultEntity.success(example);\n" +
                "        } else {\n" +
                "            return ResultEntity.error(\"删除失败\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 3-更新\n" +
                "     *\n" +
                "     * @param dto dto\n" +
                "     * @return  ResultEntity\n" +
                "     */\n" +
                "    @Override\n" +
                "    public ResultEntity<Example> update(ExampleDTO dto) {\n" +
                "        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getDetailId())) {\n" +
                "            return ResultEntity.error(\"更新失败，参数为空\");\n" +
                "        }\n" +
                "        //dto-->entity\n" +
                "        Example example = new Example();\n" +
                "        BeanUtils.copyProperties(dto, example);\n" +
                "        int count = exampleMapper.updateExample(example);\n" +
                "        if (count > 0) {\n" +
                "            return ResultEntity.success(example);\n" +
                "        } else {\n" +
                "            return ResultEntity.error(\"更新失败\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 4-查询-列表/搜索\n" +
                "     *\n" +
                "     * @param dto 列表搜索\n" +
                "     * @return  ResultEntity\n" +
                "     */\n" +
                "    @Override\n" +
                "    public ResultEntity<Page<Example>> list(ExampleDTO dto) {\n" +
                "        if (ObjectUtil.isEmpty(dto)) {\n" +
                "            return ResultEntity.error(\"查询失败，参数为空\");\n" +
                "        }\n" +
                "        Page<Example> page = new Page<>(dto.getCurrent(), dto.getSize());\n" +
                "        //dto-->entity\n" +
                "        Example example = new Example();\n" +
                "        BeanUtils.copyProperties(dto, example);\n" +
                "        //分页查询\n" +
                "        Page<Example> examples = exampleMapper.selectExample(page, example);\n" +
                "        if (ObjectUtil.isNotEmpty(examples)) {\n" +
                "            //entity-->vo\n" +
                "            List<ExampleVO> exampleVOS = examples.getRecords().stream().map(exampleTemp -> {\n" +
                "                ExampleVO exampleVO = new ExampleVO();\n" +
                "                BeanUtils.copyProperties(exampleTemp, exampleVO);\n" +
                "                return exampleVO;\n" +
                "            }).toList();\n" +
                "            return ResultEntity.success(examples);\n" +
                "        } else {\n" +
                "            return ResultEntity.success(page);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 4.1-查询-详情\n" +
                "     *\n" +
                "     * @param dto dto\n" +
                "     * @return  ResultEntity\n" +
                "     */\n" +
                "    @Override\n" +
                "    public ResultEntity<ExampleVO> detail(ExampleDTO dto) {\n" +
                "        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getDetailId())) {\n" +
                "            return ResultEntity.error(\"查询失败，参数为空\");\n" +
                "        }\n" +
                "        //dto-->entity\n" +
                "        Example example = new Example();\n" +
                "        example.setDetailId(dto.getDetailId());\n" +
                "        List<Example> examples = exampleMapper.selectExample(example);\n" +
                "        if (ObjectUtil.isNotEmpty(examples)) {\n" +
                "            //entity-->vo\n" +
                "            Example exampleTemp = examples.get(0);\n" +
                "            ExampleVO exampleVO = new ExampleVO();\n" +
                "            BeanUtils.copyProperties(exampleTemp, exampleVO);\n" +
                "            return ResultEntity.success(exampleVO);\n" +
                "        } else {\n" +
                "            return ResultEntity.error(\"查询失败\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 5-导入\n" +
                "     *\n" +
                "     * @param multipartFile 文件流\n" +
                "     * @param userId        用户id\n" +
                "     * @param response      响应流\n" +
                "     * @throws IOException\n" +
                "     */\n" +
                "    @Override\n" +
                "    public void importFile(MultipartFile multipartFile, String userId, HttpServletResponse response) throws IOException {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 5.1-模版下载\n" +
                "     *\n" +
                "     * @param response response\n" +
                "     * @throws Exception Exception\n" +
                "     */\n" +
                "    @Override\n" +
                "    public void downloadTemplate(HttpServletResponse response) throws Exception {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * 6-导出\n" +
                "     *\n" +
                "     * @param dto      搜索条件\n" +
                "     * @param response response\n" +
                "     * @throws Exception Exception\n" +
                "     */\n" +
                "    @Override\n" +
                "    public void exportFile(ExampleDTO dto, HttpServletResponse response) throws Exception {\n" +
                "\n" +
                "\n" +
                "    }");
        sb_service_impl.append("\n}");
//
        return sb_service_impl.toString().replace("Example", generateCode.new_name_upper).replace("example", generateCode.new_name_small);
    }

    public static String getMapper_JavaContent(final String new_name) throws IOException {

        GenerateCode generateCode = new GenerateCode();
        StringBuilder sb_mapper = new StringBuilder();

        sb_mapper.append("import com.baomidou.mybatisplus.core.mapper.BaseMapper;\nimport org.apache.ibatis.annotations.Param;\n" +
                "import org.springframework.stereotype.Repository;\nimport java.util.List;");
        sb_mapper.append("\n/**\n" +
                " * @author AndyFan\n" +
                " * @description 针对表【" + generateCode.tableName + "】的数据库操作Mapper\n" +
                " * @createDate " + CodeX.getNowTime() +
                " */");
        sb_mapper.append("");
        sb_mapper.append("\n@Repository\n" +
                "public interface " + generateCode.new_name_upper + "Mapper extends BaseMapper<" + generateCode.new_name_upper + "> {\n");

//        sb_mapper.append(generateCode.vo_name + " select" + generateCode.vo_name + "ByCondition(" + generateCode.vo_name + " " + CodeX.firstToSmall(generateCode.vo_name) + ");\n");
//        sb_mapper.append(generateCode.vo_name + " select" + generateCode.vo_name + "ById(@Param(\"Id\") String Id);\n");

        sb_mapper.append("  /**\n" +
                "     * 1-新增\n" +
                "     *\n" +
                "     * @param entity entity\n" +
                "     * @return int\n" +
                "     */\n" +
                "    int insertExample(Example entity);\n" +
                "\n" +
                "    /**\n" +
                "     * 2-删除\n" +
                "     *\n" +
                "     * @param entity\n" +
                "     * @return\n" +
                "     */\n" +
                "    int deleteExample(Example entity);\n" +
                "\n" +
                "    /**\n" +
                "     * 3-修改\n" +
                "     *\n" +
                "     * @param entity entity\n" +
                "     * @return int\n" +
                "     */\n" +
                "    int updateExample(Example entity);\n" +
                "\n" +
                "    /**\n" +
                "     * 4-查询\n" +
                "     *\n" +
                "     * @param entity entity\n" +
                "     * @return List\n" +
                "     */\n" +
                "    List<Example> selectExample(@Param(\"entity\")Example entity);" + "\n /**\n" +
                "     * 4-查询\n" +
                "     *\n" +
                "     * @param page page\n" +
                "     * @param entity entity\n" +
                "     * @return Page" +
                "     */\n" +
                "    Page<Example> selectExample(@Param(\"page\")Page<Example> page, @Param(\"entity\")Example entity);");
        sb_mapper.append("\n}");
        return sb_mapper.toString().replace("Example", generateCode.new_name_upper).replace("example", generateCode.new_name_small);
    }

    public static String getMapper_XMLContent(final String new_name) throws IOException {
        GenerateCode generateCode = new GenerateCode();

        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.toString().replace("Example", generateCode.new_name_upper).replace("example", generateCode.new_name_small);
    }

    /**
     * 4-generate_Test
     *
     * @throws IOException new_name
     */
    public static void generate_Test(final String path, LinkedHashMap<String, String> columnName_comment_map) throws IOException {
        GenerateCode generateCode = new GenerateCode();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n" +
                "    /**\n" +
                "     * 单元测试方法\n" +
                "     */\n" +
                "    @Test\n" +
                "    void Test001() {\n" +
                "        List<SettleAdjustmentDetail> list = new ArrayList<>();\n" +
                "        Random random = new Random();\n" +
                "        int praimaryKey = 0;\n" +


                "        for (int i = 0; i < 5; i++) {\n" +
                "            SettleAdjustmentDetail settleAdjustmentDetail = new SettleAdjustmentDetail();\n");

        for (Map.Entry<String, String> stringStringEntry : columnName_comment_map.entrySet()) {
            // 获取字段名
            String fieldName = stringStringEntry.getKey();
            // 将字段名首字母大写，用于生成 setter 方法名
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            // 生成 setter 方法调用代码
            stringBuilder.append(String.format(generateCode.new_name_upper + ".%s(%s);\n", setterName, "1"));
        }
        stringBuilder.append(

                "            int insertCount = settleAdjustmentDetailMapper.insertSettleAdjustmentDetail(settleAdjustmentDetail);\n" +
                        "            praimaryKey = settleAdjustmentDetail.getAdjustmentDetailId();\n" +
                        "\n" +
                        "            System.out.println(\"praimaryKey\");\n" +
                        "            System.out.println(praimaryKey);\n" +
                        "            if (insertCount == 1) {\n" +
                        "                System.out.println(\"1-单个插入数据测试成功\");\n" +
                        "            }\n" +
                        "\n" +
                        "            if (i == 1) {\n" +
                        "\n" +
                        "                /**\n" +
                        "                 * 搜索测试\n" +
                        "                 */\n" +
                        "                List<SettleAdjustmentDetail> settleAdjustmentDetailList = settleAdjustmentDetailMapper.selectSettleAdjustmentDetail(settleAdjustmentDetail);\n" +
                        "                if (ObjectUtil.isNotNull(settleAdjustmentDetailList)) {\n" +
                        "                    System.out.println(\"3-搜索测试成功\");\n" +
                        "                    System.out.println(JSONUtil.toJsonStr(settleAdjustmentDetailList));\n" +
                        "                }\n" +
                        "\n" +
                        "                settleAdjustmentDetail.setDescription(\"修改数据\");\n" +
                        "                int i_update = settleAdjustmentDetailMapper.updateSettleAdjustmentDetail(settleAdjustmentDetail);\n" +
                        "                if (i_update == 1) {\n" +
                        "                    System.out.println(\"3-修改数据成功\");\n" +
                        "                }\n" +
                        "\n" +
                        "                List<SettleAdjustmentDetail> settleAdjustmentDetailList222 = settleAdjustmentDetailMapper.selectSettleAdjustmentDetail(settleAdjustmentDetail);\n" +
                        "                if (ObjectUtil.isNotNull(settleAdjustmentDetailList222)) {\n" +
                        "                    System.out.println(\"4-修改后--搜索测试成功\");\n" +
                        "                    System.out.println(JSONUtil.toJsonStr(settleAdjustmentDetailList222));\n" +
                        "                }\n" +
                        "\n" +
                        "                int i2 = settleAdjustmentDetailMapper.deleteSettleAdjustmentDetail(settleAdjustmentDetail);\n" +
                        "                if (i2 == 1) {\n" +
                        "                    System.out.println(\"2-删除数据 测试成功\");\n" +
                        "                }\n" +
                        "            }\n" +
                        "            settleAdjustmentDetail.setDescription(\"批量插入数据测试\");\n" +
                        "            list.add(settleAdjustmentDetail);\n" +
                        "        }\n" +
                        "        System.out.println(\"-----------批量插入数据测试----------\");\n" +
                        "        Integer i = settleAdjustmentDetailMapper.insertSettleAdjustmentDetailList(list);\n" +
                        "        if (i > 0) {\n" +
                        "            System.out.println(i);\n" +
                        "            System.out.println(\"1.1-批量插入数据测试成功\");\n" +
                        "        }\n" +
                        "\n" +
                        "        List<SettleAdjustmentDetail> settleAdjustmentDetailList = settleAdjustmentDetailMapper.selectSettleAdjustmentDetail(new SettleAdjustmentDetail());\n" +
                        "        if (ObjectUtil.isNotNull(settleAdjustmentDetailList)) {\n" +
                        "            System.out.println(\"4-查询多个所有测试成功\");\n" +
                        "            System.out.println(\"共有数据：\" + settleAdjustmentDetailList.size() + \" rows\");\n" +
                        "        }\n" +
                        "\n" +
                        "        System.out.println(\"-----------测试 分页查询----------\");\n" +
                        "        Page<SettleAdjustmentDetail> settleAdjustmentDetailPage = settleAdjustmentDetailMapper.selectSettleAdjustmentDetail(new Page<SettleAdjustmentDetail>(1, 10), new SettleAdjustmentDetail());\n" +
                        "        if (ObjectUtil.isNotNull(settleAdjustmentDetailPage)) {\n" +
                        "            System.out.println(\"4-分页查询成功\");\n" +
                        "            System.out.println(settleAdjustmentDetailPage.getTotal());\n" +
                        "            System.out.println(settleAdjustmentDetailPage.getPages());\n" +
                        "            System.out.println(settleAdjustmentDetailPage.getRecords());\n" +
                        "            System.out.println(JSONUtil.toJsonStr(settleAdjustmentDetailPage));\n" +
                        "        }\n" +
                        "        System.out.println(\"\\n-------------恭喜你 测试 全部正常 well done\");\n" +
                        "\n" +
                        "    }");

        String replaceContent = stringBuilder.toString().replace("settleAdjustmentDetail", generateCode.new_name_small).replace("SettleAdjustmentDetail", generateCode.new_name_upper);

        CodeX.writeFile(path, replaceContent, true);

    }
}
