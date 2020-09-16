import selenium.SeleniumWork;

public class Main {

    public static void main(String[] args) throws Exception {

        //----------------------------Настройки:
        // при добавлении нового аккаунта просто скопипастить строчку ниже
        // и подставить нужные логин и пороль:

        SeleniumWork.addUser("89502108777","33n8p8ez");

        SeleniumWork.addUser("89502183545","57xwvfep");

        //----------------------Другие настройки:

        // Деректория куда будут сохраняться html новых страничек:
        SeleniumWork.saveScreenDirectory = "D:\\";

        // Путь до драйвера лежит в папке resources "chromedriver35"
        SeleniumWork.PATH_TO_CHROMEDRIVER_EXE = "D:\\Develop\\Java\\Projects\\WebParser_Java\\src\\main\\resources\\chromedriver35.exe";

        // URL на страничку где заканчиваются закрепленные обьявления(желательно с фильтрами):
        SeleniumWork.baseUrl = "https://auto.drom.ru/region55/all/page2/?tcb=1599890514&maxprice=500000&unsold=1";

        // Выводить ли трасерные сообщения о том что делается: (false = отключено, true = включено)
        SeleniumWork.isDebug = false;

        // Проигрывать ли звук когда сохранена новая страничка:
        // Указать путь до файла, формат waf рекомендуется тот что в resources Warbeat.wav
        SeleniumWork.isPlaySound = false;
        SeleniumWork.fullPathToSound = "D:\\Develop\\Java\\Projects\\WebParser_Java\\src\\main\\resources\\Warbeat.wav";

        // Число успешных срабатываний просмотра номера на одну учетку:
        SeleniumWork.accountSucessPhoneCount = 20;

        SeleniumWork work = new SeleniumWork();
        work.checkNewOrder();

    }


}
