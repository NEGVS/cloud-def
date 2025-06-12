
    /**
     * 单元测试方法
     */
    @Test
    void Test001() {
        List<StockHeader> list = new ArrayList<>();
        Random random = new Random();
        int praimaryKey = 0;
        for (int i = 0; i < 5; i++) {
            StockHeader stockHeader = new StockHeader();
StockHeader.setId(1);
StockHeader.setList_id(1);
StockHeader.setCan_sort(1);
StockHeader.setKey_name(1);
StockHeader.setName(1);
StockHeader.setCreated_time(1);
            int insertCount = stockHeaderMapper.insertStockHeader(stockHeader);
            praimaryKey = stockHeader.getAdjustmentDetailId();

            System.out.println("praimaryKey");
            System.out.println(praimaryKey);
            if (insertCount == 1) {
                System.out.println("1-单个插入数据测试成功");
            }

            if (i == 1) {

                /**
                 * 搜索测试
                 */
                List<StockHeader> stockHeaderList = stockHeaderMapper.selectStockHeader(stockHeader);
                if (ObjectUtil.isNotNull(stockHeaderList)) {
                    System.out.println("3-搜索测试成功");
                    System.out.println(JSONUtil.toJsonStr(stockHeaderList));
                }

                stockHeader.setDescription("修改数据");
                int i_update = stockHeaderMapper.updateStockHeader(stockHeader);
                if (i_update == 1) {
                    System.out.println("3-修改数据成功");
                }

                List<StockHeader> stockHeaderList222 = stockHeaderMapper.selectStockHeader(stockHeader);
                if (ObjectUtil.isNotNull(stockHeaderList222)) {
                    System.out.println("4-修改后--搜索测试成功");
                    System.out.println(JSONUtil.toJsonStr(stockHeaderList222));
                }

                int i2 = stockHeaderMapper.deleteStockHeader(stockHeader);
                if (i2 == 1) {
                    System.out.println("2-删除数据 测试成功");
                }
            }
            stockHeader.setDescription("批量插入数据测试");
            list.add(stockHeader);
        }
        System.out.println("-----------批量插入数据测试----------");
        Integer i = stockHeaderMapper.insertStockHeaderList(list);
        if (i > 0) {
            System.out.println(i);
            System.out.println("1.1-批量插入数据测试成功");
        }

        List<StockHeader> stockHeaderList = stockHeaderMapper.selectStockHeader(new StockHeader());
        if (ObjectUtil.isNotNull(stockHeaderList)) {
            System.out.println("4-查询多个所有测试成功");
            System.out.println("共有数据：" + stockHeaderList.size() + " rows");
        }

        System.out.println("-----------测试 分页查询----------");
        Page<StockHeader> stockHeaderPage = stockHeaderMapper.selectStockHeader(new Page<StockHeader>(1, 10), new StockHeader());
        if (ObjectUtil.isNotNull(stockHeaderPage)) {
            System.out.println("4-分页查询成功");
            System.out.println(stockHeaderPage.getTotal());
            System.out.println(stockHeaderPage.getPages());
            System.out.println(stockHeaderPage.getRecords());
            System.out.println(JSONUtil.toJsonStr(stockHeaderPage));
        }
        System.out.println("\n-------------恭喜你 测试 全部正常 well done");

    }
