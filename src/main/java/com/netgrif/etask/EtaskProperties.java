package com.netgrif.etask;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "etask")
public class EtaskProperties {

    private Map<String, UserProperties> users;

    @Data
    @NoArgsConstructor
    public static class UserProperties {
        private String email;
        private String name;
        private String password;
        private List<String> authorities = new ArrayList<>();
    }

}
