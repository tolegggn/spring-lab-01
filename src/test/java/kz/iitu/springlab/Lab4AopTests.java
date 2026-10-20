package kz.iitu.springlab;

import kz.iitu.springlab.aspect.SlowMethodAspect;
import kz.iitu.springlab.service.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Lab4AopTests {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private SlowMethodAspect slowMethodAspect;

    @BeforeEach
    void setUp() {
        slowMethodAspect.clearSlowCalls();
    }

    @Test
    void testProxyStructure() {
        assertTrue(AopUtils.isAopProxy(catalogService), "catalogService must be an AOP proxy");
        assertTrue(AopUtils.isCglibProxy(catalogService), "catalogService must be a CGLIB proxy");
        assertTrue(catalogService.getClass().getName().contains("SpringCGLIB"), "Proxy class name must contain SpringCGLIB");
        assertEquals("CatalogService", catalogService.getClass().getSuperclass().getSimpleName());
    }

    @Test
    void testFindByIdSuccess() {
        String item = catalogService.findById(42);
        assertEquals("Item no. 42", item);
    }

    @Test
    void testFindAllSlowAndCollectedInVariant2() {
        List<String> items = catalogService.findAll(5);
        assertEquals(5, items.size());
        assertEquals("Item no. 1", items.get(0));
        assertEquals("Item no. 5", items.get(4));

        // Variant 2 check: findAll sleeps 300ms, threshold is 200ms -> should be collected
        List<SlowMethodAspect.SlowMethodRecord> slowCalls = slowMethodAspect.getSlowCalls();
        assertFalse(slowCalls.isEmpty(), "findAll execution should exceed 200ms threshold and be collected");
        SlowMethodAspect.SlowMethodRecord record = slowCalls.get(0);
        assertTrue(record.methodName().contains("findAll"));
        assertTrue(record.executionTimeMs() >= 200, "Recorded execution time should be >= 200ms");
    }

    @Test
    void testRemoveSuccess() {
        String res = catalogService.remove(10);
        assertEquals("Removed item no. 10", res);
    }

    @Test
    void testRemoveThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> catalogService.remove(0));
        assertEquals("Invalid identifier: 0", ex.getMessage());
    }

    @Test
    void testRemoveTwiceSelfInvocation() {
        String res = catalogService.removeTwice(5);
        assertEquals("Removed item no. 5; Removed item no. 6", res);
    }

    @Test
    void testRemoveTwiceFixedProxyInterception() {
        String res = catalogService.removeTwiceFixed(5);
        assertEquals("Removed item no. 5; Removed item no. 6", res);
    }
}
