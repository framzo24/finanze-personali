package it.francescozoni.finanzepersonali;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@SpringBootApplication
@Slf4j
public class FinanzePersonaliApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FinanzePersonaliApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = env.getProperty("server.ssl.key-store") != null ? "https" : "http";
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (!StringUtils.hasText(contextPath)) {
            contextPath = "/";
        }
        String swaggerPath = env.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");

        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Impossibile determinare l'indirizzo dell'host, uso 'localhost'");
        }

        String[] profiles = env.getActiveProfiles().length == 0
                ? env.getDefaultProfiles()
                : env.getActiveProfiles();

        log.info("""

                ----------------------------------------------------------
                Application '{}' is running! Access URLs:
                Local:      {}://localhost:{}{}
                External:   {}://{}:{}{}
                Swagger:    {}://localhost:{}{}
                Profile(s): {}
                ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                protocol, port, contextPath,
                protocol, hostAddress, port, contextPath,
                protocol, port, swaggerPath,
                Arrays.toString(profiles));
    }
}