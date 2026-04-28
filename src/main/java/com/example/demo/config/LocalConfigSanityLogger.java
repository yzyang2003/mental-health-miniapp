package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 启动时打印关键本地配置是否真正生效，避免 IDEA 运行时未加载 application-local.yml 却难以察觉。
 */
@Component
public class LocalConfigSanityLogger {

    private static final Logger log = LoggerFactory.getLogger(LocalConfigSanityLogger.class);

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        boolean rootFileExists = Files.exists(Path.of("application-local.yml"));
        boolean srcFileExists = Files.exists(Path.of("src/main/resources/application-local.yml"));
        boolean classpathFileExists = new ClassPathResource("application-local.yml").exists();
        boolean hasPassword = StringUtils.hasText(datasourcePassword);

        log.info(
                "Local config check: datasource.username='{}', datasource.password.present={}, localConfig(root/src/classpath)={}/{}/{}",
                datasourceUsername,
                hasPassword,
                rootFileExists,
                srcFileExists,
                classpathFileExists
        );

        if (!hasPassword) {
            String envPwd = System.getenv("SPRING_DATASOURCE_PASSWORD");
            if (envPwd != null && envPwd.isEmpty()) {
                log.warn("SPRING_DATASOURCE_PASSWORD is set to an empty string (e.g. in IntelliJ Run Configuration). "
                        + "It overrides application-local.yml — remove this env var entirely or set a real password.");
            } else {
                log.warn("spring.datasource.password is empty. If MySQL requires a password, login will fail. "
                        + "Set it in application-local.yml or environment variable SPRING_DATASOURCE_PASSWORD.");
            }
        }
    }
}
