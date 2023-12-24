package com.netgrif.etask;

import com.netgrif.etask.startup.EtaskRunnerController;
import com.netgrif.application.engine.ApplicationEngine;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate;
import com.netgrif.application.engine.startup.RunnerController;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@SpringBootApplication
@ComponentScan({"com.netgrif.application.engine", "com.netgrif.etask"})
public class EtaskApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(ApplicationEngine.class, EtaskApplication.class)
                .run(args);
    }

    @Bean
    @Primary
    public RunnerController runnerController() {
        return new EtaskRunnerController();
    }

    @Primary
    @Bean("actionDelegate")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ActionDelegate actionDelegate() {
        return new EtaskActionDelegate();
    }
}



