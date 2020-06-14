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
        loadFundamentals(fundamentalsUrl);
        log.info("Fundamentals loaded");
    }

    public InstrumentFundamentals getInstrumentProfile(String symbol) {
        return profiles.computeIfAbsent(symbol, key -> {
            int lotSize = getLotSize(symbol);
            return new InstrumentFundamentals(lotSize, priceToEarnings.get(symbol), priceToBookValue.get(symbol));
        });
    }

    private void loadFundamentals(String url) {
        try {
            ResponseEntity<String> response = httpsRestTemplate.exchange(url, HttpMethod.GET, null, String.class);
            //log.info(response);
            if (response.getStatusCode().is2xxSuccessful()) {
                Document document = Jsoup.parse(response.getBody());
                Elements tables = document.select("table.trades-table");
                tables.forEach(this::loadFundamentals);
            }
        } catch (RestClientException e) {
            log.error("Unable to load fundamentals", e);
        }
    }

    private void loadFundamentals(Element table) {
        Elements headers = table.select("th");
        int pe_index = -1;
        int pb_index = -1;
        for (int i = 0; i < headers.size(); i++) {
            String text = headers.get(i).text().trim();
            switch (text) {
                case "P/E":
                   pe_index = i;
                   break;
                case "P/B":
                    pb_index = i;
                    break;
            }
        }
        Elements rows = table.select("tr");
        for (int i = 0; i < rows.size(); i++) {
            Elements cells = rows.get(i).select("td");
            if (cells.isEmpty()) {
                continue;
            }
            String link = cells.get(2).selectFirst("a").attr("href");
            String symbol = link.substring(link.lastIndexOf(".") + 1).trim();
            if (symbol.isEmpty()) {
                link = cells.get(1).selectFirst("a").attr("href");
                symbol = link.substring(link.lastIndexOf("/") + 1).trim();
            }
            loadFundamental(symbol, pe_index, cells, priceToEarnings);
            loadFundamental(symbol, pb_index, cells, priceToBookValue);
            log.info("{} P/E={} P/B={}", symbol, priceToEarnings.get(symbol), priceToBookValue.get(symbol));
        }
    }

    private void loadFundamental(String symbol, int index, Elements cells, Map<String, BigDecimal> result) {
        if (index < 0 || index >= cells.size()) {
            return;
        }
        String text = cells.get(index).text().trim();
        try {
            if (!text.isEmpty()) {
                BigDecimal value  = BigDecimal.valueOf(Double.parseDouble(text));
                result.put(symbol, value);
            }
        } catch (NumberFormatException e) {
            log.error("Cannot parse fundamental for "+ symbol, e);
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
