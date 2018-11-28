package com.bulletbalance.webinf;

import com.bulletbalance.controller.RequestLoggingFilter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Log4j2
public class BulletbalanceWebConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter(createObjectMapper()));
        log.info("Data converters initialized");
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
