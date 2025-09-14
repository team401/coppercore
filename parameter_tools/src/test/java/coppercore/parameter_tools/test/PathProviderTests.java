package coppercore.parameter_tools.test;

import coppercore.parameter_tools.path_provider.EnvironmentHandler;
import coppercore.parameter_tools.path_provider.EnvironmentPathProvider;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author avidraccoon
 */
public class PathProviderTests {

    private static UnitTestingPathProvider pathProvider;
    private EnvironmentHandler environmentHandler;
    private EnvironmentPathProvider testPathProvider;

    @BeforeAll
    public static void TestPrep() {
        pathProvider = new UnitTestingPathProvider().getDirectory("PathProviderTests");
    }

    @BeforeEach
    public void CleanTest() {
        environmentHandler =
                EnvironmentHandler.getEnvironmentHandler(pathProvider.resolvePath("config.json"));
        testPathProvider = environmentHandler.getEnvironmentPathProvider();
    }

    @Test
    public void getReadPathNormalBehaviorTest() {
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "normal"
                        + File.separator
                        + "NormalFile.json",
                testPathProvider.resolveReadPath("NormalFile.json"));
    }

    @Test
    public void getReadPathDefualtingBehaviorTest() {
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "defaults"
                        + File.separator
                        + "DefaultFile.json",
                testPathProvider.resolveReadPath("DefaultFile.json"));
    }

    @Test
    public void getReadPathMissingBehaviorTest() {
        try {
            testPathProvider.resolveReadPath("RandomNoneExistentFile.json");
            Assertions.fail("Should throw error about file missing");
        } catch (Exception e) {
            Assertions.assertEquals(
                    "RandomNoneExistentFile.json does not exist at "
                            + pathProvider.getFullPath()
                            + File.separator
                            + "defaults"
                            + File.separator
                            + "RandomNoneExistentFile.json",
                    e.getMessage());
        }
    }

    @Test
    public void getWritePathNormalBehaviorTest() {
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "normal"
                        + File.separator
                        + "NormalFile.json",
                testPathProvider.resolveWritePath("NormalFile.json"));
    }

    @Test
    public void getWritePathDefualtingBehaviorTest() {
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "defaults"
                        + File.separator
                        + "DefaultFile.json",
                testPathProvider.resolveWritePath("DefaultFile.json"));
    }

    @Test
    public void getWritePathDefualtingMissingTest() {
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "normal"
                        + File.separator
                        + "RandomNoneExistentFile.json",
                testPathProvider.resolveWritePath("RandomNoneExistentFile.json"));
    }

    @Test
    public void customPathTest() {
        environmentHandler = environmentHandler.setEnvironment("custom_path");
        testPathProvider = environmentHandler.getEnvironmentPathProvider();
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "custom"
                        + File.separator
                        + "CustomFile.json",
                testPathProvider.resolveWritePath("CustomFile.json"));
    }

    @Test
    public void isolatedPathTest() {
        environmentHandler = environmentHandler.setEnvironment("isolated");
        testPathProvider = environmentHandler.getEnvironmentPathProvider();
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "isolated"
                        + File.separator
                        + "RandomNoneExistentFile.json",
                testPathProvider.resolveWritePath("RandomNoneExistentFile.json"));
    }
}
