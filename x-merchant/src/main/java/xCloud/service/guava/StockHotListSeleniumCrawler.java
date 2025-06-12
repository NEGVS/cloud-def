package xCloud.service.guava;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import xCloud.tools.HttpUtil;

import java.time.Duration;
import java.util.List;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/9 16:27
 * @ClassName StockHotListSeleniumCrawler
 */
public class StockHotListSeleniumCrawler {

    public static void main(String[] args) {
        // 设置ChromeDriver路径（需下载并配置chromedriver）
        System.setProperty("webdriver.chrome.driver", "/Users/andy_mac/Downloads/chromedriver-mac-arm64/chromedriver");

        // 配置Chrome选项
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 无头模式，不打开浏览器窗口
//        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.7151.69 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled"); // 绕过反爬检测
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);

        String url = "https://finance.pae.baidu.com/vapi/v1/hotrank?tn=wisexmlnew&dsp=iphone&product=stock&style=tablelist&market=all&type=hour&day=20250609&hour=17&pn=0&rn=&finClientType=pc";

        try {
            // 访问页面
            driver.get(url);
            String s = HttpUtil.doPost(url);
            System.out.println(s);
            // 等待表格数据加载完成
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            List<WebElement> stockRows = wait.until(driver1 -> driver1.findElements(By.cssSelector("table tbody tr")));

            // 尝试宽泛选择器
//            List<WebElement> stockRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
//                    By.cssSelector("table tbody tr"))); // 替换为实际选择器
//            List<WebElement> stockRows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
//                    By.cssSelector(".hot-list-table tbody tr")));
            // 调试：输出页面源码
            System.out.println("页面源码片段：" + driver.getPageSource().substring(0, Math.min(500, driver.getPageSource().length())));
            System.out.println("热门股票列表:");
            System.out.println("排名 | 股票名称 | 股票代码 | 当前价格 | 涨跌幅 | 热度");
            System.out.println("---------------------------------------------------");
            if (stockRows.size() > 0) {// 遍历每一行数据
                for (WebElement row : stockRows) {
                    String rank = row.findElement(By.cssSelector(".rank")).getText();
                    String name = row.findElement(By.cssSelector(".stock-name")).getText();
                    String code = row.findElement(By.cssSelector(".stock-code")).getText();
                    String price = row.findElement(By.cssSelector(".stock-price")).getText();
                    String change = row.findElement(By.cssSelector(".stock-change")).getText();
                    String hot = row.findElement(By.cssSelector(".stock-hot")).getText();

                    System.out.printf("%s | %s | %s | %s | %s | %s%n",
                            rank, name, code, price, change, hot);
                }
            } else {
                System.out.println("没有找到任何数据");
            }


        } catch (Exception e) {
            System.err.println("爬取数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭浏览器
            driver.quit();
        }
    }
}
