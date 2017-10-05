import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import io.vertx.ext.reactivestreams.ReactiveWriteStream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import rx.Observable;
import rx.RxReactiveStreams;
import rx.schedulers.Schedulers;

import java.util.Random;
import java.util.concurrent.Executors;

public class StreamVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        vertx.createNetServer().connectHandler(netSocket -> {
            //write data to client <- read from rxJava Observable
            readStream(netSocket, RxReactiveStreams.toPublisher(getTcpResponseFromUnlimitedObservable()));


            //read data from tcp client -> send to rxJava Subscriber
            writeStream(netSocket, RxReactiveStreams.toSubscriber(getTcpRequestSubsciber()));

        }).listen(3000, server -> {
            System.out.println("TCP server started");
        });
    }

    private rx.Subscriber <Buffer> getTcpRequestSubsciber() {
        return new rx.Subscriber <Buffer>() {
            @Override
            public void onCompleted() {
                System.out.println("Completed");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Error: " + e.getMessage());
            }

            @Override
            public void onNext(Buffer buffer) {
                System.out.println("Client sent: " + buffer.toString());
            }
        };
    }

    private void readStream(WriteStream <Buffer> writeStream, Publisher <Buffer> publisher) {
        ReactiveReadStream <Buffer> rrs = ReactiveReadStream.readStream();
        publisher.subscribe(rrs);

        Pump pump = Pump.pump(rrs, writeStream);
        pump.start();
    }

    private void writeStream(ReadStream <Buffer> readStream, Subscriber <? super Buffer> otherSubscriber) {
        ReactiveWriteStream <Buffer> rws = ReactiveWriteStream.writeStream(vertx);
        rws.subscribe(otherSubscriber);


        Pump pump = Pump.pump(readStream, rws);
        pump.start();
    }

    private Observable <Buffer> getTcpResponseObservable() {
        return Observable.range(1, 100)
                .map(integer -> String.valueOf(integer) + "n")
                .concatWith(Observable.just("Give me some data. To end send ENDNOW"))
                .map(Buffer::buffer);
    }

    private Observable <Buffer> getTcpResponseFromUnlimitedObservable() {
        return Observable.create(subscriber -> {
            try {
                while (true) {
                    subscriber.onNext(new Random().nextInt(1000000));
                }
            } catch (Throwable t) {
                subscriber.onError(t);
            }
        }).map(integer -> String.valueOf(integer) + "This is " + Thread.currentThread() + " speaking\n")
                .map(Buffer::buffer)
                .subscribeOn(Schedulers.io());
    }


}
