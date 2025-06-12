package xCloud.stock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xCloud.entity.Stock;
import xCloud.mapper.StockMapper;
import xCloud.service.guava.StockDataParser;
import xCloud.entity.StockData;
import xCloud.mapper.StockDataMapper;
import xCloud.entity.StockHeader;
import xCloud.mapper.StockHeaderMapper;
import xCloud.tools.HttpUtil;
import xCloud.util.CodeX;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SpringBootTest
class StockServiceTest {

    @Resource
    StockMapper stockMapper;
    @Resource
    StockDataMapper stockDataMapper;
    @Resource
    StockHeaderMapper stockHeaderMapper;


    @Test
    void test() {
        try {
            //9:30开盘后几分钟内逐渐（9:31，9:33）升高的（5%以上9%以下）立马买掉，随时都会突然极速下来（3%左右），此时你肯定不甘心卖掉，
            String url = "https://finance.pae.baidu.com/vapi/v1/hotrank?tn=wisexmlnew&dsp=iphone&product=stock&style=tablelist&market=all&type=hour&day=20250609&hour=17&pn=0&rn=&finClientType=pc";
            String urlAllDay = "https://finance.pae.baidu.com/vapi/v1/hotrank?tn=wisexmlnew&dsp=iphone&product=stock&style=tablelist&market=all&type=day&day=" + CodeX.getDateOfyyyyMMdd() + "&hour=17&pn=0&rn=&finClientType=pc";
            String jsonString = HttpUtil.doPost(url);
            ObjectMapper mapper = new ObjectMapper();
            StockDataParser.XStockData xStockData = mapper.readValue(jsonString, StockDataParser.XStockData.class);
            System.out.println(xStockData.getQueryID());
            System.out.println(xStockData.getResultCode());
            System.out.println(xStockData.getResultNum());

            System.out.println(xStockData.getResult().getList().getHeaders());
            List<StockDataParser.XStock> body = xStockData.getResult().getList().getBody();

            saveStockData(xStockData);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 10)
    public void saveStockData(StockDataParser.XStockData xStockData) throws InterruptedException {
        //1-
        StockData stockData = new StockData();
        stockData.setQuery_id(xStockData.getQueryID());
        stockData.setResult_code(xStockData.getResultCode());
        stockData.setResult_num(xStockData.getResultNum());
        stockData.setCreated_time(new Date());

        List<StockHeader> stockHeaderList = new ArrayList<>();

        List<StockDataParser.Header> headers = xStockData.getResult().getList().getHeaders();
        for (StockDataParser.Header header : headers) {
            StockHeader stockHeader = new StockHeader();
            stockHeader.setKey_name(header.getKey());
            stockHeader.setCan_sort(header.getCanSort());
            stockHeader.setName(header.getName());
            stockHeaderList.add(stockHeader);
        }
        List<Stock> stockList = new ArrayList<>();
        for (StockDataParser.XStock xStock : xStockData.getResult().getList().getBody()) {
            Stock stock = new Stock();
            stock.setBlock_name(xStock.getBlockName());
            stock.setCode(xStock.getCode());
            stock.setExchange(xStock.getExchange());
            stock.setFinance_type(xStock.getFinanceType());
            stock.setHeat(xStock.getHeat());
            stock.setLast_px(xStock.getLastPx());
            stock.setMarket(xStock.getMarket());
            stock.setName(xStock.getName());
            stock.setLogo_type(xStock.getLogo().getType());
            stock.setLogo_url(xStock.getLogo().getLogo());
            stock.setPx_change_rate(xStock.getPxChangeRate());
            stock.setRank_diff(xStock.getRankDiff());
            stock.setCreated_time(new Date());
            stockList.add(stock);
        }
        System.out.println("Thread.sleep(10000);");
        Thread.sleep(11000);
        System.out.println("Thread.sleep(10000); over");
        stockMapper.insertStockList(stockList);
        stockDataMapper.insertStockData(stockData);
        stockHeaderMapper.insertStockHeaderList(stockHeaderList);
    }

}