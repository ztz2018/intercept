package intercept;

import intercept.proxy.ProxyFactory;
import intercept.proxy.ProxyServer;
import intercept.server.InterceptServer;
import intercept.utils.Block;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

class TestContext {
    TestAsset asset;
    private InterceptServer interceptServer;
    WebDriver driver;

    public TestContext(TestAsset asset) {
        this.asset = asset;
    }

    public static TestContext using(TestAsset asset) {
        return new TestContext(asset);
    }

    public void verify(Block<TestContext> block) {
        try {
            asset.construct(this);

            block.yield(this);
        } finally {
            asset.close();
        }
    }

    public WebDriver driver() {
        return driver;
    }

    public void putIntercept(InterceptServer interceptServer) {
        this.interceptServer = interceptServer;
    }

    public ProxyServer proxy() {
        return ProxyFactory.getRunningProxy(0);
    }

    boolean proxied() {
        return interceptServer != null && proxy() != null;
    }

    public WebDriver configure(HtmlUnitDriver htmlUnitDriver) {
        if (proxied()) {
            htmlUnitDriver.setProxy("localhost", proxy().getConfig().getPort());
        }

        return htmlUnitDriver;
    }

    public void start() {
        asset.construct(this);
    }

    public void stop() {
        asset.close();
    }

    private static TestContext testContext;

    public static void set(TestContext context) {
        testContext = context;
    }

    public static TestContext get() {
        return testContext;
    }

    public static void cleanup() {
        testContext.stop();
        testContext = null;
    }
}
