package com.fladx.otpservice.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.smpp.Connection;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.SubmitSM;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SmppEmulatorConfig {

    @Value("${smpp.host}")
    private String host;

    @Value("${smpp.port}")
    private int port;

    @Value("${smpp.system_id}")
    private String systemId;

    @Value("${smpp.password}")
    private String password;

    @Value("${smpp.system_type}")
    private String systemType;

    @Value("${smpp.source_addr}")
    private String sourceAddr;

    @Bean
    public Session smppSession() {
        try {
            Connection connection = new TCPIPConnection(host, port);
            Session session = new Session(connection);
            BindRequest bindRequest = new BindTransmitter();
            bindRequest.setSystemId(systemId);
            bindRequest.setPassword(password);
            bindRequest.setSystemType(systemType);
            bindRequest.setInterfaceVersion((byte) 0x34);
            bindRequest.setAddressRange(sourceAddr);

            BindResponse bindResponse = session.bind(bindRequest);
            if (bindResponse.getCommandStatus() != 0) {
                throw new Exception("Bind failed: " + bindResponse.getCommandStatus());
            }

            return session;
        }

        catch (Exception exception){
            log.error("Cant create smppSession", exception);
        }

        return null;
    }


}
