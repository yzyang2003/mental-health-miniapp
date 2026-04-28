package com.example.demo.exception;

import com.example.demo.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mybatis.spring.MyBatisSystemException;

import java.sql.SQLException;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Spring MVC / Controller 中抛出的 {@link ResponseStatusException}（如未登录 401）必须保持 HTTP 状态码，
     * 不能被 {@link #handleException(Exception)} 兜底成 500。
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> handleResponseStatusException(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        String message = ex.getReason() != null ? ex.getReason() : HttpStatus.valueOf(status).getReasonPhrase();
        return ResponseEntity.status(ex.getStatusCode()).body(Result.error(status, message));
    }

    /**
     * 登录等接口查库失败时，MyBatis 常包装为 {@link MyBatisSystemException}，根因多为未配置密码、库未启动等。
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public ResponseEntity<Result<Void>> handleMyBatisSystemException(MyBatisSystemException ex) {
        SQLException sql = findSqlException(ex);
        if (sql != null) {
            String m = sql.getMessage() != null ? sql.getMessage() : "";
            if (m.contains("Access denied")) {
                log.error("MySQL access denied (check SPRING_DATASOURCE_PASSWORD / application-local.yml)", ex);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Result.error(HttpStatus.SERVICE_UNAVAILABLE.value(),
                                "数据库连接被拒绝：请确认 MySQL 已启动，并在 application-local.yml 中配置 spring.datasource.password（或环境变量 SPRING_DATASOURCE_PASSWORD）。"));
            }
            if (m.contains("Communications link failure") || m.contains("Connection refused")) {
                log.error("MySQL not reachable", ex);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Result.error(HttpStatus.SERVICE_UNAVAILABLE.value(),
                                "无法连接 MySQL：请确认服务已启动且 jdbc URL 正确。"));
            }
        }
        log.error("MyBatis error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器开小差了，请稍后重试"));
    }

    private static SQLException findSqlException(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof SQLException) {
                return (SQLException) t;
            }
            t = t.getCause();
        }
        return null;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        // 必须打完整栈，否则 IDEA 控制台看不到根因（此前仅返回固定文案给前端）
        log.error("Unhandled server exception", ex);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器开小差了，请稍后重试");
    }
}
