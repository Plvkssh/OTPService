package com.fladx.otpservice.exception;

import com.fladx.otpservice.dto.error.ErrorField;
import com.fladx.otpservice.dto.error.ErrorResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ловит все ошибки в приложении и возвращает аккуратные ответы.
 * Как хороший врач - ставит диагноз (HTTP-статус) и объясняет проблему.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Когда искомый объект не найден в базе (404)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(EntityNotFoundException ex, WebRequest request) {
        return buildError(ex, request);
    }

    /**
     * Когда OTP-код уже недействителен (410)
     * - Просрочен
     * - Уже использован
     */
    @ExceptionHandler({OtpCodeNotActiveException.class, OtpCodeExpiredException.class})
    @ResponseStatus(HttpStatus.GONE)
    public ErrorResponse handleInvalidOtpCode(Exception ex, WebRequest request) {
        return buildError(ex, request);
    }

    /**
     * Когда пытаемся создать дубликат (400)
     * - Пользователь с таким email уже есть
     */
    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAlreadyExists(EntityExistsException ex, WebRequest request) {
        return buildError(ex, request);
    }

    /**
     * Когда данные в запросе не прошли проверку (400)
     * - Неверный формат email
     * - Слишком короткий пароль
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidData(MethodArgumentNotValidException ex, WebRequest request) {
        List<ErrorField> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorField(
                    error.getField(), 
                    error.getDefaultMessage())
                )
                .toList();

        return ErrorResponse.builder()
                .message("Некорректные данные в запросе")
                .path(getRequestPath(request))
                .details(errors)
                .build();
    }

    /**
     * Все остальные непредвиденные ошибки (500)
     * - Сюда попадает всё, что мы не предусмотрели
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedErrors(Exception ex, WebRequest request) {
        return buildError(ex, request);
    }

    // Собирает стандартный ответ об ошибке
    private ErrorResponse buildError(Exception ex, WebRequest request) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .path(getRequestPath(request))
                .build();
    }

    // Извлекает путь запроса из WebRequest
    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
