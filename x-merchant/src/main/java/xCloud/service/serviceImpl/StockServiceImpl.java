package xCloud.service.serviceImpl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.entity.Stock;
import xCloud.entity.StockDTO;
import xCloud.entity.StockVO;
import xCloud.mapper.StockMapper;
import xCloud.service.StockHeaderService;
import xCloud.service.StockService;
import xCloud.service.stock.StockDataParser;
import xCloud.tools.HttpUtil;
import xCloud.util.CodeX;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author andy_mac
 * @description 针对表【stock】的数据库操作Service实现
 * @createDate 2025-06-10 13:59:52
 */
@Slf4j
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    @Resource
    StockHeaderService stockHeaderService;
    @Resource
    private StockMapper stockMapper;


    @Autowired
    private TransactionTemplate transactionTemplate;

    AtomicInteger count = new AtomicInteger(0);

    //    @Scheduled(cron = "0 0 17 * * MON-FRI")
    @Scheduled(fixedRate = 1000 * 30)
    @Override
    public void test() {
        log.info("start get Stock data....");
        try {
            Stock stock = new Stock();
            stock.setStartDate(CodeX.getDate_yyyy_MM_dd());
            List<Stock> stocks = stockMapper.selectStock(stock);

//            if (count.get() == 0) {
//                count.getAndIncrement();
//            } else {
//                return;
//            }
            if (stocks != null && !stocks.isEmpty()) {
                log.info("今日数据已经更新，无需重复更新");
                return;
            }
            String url = "https://finance.pae.baidu.com/vapi/v1/hotrank?tn=wisexmlnew&dsp=iphone&product=stock&style=tablelist&market=all&type=hour&day=20250609&hour=17&pn=0&rn=&finClientType=pc";
            String urlAllDay = "https://finance.pae.baidu.com/vapi/v1/hotrank?tn=wisexmlnew&dsp=iphone&product=stock&style=tablelist&market=all&type=day&day=" + CodeX.getDateOfyyyyMMdd() + "&hour=17&pn=0&rn=&finClientType=pc";
            String jsonString = HttpUtil.doPost(urlAllDay);
            ObjectMapper mapper = new ObjectMapper();
            StockDataParser.XStockData xStockData = mapper.readValue(jsonString, StockDataParser.XStockData.class);
            System.out.println(xStockData.getQueryID());
            System.out.println(xStockData.getResultCode());
            System.out.println(xStockData.getResultNum());

            System.out.println(xStockData.getResult().getList().getHeaders());
            List<StockDataParser.XStock> body = xStockData.getResult().getList().getBody();
            stockHeaderService.saveStockData(xStockData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 1-新增
     *
     * @param dto dto
     * @return ResultEntity
     */
    @Override
    public ResultEntity<Stock> add(StockDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return ResultEntity.error("新增失败，参数为空");
        }
        //dto-->entity
        Stock stock = new Stock();
        BeanUtils.copyProperties(dto, stock);
        int count = stockMapper.insertStock(stock);
        if (count > 0) {
            return ResultEntity.success(stock);
        } else {
            return ResultEntity.error("新增失败");
        }
    }

    /**
     * 2-删除
     *
     * @param dto dto
     * @return ResultEntity
     */
    @Override
    public ResultEntity<Stock> delete(StockDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return ResultEntity.error("删除失败，参数为空");
        }
        //dto-->entity
        Stock stock = new Stock();
        BeanUtils.copyProperties(dto, stock);
        int count = stockMapper.deleteStock(stock);
        if (count > 0) {
            return ResultEntity.success(stock);
        } else {
            return ResultEntity.error("删除失败");
        }
    }

    /**
     * 3-更新
     *
     * @param dto dto
     * @return ResultEntity
     */
    @Override
    public ResultEntity<Stock> update(StockDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return ResultEntity.error("更新失败，参数为空");
        }
        //dto-->entity
        Stock stock = new Stock();
        BeanUtils.copyProperties(dto, stock);
        int count = stockMapper.updateStock(stock);
        if (count > 0) {
            return ResultEntity.success(stock);
        } else {
            return ResultEntity.error("更新失败");
        }
    }

    /**
     * 4-查询-列表/搜索
     *
     * @param dto 列表搜索
     * @return ResultEntity
     */
    @Override
    public ResultEntity<Page<Stock>> list(StockDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return ResultEntity.error("查询失败，参数为空");
        }
        Page<Stock> page = new Page<>(dto.getCurrent(), dto.getSize());
        //dto-->entity
        Stock stock = new Stock();
        BeanUtils.copyProperties(dto, stock);
        //分页查询
        Page<Stock> stocks = stockMapper.selectStock(page, stock);
        if (ObjectUtil.isNotEmpty(stocks)) {
            //entity-->vo
            List<StockVO> stockVOS = stocks.getRecords().stream().map(stockTemp -> {
                StockVO stockVO = new StockVO();
                BeanUtils.copyProperties(stockTemp, stockVO);
                return stockVO;
            }).toList();
            return ResultEntity.success(stocks);
        } else {
            return ResultEntity.success(page);
        }
    }

    /**
     * 4.1-查询-详情
     *
     * @param dto dto
     * @return ResultEntity
     */
    @Override
    public ResultEntity<StockVO> detail(StockDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return ResultEntity.error("查询失败，参数为空");
        }
        //dto-->entity
        Stock stock = new Stock();
        stock.setId(dto.getId());
        List<Stock> stocks = stockMapper.selectStock(stock);
        if (ObjectUtil.isNotEmpty(stocks)) {
            //entity-->vo
            Stock stockTemp = stocks.get(0);
            StockVO stockVO = new StockVO();
            BeanUtils.copyProperties(stockTemp, stockVO);
            return ResultEntity.success(stockVO);
        } else {
            return ResultEntity.error("查询失败");
        }
    }

    /**
     * 5-导入
     *
     * @param multipartFile 文件流
     * @param userId        用户id
     * @param response      响应流
     * @throws IOException
     */
    @Override
    public void importFile(MultipartFile multipartFile, String userId, HttpServletResponse response) throws IOException {

    }

    /**
     * 5.1-模版下载
     *
     * @param response response
     * @throws Exception Exception
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) throws Exception {

    }

    /**
     * 6-导出
     *
     * @param dto      搜索条件
     * @param response response
     * @throws Exception Exception
     */
    @Override
    public void exportFile(StockDTO dto, HttpServletResponse response) throws Exception {

    }
}
