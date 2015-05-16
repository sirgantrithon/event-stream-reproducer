package com.sirgantrithon;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.streams.ReadStream;

public class MessageBodyConsumer implements ReadStream<Buffer> {

	private MessageConsumer<Buffer> ingest;

	public MessageBodyConsumer(MessageConsumer<Buffer> ingest) {
		this.ingest = ingest;
	}

	@Override
	public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
		ingest.exceptionHandler(handler);
		return this;
	}

	@Override
	public ReadStream<Buffer> handler(Handler<Buffer> handler) {
		ingest.handler(message -> {
			Buffer body = (Buffer) message.body();
			handler.handle(body);
		});

		return this;
	}

	@Override
	public ReadStream<Buffer> pause() {
		ingest.pause();
		return this;
	}

	@Override
	public ReadStream<Buffer> resume() {
		ingest.resume();
		return this;
	}

	@Override
	public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
		ingest.endHandler(endHandler);
		return this;
	}
}
