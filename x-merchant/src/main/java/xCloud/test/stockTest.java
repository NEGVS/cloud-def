//package xCloud.stock.test;
//
//import cn.hutool.core.util.ObjectUtil;
//import cn.hutool.json.JSONUtil;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import xCloud.entity.Stock;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random; /**
// * 单元测试方法
// */
//@Test
//void Test001() {
//    List<Stock> list = new ArrayList<>();
//    Random random = new Random();
//    int praimaryKey = 0;
//    for (int i = 0; i < 5; i++) {
//        Stock stock = new Stock();
//        Stock.setId(1);
//        Stock.setList_id(1);
//        Stock.setBlock_name(1);
//        Stock.setCode(1);
//        Stock.setExchange(1);
//        Stock.setFinance_type(1);
//        Stock.setHeat(1);
//        Stock.setLast_px(1);
//        Stock.setLogo_id(1);
//        Stock.setMarket(1);
//        Stock.setName(1);
//        Stock.setLogo_type(1);
//        Stock.setLogo_url(1);
//        Stock.setPx_change_rate(1);
//        Stock.setRank_diff(1);
//        Stock.setCreated_time(1);
//        int insertCount = stockMapper.insertStock(stock);
//        praimaryKey = stock.getAdjustmentDetailId();
//
//        System.out.println("praimaryKey");
//        System.out.println(praimaryKey);
//        if (insertCount == 1) {
//            System.out.println("1-单个插入数据测试成功");
//        }
//
//        if (i == 1) {
//
//            /**
//             * 搜索测试
//             */
//            List<Stock> stockList = stockMapper.selectStock(stock);
//            if (ObjectUtil.isNotNull(stockList)) {
//                System.out.println("3-搜索测试成功");
//                System.out.println(JSONUtil.toJsonStr(stockList));
//            }
//
//            stock.setDescription("修改数据");
//            int i_update = stockMapper.updateStock(stock);
//            if (i_update == 1) {
//                System.out.println("3-修改数据成功");
//            }
//
//            List<Stock> stockList222 = stockMapper.selectStock(stock);
//            if (ObjectUtil.isNotNull(stockList222)) {
//                System.out.println("4-修改后--搜索测试成功");
//                System.out.println(JSONUtil.toJsonStr(stockList222));
//            }
//
//            int i2 = stockMapper.deleteStock(stock);
//            if (i2 == 1) {
//                System.out.println("2-删除数据 测试成功");
//            }
//        }
//        stock.setDescription("批量插入数据测试");
//        list.add(stock);
//    }
//    System.out.println("-----------批量插入数据测试----------");
//    Integer i = stockMapper.insertStockList(list);
//    if (i > 0) {
//        System.out.println(i);
//        System.out.println("1.1-批量插入数据测试成功");
//    }
//
//    List<Stock> stockList = stockMapper.selectStock(new Stock());
//    if (ObjectUtil.isNotNull(stockList)) {
//        System.out.println("4-查询多个所有测试成功");
//        System.out.println("共有数据：" + stockList.size() + " rows");
//    }
//
//    System.out.println("-----------测试 分页查询----------");
//    Page<Stock> stockPage = stockMapper.selectStock(new Page<Stock>(1, 10), new Stock());
//    if (ObjectUtil.isNotNull(stockPage)) {
//        System.out.println("4-分页查询成功");
//        System.out.println(stockPage.getTotal());
//        System.out.println(stockPage.getPages());
//        System.out.println(stockPage.getRecords());
//        System.out.println(JSONUtil.toJsonStr(stockPage));
//    }
//    System.out.println("\n-------------恭喜你 测试 全部正常 well done");
//
//}
