package com.bulletbalance.webinf;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Charsets;
import lombok.extern.log4j.Log4j2;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@Log4j2
public class BulletbalanceWebConfig implements WebMvcConfigurer {

    private List<HttpMessageConverter<?>> convertersList = customConverterList();

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addAll(convertersList);
        log.info("Data converters initialized");
    }

    public List<HttpMessageConverter<?>> customConverterList() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(createObjectMapper());
        List<MediaType> mediaTypes = new ArrayList<>(jsonConverter.getSupportedMediaTypes());
        mediaTypes.add(new MediaType("application", "javascript", Charsets.UTF_8));
        jsonConverter.setSupportedMediaTypes(mediaTypes);
        return Arrays.asList(
                new StringHttpMessageConverter(),
                jsonConverter
        );
    }

    @Bean
    @Qualifier("httpsRestTemplate")
    public RestTemplate getHttpsRestTemplate() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setMessageConverters(convertersList);
        return restTemplate;
    }

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .enable(
                        MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,
                        MapperFeature.USE_ANNOTATIONS
                ).disable(
                        MapperFeature.AUTO_DETECT_CREATORS,
                        MapperFeature.AUTO_DETECT_FIELDS,
                        MapperFeature.AUTO_DETECT_GETTERS,
                        MapperFeature.AUTO_DETECT_IS_GETTERS,
                        MapperFeature.AUTO_DETECT_SETTERS
                ).enable(
                        SerializationFeature.INDENT_OUTPUT
                ).disable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                ).disable(
                        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                ).registerModule(
                        new JavaTimeModule()
                ).setDefaultVisibility(
                        JsonAutoDetect.Value.construct(
                                JsonAutoDetect.Visibility.NONE,
                                JsonAutoDetect.Visibility.NONE,
                                JsonAutoDetect.Visibility.NONE,
                                JsonAutoDetect.Visibility.NONE,
                                JsonAutoDetect.Visibility.NONE
                        )
                ).setDefaultPropertyInclusion(
                        JsonInclude.Include.NON_NULL
                );
    }

//    @Bean
//    public RequestLoggingFilter getLoggingFilter() {
//        return new RequestLoggingFilter();
//    }
}
