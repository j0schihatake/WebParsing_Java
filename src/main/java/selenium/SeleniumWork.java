package selenium;

import com.google.common.io.Files;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
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

    private static int accountNumber = 0;

    public static ArrayList<String> account = new ArrayList<>();

    public static HashMap<String, String> allUsers = new HashMap<>();

    public static boolean isPlaySound = false;

    public static boolean isDebug = false;

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
     * Метод выполняет проверку не появились ли новые обьявления:
     */
    public void checkNewOrder() throws Exception {

        driver.manage().window().maximize();

        getNextUser(0);

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

            if(isDebug)
                System.out.println("chekNewOrder: начал новый цикл.");

            List<WebElement> allElementOrder = findAllOrder();

            for (int i = 0; i < allElementOrder.size(); i++) {
                WebElement nextElement = allElementOrder.get(i);
                String nextKey = generateOrderInfo(nextElement.getText());
                WebElement actual = null;
                actual = isNewOrder(nextElement, nextKey);
                if (actual != null) {
                    actual = nextElement;
                    nextKey = nextKey;

                    // Получаем номер телефона:
                    String phoneNumber = updateNew(actual);

                    // Если номер телефона пустой меняем учетную запись.
                    if(!phoneNumber.equals("")) {
                        addNewOrder(nextKey);
                        saveAsPage(phoneNumber);
                        if(isPlaySound)
                            playSound();
                    }else{
                        changeAccount();
                    }
                    goTo(baseUrl);
                    break;
                }
            }
        }
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

    /**
     * Метод выполняет авторизацию на сайте:
     */
    public boolean autentificate() throws InterruptedException {

        boolean result = false;

        goTo("https://my.drom.ru/sign");

        try {
            WebElement loginField = driver.findElement(By.name("sign"));
            loginField.clear();
            loginField.sendKeys(login);
            if(isDebug)
                System.out.println("autentificate: Элемент loginField успешно найден.");
        }catch (org.openqa.selenium.StaleElementReferenceException ex){
            if(isDebug)
                System.out.println("autentificate: не удалось найти элемент loginField. Описание ошибки: " + ex.getStackTrace());
        }
        try {
            WebElement passwordField = driver.findElement(By.name("password"));
            passwordField.clear();
            passwordField.sendKeys(password);
            if(isDebug)
                System.out.println("autentificate: Элемент passwordField успешно найден.");
        }catch (org.openqa.selenium.StaleElementReferenceException ex) {
            if(isDebug)
                System.out.println("autentificate: Элемент passwordField не найден. Описание ошибки: " + ex.getStackTrace());
        }

        try{
            WebElement signButton = driver.findElement(By.id("signbutton"));
            signButton.click();
            if(isDebug)
                System.out.println("autentificate: успешно найдена кнопка прохождения авторизации.");
        }catch (org.openqa.selenium.StaleElementReferenceException ex) {
            if(isDebug)
                System.out.println("autentificate: не удалось найти кнопку прохождения авторизации.");
        }

        Thread.sleep(300);
        result = driver.getCurrentUrl().equals("https://my.drom.ru/sign");

        if(isDebug)
            System.out.println("autentificate: Аутентификация прошла: " + result);

        return result;
    }

    /**
     * Метод выполняет выход из рабочей учетки:
     */
    public void logout() throws InterruptedException {
        goTo("https://my.drom.ru/logout");
        Thread.sleep(100);
    }

    /**
     * Смена аккаунта на следующий:
     * @throws InterruptedException
     */
    public void changeAccount() throws InterruptedException {
        logout();
        int nextNumber = (accountNumber + 1)
                >= (account.size())
                ? 0 : accountNumber + 1;
        getNextUser(nextNumber);

        if(isDebug)
            System.out.println("changeAccount смена аккаунта на: login = " + login + ", password = " + password);

        autentificate();
    }

    /**
     * Метод возвращает пользователя по его номеру в списке(порядок добавления в Main)
     * @param number
     */
    public void getNextUser(int number){
        login = account.get(number);
        password = allUsers.get(login);
        accountNumber = number;
    }

    /**
     * Метод добавляет связку логина и пороля нового юзера.
     * @param login
     * @param password
     */
    public static void addUser(String login, String password){
        account.add(login);
        allUsers.put(login,password);
    }

    /**
     * Обработка новых обьявлений:
     */
    public String updateNew(WebElement element) throws Exception {

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
            if(isDebug)
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
            if(isDebug)
                System.out.println("UpdateNew: Номер телефона = " + phoneNumber);
        }catch (org.openqa.selenium.StaleElementReferenceException ex) {
            if(isDebug)
                System.out.println("UpdateNew: не удалось найти элемент номер телефона на странице, описание ошибки: " + ex.getStackTrace());
        }
        return phoneNumber;
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
            if(isDebug)
                System.out.println("saveAsPage: не удалось получить тело html страницы, описание ошибки: " + ex);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        if(isDebug)
            System.out.println(dtf.format(now));

        String fullPath = saveScreenDirectory + dtf.format(now) + phoneNumber + "report" + ".html";

        File f = new File(fullPath);
        FileWriter writer = new FileWriter(f,true);
        writer.write(stored_report);
        if(isDebug)
            System.out.println("saveAsPage: Report Created is in Location : " + f.getAbsolutePath());
        writer.close();
    }

    /**
     * Метод находит все обьявления на странице:
     * @return
     */
    public List<WebElement> findAllOrder(){
        if(isDebug)
            System.out.println("findAllOrder: выполнение поиска всех обновлений.");
        List<WebElement> element = null;
        goTo(baseUrl);
        try{
            element = driver.findElements(By.className(findClassName));
            if(isDebug)
                System.out.println("findAllOrder: Число найденых элементов: " + element.size());
        }catch (org.openqa.selenium.StaleElementReferenceException ex){
            if(isDebug)
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

        if(isDebug)
            System.out.println("generateOrderInfo: генерация ключа изинформации выполнена, result = " + result + ".");

        return result;
    }

    /**
     * Проверка не новое ли это обьявление:
     * @param element
     * @return
     */
    public WebElement isNewOrder(WebElement element, String key){
        if(isDebug)
            System.out.println("isNewOrder: Проверка новвое ли обьявление: key = " + key + ", !allOrder.containsKey(key) = " + !allOrder.containsKey(key) + ".");
        return !allOrder.containsKey(key) ? element : null;
    }

    /**
     * Метод обновляет страницу:
     */
    public void refresh(){
        if(isDebug)
            System.out.println("refresh: обновление страницы.");
        driver.navigate().refresh();
    }

    /**
     * Переход по указанному url:
     * @param url
     */
    public void goTo(String url){
        driver.get(url);
        if(isDebug)
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
            allOrder.put(key,order);
            if(isDebug)
                System.out.println("addNewOrder: Добавление нового обьявления в HashMap. key = " + key);
        }
        if(isDebug)
            System.out.println("addNewOrder: allOrder.toString = " + allOrder.toString());
    }

    public void playSound() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        File soundFile = new File("Warbeat.wav"); //Звуковой файл

        //Получаем AudioInputStream
        //Вот тут могут полететь IOException и UnsupportedAudioFileException
        AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);

        //Получаем реализацию интерфейса Clip
        //Может выкинуть LineUnavailableException
        Clip clip = AudioSystem.getClip();

        //Загружаем наш звуковой поток в Clip
        //Может выкинуть IOException и LineUnavailableException
        clip.open(ais);

        clip.setFramePosition(0); //устанавливаем указатель на старт
        clip.start(); //Поехали!!!
    }

    /**
     * Метод создает скриншот экрана и сохраняет в Image.
     * @throws Exception
     */
    public static BufferedImage createScreenImage() throws Exception {
        Robot robot = new Robot();
        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        return screenShot;
    }

    public static void saveImageAs(BufferedImage image, String patchName) throws IOException {
        ImageIO.write(image, "JPG", new File(patchName));
    }
}
