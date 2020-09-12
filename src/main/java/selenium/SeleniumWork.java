package selenium;

import com.google.common.io.Files;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
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

    public static String saveScreenDirectory = "D:\\";

    public static String baseUrl = "https://auto.drom.ru/all/page3/?tcb=1599890514";

    public static String PATH_TO_CHROMEDRIVER_EXE = "D:\\Develop\\Java\\Projects\\WebParser_Java\\src\\main\\resources\\chromedriver35.exe";

    // Список всех обьявлений на момент старта:
    public static HashMap<String, Order> allOrder = new HashMap<String, Order>();

    public SeleniumWork() throws IOException {
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
            addNewOrder(allElementOrder.get(i));
        }
    }

    /**
     * Метод выполняет проверку не появились ли новые обьявления:
     */
    public void checkNewOrder() throws Exception {

        goTo(baseUrl);
        driver.manage().window().maximize();

        initializePage();

        /**
         * Зацикливаем проверку:
         */
        while(true) {

            Thread.sleep(1000);

            // Обновляем страничку:
            refresh();

            List<WebElement> allElementOrder = findAllOrder();

            for (int i = 0; i < allElementOrder.size(); i++) {
                WebElement actual = isNewOrder(allElementOrder.get(i));
                if (actual != null) {
                    /**
                     * Тут переходим по ссылке в новую вкладку,
                     * Нажимаем кнопку,
                     * Делаем скрин(теперь просто в файл сохраняем).
                     */
                    updateNew(actual);
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

        goTo(element.getAttribute("href"));

        // Нажимаем кнопку показать номер телефона:
        //class="b-button b-button_theme_blue b-button_width_available"
        List<WebElement> targets = driver.findElements(By.tagName("button"));
        for(int i = 0; i < targets.size(); i++){
            WebElement button = targets.get(i);
            if(button.getText().equals("Показать контакты")){
                System.out.println(targets.get(i).getText());
                button.click();
            }
        }

        // Запоминаем обьявление так как мы его обработали:
        addNewOrder(element);

        // Размер странички который будем прокручивать вниз и скринить(в числе поворотов ролика мышки):
        //int pageSize = 50;

        saveAsPage(element);

        /**
         * Перешли на страничку, надо скрин сделать(я вот боюсь что ширина рекламы может сожрать часть данных)
         */
        //for (int i = 0; i < pageSize; i += 10){

            //String saveDirectory = saveScreenDirectory + generateOrderInfo(element.getText()) + ".jpg";

            //saveImageAs(createScreenImage(), saveDirectory);

            //Robot robot = new Robot();

            // Покрутить колесико мышки:
            //robot.mouseWheel(10);
        //}

        goTo(baseUrl);
    }

    /**
     * Метод сохраняет страничку для браузера полностью.
     * @param element
     * @throws IOException
     */
    public void saveAsPage(WebElement element) throws IOException {

        String stored_report = driver.getPageSource();
        byte[] allBytes = driver.getPageSource().getBytes("UTF-8");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));

        String fullPath = saveScreenDirectory + dtf.format(now) + "report" + ".html";

        File f = new File(fullPath);
        Files.write(allBytes, f);
        //FileWriter writer = new FileWriter(f,true);
        //writer.write(stored_report);
        System.out.println("Report Created is in Location : " + f.getAbsolutePath());
        //writer.close();
    }

    /**
     * Метод находит все обьявления на странице:
     * @return
     */
    public List<WebElement> findAllOrder(){
        // Тут у обьекта driver(я так понимаю в нем вся страничка)
        // запрашиваем определенные элементы надо в хроме глянуть что ищем: например у всех ссылок одинаковый класс class=... class="css-1dyr1oa erw2ohd2"
        List<WebElement> element = driver.findElements(By.className(findClassName));

        // теперь в element коллекции у нас по идее все ссылки на обьявления:
        // попробую их попотрашить на содержимое:
        //WebElement element1 = element.get(0);

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

        //System.out.print(splited[0]);
        //System.out.println(splited[1]);

        return result;
    }

    /**
     * Проверка не новое ли это обьявление:
     * @param element
     * @return
     */
    public WebElement isNewOrder(WebElement element){
        WebElement result = null;

        String nextKey = "";
        nextKey = generateOrderInfo(element.getText());

        return !allOrder.containsKey(nextKey) ? element : null;
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
    public void addNewOrder(WebElement element){
        Order order = new Order(element.getText());
        if(!allOrder.containsKey(generateOrderInfo(element.getText()))){
            allOrder.put(generateOrderInfo(element.getText()),order);
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
