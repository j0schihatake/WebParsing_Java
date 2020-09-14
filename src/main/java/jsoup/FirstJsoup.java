package jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class FirstJsoup {

    public void start() throws IOException {
        Document doc = Jsoup.connect("https://my.drom.ru/sign")
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .referrer("http://www.google.com")
                .get();
        System.out.println(doc.toString());
    }

    /**
     * При первом запуске получаем/запоминаем все обьявления на странице:
     */
    public void initializePage(){
        //List<WebElement> allElementOrder = findAllOrder();

        //for (int i = 0; i < allElementOrder.size(); i++ ){
            //WebElement nextElement = allElementOrder.get(i);
            //String nextKey = generateOrderInfo(nextElement.getText());
            //addNewOrder(nextKey);
        //}
    }


}
