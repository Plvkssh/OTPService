package com.fladx.otpservice.service.notification;

import com.fladx.otpservice.model.user.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Отправляет OTP-коды через Telegram бота.
 * Также обрабатывает команды бота (/start).
 */
@Slf4j
@Service
public class TelegramBotService implements NotificationSender, SpringLongPollingBot, 
                                         LongPollingSingleThreadUpdateConsumer {

    @Value("${telegram.bot.token}")
    private String botToken;
    
    private TelegramClient telegramClient;

    @PostConstruct
    public void initializeBot() {
        telegramClient = new OkHttpTelegramClient(getBotToken());
        log.info("Telegram бот инициализирован");
    }

    /**
     * Отправляет OTP-код пользователю в Telegram
     * @param otpCode Код подтверждения
     * @param user Получатель (должен иметь telegramId)
     */
    @Override
    @Async
    public void send(String otpCode, User user) {
        Long chatId = user.getTelegramId();
        if (chatId == null) {
            log.warn("Не удалось отправить OTP: пользователь {} не привязал Telegram", 
                    user.getUsername());
            return;
        }

        SendMessage message = buildTelegramMessage(chatId, 
                "Ваш код подтверждения: " + otpCode);
        
        try {
            telegramClient.execute(message);
            log.debug("OTP-код отправлен в Telegram, chatId: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки в Telegram (chatId: {}): {}", chatId, e.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    /**
     * Обрабатывает входящие сообщения в боте
     */
    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();

        if ("/start".equals(text)) {
            handleStartCommand(chatId);
        }
    }

    /**
     * Обрабатывает команду /start
     */
    private void handleStartCommand(Long chatId) {
        String responseText = String.format(
                "Вы успешно зарегистрированы!\nВаш ID для уведомлений: %d",
                chatId);
        
        sendTelegramMessage(chatId, responseText);
    }

    /**
     * Отправляет сообщение через Telegram API
     */
    private void sendTelegramMessage(Long chatId, String text) {
        try {
            telegramClient.execute(buildTelegramMessage(chatId, text));
            log.debug("Сообщение отправлено в Telegram, chatId: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения (chatId: {}): {}", chatId, e.getMessage());
        }
    }

    /**
     * Создает объект сообщения Telegram
     */
    private SendMessage buildTelegramMessage(Long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }
}
