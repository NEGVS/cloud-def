package xCloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
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
    void tes0711() {
        try {
            CodeX codeX = new CodeX();

            List<String> list = codeX.readFileLines("/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/andy/file/bbq.txt");
            List<String> list2222 = codeX.readFileLines("/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/andy/file/aaa.txt");

            System.out.println(list);

            XProductsB xProducts = new XProductsB();

            xProducts.setMerchant_id(1);

            List<XProductsB> xProductsList = new ArrayList<>();
            int indexN = 0;
            for (String s : list) {
                if (s.contains("-")) {
                    String[] split = s.split("-");
                    String s1 = split[0];
                    String s2 = split[1];
                    XProductsB products = new XProductsB();
                    products.setProduct_id(IdWorker.getId());

                    if (s1.contains("*")) {
                        products.setName(s1.replace("*", ""));
                        products.setNotes("💙");

                    } else if (s1.contains("#")) {
                        products.setName(s1.replace("#", ""));
                        products.setNotes("❤️");
                    } else {
                        products.setName(s1);
                        products.setNotes("");
                    }
                    products.setDescription("️prefect good");
                    products.setPrice(BigDecimal.valueOf(Long.parseLong(s2)));
                    products.setPre_price(BigDecimal.ZERO);
                    products.setCollaborate_price(BigDecimal.ZERO);
                    products.setCost_price(BigDecimal.ZERO);
                    products.setOriginal_price(BigDecimal.ZERO);
                    products.setStock(9999);
                    products.setCategory_id(1);
                    products.setMerchant_id(1);

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
        }
//    productsService.insertXProducts( products)


    }
}