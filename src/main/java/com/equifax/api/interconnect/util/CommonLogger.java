package com.equifax.api.interconnect.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonLogger {
    private final Logger logger;
    private final Class<?> clazz;

    private CommonLogger(Class<?> clazz) {
        this.clazz = clazz;
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static CommonLogger getLogger(Class<?> clazz) {
        return new CommonLogger(clazz);
    }

    public void info(String message) {
        logger.info("[{}] {}", clazz.getSimpleName(), message);
    }

    public void info(String message, Object... args) {
        logger.info("[{}] {}", clazz.getSimpleName(), String.format(message, args));
    }

    public void error(String message) {
        logger.error("[{}] {}", clazz.getSimpleName(), message);
    }

    public void error(String message, Throwable throwable) {
        logger.error("[{}] {}", clazz.getSimpleName(), message, throwable);
    }

    public void error(String message, Object... args) {
        logger.error("[{}] {}", clazz.getSimpleName(), String.format(message, args));
    }

    public void debug(String message) {
        logger.debug("[{}] {}", clazz.getSimpleName(), message);
    }

    public void debug(String message, Object... args) {
        logger.debug("[{}] {}", clazz.getSimpleName(), String.format(message, args));
    }

    public void warn(String message) {
        logger.warn("[{}] {}", clazz.getSimpleName(), message);
    }

    public void warn(String message, Object... args) {
        logger.warn("[{}] {}", clazz.getSimpleName(), String.format(message, args));
    }
}
