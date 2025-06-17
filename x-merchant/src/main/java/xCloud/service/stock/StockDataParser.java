package xCloud.service.stock;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import xCloud.entity.Stock;
import xCloud.entity.StockData;
import xCloud.entity.StockHeader;
import xCloud.tools.HttpUtil;
import xCloud.util.CodeX;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/9 17:25
 * @ClassName StockDataParser
 */

@Data
public class StockDataParser {


    // Classes to map the JSON structure
    @Data
    public static class XStockData {
        public int ResultCode;
        public int ResultNum;
        public String QueryID;
        public Result Result;
    }

    @Data
    public static class Result {
        public ListData list;
    }

    @Data
    public static class ListData {
        public List<Header> headers;
        public List<XStock> body;
    }

    @Data
    public static class Header {
        public int canSort;
        public String key;
        public String name;
    }

    @Data
    public static class XStock {
        public String blockName;
        public String code;
        public String exchange;
        public String financeType;
        public String heat;
        public String lastPx;
        public Logo logo;
        public String market;
        public String name;
        public String pxChangeRate;
        public String rankDiff;
    }

    @Data
    public static class Logo {
        public String type;
        public String logo;
    }

    public static void main(String[] args) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String currentDate = ZonedDateTime.now().format(formatter);
            System.out.println(currentDate); // 输出如: 20230615
            System.out.println(DateUtil.date());
            System.out.println(CodeX.getDateOfyyyyMMdd());
//            if (true) {
//                return;
//            }
            //9:30开盘后几分钟内逐渐（9:31，9:33）升高的（5%以上9%以下）立马买掉，随时都会突然极速下来（3%左右），此时你肯定不甘心卖掉，
            String url = "https://finance.pae.baidu.com/vapi/v1/hotrank?tn=wisexmlnew&dsp=iphone&product=stock&style=tablelist&market=all&type=hour&day=20250609&hour=17&pn=0&rn=&finClientType=pc";
            String urlAllDay = "https://finance.pae.baidu.com/vapi/v1/hotrank?tn=wisexmlnew&dsp=iphone&product=stock&style=tablelist&market=all&type=day&day=" + CodeX.getDateOfyyyyMMdd() + "&hour=17&pn=0&rn=&finClientType=pc";

