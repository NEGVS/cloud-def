package xCloud.service.serviceImpl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.entity.StockData;
import xCloud.entity.StockDataDTO;
import xCloud.entity.StockDataVO;
import xCloud.mapper.StockDataMapper;
import xCloud.service.StockDataService;

import java.io.IOException;
import java.util.List;

/**
 * @author andy_mac
 * @description 针对表【stock_data】的数据库操作Service实现
 * @createDate 2025-06-10 14:35:31
 */
@Service
public class StockDataServiceImpl extends ServiceImpl<StockDataMapper, StockData> implements StockDataService {

    @Resource
    private StockDataMapper stockDataMapper;

    /**
     * 1-新增
     *
     * @param dto dto
     * @return ResultEntity
     */
    @Override
    public ResultEntity<StockData> add(StockDataDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return ResultEntity.error("新增失败，参数为空");
        }
        //dto-->entity
        StockData stockData = new StockData();
        BeanUtils.copyProperties(dto, stockData);
        int count = stockDataMapper.insertStockData(stockData);
        if (count > 0) {
            return ResultEntity.success(stockData);
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
    public ResultEntity<StockData> delete(StockDataDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return ResultEntity.error("删除失败，参数为空");
        }
        //dto-->entity
        StockData stockData = new StockData();
        BeanUtils.copyProperties(dto, stockData);
        int count = stockDataMapper.deleteStockData(stockData);
        if (count > 0) {
            return ResultEntity.success(stockData);
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
    public ResultEntity<StockData> update(StockDataDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return ResultEntity.error("更新失败，参数为空");
        }
        //dto-->entity
        StockData stockData = new StockData();
        BeanUtils.copyProperties(dto, stockData);
        int count = stockDataMapper.updateStockData(stockData);
        if (count > 0) {
            return ResultEntity.success(stockData);
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
    public ResultEntity<Page<StockData>> list(StockDataDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return ResultEntity.error("查询失败，参数为空");
        }
        Page<StockData> page = new Page<>(dto.getCurrent(), dto.getSize());
        //dto-->entity
        StockData stockData = new StockData();
        BeanUtils.copyProperties(dto, stockData);
        //分页查询
        Page<StockData> stockDatas = stockDataMapper.selectStockData(page, stockData);
        if (ObjectUtil.isNotEmpty(stockDatas)) {
            //entity-->vo
            List<StockDataVO> stockDataVOS = stockDatas.getRecords().stream().map(stockDataTemp -> {
                StockDataVO stockDataVO = new StockDataVO();
                BeanUtils.copyProperties(stockDataTemp, stockDataVO);
                return stockDataVO;
            }).toList();
            return ResultEntity.success(stockDatas);
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
    public ResultEntity<StockDataVO> detail(StockDataDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return ResultEntity.error("查询失败，参数为空");
        }
        //dto-->entity
        StockData stockData = new StockData();
        stockData.setId(dto.getId());
        List<StockData> stockDatas = stockDataMapper.selectStockData(stockData);
        if (ObjectUtil.isNotEmpty(stockDatas)) {
            //entity-->vo
            StockData stockDataTemp = stockDatas.get(0);
            StockDataVO stockDataVO = new StockDataVO();
            BeanUtils.copyProperties(stockDataTemp, stockDataVO);
            return ResultEntity.success(stockDataVO);
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
    public void exportFile(StockDataDTO dto, HttpServletResponse response) throws Exception {


    }
}
