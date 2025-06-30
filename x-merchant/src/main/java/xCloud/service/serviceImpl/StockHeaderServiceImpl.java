package xCloud.service.serviceImpl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.entity.Stock;
import xCloud.mapper.StockMapper;
import xCloud.service.StockHeaderService;
import xCloud.service.stock.StockDataParser;
import xCloud.entity.StockData;
import xCloud.mapper.StockDataMapper;
import xCloud.entity.StockHeader;
import xCloud.entity.StockHeaderDTO;
import xCloud.entity.StockHeaderVO;
import xCloud.mapper.StockHeaderMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author andy_mac
 * @description 针对表【stock_header】的数据库操作Service实现
 * @createDate 2025-06-10 14:25:33
 */
@Slf4j
@Service
public class StockHeaderServiceImpl extends ServiceImpl<StockHeaderMapper, StockHeader> implements StockHeaderService {

    @Resource
    private StockMapper stockMapper;
    @Resource
    private StockHeaderMapper stockHeaderMapper;
    @Resource
    StockDataMapper stockDataMapper;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 10)
    public void saveStockData(StockDataParser.XStockData xStockData, String dateStr) throws InterruptedException {

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
            stockHeader.setCreated_time(new Date());
            stockHeaderList.add(stockHeader);
        }
        List<Stock> stockList = new ArrayList<>();
        for (StockDataParser.XStock xStock : xStockData.getResult().getList().getBody()) {
            Stock stock = new Stock();
            stock.setBlock_name(xStock.getBlockName());
            stock.setCode(xStock.getCode());
            stock.setExchange(xStock.getExchange());
            stock.setHeat(xStock.getHeat());
            stock.setLast_px(xStock.getLastPx());
            stock.setMarket(xStock.getMarket());
            stock.setName(xStock.getName());
            stock.setLogo_type(dateStr);
            stock.setLogo_url(xStock.getLogo().getLogo());
            stock.setPx_change_rate(xStock.getPxChangeRate());
            stock.setRank_diff(xStock.getRankDiff());
            stock.setCreated_time(new Date());
            stockList.add(stock);
        }

        log.info("\n---------------------saveStockData start");
        Map<String, Long> collect = stockList.parallelStream().filter(st -> st.getBlock_name() != null).collect(Collectors.groupingBy(Stock::getBlock_name, Collectors.counting()));
        stockList.parallelStream().forEach(st -> {
            st.setFinance_type(String.valueOf(collect.get(st.getBlock_name())));
        });

        int i = stockMapper.insertStockList(stockList);
        if (i > 0) {
            log.info("\n--------------------saveStockData insertStockList over,success insert " + i + " 条数据");
        } else {
            log.info("\n-----------------error insert ");
        }
        log.info("\n--------------------saveStockData insertStockList over");
        stockDataMapper.insertStockData(stockData);
        stockHeaderMapper.insertStockHeaderList(stockHeaderList);

    }


    /**
     * 1-新增
     *
     * @param dto dto
     * @return ResultEntity
     */
    @Override
    public ResultEntity<StockHeader> add(StockHeaderDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return ResultEntity.error("新增失败，参数为空");
        }
        //dto-->entity
        StockHeader stockHeader = new StockHeader();
        BeanUtils.copyProperties(dto, stockHeader);
        int count = stockHeaderMapper.insertStockHeader(stockHeader);
        if (count > 0) {
            return ResultEntity.success(stockHeader);
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
    public ResultEntity<StockHeader> delete(StockHeaderDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return ResultEntity.error("删除失败，参数为空");
        }
        //dto-->entity
        StockHeader stockHeader = new StockHeader();
        BeanUtils.copyProperties(dto, stockHeader);
        int count = stockHeaderMapper.deleteStockHeader(stockHeader);
        if (count > 0) {
            return ResultEntity.success(stockHeader);
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
    public ResultEntity<StockHeader> update(StockHeaderDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return ResultEntity.error("更新失败，参数为空");
        }
        //dto-->entity
        StockHeader stockHeader = new StockHeader();
        BeanUtils.copyProperties(dto, stockHeader);
        int count = stockHeaderMapper.updateStockHeader(stockHeader);
        if (count > 0) {
            return ResultEntity.success(stockHeader);
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
    public ResultEntity<Page<StockHeader>> list(StockHeaderDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return ResultEntity.error("查询失败，参数为空");
        }
        Page<StockHeader> page = new Page<>(dto.getCurrent(), dto.getSize());
        //dto-->entity
        StockHeader stockHeader = new StockHeader();
        BeanUtils.copyProperties(dto, stockHeader);
        //分页查询
        Page<StockHeader> stockHeaders = stockHeaderMapper.selectStockHeader(page, stockHeader);
        if (ObjectUtil.isNotEmpty(stockHeaders)) {
            //entity-->vo
            List<StockHeaderVO> stockHeaderVOS = stockHeaders.getRecords().stream().map(stockHeaderTemp -> {
                StockHeaderVO stockHeaderVO = new StockHeaderVO();
                BeanUtils.copyProperties(stockHeaderTemp, stockHeaderVO);
                return stockHeaderVO;
            }).toList();
            return ResultEntity.success(stockHeaders);
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
    public ResultEntity<StockHeaderVO> detail(StockHeaderDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return ResultEntity.error("查询失败，参数为空");
        }
        //dto-->entity
        StockHeader stockHeader = new StockHeader();
        stockHeader.setId(dto.getId());
        List<StockHeader> stockHeaders = stockHeaderMapper.selectStockHeader(stockHeader);
        if (ObjectUtil.isNotEmpty(stockHeaders)) {
            //entity-->vo
            StockHeader stockHeaderTemp = stockHeaders.get(0);
            StockHeaderVO stockHeaderVO = new StockHeaderVO();
            BeanUtils.copyProperties(stockHeaderTemp, stockHeaderVO);
            return ResultEntity.success(stockHeaderVO);
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
    public void exportFile(StockHeaderDTO dto, HttpServletResponse response) throws Exception {


    }
}
