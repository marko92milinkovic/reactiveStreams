import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.net.NetServer;
import rx.Observable;
import rx.Single;
import rx.observables.ConnectableObservable;

/**
 * Created by markom on 10/5/17.
 */
public class RxStreamVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Single<NetServer> netServerSingle =
                vertx.createNetServer().connectHandler(socket -> {

                    //receive data
                    Observable<Buffer> bufferObservable = socket.toObservable();
                    bufferObservable.subscribe(next -> {
                        System.out.println("Client in RxVertx: " + next.toString());
                    }, error -> {
                    }, () -> {
                    });


                    //send to client
                    getTcpResponseFromUnlimitedObservable()
                            .map(buffer -> "This goes out: " + buffer.toString())
                            .subscribe(next -> socket.write(next));
                }).rxListen(2500);

        netServerSingle.subscribe(server -> System.out.println("Rx Server started"));

    }

    private Observable<Buffer> getTcpResponseFromUnlimitedObservable() {
        return ObservableFactory.getUnlimitedIntegerObservable()
                .map(integer -> String.valueOf(integer) + "  --> This is Observable from RxVertx speaking\n")
                .map(Buffer::buffer);
    }
}
