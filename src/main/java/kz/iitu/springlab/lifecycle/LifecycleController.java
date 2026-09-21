package kz.iitu.springlab.lifecycle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LifecycleController {

    private final LifecycleDemo lifecycleDemo;

    public LifecycleController(LifecycleDemo lifecycleDemo) {
        this.lifecycleDemo = lifecycleDemo;
    }

    @GetMapping("/lifecycle")
    public Object lifecycle() {
        return lifecycleDemo.events();
    }
}