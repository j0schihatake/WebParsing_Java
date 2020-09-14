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

    public static String login = "";

    public static String password = "";

    public static String findClassName = "erw2ohd2";

    public static String saveScreenDirectory = "D:\\";

    public static String baseUrl = "https://auto.drom.ru/region55/all/page2/?tcb=1599890514&maxprice=500000&unsold=1";

    public static String PATH_TO_CHROMEDRIVER_EXE = "D:\\Develop\\Java\\Projects\\WebParser_Java\\src\\main\\resources\\chromedriver35.exe";

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
            addNewOrder(nextKey);
        }
    }

    public void autentificate(){

        goTo("https://my.drom.ru/sign");

        //String login = "89502108777";
        //String login = "89502183545";
        //String password = "33n8p8ez";
        //String password = "57xwvfep";

        try {
            WebElement loginField = driver.findElement(By.name("sign"));
            loginField.sendKeys(login);
            System.out.println("autentificate: Элемент loginField успешно найден.");
        }catch (org.openqa.selenium.StaleElementReferenceException ex){
            System.out.println("autentificate: не удалось найти элемент loginField. Описание ошибки: " + ex.getStackTrace());
        }
        try {
            WebElement passwordField = driver.findElement(By.name("password"));
            passwordField.sendKeys(password);
            System.out.println("autentificate: Элемент passwordField успешно найден.");
        }catch (org.openqa.selenium.StaleElementReferenceException ex) {
            System.out.println("autentificate: Элемент passwordField не найден. Описание ошибки: " + ex.getStackTrace());
        }

        try{
            WebElement signButton = driver.findElement(By.id("signbutton"));
            signButton.click();
            System.out.println("autentificate: успешно найдена кнопка прохождения авторизации.");
        }catch (org.openqa.selenium.StaleElementReferenceException ex) {
            System.out.println("autentificate: не удалось найти кнопку прохождения авторизации.");
        }

        System.out.println("autentificate: Аутентификация прошла успешно.");
    }

    /**
     * Метод выполняет проверку не появились ли новые обьявления:
     */
    public void checkNewOrder() throws Exception {

        driver.manage().window().maximize();

        autentificate();

        goTo(baseUrl);

        initializePage();

        /**
         * Зацикливаем проверку:
         */
        while(true) {

            // Обновляем страницу:
            goTo(baseUrl);

            Thread.sleep(3000);

            System.out.println("chekNewOrder: начал новый цикл.");

            List<WebElement> allElementOrder = findAllOrder();

            for (int i = 0; i < allElementOrder.size(); i++) {
                WebElement nextElement = allElementOrder.get(i);
                String nextKey = generateOrderInfo(nextElement.getText());
                WebElement actual = null;
                actual = isNewOrder(nextElement, nextKey);
                //|| i == 4
                if (actual != null) {
                    actual = nextElement;
                    nextKey = nextKey;

                    addNewOrder(nextKey);

                    updateNew(actual);

                    goTo(baseUrl);
                    break;
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

        List<WebElement> targets = null;
        List<WebElement> targets3 = null;
        WebElement button = null;
        WebElement elem = null;

        // Нажимаем кнопку показать номер телефона:
        try {
            targets = driver.findElements(By.tagName("button"));
            for (int i = 0; i < targets.size(); i++) {
                button = targets.get(i);
                if (button.getText().equals("Показать контакты")) {
                    //System.out.println(targets.get(i).getText());
                    button.click();
                }
            }
        }catch (org.openqa.selenium.StaleElementReferenceException ex) {
            System.out.println("Не удалось найти кнопку показать номер телефона: Описание ошибки " + ex.getStackTrace());
        }

        // Пауза перед загрузкой номера на странице:
        Thread.sleep(3000);
        try{
            targets3 = driver.findElements(By.tagName("span"));
            for(int i = 0; i < targets3.size(); i++){
                elem = targets3.get(i);
                //System.out.println(elem.getText());
                if(elem.getText().contains("+7")){
                    phoneNumber = elem.getText();
                }
            }
            System.out.println("UpdateNew: Номер телефона = " + phoneNumber);
        }catch (org.openqa.selenium.StaleElementReferenceException ex) {
            System.out.println("UpdateNew: не удалось найти элемент номер телефона на странице, описание ошибки: " + ex.getStackTrace());
        }

        saveAsPage(phoneNumber);
    }

    /**
     * Метод сохраняет страничку для браузера полностью.
     * @throws IOException
     */
    public void saveAsPage(String phoneNumber) throws IOException, InterruptedException {

        Thread.sleep(100);
        String stored_report = "";
        try{
            stored_report = driver.getPageSource();
        }catch (org.openqa.selenium.StaleElementReferenceException ex) {
            System.out.println("saveAsPage: не удалось получить тело html страницы, описание ошибки: " + ex);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));

        String fullPath = saveScreenDirectory + dtf.format(now) + phoneNumber + "report" + ".html";

        File f = new File(fullPath);
        FileWriter writer = new FileWriter(f,true);
        writer.write(stored_report);
        System.out.println("saveAsPage: Report Created is in Location : " + f.getAbsolutePath());
        writer.close();
    }

    /**
     * Метод находит все обьявления на странице:
     * @return
     */
    public List<WebElement> findAllOrder(){
        System.out.println("findAllOrder: выполнение поиска всех обновлений.");
        List<WebElement> element = null;
        goTo(baseUrl);
        try{
            element = driver.findElements(By.className(findClassName));
            System.out.println("findAllOrder: Число найденых элементов: " + element.size());
        }catch (org.openqa.selenium.StaleElementReferenceException ex){
            System.out.println("Не удалось найти элемент обьявления. Описание ошибки: " + ex);
        }
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

        System.out.println("generateOrderInfo: генерация ключа изинформации выполнена, result = " + result + ".");

        return result;
    }

    /**
     * Проверка не новое ли это обьявление:
     * @param element
     * @return
     */
    public WebElement isNewOrder(WebElement element, String key){
        System.out.println("isNewOrder: Проверка новвое ли обьявление: key = " + key + ", !allOrder.containsKey(key) = " + !allOrder.containsKey(key) + ".");
        return !allOrder.containsKey(key) ? element : null;
    }

    /**
     * Метод обновляет страницу:
     */
    public void refresh(){
        System.out.println("refresh: обновление страницы.");
        driver.navigate().refresh();
    }

    /**
     * Переход по указанному url:
     * @param url
     */
    public void goTo(String url){
        driver.get(url);
        System.out.println("goTo: Переход на указанный url выполнен.");
    }

    /**
     * Вроде закрывает вкладку(или окно? ща проверим)
     */
    public void closeTab(){
        driver.close();
    }

    /**
     * Добавляем обьявление в обработанные:
     */
    public void addNewOrder(String key){
        Order order = new Order(key);
        if(!allOrder.containsKey(key)){
            System.out.println("addNewOrder: Добавление нового обьявления в HashMap. key = " + key);
            allOrder.put(key,order);
        }
        System.out.println("addNewOrder: allOrder.toString = " + allOrder.toString());
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
