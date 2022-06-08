package github.javaguide.registry;

import com.wang.registry.ServiceDiscovery;
import com.wang.registry.ServiceRegistry;
import com.wang.registry.ZkServiceDiscovery;
import com.wang.registry.ZkServiceRegistry;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author shuang.kou
 * @createTime 2020年05月31日 16:25:00
 */
class ZkServiceRegistryTest {

    @Test
    void should_register_service_successful_and_lookup_service_by_service_name() throws InterruptedException {
        ServiceRegistry zkServiceRegistry = new ZkServiceRegistry();
        InetSocketAddress givenInetSocketAddress = new InetSocketAddress("127.0.0.1", 9333);
        zkServiceRegistry.registerService("registry.ZkServiceRegistry", givenInetSocketAddress);

        ServiceDiscovery zkServiceDiscovery = new ZkServiceDiscovery();
        InetSocketAddress acquiredInetSocketAddress = zkServiceDiscovery.lookupService("registry.ZkServiceRegistry");
        assertEquals(givenInetSocketAddress.toString(), acquiredInetSocketAddress.toString());
    }
}
