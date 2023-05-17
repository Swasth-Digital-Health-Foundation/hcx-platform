package org.swasth.apigateway.decorator;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

public class RequestDecorator extends ServerHttpRequestDecorator {

    private final byte[] newReq;
    public RequestDecorator(ServerHttpRequest delegate,byte[] updatedRequest) {
        super(delegate);
        this.newReq = updatedRequest;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DefaultDataBuffer buffer = factory.wrap(newReq);
        return Flux.just(buffer);
    }
}