            // JSON string (shortened for brevity; replace with full JSON in practice)
            String jsonString = HttpUtil.doPost(url);
//            String jsonString = "{\"ResultCode\":0,\"ResultNum\":0,\"QueryID\":\"12793919602953757063\",\"Result\":{\"list\":{\"headers\":[{\"canSort\":0,\"key\":\"codeName\",\"name\":\"代码\"},{\"canSort\":0,\"key\":\"pxChangeRate\",\"name\":\"涨跌幅\"},{\"canSort\":0,\"key\":\"heat\",\"name\":\"热度\"}],\"body\":[{\"blockName\":\"互联网电商\",\"code\":\"002640\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"56153\",\"lastPx\":\"5.03\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002640.svg\"},\"market\":\"ab\",\"name\":\"跨境通\",\"pxChangeRate\":\"+10.07%\",\"rankDiff\":\"6\"},{\"blockName\":\"电网设备\",\"code\":\"002471\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"55895\",\"lastPx\":\"6.24\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002471.svg\"},\"market\":\"ab\",\"name\":\"中超控股\",\"pxChangeRate\":\"+10.05%\",\"rankDiff\":\"3\"},{\"blockName\":\"电网设备\",\"code\":\"600468\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"54632\",\"lastPx\":\"8.75\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/ab_600468_2266.svg\"},\"market\":\"ab\",\"name\":\"百利电气\",\"pxChangeRate\":\"+10.06%\",\"rankDiff\":\"0\"},{\"blockName\":\"汽车\",\"code\":\"TSLA\",\"exchange\":\"US\",\"financeType\":\"stock\",\"heat\":\"53202\",\"lastPx\":\"295.140\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_us_TSLA.svg\"},\"market\":\"us\",\"name\":\"特斯拉\",\"pxChangeRate\":\"+3.67%\",\"rankDiff\":\"0\"},{\"blockName\":\"套装软件\",\"code\":\"CRCL\",\"exchange\":\"US\",\"financeType\":\"stock\",\"heat\":\"48181\",\"lastPx\":\"107.700\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/20250606/stock_us_CRCL_0763.svg\"},\"market\":\"us\",\"name\":\"Circle Internet Group, Inc. Class A\",\"pxChangeRate\":\"+29.40%\",\"rankDiff\":\"0\"},{\"blockName\":\"通信设备\",\"code\":\"002104\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"46983\",\"lastPx\":\"12.12\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002104.svg\"},\"market\":\"ab\",\"name\":\"恒宝股份\",\"pxChangeRate\":\"+9.98%\",\"rankDiff\":\"0\"},{\"blockName\":\"文娱用品\",\"code\":\"300651\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"44123\",\"lastPx\":\"36.90\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300651.svg\"},\"market\":\"ab\",\"name\":\"金陵体育\",\"pxChangeRate\":\"+15.31%\",\"rankDiff\":\"0\"},{\"blockName\":\"通用设备\",\"code\":\"002639\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"42714\",\"lastPx\":\"13.09\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002639.svg\"},\"market\":\"ab\",\"name\":\"雪人股份\",\"pxChangeRate\":\"+1.47%\",\"rankDiff\":\"0\"},{\"blockName\":\"文娱用品\",\"code\":\"301287\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"42265\",\"lastPx\":\"48.60\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/ab_301287_3959.svg\"},\"market\":\"ab\",\"name\":\"康力源\",\"pxChangeRate\":\"+20.00%\",\"rankDiff\":\"0\"},{\"blockName\":\"计算机设备\",\"code\":\"002177\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"41568\",\"lastPx\":\"8.22\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002177.svg\"},\"market\":\"ab\",\"name\":\"御银股份\",\"pxChangeRate\":\"+4.71%\",\"rankDiff\":\"0\"},{\"blockName\":\"商用车\",\"code\":\"600418\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"40022\",\"lastPx\":\"38.54\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_600418.svg\"},\"market\":\"ab\",\"name\":\"江淮汽车\",\"pxChangeRate\":\"+9.99%\",\"rankDiff\":\"0\"},{\"blockName\":\"航空装备Ⅱ\",\"code\":\"600760\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"37823\",\"lastPx\":\"54.65\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_600760.svg\"},\"market\":\"ab\",\"name\":\"中航沈飞\",\"pxChangeRate\":\"+9.30%\",\"rankDiff\":\"0\"},{\"blockName\":\"电池\",\"code\":\"300340\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"36880\",\"lastPx\":\"15.72\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300340.svg\"},\"market\":\"ab\",\"name\":\"科恒股份\",\"pxChangeRate\":\"+20.00%\",\"rankDiff\":\"0\"},{\"blockName\":\"化学制药\",\"code\":\"300255\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"35157\",\"lastPx\":\"52.09\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300255.svg\"},\"market\":\"ab\",\"name\":\"常山药业\",\"pxChangeRate\":\"+20.00%\",\"rankDiff\":\"0\"},{\"blockName\":\"半导体\",\"code\":\"NVDA\",\"exchange\":\"US\",\"financeType\":\"stock\",\"heat\":\"35071\",\"lastPx\":\"141.720\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_us_NVDA.svg\"},\"market\":\"us\",\"name\":\"英伟达\",\"pxChangeRate\":\"+1.24%\",\"rankDiff\":\"0\"},{\"blockName\":\"IT服务Ⅱ\",\"code\":\"837748\",\"exchange\":\"BJ\",\"financeType\":\"stock\",\"heat\":\"34215\",\"lastPx\":\"77.98\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/20250514/stock_ab_837748.png\"},\"market\":\"ab\",\"name\":\"路桥信息\",\"pxChangeRate\":\"+29.99%\",\"rankDiff\":\"0\"},{\"blockName\":\"通用设备\",\"code\":\"301137\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"33794\",\"lastPx\":\"38.90\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_301137.svg\"},\"market\":\"ab\",\"name\":\"哈焊华通\",\"pxChangeRate\":\"+19.99%\",\"rankDiff\":\"0\"},{\"blockName\":\"其他电子Ⅱ\",\"code\":\"300493\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"33125\",\"lastPx\":\"23.32\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300493.svg\"},\"market\":\"ab\",\"name\":\"润欣科技\",\"pxChangeRate\":\"+15.67%\",\"rankDiff\":\"0\"},{\"blockName\":\"航空装备Ⅱ\",\"code\":\"002651\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"31436\",\"lastPx\":\"12.16\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002651.svg\"},\"market\":\"ab\",\"name\":\"利君股份\",\"pxChangeRate\":\"+10.05%\",\"rankDiff\":\"0\"},{\"blockName\":\"化学制药\",\"code\":\"300584\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"31215\",\"lastPx\":\"41.06\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300584.svg\"},\"market\":\"ab\",\"name\":\"海辰药业\",\"pxChangeRate\":\"+19.99%\",\"rankDiff\":\"0\"},{\"blockName\":\"化学制药\",\"code\":\"300204\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"30453\",\"lastPx\":\"34.94\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300204.svg\"},\"market\":\"ab\",\"name\":\"舒泰神\",\"pxChangeRate\":\"+19.99%\",\"rankDiff\":\"0\"},{\"blockName\":\"农化制品\",\"code\":\"002250\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"29961\",\"lastPx\":\"14.08\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002250.svg\"},\"market\":\"ab\",\"name\":\"联化科技\",\"pxChangeRate\":\"+10.00%\",\"rankDiff\":\"0\"},{\"blockName\":\"金属新材料\",\"code\":\"300963\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"29877\",\"lastPx\":\"24.88\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300963.svg\"},\"market\":\"ab\",\"name\":\"中洲特材\",\"pxChangeRate\":\"+12.78%\",\"rankDiff\":\"0\"},{\"blockName\":\"通信设备\",\"code\":\"603042\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"29793\",\"lastPx\":\"19.50\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_603042.svg\"},\"market\":\"ab\",\"name\":\"华脉科技\",\"pxChangeRate\":\"+4.45%\",\"rankDiff\":\"0\"},{\"blockName\":\"通信服务\",\"code\":\"002467\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"29744\",\"lastPx\":\"6.74\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002467.svg\"},\"market\":\"ab\",\"name\":\"二六三\",\"pxChangeRate\":\"+0.90%\",\"rankDiff\":\"-16\"},{\"blockName\":\"化学制品\",\"code\":\"300169\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"29155\",\"lastPx\":\"8.81\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300169.svg\"},\"market\":\"ab\",\"name\":\"天晟新材\",\"pxChangeRate\":\"+9.03%\",\"rankDiff\":\"0\"},{\"blockName\":\"其他电源设备Ⅱ\",\"code\":\"002366\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"28251\",\"lastPx\":\"7.25\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002366.svg\"},\"market\":\"ab\",\"name\":\"融发核电\",\"pxChangeRate\":\"+6.15%\",\"rankDiff\":\"0\"},{\"blockName\":\"广告营销\",\"code\":\"300061\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"28196\",\"lastPx\":\"14.00\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300061.svg\"},\"market\":\"ab\",\"name\":\"旗天科技\",\"pxChangeRate\":\"+16.86%\",\"rankDiff\":\"0\"},{\"blockName\":\"包装印刷\",\"code\":\"002735\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"27996\",\"lastPx\":\"17.38\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002735.svg\"},\"market\":\"ab\",\"name\":\"王子新材\",\"pxChangeRate\":\"+4.26%\",\"rankDiff\":\"0\"},{\"blockName\":\"塑料\",\"code\":\"300834\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"27956\",\"lastPx\":\"24.38\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300834.svg\"},\"market\":\"ab\",\"name\":\"星辉环材\",\"pxChangeRate\":\"+13.98%\",\"rankDiff\":\"0\"},{\"blockName\":\"软件开发\",\"code\":\"002298\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"27807\",\"lastPx\":\"7.68\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/ab_002298_8911.svg\"},\"market\":\"ab\",\"name\":\"中电鑫龙\",\"pxChangeRate\":\"-3.40%\",\"rankDiff\":\"0\"},{\"blockName\":\"金属新材料\",\"code\":\"000795\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"27525\",\"lastPx\":\"11.12\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_000795.svg\"},\"market\":\"ab\",\"name\":\"英洛华\",\"pxChangeRate\":\"+9.99%\",\"rankDiff\":\"0\"},{\"blockName\":\"汽车零部件\",\"code\":\"000981\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"27206\",\"lastPx\":\"2.28\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_000981.svg\"},\"market\":\"ab\",\"name\":\"山子高科\",\"pxChangeRate\":\"+4.11%\",\"rankDiff\":\"-31\"},{\"blockName\":\"广告营销\",\"code\":\"002878\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"26839\",\"lastPx\":\"19.20\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002878.svg\"},\"market\":\"ab\",\"name\":\"元隆雅图\",\"pxChangeRate\":\"+10.03%\",\"rankDiff\":\"0\"},{\"blockName\":\"汽车零部件\",\"code\":\"002510\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"26646\",\"lastPx\":\"7.05\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/ab_002510_3643.svg\"},\"market\":\"ab\",\"name\":\"天汽模\",\"pxChangeRate\":\"+5.86%\",\"rankDiff\":\"0\"},{\"blockName\":\"资讯科技器材\",\"code\":\"01810\",\"exchange\":\"HK\",\"financeType\":\"stock\",\"heat\":\"26588\",\"lastPx\":\"54.150\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_hk_01810.svg\"},\"market\":\"hk\",\"name\":\"小米集团-W\",\"pxChangeRate\":\"+1.98%\",\"rankDiff\":\"0\"},{\"blockName\":\"医疗服务\",\"code\":\"300149\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"26381\",\"lastPx\":\"11.65\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300149.svg\"},\"market\":\"ab\",\"name\":\"睿智医药\",\"pxChangeRate\":\"+19.98%\",\"rankDiff\":\"0\"},{\"blockName\":\"化学制药\",\"code\":\"002365\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"24790\",\"lastPx\":\"21.96\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002365.svg\"},\"market\":\"ab\",\"name\":\"永安药业\",\"pxChangeRate\":\"+9.91%\",\"rankDiff\":\"0\"},{\"blockName\":\"专用设备\",\"code\":\"603011\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"24729\",\"lastPx\":\"18.70\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_603011.svg\"},\"market\":\"ab\",\"name\":\"合锻智能\",\"pxChangeRate\":\"+10.00%\",\"rankDiff\":\"0\"},{\"blockName\":\"计算机设备\",\"code\":\"603019\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"24485\",\"lastPx\":\"61.90\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_603019.svg\"},\"market\":\"ab\",\"name\":\"中科曙光\",\"pxChangeRate\":\"0.00%\",\"rankDiff\":\"0\"},{\"blockName\":\"其他电子Ⅱ\",\"code\":\"002130\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"24450\",\"lastPx\":\"22.08\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002130.svg\"},\"market\":\"ab\",\"name\":\"沃尔核材\",\"pxChangeRate\":\"+3.66%\",\"rankDiff\":\"0\"},{\"blockName\":\"贸易Ⅱ\",\"code\":\"600250\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"24273\",\"lastPx\":\"11.17\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_600250.svg\"},\"market\":\"ab\",\"name\":\"南京商旅\",\"pxChangeRate\":\"+10.05%\",\"rankDiff\":\"0\"},{\"blockName\":\"计算机设备\",\"code\":\"300368\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"23890\",\"lastPx\":\"11.75\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_300368.svg\"},\"market\":\"ab\",\"name\":\"汇金股份\",\"pxChangeRate\":\"+6.05%\",\"rankDiff\":\"0\"},{\"blockName\":\"化学原料\",\"code\":\"600610\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"23507\",\"lastPx\":\"15.33\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_600610.svg\"},\"market\":\"ab\",\"name\":\"中毅达  \",\"pxChangeRate\":\"-4.19%\",\"rankDiff\":\"0\"},{\"blockName\":\"一般零售\",\"code\":\"603123\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"23125\",\"lastPx\":\"12.80\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_603123.svg\"},\"market\":\"ab\",\"name\":\"翠微股份\",\"pxChangeRate\":\"-2.07%\",\"rankDiff\":\"0\"},{\"blockName\":\"证券Ⅱ\",\"code\":\"601059\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"23072\",\"lastPx\":\"16.04\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_601059.svg\"},\"market\":\"ab\",\"name\":\"信达证券\",\"pxChangeRate\":\"+10.01%\",\"rankDiff\":\"0\"},{\"blockName\":\"环境治理\",\"code\":\"301148\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"22565\",\"lastPx\":\"22.66\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/ab_301148_2310.svg\"},\"market\":\"ab\",\"name\":\"嘉戎技术\",\"pxChangeRate\":\"+8.84%\",\"rankDiff\":\"0\"},{\"blockName\":\"电力\",\"code\":\"600644\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"22481\",\"lastPx\":\"15.38\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_600644.svg\"},\"market\":\"ab\",\"name\":\"乐山电力\",\"pxChangeRate\":\"-0.06%\",\"rankDiff\":\"0\"},{\"blockName\":\"化学制药\",\"code\":\"002437\",\"exchange\":\"SZ\",\"financeType\":\"stock\",\"heat\":\"22464\",\"lastPx\":\"3.04\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_002437.svg\"},\"market\":\"ab\",\"name\":\"誉衡药业\",\"pxChangeRate\":\"+10.14%\",\"rankDiff\":\"0\"},{\"blockName\":\"通信设备\",\"code\":\"600105\",\"exchange\":\"SH\",\"financeType\":\"stock\",\"heat\":\"22412\",\"lastPx\":\"8.56\",\"logo\":{\"type\":\"normal\",\"logo\":\"https://baidu-finance.cdn.bcebos.com/imgs/logo/stocks/stock_ab_600105.svg\"},\"market\":\"ab\",\"name\":\"永鼎股份\",\"pxChangeRate\":\"+5.81%\",\"rankDiff\":\"0\"}]}}}";
            System.out.println(jsonString);

