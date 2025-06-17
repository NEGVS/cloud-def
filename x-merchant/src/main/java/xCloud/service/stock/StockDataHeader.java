package xCloud.service.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @Description 头部信息
 * @Author Andy Fan
 * @Date 2025/6/13 11:48
 * @ClassName ddd
 * 头部信息，[时间戳, 时间, 价格, 均价, 涨跌额, 涨跌幅, 成交量, 成交额, 累积成交量, 累积成交额]
 * key，[timestamp, time, price, avgPrice, range, ratio, volume, amount, totalVolume, totalAmount]
 * 1749778200,06-13 09:30,16.98,16.98,+0.40,+2.41,799500,13575510,799500,13575510
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDataHeader {

    /**
     * 时间戳
     */
    public String timestamp;
    /**
     * 时间
     */
    public String time;
    /**
     * 当前价格
     */

    public String price;
    /**
     * 均价
     */
    public String avgPrice;
    /**
     * 涨跌额
     */
    public String range;
    /**
     * 涨跌幅
     */
    public String ratio;
    /**
     * 成交额
     */
    public String volume;
    /**
     * 累积成交量
     */
    public String totalVolume;
    /**
     * 累积成交额
     */
    public String totalAmount;
//    /**
//     * 代码
//     */
//    public String stockCode;
//
//    /**
//     * 日期
//     */
//    public String day;
}