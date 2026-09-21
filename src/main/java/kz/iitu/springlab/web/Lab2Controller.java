package kz.iitu.springlab.web;

import kz.iitu.springlab.notify.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {

    private final NotificationService notifications;

    public Lab2Controller(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping("/notify")
    public Map<String, Object> notify(
            @RequestParam(defaultValue = "Hello") String text) {

        return Map.of(
                "primary", notifications.viaPrimary(text),
                "console", notifications.viaConsole(text),
                "all", notifications.viaAll(text),
                "beanNames", notifications.names()
        );
    }

    @GetMapping("/custom")
    public String custom(
            @RequestParam(defaultValue = "Hello") String text) {

        return notifications.viaReversed(text);
    }
}
