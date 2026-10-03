package ru.yandex.practicum.order.feign;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.dto.ReserveRequest;
import ru.yandex.practicum.order.dto.ReserveResponse;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;

@Slf4j
@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        return new InventoryClient() {

            @Override
            public ReserveResponse reserveStock(ReserveRequest request) {
                Throwable current = cause;

                while (current != null) {
                    if (current instanceof FeignException.NotFound notFound) {
                        throw notFound;
                    }

                    if (current instanceof FeignException.Conflict conflict) {
                        throw conflict;
                    }

                    current = current.getCause();
                }

                log.error("Технический сбой inventory-service при резервировании: productId={}, cause={}",
                        request.productId(), cause.toString(), cause);

                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }

            @Override
            public ReserveResponse releaseStock(ReserveRequest request) {
                Throwable current = cause;

                while (current != null) {
                    if (current instanceof FeignException feignException
                            && feignException.status() >= 400
                            && feignException.status() < 500) {
                        throw feignException;
                    }

                    current = current.getCause();
                }

                log.error("Технический сбой inventory-service при снятии резерва: productId={}, cause={}",
                        request.productId(), cause.toString(), cause);

                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }
        };
    }
}