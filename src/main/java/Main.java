import selenium.SeleniumWork;

public class Main {

    public static void main(String[] args) throws Exception {

        //----------------------------Настройки:

        // Логин для авторизации:
        SeleniumWork.login = "89502108777";

        // Пороль для авторизации:
        SeleniumWork.password = "33n8p8ez";

        // Деректория куда будут сохраняться html новых страничек:
        SeleniumWork.saveScreenDirectory = "D:\\";

        // Путь до драйвера лежит в папке resources "chromedriver35"
        SeleniumWork.PATH_TO_CHROMEDRIVER_EXE = "D:\\Develop\\Java\\Projects\\WebParser_Java\\src\\main\\resources\\chromedriver35.exe";

        // URL на страничку где заканчиваются закрепленные обьявления(желательно с фильтрами):
        SeleniumWork.baseUrl = "https://auto.drom.ru/region55/all/page2/?tcb=1599890514&maxprice=500000&unsold=1";

        SeleniumWork work = new SeleniumWork();
        work.checkNewOrder();

    }


}
