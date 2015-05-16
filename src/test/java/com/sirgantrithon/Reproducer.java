package com.sirgantrithon;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.streams.Pump;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class Reproducer {

	static Vertx vertx;

	@BeforeClass
	public static void setUp(TestContext context) {
		Async async = context.async();
		Vertx.clusteredVertx(new VertxOptions().setClustered(true), context.asyncAssertSuccess(result -> {
			vertx = result;
			async.complete();
		}));
	}

	@Test
	public void testStreaming(TestContext context) {
		Async async = context.async();

		// First, set up the pump from event bus to file
		Future<Void> pumpReady = Future.future();
		OpenOptions options = new OpenOptions();
		String path = UUID.randomUUID().toString();
		vertx.fileSystem().open(path, options, context.asyncAssertSuccess(file -> {
			MessageConsumer<Buffer> consumer = vertx.eventBus().consumer("address", message -> {
				Buffer body = (Buffer) message.body();
				file.write(body);
			});

			MessageBodyConsumer bodyConsumer = new MessageBodyConsumer(consumer);
			Pump.pump(bodyConsumer, file).start();
			pumpReady.complete();
		}));

		// Next, open file and begin streaming over event bus
		Future<Void> fileWritten = Future.future();
		pumpReady.setHandler(context.asyncAssertSuccess(result -> {
			vertx.fileSystem().open("src/test/resources/file.xml", options, context.asyncAssertSuccess(file -> {
				MessageProducer<Buffer> sender = vertx.eventBus().sender("address");
				file.endHandler(u -> {
					// Wait a little bit after finishing the file read, to give the file writing a chance to catch up
					vertx.setTimer(1000, unused -> {
						fileWritten.complete();
					});
				});
				Pump.pump(file, sender).start();
			}));
		}));

		// Finally, compare the contents of the files
		fileWritten.setHandler(context.asyncAssertSuccess(result -> {
			vertx.fileSystem().readFile("src/test/resources/file.xml", context.asyncAssertSuccess(f1 -> {
				vertx.fileSystem().readFile(path, context.asyncAssertSuccess(f2 -> {
					context.assertTrue(f1.toString().equals(f2.toString()));
					async.complete();
				}));
			}));
		}));
	}
}
