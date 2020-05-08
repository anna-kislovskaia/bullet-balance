package com.bulletbalance.service;

import com.bulletbalance.utils.PortfolioUtils;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Repository
public class MoexPriceRepository {
    private static final Logger log = LogManager.getLogger(MoexPriceRepository.class);
    private static int PRICE_INDEX = 4;
    private static int DATE_INDEX = 2;

    private final Map<String, NavigableMap<LocalDate, Double>> instrumentPrices = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${moex.export.host}")
    private String host;

    @Autowired
    private MoexInstrumentService instrumentService;

    public List<Double> getPrices(String ticker, LocalDate startDate, LocalDate endDate) {
        Preconditions.checkArgument(startDate.isBefore(endDate), "Start date must be before end date");
        MoexInstrumentProfile instrumentProfile = instrumentService.getProfile(ticker);
        NavigableMap<LocalDate, Double> prices = getPrices(ticker);
        // check if data loaded
        LocalDate earliest = prices.isEmpty() ?  null : prices.firstKey();
        if (earliest == null) {
            loadPrices(instrumentProfile, startDate, LocalDate.now());
        } else if (startDate.isBefore(earliest)) {
            loadPrices(instrumentProfile, startDate, earliest);
        }
        return new ArrayList<>(prices.subMap(startDate, true, endDate, true).values());
    }

    private void loadPrices(MoexInstrumentProfile profile, LocalDate fromDate, LocalDate toDate) {
        String ticker = profile.getTicker();
        log.info("Loading {} daily close {} - {} ", ticker, fromDate, toDate);
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

        NavigableMap<LocalDate, Double> prices = getPrices(ticker);
        restTemplate.execute(URI.create(url), HttpMethod.GET, requestCallback, clientHttpResponse -> {
            InputStream content = clientHttpResponse.getBody();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
            bufferedReader.lines().forEach(line -> {
                String[] tokens = line.split(";");
                Double price = Double.valueOf(tokens[PRICE_INDEX]);
                LocalDate date = PortfolioUtils.intToLocalDate(Integer.valueOf(tokens[DATE_INDEX]));
                prices.put(date, price);
            });
            bufferedReader.close();
            return null;
        });
    }

    private NavigableMap<LocalDate, Double> getPrices(String ticker) {
         return instrumentPrices.computeIfAbsent(ticker, symbol -> new ConcurrentSkipListMap<>());
    }
}
