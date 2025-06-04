package com.fladx.otpservice.config;

import org.smpp.*;
import org.smpp.pdu.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SmsGatewayConfig {

    @Value("${smpp.host}") 
    private String smppServerHost;
    
    @Value("${smpp.port}")
    private int smppServerPort;
    
    @Value("${smpp.system_id}")
    private String smppLogin;
    
    @Value("${smpp.password}")
    private String smppPassword;
    
    @Value("${smpp.system_type}")
    private String systemType;
    
    @Value("${smpp.source_addr}") 
    private String senderPhone;

    /**
     * Настраивает соединение с SMPP-сервером для отправки SMS.
     * Если что-то пойдет не так - вернет null и запишет в лог ошибку.
     */
    @Bean
    public Session smppSession() {
        log.info("Пытаемся подключиться к SMPP серверу {}:{}", smppServerHost, smppServerPort);
        
        try {
            // 1. Устанавливаем соединение
            Connection connection = new TCPIPConnection(smppServerHost, smppServerPort);
            Session session = new Session(connection);

            // 2. Настраиваем запрос на подключение
            BindRequest authRequest = createAuthRequest();
            
            // 3. Пытаемся авторизоваться
            BindResponse response = session.bind(authRequest);
            
            if (!isSuccess(response)) {
                throw new RuntimeException("Не удалось подключиться: код ошибки " + response.getCommandStatus());
            }
            
            log.info("Успешно подключились к SMPP серверу");
            return session;

        } catch (Exception e) {
            log.error("Ошибка при подключении к SMPP: {}", e.getMessage());
            return null;
        }
    }

    // Создает запрос авторизации с нашими учетными данными
    private BindRequest createAuthRequest() {
        BindRequest request = new BindTransmitter();
        request.setSystemId(smppLogin);
        request.setPassword(smppPassword);
        request.setSystemType(systemType);
        request.setInterfaceVersion((byte) 0x34);
        request.setAddressRange(senderPhone);
        return request;
    }

    // Проверяет успешность подключения
    private boolean isSuccess(BindResponse response) {
        return response.getCommandStatus() == 0;
    }
}
