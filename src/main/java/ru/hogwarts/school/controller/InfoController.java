package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    private final Logger logger = LoggerFactory.getLogger(InfoController.class);

    @Value("${server.port}")
    private Integer serverPort;

    @GetMapping("/port")
    public Integer getPort() {
        logger.info("Was invoked method for get port");
        logger.debug("Current server port: {}", serverPort);

        logger.info("Returning server port: {}", serverPort);
        return serverPort;
    }
}