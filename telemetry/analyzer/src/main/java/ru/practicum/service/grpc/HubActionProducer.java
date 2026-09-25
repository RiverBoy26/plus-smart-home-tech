package ru.practicum.service.grpc;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.grpc.telemetry.messages.hub.DeviceActionRequestProto;

@Slf4j
@Component
public class HubActionProducer {
    @GrpcClient("hub-router")
    private HubRouterControllerBlockingStub hubRouterStub;

    public void sendAction(DeviceActionRequestProto actionRequest) {
        log.info("Sending actionRequest: {}", actionRequest.getAllFields());
        hubRouterStub.handleDeviceAction(actionRequest);
    }
}