            // Initialize ObjectMapper--
            ObjectMapper mapper = new ObjectMapper();

            // Parse JSON into XStockData object--
            XStockData xStockData = mapper.readValue(jsonString, XStockData.class);

            // Access and print parsed data--
            System.out.println("Query ID: " + xStockData.QueryID);
            System.out.println("Headers:");
            System.out.println("Stocks:");
            System.out.println(xStockData.getQueryID());
            System.out.println(xStockData.getResultCode());
            System.out.println(xStockData.getResultNum());
            System.out.println(xStockData.getResult().getList().getHeaders());
            List<XStock> body = xStockData.getResult().getList().getBody();


            Map<String, Integer> map = new HashMap<>();
            int count = 0;
            for (XStock stock : body) {
                count++;
                System.out.println("----第" + count + "条数据");
                if (map.containsKey(stock.getBlockName())) {
                    map.put(stock.getBlockName(), map.get(stock.getBlockName()) + 1);
                } else {
                    map.put(stock.getBlockName(), 1);
                }
                System.out.println(stock.getCode());
                System.out.println("getName: " + stock.getName());
                System.out.println("getHeat: " + stock.getHeat());
                System.out.println("getPxChangeRate: " + stock.getPxChangeRate());
                System.out.println("getRankDiff: " + stock.getRankDiff());
                System.out.println("getLogo().getLogo(): " + stock.getLogo().getLogo());
                System.out.println("getLogo getType: " + stock.getLogo().getType());
                System.out.println("getMarket: " + stock.getMarket());
                System.out.println("getFinanceType: " + stock.getFinanceType());
                System.out.println("getExchange: " + stock.getExchange());
                System.out.println("getBlockName: " + stock.getBlockName());
                System.out.println("getLastPx: " + stock.getLastPx());
            }
            // 创建 TreeMap，基于值的降序和键的升序排序
            TreeMap<String, Integer> sortedMap = new TreeMap<>(
                    (o1, o2) -> {
                        int valueCompare = map.get(o2).compareTo(map.get(o1)); // 降序
                        return valueCompare != 0 ? valueCompare : o1.compareTo(o2); // 值相等时按键升序
                    }
            );
            // 将 map 的数据添加到 sortedMap
            sortedMap.putAll(map);
            for (Map.Entry<String, Integer> stringIntegerEntry : sortedMap.entrySet()) {
                System.out.println(stringIntegerEntry.getKey() + "---" + stringIntegerEntry.getValue());
            }

            //            1-
            StockData stockData = new StockData();
            stockData.setQuery_id(xStockData.getQueryID());
            stockData.setResult_code(xStockData.getResultCode());
            stockData.setResult_num(xStockData.getResultNum());
            stockData.setCreated_time(new Date());


            List<StockDataParser.Header> headers = xStockData.getResult().getList().getHeaders();
            for (StockDataParser.Header header : headers) {
                StockHeader stockHeader = new StockHeader();
                stockHeader.setKey_name(header.getKey());
                stockHeader.setCan_sort(header.getCanSort());
                stockHeader.setName(header.getName());
            }
//
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

            System.out.println("-----");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
