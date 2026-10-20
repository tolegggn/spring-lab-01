package kz.iitu.springlab.web;

import kz.iitu.springlab.aspect.SlowMethodAspect;
import kz.iitu.springlab.service.CatalogService;
import org.springframework.aop.support.AopUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab4")
public class CatalogController {

    private final CatalogService catalogService;
    private final SlowMethodAspect slowMethodAspect;

    public CatalogController(CatalogService catalogService, SlowMethodAspect slowMethodAspect) {
        this.catalogService = catalogService;
        this.slowMethodAspect = slowMethodAspect;
    }

    @GetMapping("/item/{id}")
    public String getItem(@PathVariable long id) {
        return catalogService.findById(id);
    }

    @GetMapping("/items")
    public List<String> getItems(@RequestParam(defaultValue = "5") int limit) {
        return catalogService.findAll(limit);
    }

    @DeleteMapping("/item/{id}")
    public String deleteItem(@PathVariable long id) {
        return catalogService.remove(id);
    }

    @GetMapping("/proxy")
    public Map<String, String> proxyInfo() {
        return Map.of(
                "className", catalogService.getClass().getName(),
                "superClass", catalogService.getClass().getSuperclass().getSimpleName(),
                "isAopProxy", String.valueOf(AopUtils.isAopProxy(catalogService)),
                "isCglib", String.valueOf(AopUtils.isCglibProxy(catalogService))
        );
    }

    @GetMapping("/remove-twice/{id}")
    public String removeTwice(@PathVariable long id) {
        return catalogService.removeTwice(id);
    }

    @GetMapping("/remove-twice-fixed/{id}")
    public String removeTwiceFixed(@PathVariable long id) {
        return catalogService.removeTwiceFixed(id);
    }

    @GetMapping("/slow-methods")
    public List<SlowMethodAspect.SlowMethodRecord> getSlowMethods() {
        return slowMethodAspect.getSlowCalls();
    }

    @DeleteMapping("/slow-methods")
    public Map<String, String> clearSlowMethods() {
        slowMethodAspect.clearSlowCalls();
        return Map.of("status", "cleared");
    }
}
