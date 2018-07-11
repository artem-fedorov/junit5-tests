import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.PackageNameFilter.excludePackageNames;
import static org.junit.platform.engine.discovery.PackageNameFilter.includePackageNames;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;

public class TestRunner {

    public static void main(String[] args) throws Throwable {
        URL jar = TestRunner.class.getResource("junit-test-1.1.jar");
        String file = jar.getFile();

        // test that this package does not exists
        Package pack = Package.getPackage("testcases.subpackage");
        assert pack == null;

        URL testJar = new URL("jar:file:" + file + "!/");
//        addURL(jar); // works OK, because URL start with "file:/path/junit-test-1.1.jar"
        addURL(testJar); // does not work. URL="jar:file:/path/junit-tests-1.1.jar!/"

        // test that this package exists
        pack = Package.getPackage("testcases.subpackage");
        assert pack != null; // if ok it's mean, that this URL is valid

        // compare that these 2 paths does not equal
        assert !jar.toString().equals(testJar.toString());
        System.out.println(jar);
        System.out.println(testJar);

        // create request and execute
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request().selectors(
                selectPackage("testcases.subpackage")
        ).build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        // assert that 4 tests were executed
        assert 4 == listener.getSummary().getTestsStartedCount();
    }

    // add URL to classpath
    private static void addURL(URL url) throws ReflectiveOperationException {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(systemClassLoader, url);
    }
}
