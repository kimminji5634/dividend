package com.dayone.scraper;


import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{

    // url을 멤버 변수로 빼주기 -> 멤버 변수로 빼놓으면 갖는 이점??
    // 나중에 찾기 쉽고 수정 어렵지 않음, 메모리 관점
    // ***final 을 쓰면 초기화 이후에 값을 바꿀 수 없다는 뜻!!
    // URL이 너무 길어서 우리가 원하는 데이터가 주소 중 파라미터 몇 개 지워도 나타난다면 파라미터 제거하고 써도 됨
    // 원래 https://finance.yahoo.com/quote/COKE/history?period1=99100800&period2=1670198400&interval=1mo
    // &filter=history&frequency=1mo&includeAdjustedClose=true 를 아래와 같이 바꿔줌
    // https://finance.yahoo.com/quote/COKE/history?period1=99100800&period2=1670198400&interval=1mo
    // 유동적으로 바꿀 수 있는 값들을 바꿔주기 위해 스트링 포맷으로 처리함!!
    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    // https://finance.yahoo.com/quote/CRM?p=CRM -> 회사 목록에서 회사 들어가면 뜸
    // ticker는 GitLab Inc. (GTLB) 중 GTLB를 의미하는데 Symbol임!!
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400; // 60초 * 60분 * 24시간

    @Override
    public ScrapedResult scrap(Company company) { // 회사 내의 배당금 정보 가져옴
        var scrapResult = new ScrapedResult(); // 해주는 이유?? return 문 위해서 -> 회사, 배당금 리턴
        scrapResult.setCompany(company); //인자로 받은 company 값 넣어줌

        try {
            long now = System.currentTimeMillis() / 1000; // 현재 시간
            // 회사마다  ticker, 시작날짜, 끝날짜 다르기 때문에 다른 값을 넣어줘야 함
            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now); // URL에 있는 %s, %d, %d
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0); // table 전체 가져옴
            //System.out.println(ele);
            Element tbody = tableEle.children().get(1); // thead, tbody, tfoot 중 tbody 가져옴
            List<Dividend> dividends = new ArrayList<>();
            // tbody안에 있는 데이터 다 가져옴
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) { // Dividend로 끝나지 않는다면
                    continue; // 넘어가기
                }
                // Dividend로 끝나는 데이터는 출력
                //System.out.println(txt);
                String[] splits = txt.split(" ");
                /* 원래 아래와 같이 넣어줬는데 Dividend에 보면 배당금 날짜를 조회한 날짜를 가져오므로 변경!!
                String month = splits[0];
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];*/

                // Month를 객체 생성해서 안쓰고 바로 쓸 수 있는 이유는 strToNumber를 static으로 구현했기 때문
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];
                // strToNumber에서 값을 못찾는 경우 -1을 리턴시킴
                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }
                // for문 돌 떄마다 dividends 하나씩 아래가 추가됨
                dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));
                //System.out.println(year + "/" + month + "/" + day + " -> " + dividend);
            }
            scrapResult.setDividends(dividends);
        }
        catch (IOException e){
            // 정상적으로 실행되지 않았다라는 메시지 주기
            e.printStackTrace();
        }

        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) { // 회사 가져옴
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0); // 회사명 가져오기
            // text() : 모든 text 가져오기, abc - def - ghi 가 있는 경우 첫번째 값인 def 가져와라
            // trim() : 앞 뒤에 있는 공백 지움
            String title = titleEle.text().split(" - ")[1].trim();

            return new Company(ticker, title);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
