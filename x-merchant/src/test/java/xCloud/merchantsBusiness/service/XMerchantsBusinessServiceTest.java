package xCloud.merchantsBusiness.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xCloud.entity.XMerchantsBusiness;
import xCloud.mapper.XMerchantsBusinessMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


@SpringBootTest
class XMerchantsBusinessServiceTest {


    @Autowired
    XMerchantsBusinessMapper xMerchantsBusinessMapper;

    /**
     * 单元测试方法
     */
    @Test
    void Test001() {
        List<XMerchantsBusiness> list = new ArrayList<>();
        Random random = new Random();
        int praimaryKey = 0;
        for (int i = 0; i < 5; i++) {
            XMerchantsBusiness xMerchantsBusiness = new XMerchantsBusiness();
            xMerchantsBusiness.setDayOfWeek(i);
            xMerchantsBusiness.setEndDate(new Date());
            xMerchantsBusiness.setMerchant_id("111");
            xMerchantsBusiness.setStartDate(new Date());
            int insertCount = xMerchantsBusinessMapper.insertXMerchantsBusiness(xMerchantsBusiness);
            praimaryKey = xMerchantsBusiness.getBusiness_id();

            System.out.println("praimaryKey");
            System.out.println(praimaryKey);
            if (insertCount == 1) {
                System.out.println("1-单个插入数据测试成功");
            }

            if (i == 1) {

                /**
                 * 搜索测试
                 */
                List<XMerchantsBusiness> xMerchantsBusinessList = xMerchantsBusinessMapper.selectXMerchantsBusiness(xMerchantsBusiness);
                if (ObjectUtil.isNotNull(xMerchantsBusinessList)) {
                    System.out.println("3-搜索测试成功");
                    System.out.println(JSONUtil.toJsonStr(xMerchantsBusinessList));
                }

                xMerchantsBusiness.setMerchant_id("1112");
                int i_update = xMerchantsBusinessMapper.updateXMerchantsBusiness(xMerchantsBusiness);
                if (i_update == 1) {
                    System.out.println("3-修改数据成功");
                }

                List<XMerchantsBusiness> xMerchantsBusinessList222 = xMerchantsBusinessMapper.selectXMerchantsBusiness(xMerchantsBusiness);
                if (ObjectUtil.isNotNull(xMerchantsBusinessList222)) {
                    System.out.println("4-修改后--搜索测试成功");
                    System.out.println(JSONUtil.toJsonStr(xMerchantsBusinessList222));
                }

                int i2 = xMerchantsBusinessMapper.deleteXMerchantsBusiness(xMerchantsBusiness);
                if (i2 == 1) {
                    System.out.println("2-删除数据 测试成功");
                }
            }
            xMerchantsBusiness.setMerchant_id("1123");
            list.add(xMerchantsBusiness);
        }
        System.out.println("-----------批量插入数据测试----------");
//        Integer i = xMerchantsBusinessMapper.insertXMerchantsBusiness(list);
//        if (i > 0) {
//            System.out.println(i);
//            System.out.println("1.1-批量插入数据测试成功");
//        }

        List<XMerchantsBusiness> xMerchantsBusinessList = xMerchantsBusinessMapper.selectXMerchantsBusiness(new XMerchantsBusiness());
        if (ObjectUtil.isNotNull(xMerchantsBusinessList)) {
            System.out.println("4-查询多个所有测试成功");
            System.out.println("共有数据：" + xMerchantsBusinessList.size() + " rows");
        }

        System.out.println("-----------测试 分页查询----------");
        Page<XMerchantsBusiness> xMerchantsBusinessPage = xMerchantsBusinessMapper.selectXMerchantsBusiness(new Page<XMerchantsBusiness>(1, 10), new XMerchantsBusiness());
        if (ObjectUtil.isNotNull(xMerchantsBusinessPage)) {
            System.out.println("4-分页查询成功");
            System.out.println(xMerchantsBusinessPage.getTotal());
            System.out.println(xMerchantsBusinessPage.getPages());
            System.out.println(xMerchantsBusinessPage.getRecords());
            System.out.println(JSONUtil.toJsonStr(xMerchantsBusinessPage));
        }
        System.out.println("\n-------------恭喜你 测试 全部正常 well done");

    }
}