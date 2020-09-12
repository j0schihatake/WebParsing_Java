package selenium;

import com.google.common.io.Files;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class SeleniumWork {

    private static ChromeDriverService service;
    public static WebDriver driver;

    public static ArrayList<String> tabs = new ArrayList<String>();

    public static String findClassName = "erw2ohd2";

    public static String saveScreenDirectory = "E:\\Projects\\Reports";

    public static String baseUrl = "https://auto.drom.ru/region55/all/page3/?tcb=1599890514&maxprice=500000&unsold=1";

    public static String PATH_TO_CHROMEDRIVER_EXE = "E:\\Projects\\WebParsing_Java\\src\\main\\resources\\chromedriver35.exe";

    // Список всех обьявлений на момент старта:
    public static HashMap<String, Order> allOrder = new HashMap<String, Order>();

    public SeleniumWork() throws IOException {

        ChromeOptions options = new ChromeOptions();

        service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(PATH_TO_CHROMEDRIVER_EXE))
                .usingAnyFreePort()
                .build();
        service.start();

        driver = new ChromeDriver(service);
    }

    /**
     * При первом запуске получаем/запоминаем все обьявления на странице:
     */
    public void initializePage(){
        List<WebElement> allElementOrder = findAllOrder();

        for (int i = 0; i < allElementOrder.size(); i++ ){
            WebElement nextElement = allElementOrder.get(i);
            String nextKey = generateOrderInfo(nextElement.getText());
            addNewOrder(nextElement, nextKey);
        }
    }

    public void autentificate(){
        goTo("https://my.drom.ru/sign");

        String login = "89502108777";
        String password = "33n8p8ez";

        WebElement loginField = driver.findElement(By.name("sign"));
        WebElement passwordField = driver.findElement(By.name("password"));

        loginField.sendKeys(login);
        passwordField.sendKeys(password);

        WebElement signButton = driver.findElement(By.id("signbutton"));
        signButton.click();
    }

    /**
     * Метод выполняет проверку не появились ли новые обьявления:
     */
    public void checkNewOrder() throws Exception {

        goTo(baseUrl);
        driver.manage().window().maximize();

        autentificate();

        goTo(baseUrl);

        initializePage();

        /**
         * Зацикливаем проверку:
         */
        while(true) {

            Thread.sleep(3000);

            // Обновляем страничку:
            refresh();

            List<WebElement> allElementOrder = findAllOrder();

            for (int i = 0; i < allElementOrder.size(); i++) {
                WebElement nextElement = allElementOrder.get(i);
                String nextKey = generateOrderInfo(nextElement.getText());
                WebElement actual = isNewOrder(nextElement, nextKey);
                if (actual != null) {
                    /**
                     * Тут переходим по ссылке в новую вкладку,
                     * Нажимаем кнопку,
                     * Делаем скрин(теперь просто в файл сохраняем).
                     */
                    updateNew(actual);
                    addNewOrder(actual, nextKey);
                }
            }
        }

        // Остаемся на той-же вкладке:
        //updateNew(allElementOrder.get(0));
    }

    /**
     * Обработка новых обьявлений:
     */
    public void updateNew(WebElement element) throws Exception {

        String phoneNumber = "";

        goTo(element.getAttribute("href"));

        // Нажимаем кнопку показать номер телефона:
        List<WebElement> targets = driver.findElements(By.tagName("button"));
        for(int i = 0; i < targets.size(); i++){
            WebElement button = targets.get(i);
            if(button.getText().equals("Показать контакты")){
                System.out.println(targets.get(i).getText());
                button.click();
            }
        }

        Thread.sleep(3000);
        List<WebElement> targets3 = driver.findElements(By.tagName("span"));
        for(int i = 0; i < targets3.size(); i++){
            WebElement elem = targets3.get(i);
            System.out.println(elem.getText());
            if(elem.getText().contains("+7")){
                phoneNumber = elem.getText();
            }
        }
        System.out.println("Номер телефона = " + phoneNumber);

        saveAsPage(element, phoneNumber);

        goTo(baseUrl);
    }

    /**
     * Метод сохраняет страничку для браузера полностью.
     * @param element
     * @throws IOException
     */
    public void saveAsPage(WebElement element, String phoneNumber) throws IOException {

        String stored_report = driver.getPageSource();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));

        String fullPath = saveScreenDirectory + dtf.format(now) + phoneNumber + "report" + ".html";

        File f = new File(fullPath);
        FileWriter writer = new FileWriter(f,true);
        writer.write(stored_report);
        System.out.println("Report Created is in Location : " + f.getAbsolutePath());
        writer.close();
    }

    /**
     * Метод находит все обьявления на странице:
     * @return
     */
    public List<WebElement> findAllOrder(){
        // Тут у обьекта driver(я так понимаю в нем вся страничка)
        // запрашиваем определенные элементы надо в хроме глянуть что ищем: например у всех ссылок одинаковый класс class=... class="css-1dyr1oa erw2ohd2"
        List<WebElement> element = driver.findElements(By.className(findClassName));

        //System.out.println("Число найденых элементов: " + element.size());
        return element;
    }

    /**
     * Метод переключается в основную вкладку браузера:
     */
    public void goToMainTab(){
        driver.switchTo().window(tabs.get(0)); // возврат к основной вкладке... 0 это первая
    }

    /**
     * Метод для выуживания признаков из текста обьявления
     * @param findString
     * @return
     */
    public String generateOrderInfo(String findString) {

        String[] splited = findString.split("\\n");

        String result = "";

        result = splited[0] + splited[1];

        return result;
    }

    /**
     * Проверка не новое ли это обьявление:
     * @param element
     * @return
     */
    public WebElement isNewOrder(WebElement element, String key){
        WebElement result = null;
        return !allOrder.containsKey(key) ? element : null;
    }

    /**
     * Метод обновляет страницу:
     */
    public void refresh(){
        driver.navigate().refresh();
    }

    /**
     * Переход по указанному url:
     * @param url
     */
    public void goTo(String url){
        driver.get(url);
    }

    /**
     * Вроде закрывает вкладку(или окно? ща проверим)
     */
    public void closeTab(){
        driver.close();
    }

    /**
     * Добавляем обьявление в обработанные:
     * @param element
     */
    public void addNewOrder(WebElement element, String key){
        Order order = new Order(element.getText());
        if(!allOrder.containsKey(key)){
            allOrder.put(key,order);
        }
    }

    /**
     * Метод создает скриншот экрана и сохраняет в Image.
     * @throws Exception
     */
    public static BufferedImage createScreenImage() throws Exception
    {
        Robot robot = new Robot();
        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        return screenShot;
    }

    public static void saveImageAs(BufferedImage image, String patchName) throws IOException {
        ImageIO.write(image, "JPG", new File(patchName));
    }
}
