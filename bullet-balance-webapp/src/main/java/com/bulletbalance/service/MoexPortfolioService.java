package com.bulletbalance.service;

import com.bulletbalance.portfolio.Portfolio;
import com.bulletbalance.portfolio.PortfolioBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Service
public class MoexPortfolioService {
    private static final Logger log = LogManager.getLogger(MoexPortfolioService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${moex.export.host}")
    private String host;

    @Autowired
    private MoexInstrumentService instrumentService;

    public Portfolio<String> createPortfolio(List<String> tickers, LocalDate fromDate, LocalDate toDate) {
        log.info("Loading trading data {} - {} for {} ", fromDate, toDate, tickers);
        PortfolioBuilder<String> builder = new PortfolioBuilder<>();
        tickers.forEach(ticker -> {
            double[] prices = loadMoexDailyPrices(ticker, fromDate, toDate);
            builder.add(ticker, prices);
        });
        return builder.build();
    }


    private double[] loadMoexDailyPrices(String ticker, LocalDate fromDate, LocalDate toDate) {
        log.info("Loading {} daily close {} - {} ", ticker, fromDate, toDate);
        MoexInstrumentProfile profile = instrumentService.getProfile(ticker);
        String url = new StringBuilder()
                .append("http://")
                .append(host)
                .append("/export9.out")
                .append("?")
                .append("apply=0&dtf=1&tmf=1&MSOR=1&mstime=on&mstimever=1&sep=3&sep2=1&datf=4&p=8&e=.csv")
                .append("&market=").append(profile.getMarket())
                .append("&em=").append(profile.getId())
                .append("&code=").append(ticker)
                .append("&cn=").append(ticker)
                .append("&f=").append(ticker)
                .append("&yf=").append(fromDate.getYear())
                .append("&mf=").append(fromDate.getMonthValue() - 1)
                .append("&df=").append(fromDate.getDayOfMonth() - 1)
                .append("&yt=").append(toDate.getYear())
                .append("&mt=").append(toDate.getMonthValue() - 1)
                .append("&dt=").append(toDate.getDayOfMonth() - 1)
                .toString();
        log.info("URL {} ", url);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Host", host);
        requestHeaders.add("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity<>(null, requestHeaders);
        RequestCallback requestCallback = restTemplate.httpEntityCallback(requestEntity);

        double[] prices = restTemplate.execute(URI.create(url), HttpMethod.GET, requestCallback, clientHttpResponse -> {
            InputStream content = clientHttpResponse.getBody();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
            double[] result = bufferedReader.lines().mapToDouble(this::extractPrice).toArray();
            bufferedReader.close();
            return result;
        });
        log.info("Content \n{} ", prices);
        return prices;
    }

    private double extractPrice(String line) {
        log.info("Parsing {}", line);
        String[] tokens = line.split(";");
        return Double.valueOf(tokens[4]);
    }
}
