import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Random;

/**
 * Created by markom on 10/5/17.
 */
public class ObservableFactory {


    public static Observable<Integer> getUnlimitedIntegerObservable() {
        return observable;
    }

    private static Observable<Integer> observable;

    static {
        observable = createUnlimitedIntegerObservable();
    }


    private static Observable<Integer> createUnlimitedIntegerObservable() {
        return Observable.<Integer>create(subscriber -> {
            try {
                while (true) {
                    subscriber.onNext(new Random().nextInt(1000000));
                    Thread.currentThread().sleep(1500);
                }
            } catch (Throwable t) {
                subscriber.onError(t);
            }
        }).subscribeOn(Schedulers.io()).share();

    }


}
