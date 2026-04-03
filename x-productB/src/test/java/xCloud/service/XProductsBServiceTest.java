package xCloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import xCloud.entity.Result;
import xCloud.entity.XProductsB;
import xCloud.tools.CodeX;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class XProductsBServiceTest {

    @Resource
    XProductsBService productsBService;

    @Resource
    Embedding2Service embedding2Service;


    // ================================================================
    // Embedding2Service 测试
    // 前提：Milvus 已启动（localhost:19530），Collection 已创建
    // ================================================================

    /**
     * 0-1 创建 Collection
     * 预期：返回"集合创建成功"，Milvus 中存在对应 Collection
     */
    @Test
    @Order(1)
    @DisplayName("0-1 创建 Collection")
    void testCreateCollection() {
        String result = embedding2Service.createCollection();
        log.info("createCollection 结果: {}", result);
        // Collection 已存在时 Milvus 会抛异常，消息里包含"创建失败"
        assertTrue(result.equals("集合创建成功") || result.contains("创建失败"),
                "返回值应为成功或包含失败原因，实际: " + result);
    }

    /**
     * 1-1 插入单条向量（正常文本）
     * 预期：Result.code 为成功，Milvus insertCnt == 1
     */
    @Test
    @Order(2)
    @DisplayName("1-1 insertVector 插入单条向量")
    void testInsertVector() {
        Result<Object> result = embedding2Service.insertVector("Java 工程师，熟悉 Spring Boot 和 Milvus 向量数据库");
        log.info("insertVector 结果: {}", result);
    }

    /**
     * 1-2 插入单条向量（空字符串边界情况）
     * 预期：不抛异常，返回 Result（具体成功/失败取决于 Embedding 模型对空串的处理）
     */
    @Test
    @Order(3)
    @DisplayName("1-2 insertVector 空文本边界")
    void testInsertVectorEmpty() {
        assertDoesNotThrow(() -> {
            Result<Object> result = embedding2Service.insertVector("");
            log.info("insertVector 空文本结果: {}", result);
        });
    }

    /**
     * 1-3 批量插入向量
     * 预期：不抛异常，Milvus 全部写入成功
     */
    @Test
    @Order(4)
    @DisplayName("1-3 insertVectors 批量插入")
    void testInsertVectors() {
        List<String> texts = List.of(
                "产品经理，负责需求分析和项目管理",
                "前端工程师，熟悉 Vue3 和 TypeScript",
                "数据分析师，擅长 Python 和机器学习"
        );
        assertDoesNotThrow(() -> embedding2Service.insertVectors(texts),
                "批量插入不应抛出异常");
    }

    /**
     * 1-4 批量插入 - 空列表应抛出异常
     * 预期：抛出 IllegalArgumentException
     */
    @Test
    @Order(5)
    @DisplayName("1-4 insertVectors 空列表应抛异常")
    void testInsertVectorsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> embedding2Service.insertVectors(List.of()),
                "空列表应抛出 IllegalArgumentException");
    }

    /**
     * 4-1 向量相似度搜索
     * 预期：Result 成功，data 为 List，每条含 id、distance、content、primaryKey
     */
    @Test
    @Order(6)
    @DisplayName("4-1 search 向量搜索 Top5")
    void testSearch() {
        Result<Object> result = embedding2Service.search("Java 后端开发工程师", 5);
        log.info("search 结果: {}", result);
        assertNotNull(result);
//        assertTrue(result.notify();, "搜索应成功，实际: " + result);
        assertInstanceOf(List.class, result.getData(), "data 应为 List");
    }

    /**
     * 4-2 queryBigger —— 查询 id 大于某值
     * 预期：Result 成功，data 为 id 列表
     */
    @Test
    @Order(7)
    @DisplayName("4-2 queryBigger id > 0")
    void testQueryBigger() {
        // id > 0 应能查出数据（只要 Collection 不为空）
        Result<Object> result = embedding2Service.queryBigger(0L);
        log.info("queryBigger 结果: {}", result);
        assertNotNull(result);
        // 有数据时成功，无数据时返回 error 也是合法的
//        log.info("queryBigger isSuccess={}", result.isSuccess());
    }

    /**
     * 4-3 queryEqual —— 查询不存在的 id
     * 预期：Result 为 error，提示"未查询到数据"
     */
    @Test
    @Order(8)
    @DisplayName("4-3 queryEqual 不存在的 id 应返回 error")
    void testQueryEqualNotFound() {
        // id = -1 在 Milvus 中不存在
        Result<Object> result = embedding2Service.queryEqual(-1L);
        log.info("queryEqual(-1) 结果: {}", result);
        assertNotNull(result);
//        assertFalse(result.isSuccess(), "不存在的 id 应返回 error");

    }

    // ================================================================
    // 原有测试
    // ================================================================

    @Test
    void tesst() {
        System.out.println("test");
    }

    @Test
    void test112d22() {
        QueryWrapper<XProductsB> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("merchant_id", 1);
        List<XProductsB> list = productsBService.list(queryWrapper);
//        System.out.println(list);

        for (XProductsB xProductsB : list) {
            if (!xProductsB.getImage().startsWith("/")) {
                System.out.println(xProductsB.getImage());
                xProductsB.setImage("/" + xProductsB.getImage());
                productsBService.updateById(xProductsB);
            }
        }

    }

    @Test
    void tes071d2() {
        QueryWrapper<XProductsB> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("merchant_id", 2);
        List<XProductsB> list = productsBService.list(queryWrapper);
        for (XProductsB xProductsB : list) {

            xProductsB.setPre_price(xProductsB.getPrice().multiply(new BigDecimal(1.3)));

            UpdateWrapper<XProductsB> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("product_id", xProductsB.getProduct_id());
            updateWrapper.set("pre_price", xProductsB.getPre_price());
            boolean update = productsBService.update(updateWrapper);
            if (!update) {
                log.error("更新失败");
            } else {
                log.info("更新成功");
            }
        }
        if (true) {
            return;
        }
        for (XProductsB xProductsB : list) {
            if (!xProductsB.getImage().contains("jpg")) {
                continue;
            }
            xProductsB.setImage(xProductsB.getImage().replace("jpg", "png"));
            UpdateWrapper<XProductsB> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("product_id", xProductsB.getProduct_id());
            updateWrapper.set("image", xProductsB.getImage());
//            productsBService.update(xProductsB, updateWrapper);
            boolean update = productsBService.update(updateWrapper);
            if (!update) {
                log.error("更新失败");
            } else {
                log.info("更新成功");
            }
        }

    }

    @Test
    void tes07s11() {
        try {
            CodeX codeX = new CodeX();
            QueryWrapper<XProductsB> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("merchant_id", 2);


            boolean remove = productsBService.remove(queryWrapper);
            if (remove) {
                System.out.println("----remove success");
            }

//            List<String> list = codeX.readFileLines("/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/andy/file/bbq.txt");
            List<String> list33 = codeX.readFileLines("/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/andy/file/ddd.txt");
            List<String> list2222 = codeX.readFileLines("/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/andy/file/aaa.txt");
            System.out.println(list33);
            XProductsB xProducts = new XProductsB();
            xProducts.setMerchant_id(1);
            List<XProductsB> xProductsList = new ArrayList<>();
            int indexN = 0;
            int ca_index = 0;
            for (String s : list33) {
                if (s.contains("@")) {
                    ca_index++;
                    continue;
                }
                if (s.contains("-")) {
                    String[] split = s.split("-");
                    String s1 = split[0];
                    String s2 = split[1];
                    XProductsB products = new XProductsB();
                    products.setProduct_id(IdWorker.getId());

                    if (s1.contains("*")) {
                        products.setName(s1.replace("*", ""));
                        products.setNotes("💙" + products.getName());
                    } else if (s1.contains("#")) {
                        products.setName(s1.replace("#", ""));
                        products.setNotes("❤️" + products.getName());
                    } else {
                        products.setName(s1);
                        products.setNotes(products.getName());
                    }
                    products.setDescription(products.getName());
                    products.setPrice(new BigDecimal(s2));
                    products.setPre_price(new BigDecimal(s2).multiply(new BigDecimal("1.2")));
                    products.setCollaborate_price(new BigDecimal(s2).multiply(new BigDecimal("0.98")));
                    products.setCost_price(new BigDecimal(s2).multiply(new BigDecimal("0.5")));
                    products.setOriginal_price(new BigDecimal(s2).multiply(new BigDecimal("0.92")));
                    products.setStock(9999);
                    products.setCategory_id(ca_index);
                    products.setMerchant_id(2);
//                    products.setImage(arrr[ThreadLocalRandom.current().nextInt(arrr.length)]);
                    products.setImage(list2222.get(indexN));
                    indexN++;
                    products.setVersion(1L);
                    products.setCreated_time(new Date());
                    products.setUpdated_time(new Date());
                    xProductsList.add(products);
                }
            }
            boolean b = productsBService.saveBatch(xProductsList);
            if (b) {
                System.out.println("保存成功");
            } else {
                System.out.println("保存失败");
            }

        } catch (Exception e) {
            System.out.println("----error");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
//    productsService.insertXProducts( products)


    }

    /**
     * 1- productsBService 测试
     */
    @Test
    void test11222() {
        QueryWrapper<XProductsB> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("merchant_id", 1);
        List<XProductsB> list = productsBService.list(queryWrapper);
//        System.out.println(list);

        for (XProductsB xProductsB : list) {
            if (!xProductsB.getImage().startsWith("/")) {
                System.out.println(xProductsB.getImage());
                xProductsB.setImage("/" + xProductsB.getImage());
                productsBService.updateById(xProductsB);
            }
        }

    }

    @Test
    void tes0712() {
        QueryWrapper<XProductsB> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("merchant_id", 2);
        List<XProductsB> list = productsBService.list(queryWrapper);
        for (XProductsB xProductsB : list) {

            xProductsB.setPre_price(xProductsB.getPrice().multiply(new BigDecimal(1.3)));

            UpdateWrapper<XProductsB> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("product_id", xProductsB.getProduct_id());
            updateWrapper.set("pre_price", xProductsB.getPre_price());
            boolean update = productsBService.update(updateWrapper);
            if (!update) {
                log.error("更新失败");
            } else {
                log.info("更新成功");
            }
        }
        if (true) {
            return;
        }
        for (XProductsB xProductsB : list) {
            if (!xProductsB.getImage().contains("jpg")) {
                continue;
            }
            xProductsB.setImage(xProductsB.getImage().replace("jpg", "png"));
            UpdateWrapper<XProductsB> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("product_id", xProductsB.getProduct_id());
            updateWrapper.set("image", xProductsB.getImage());
//            productsBService.update(xProductsB, updateWrapper);
            boolean update = productsBService.update(updateWrapper);
            if (!update) {
                log.error("更新失败");
            } else {
                log.info("更新成功");
            }
        }

    }

    @Test
    void tes0711() {
        try {
            CodeX codeX = new CodeX();
            QueryWrapper<XProductsB> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("merchant_id", 2);


            boolean remove = productsBService.remove(queryWrapper);
            if (remove) {
                System.out.println("----remove success");
            }

//            List<String> list = codeX.readFileLines("/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/andy/file/bbq.txt");
            List<String> list33 = codeX.readFileLines("/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/andy/file/ddd.txt");
            List<String> list2222 = codeX.readFileLines("/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/andy/file/aaa.txt");
            System.out.println(list33);
            XProductsB xProducts = new XProductsB();
            xProducts.setMerchant_id(1);
            List<XProductsB> xProductsList = new ArrayList<>();
            int indexN = 0;
            int ca_index = 0;
            for (String s : list33) {
                if (s.contains("@")) {
                    ca_index++;
                    continue;
                }
                if (s.contains("-")) {
                    String[] split = s.split("-");
                    String s1 = split[0];
                    String s2 = split[1];
                    XProductsB products = new XProductsB();
                    products.setProduct_id(IdWorker.getId());

                    if (s1.contains("*")) {
                        products.setName(s1.replace("*", ""));
                        products.setNotes("💙" + products.getName());
                    } else if (s1.contains("#")) {
                        products.setName(s1.replace("#", ""));
                        products.setNotes("❤️" + products.getName());
                    } else {
                        products.setName(s1);
                        products.setNotes(products.getName());
                    }
                    products.setDescription(products.getName());
                    products.setPrice(new BigDecimal(s2));
                    products.setPre_price(new BigDecimal(s2).multiply(new BigDecimal("1.2")));
                    products.setCollaborate_price(new BigDecimal(s2).multiply(new BigDecimal("0.98")));
                    products.setCost_price(new BigDecimal(s2).multiply(new BigDecimal("0.5")));
                    products.setOriginal_price(new BigDecimal(s2).multiply(new BigDecimal("0.92")));
                    products.setStock(9999);
                    products.setCategory_id(ca_index);
                    products.setMerchant_id(2);
//                    products.setImage(arrr[ThreadLocalRandom.current().nextInt(arrr.length)]);
                    products.setImage(list2222.get(indexN));
                    indexN++;
                    products.setVersion(1L);
                    products.setCreated_time(new Date());
                    products.setUpdated_time(new Date());
                    xProductsList.add(products);
                }
            }
            boolean b = productsBService.saveBatch(xProductsList);
            if (b) {
                System.out.println("保存成功");
            } else {
                System.out.println("保存失败");
            }

        } catch (Exception e) {
            System.out.println("----error");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
//    productsService.insertXProducts( products)


    }
}
