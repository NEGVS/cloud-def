package xCloud.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.entity.merchants.Merchants;
import xCloud.entity.merchants.MerchantsDTO;
import xCloud.entity.merchants.MerchantsVO;
import xCloud.mapper.MerchantsMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author andy_mac
 * @description 针对表【x_merchants】的数据库操作Service实现
 * @createDate 2025-04-09 09:38:50
 */
@Service
public class MerchantsServiceImpl extends ServiceImpl<MerchantsMapper, Merchants> implements MerchantsService {

    @Resource
    private MerchantsMapper merchantsMapper;

    /**
     * 1-新增
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public Map<String, Object> add(MerchantsDTO dto) {

        //dto-->entity
        Merchants merchants = new Merchants();
        BeanUtils.copyProperties(dto, merchants);
//        int count = merchantsMapper.insertMerchants(merchants);
        return null;
    }

    /**
     * 2-删除
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public Map<String, Object> delete(MerchantsDTO dto) {

        //dto-->entity
        Merchants merchants = new Merchants();
        BeanUtils.copyProperties(dto, merchants);
//        int count = merchantsMapper.deleteMerchants(merchants);
        return null;
    }

    /**
     * 3-更新
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public Map<String, Object> update(MerchantsDTO dto) {

        //dto-->entity
        Merchants merchants = new Merchants();
        BeanUtils.copyProperties(dto, merchants);
//        int count = merchantsMapper.updateMerchants(merchants);
        return null;
    }

    /**
     * 4-查询-列表/搜索
     *
     * @param dto 列表搜索
     * @return Map
     */
    @Override
    public ResultEntity<IPage> listPage(MerchantsDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return ResultEntity.error("参数错误");
        }
        Page<Merchants> page = new Page<>(dto.getCurrent(), dto.getSize());
        //dto-->entity
        Merchants merchants = new Merchants();
        BeanUtils.copyProperties(dto, merchants);
        //分页查询
        Page<Merchants> merchantss = merchantsMapper.selectMerchants(page, merchants);

        return ResultEntity.success(merchantss);
    }

    /**
     * 4.1-查询-详情
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public MerchantsVO detail(MerchantsDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getMerchant_id())) {
            return null;
        }
        return null;
        //dto-->entity
//        Merchants merchants = new Merchants();
//        merchants.setDetailId(dto.getDetailId());
//        List<Merchants> merchantss = merchantsMapper.selectMerchants(merchants);
//        if (ObjectUtil.isNotEmpty(merchantss)) {
//            //entity-->vo
//            Merchants merchantsTemp = merchantss.get(0);
//            MerchantsVO merchantsVO = new MerchantsVO();
//            BeanUtils.copyProperties(merchantsTemp, merchantsVO);
//            return new resInfo().responseEntityOK(merchantsVO, "查询成功");
//        } else {
//            return new resInfo().responseFail("查询失败");
//        }
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
    public void exportFile(MerchantsDTO dto, HttpServletResponse response) throws Exception {

    }
}
