package xCloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    void test() {
        System.out.println("test");
    }

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
        if (true){
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