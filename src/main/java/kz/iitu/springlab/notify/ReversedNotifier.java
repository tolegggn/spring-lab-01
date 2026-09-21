package kz.iitu.springlab.notify;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("reversed")
@Order(5)
public class ReversedNotifier implements Notifier {

    @Override
    public String send(String message) {
        return "reversed: " +
                new StringBuilder(message).reverse();
    }

    @Override
    public String channel() {
        return "reversed";
    }
}