package xCloud.service.serviceImpl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.entity.merchants.Merchants;
import xCloud.entity.XMerchantsBusiness;
import xCloud.entity.XMerchantsBusinessDTO;
import xCloud.entity.XMerchantsBusinessVO;
import xCloud.mapper.XMerchantsBusinessMapper;
import xCloud.service.MerchantsService;
import xCloud.service.XMerchantsBusinessService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author andy_mac
 * @description 针对表【x_merchants_business】的数据库操作Service实现
 * @createDate 2025-06-03 09:25:16
 */
@Slf4j
@Service
public class XMerchantsBusinessServiceImpl extends ServiceImpl<XMerchantsBusinessMapper, XMerchantsBusiness> implements XMerchantsBusinessService {

    @Resource
    private XMerchantsBusinessMapper xMerchantsBusinessMapper;
    @Resource
    MerchantsService merchantsService;

    /**
     * 1-新增
     *
     * @param dto dto
     * @return Map
     */
    @Override
    @Transactional
    public ResultEntity add(XMerchantsBusinessDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return ResultEntity.error("新增失败");
        }
        //dto-->entity
        XMerchantsBusiness xMerchantsBusiness = new XMerchantsBusiness();
        BeanUtils.copyProperties(dto, xMerchantsBusiness);
        if (xMerchantsBusiness.getStartDate() == null) {
            xMerchantsBusiness.setStartDate(new Date());
        }
        if (xMerchantsBusiness.getEndDate() == null) {
            xMerchantsBusiness.setEndDate(new Date());
        }
        int count = xMerchantsBusinessMapper.insertXMerchantsBusiness(xMerchantsBusiness);

        log.info("111-新增成功");
        //insert merchants
        Merchants merchants = new Merchants();
        try {
            merchants.setMerchant_id((int) IdWorker.getId());
            merchants.setCreated_time(new Date());
            merchants.setDescription("dto.getAddress()");
            merchants.setName("dto");
            merchants.setStatus(1);
            merchants.setPackaging_rating(new BigDecimal(10));
            merchants.setQuantity_rating(new BigDecimal(10));
            merchants.setTaste_rating(new BigDecimal(10));
            ResultEntity<Merchants> add = merchantsService.add(merchants);

        } catch (Exception e) {
            log.info("测试-新增失败" + e);
        }

        if (count > 0) {
            return ResultEntity.success(count);
        } else {
            return ResultEntity.error("新增失败");
        }
    }

    /**
     * 2-删除
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public ResultEntity delete(XMerchantsBusinessDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getBusiness_id())) {
            return ResultEntity.error("删除失败");
        }
        //dto-->entity
        XMerchantsBusiness xMerchantsBusiness = new XMerchantsBusiness();
        BeanUtils.copyProperties(dto, xMerchantsBusiness);
        int count = xMerchantsBusinessMapper.deleteXMerchantsBusiness(xMerchantsBusiness);
        if (count > 0) {
            return ResultEntity.success(count);
        } else {
            return ResultEntity.error("删除失败");
        }
    }

    /**
     * 3-更新
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public ResultEntity update(XMerchantsBusinessDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getBusiness_id())) {
            return ResultEntity.error("更新失败，参数为空");
        }
        //dto-->entity
        XMerchantsBusiness xMerchantsBusiness = new XMerchantsBusiness();
        BeanUtils.copyProperties(dto, xMerchantsBusiness);
        int count = xMerchantsBusinessMapper.updateXMerchantsBusiness(xMerchantsBusiness);
        if (count > 0) {
            return ResultEntity.success(count, "更新成功");
        } else {
            return ResultEntity.error("更新失败");
        }
    }

    /**
     * 4-查询-列表/搜索
     *
     * @param dto 列表搜索
     * @return Map
     */
    @Override
    public ResultEntity list(XMerchantsBusinessDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return ResultEntity.success("查询失败，参数为空");
        }
        Page<XMerchantsBusiness> page = new Page<>(dto.getCurrent(), dto.getSize());
        //dto-->entity
        XMerchantsBusiness xMerchantsBusiness = new XMerchantsBusiness();
        BeanUtils.copyProperties(dto, xMerchantsBusiness);
        //分页查询
        Page<XMerchantsBusiness> xMerchantsBusinesss = xMerchantsBusinessMapper.selectXMerchantsBusiness(page, xMerchantsBusiness);
        if (ObjectUtil.isNotEmpty(xMerchantsBusinesss)) {
            //entity-->vo
            List<XMerchantsBusinessVO> xMerchantsBusinessVOS = xMerchantsBusinesss.getRecords().stream().map(xMerchantsBusinessTemp -> {
                XMerchantsBusinessVO xMerchantsBusinessVO = new XMerchantsBusinessVO();
                BeanUtils.copyProperties(xMerchantsBusinessTemp, xMerchantsBusinessVO);
                return xMerchantsBusinessVO;
            }).toList();
            return ResultEntity.success(xMerchantsBusinessVOS.size(), "查询成功");
        } else {
            return ResultEntity.error("无数据");
        }
    }

    /**
     * 4.1-查询-详情
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public ResultEntity detail(XMerchantsBusinessDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getBusiness_id())) {
            return ResultEntity.error("查询失败，参数为空");
        }
        //dto-->entity
        XMerchantsBusiness xMerchantsBusiness = new XMerchantsBusiness();
        xMerchantsBusiness.setBusiness_id(dto.getBusiness_id());
        List<XMerchantsBusiness> xMerchantsBusinesss = xMerchantsBusinessMapper.selectXMerchantsBusiness(xMerchantsBusiness);
        if (ObjectUtil.isNotEmpty(xMerchantsBusinesss)) {
            //entity-->vo
            XMerchantsBusiness xMerchantsBusinessTemp = xMerchantsBusinesss.get(0);
            XMerchantsBusinessVO xMerchantsBusinessVO = new XMerchantsBusinessVO();
            BeanUtils.copyProperties(xMerchantsBusinessTemp, xMerchantsBusinessVO);
            return ResultEntity.success(xMerchantsBusinessVO, "查询成功");
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
    public void exportFile(XMerchantsBusinessDTO dto, HttpServletResponse response) throws Exception {

    }
}
