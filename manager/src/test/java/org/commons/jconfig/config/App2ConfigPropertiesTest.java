package org.commons.jconfig.config;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import javax.management.NotificationListener;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.config.ConfigContext;
import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.config.KeyNotFound;
import org.commons.jconfig.config.ConfigContext.Entry;
import org.commons.jconfig.datatype.ByteUnit;
import org.commons.jconfig.internal.jmx.ConfigLoaderJvm;
import org.commons.jconfig.internal.jmx.VirtualMachineException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class App2ConfigPropertiesTest {

    private App2Config config;

    @Config(description = "App2 common config object example with properties")
    @ConfigResource(name = "app2.properties")
    public static final class App2ConfigProperties extends App2Config {

    }
    @MockClass(realClass = ConfigLoaderJvm.class)
    public static class MockConfigLoaderJvm {
        @Mock
        public void attach() {}
        
        @Mock
        public void addNotificationListener(final NotificationListener listener) throws VirtualMachineException {}
        
        @Mock
        public void subscribeConfigs(final String appName) throws VirtualMachineException {}
    }
    
    @BeforeClass
    public void setUp() {
        Mockit.setUpMock(new MockConfigLoaderJvm());
        ConfigContext context = new ConfigContext(new Entry("SUBSET1", "505"));
        config = ConfigManager.INSTANCE.getConfig(App2ConfigProperties.class, context);
    }
    
    @AfterClass
    public void tearDown() {
        Mockit.tearDownMocks();
    }

    @Test
    public void getCluster() {
        assertEquals(config.getLocalCluster(), "mud");
    }

    @Test
    public void getFarm() {
        assertEquals(config.getLocalFarm(), "323");
    }

    @Test
    public void getRocketstatSamplePercent() {
        assertEquals(config.getRocketstatSamplePercent().intValue(), 30);
    }

    @Test
    public void getTimeout() {
        assertEquals(config.getTimeout().getValue(), 500);
        assertEquals(config.getTimeout().getTimeUnit(), TimeUnit.SECONDS);
    }

    @Test
    public void getTimeoutProblem1() {
        assertEquals(config.getTimeoutProblem1().getValue(), 4);
        assertEquals(config.getTimeoutProblem1().getTimeUnit(), TimeUnit.DAYS);
    }

    @Test
    public void getTimeoutProblem2() {
        assertEquals(config.getTimeoutProblem2().getValue(), 16);
        assertEquals(config.getTimeoutProblem2().getTimeUnit(), TimeUnit.HOURS);
    }

    @Test
    public void getCacheSize() {
        assertEquals(config.getCacheSize().getValue(), 2);
        assertEquals(config.getCacheSize().getByteUnit(), ByteUnit.Mebibyte);
    }

    @Test
    public void getBufferSize() {
        assertEquals(config.getBufferSize().getValue(), 3);
        assertEquals(config.getBufferSize().getByteUnit(), ByteUnit.Mebibyte);
    }

    @Config(description = "App2ConfigMissingProps common config object example 2")
    @ConfigResource(name="app2MissingKeyWithoutDefault.properties")
    public static final class App2ConfigMissingProps extends App2Config {

    }

    @Test(
            expectedExceptions = KeyNotFound.class,
            expectedExceptionsMessageRegExp = ".*key BufferSize is required and is currently missing.*")
            public void getBufferSizeMissing() {
        ConfigContext context = new ConfigContext(new Entry("SUBSET1", "505"));
        ConfigManager.INSTANCE.getConfig(App2ConfigMissingProps.class, context);
    }

}
