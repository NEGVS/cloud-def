/**
 * 单元测试方法
 */
@
Test
void Test001() {
        List<StockData> list = new ArrayList<>();
        Random
random = new Random();
int praimaryKey = 0;
for (int i = 0; i < 5; i++) {
            StockData stockData = new StockData();
StockData.setId
(1);
StockData.setResult_code
(1);
StockData.setResult_num
(1);
StockData.setQuery_id
(1);
StockData.setResult_id
(1);
StockData.setCreated_time
(1);
int insertCount = stockDataMapper.insertStockData(stockData);
            praimaryKey
= stockData.getAdjustmentDetailId();

            System.out.println
("praimaryKey");
            System.out.println
(praimaryKey);
            if
(insertCount == 1) {
                System.out.println("1-单个插入数据测试成功");
}

            if (i == 1) {

                /**
                 * 搜索测试
                 */
                List<StockData> stockDataList = stockDataMapper.selectStockData(stockData);
                if
(ObjectUtil.isNotNull(stockDataList)) {
                    System.out.println("3-搜索测试成功");
                    System.out.println
(JSONUtil.toJsonStr(stockDataList));
}

                stockData.setDescription("修改数据");
int i_update = stockDataMapper.updateStockData(stockData);
                if
(i_update == 1) {
                    System.out.println("3-修改数据成功");
}

                List<StockData> stockDataList222 = stockDataMapper.selectStockData(stockData);
                if
(ObjectUtil.isNotNull(stockDataList222)) {
                    System.out.println("4-修改后--搜索测试成功");
                    System.out.println
(JSONUtil.toJsonStr(stockDataList222));
}

                int i2 = stockDataMapper.deleteStockData(stockData);
                if
(i2 == 1) {
                    System.out.println("2-删除数据 测试成功");
}
            }
            stockData.setDescription("批量插入数据测试");
            list.add
(stockData);
}
        System.out.println("-----------批量插入数据测试----------");
Integer i = stockDataMapper.insertStockDataList(list);
        if
(i > 0) {
            System.out.println(i);
            System.out.println
("1.1-批量插入数据测试成功");
}

        List<StockData> stockDataList = stockDataMapper.selectStockData(new StockData());
        if
(ObjectUtil.isNotNull(stockDataList)) {
            System.out.println("4-查询多个所有测试成功");
            System.out.println
("共有数据：" + stockDataList.size() + " rows");
}

        System.out.println("-----------测试 分页查询----------");
        Page
<StockData> stockDataPage = stockDataMapper.selectStockData(new Page<StockData>(1, 10), new StockData());
        if
(ObjectUtil.isNotNull(stockDataPage)) {
            System.out.println("4-分页查询成功");
            System.out.println
(stockDataPage.getTotal());
            System.out.println
(stockDataPage.getPages());
            System.out.println
(stockDataPage.getRecords());
            System.out.println
(JSONUtil.toJsonStr(stockDataPage));
}
        System.out.println("\n-------------恭喜你 测试 全部正常 well done");

}
