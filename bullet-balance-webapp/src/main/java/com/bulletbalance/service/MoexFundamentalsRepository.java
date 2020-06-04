package com.bulletbalance.service;

import com.bulletbalance.model.InstrumentFundamentals;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MoexFundamentalsRepository {
    private static final Logger log = LogManager.getLogger(MoexFundamentalsRepository.class);

    private final Map<String, InstrumentFundamentals> profiles = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> priceToEarnings = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> priceToBookValue = new ConcurrentHashMap<>();

    @Value("${moex.fundamentals.url}")
    private String fundamentalsUrl;

    @Autowired
    @Qualifier("httpsRestTemplate")
    private RestTemplate httpsRestTemplate;

    @PostConstruct
    private void init() {
        loadFundamentals(fundamentalsUrl + "?field=p_e", priceToEarnings);
        loadFundamentals(fundamentalsUrl + "?field=p_bv", priceToBookValue);
        log.info("Fundamentals loaded");
    }

    public InstrumentFundamentals getInstrumentProfile(String symbol) {
        return profiles.computeIfAbsent(symbol, key -> {
            int lotSize = getLotSize(symbol);
            return new InstrumentFundamentals(lotSize, priceToEarnings.get(symbol), priceToBookValue.get(symbol));
        });
    }

    private void loadFundamentals(String url, Map<String, BigDecimal> result) {
        try {
            ResponseEntity<String> response = httpsRestTemplate.exchange(url, HttpMethod.GET, null, String.class);
            //log.info(response);
            if (response.getStatusCode().is2xxSuccessful()) {
                Document document = Jsoup.parse(response.getBody());
                Element table = document.selectFirst("table.trades-table");
                Elements rows = table.select("tr");
                rows.forEach(row -> {
                    Elements cells = row.select("td");
                    if (cells.size() < 5) {
                        return;
                    }
                    String link = cells.get(2).selectFirst("a").attr("href");
                    String symbol = link.substring(link.lastIndexOf(".") + 1).trim();
                    if (symbol.isEmpty()) {
                        link = cells.get(1).selectFirst("a").attr("href");
                        symbol = link.substring(link.lastIndexOf("/") + 1).trim();
                    }
                    String text = cells.get(4).selectFirst("strong").text().trim();
                    BigDecimal value = BigDecimal.valueOf(Double.parseDouble(text));
                    result.put(symbol, value);
                    log.info("{} -> {}", symbol, value);
                });
            }
        } catch (RestClientException e) {
            log.error("Unable to load fundamentals", e);
        }
    }

    private int getLotSize(String symbol) {
        String url = String.format("https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities/%s.jsonp", symbol);
        int lotSize = 1;
        try {
            ResponseEntity<MetaDataContainer> response = httpsRestTemplate.getForEntity(url, MetaDataContainer.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                MoexInstrumentMetaData meta = response.getBody().securities;
                int lotSizeIndex = meta.columns.indexOf("LOTSIZE");
                if (lotSizeIndex >= 0) {
                    lotSize = (Integer)meta.data.get(0).get(lotSizeIndex);
                }
            }
        } catch (Exception e) {
            log.error("Unable to load lot size for " + symbol, e);
        }
        log.info("{} lot size={}", symbol, lotSize);
        return lotSize;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoexInstrumentMetaData {
        @JsonProperty
        List<String> columns;
        @JsonProperty
        List<List<Object>> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaDataContainer {
        @JsonProperty
        MoexInstrumentMetaData securities;
    }

}
