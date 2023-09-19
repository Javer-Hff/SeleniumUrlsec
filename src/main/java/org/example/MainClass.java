package org.example;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLEncodeUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * @authoer:hff
 * @Date 2023/9/19 18:07
 */
public class MainClass {
    public static void main(String[] args) {
        try {
            execute(URLEncodeUtil.encode("http://www.pornhub.com"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private static void execute (String url) throws IOException, InterruptedException{
        String jsFile = MainClass.class.getClassLoader().getResource("offset.js").getFile();
        System.setProperty("webdriver.edge.driver","./msedgedriver.exe");
        //设置EdgeOptions打开方式，设置headless：无头模式(不弹出浏览器)
        EdgeOptions options = new EdgeOptions();
        options.addArguments("headless");

        //防止检测的一些参数
        options.addArguments("--disable-blink-features");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--incognito");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");
        options.addArguments("--no-default-browser-check");
        List<String> excludeSwitches = new ArrayList<String>();
        excludeSwitches.add("enable-automation");
        options.setExperimentalOption("excludeSwitches", excludeSwitches);
        options.setExperimentalOption("useAutomationExtension", false);

        options.addArguments("--remote-allow-origins=*");
        EdgeDriver edgeDriver = new EdgeDriver(options);
        Map<String, Object> command = new HashMap<>();
        command.put("source", "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        edgeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", command);


        //使用Js打开页面，否则会被检测
        edgeDriver.executeScript("window.open('https://urlsec.qq.com/check.html?url="+url+"');");
        Thread.sleep(5000);
        String currentWindow = edgeDriver.getWindowHandle();
        Set<String> windowHandles = edgeDriver.getWindowHandles();
        for (String windowHandle : windowHandles) {
            if (windowHandle.equals(currentWindow)){
                edgeDriver.switchTo().window(windowHandle).close();
            }
        }
        windowHandles.remove(currentWindow);
        edgeDriver.switchTo().window(windowHandles.toArray()[0].toString());


        //点击按钮
        WebElement element = edgeDriver.findElement(By.id("check-btn"));
        element.click();
        Thread.sleep(5000);

        //切换到验证码frame
        WebElement iframe = edgeDriver.findElement(By.id("tcaptcha_iframe"));
        edgeDriver.switchTo().frame(iframe);
        Thread.sleep(2000);

        //执行js获取滑块移动需要的像素
        String js = FileUtil.readUtf8String(new File(jsFile));
        Object jsResult =edgeDriver.executeScript(js);
        int offset = Integer.valueOf(jsResult.toString());

        WebElement dragThumb = edgeDriver.findElement(By.id("tcaptcha_drag_thumb"));
        //点击并移动滑块的一半距离
        Actions actions = new Actions(edgeDriver);
        actions.moveToElement(dragThumb).click().moveByOffset(offset/2,0).perform();

        int current = 0;
        while (Math.abs(offset - current - (offset/2)) > 5){
            //每次移动加入随机数
            long distance = Math.round(Math.random() * 8) + 5;
            current += distance;
            actions.moveByOffset((int)distance,0).perform();
        }
        actions.click().perform();

        Thread.sleep(15000);
        //切换回父Frame
        edgeDriver.switchTo().parentFrame();

        List<WebElement> liList = edgeDriver.findElements(By.xpath("//ul[@class='result-warning-list']/li"));
        if (liList==null || liList.size()==0){
            System.out.println("ok");
        }else {
            WebElement warningMessage = edgeDriver.findElement(By.xpath("//label[text()='危险描述：']/../span"));
            System.out.println(warningMessage.getText());
        }
    }
}
