package eu.stamp_project.automaticbuilder.gradle;

import eu.stamp_project.Utils;
import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.utils.pit.AbstractPitResult;
import eu.stamp_project.utils.pit.PitXMLResultParser;
import eu.stamp_project.utils.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 18/07/17.
 */
public class GradleAutomaticBuilderTest {

    private AutomaticBuilder sut = null;
    PitXMLResultParser parser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Utils.reset();
    }

    private final String pathToConfFile = "src/test/resources/test-projects/test-projects.properties";

    @Before
    public void setUp() throws Exception {
        cleanTestEnv();
        Utils.init(pathToConfFile);
        Utils.LOGGER.debug("Test Set-up - Reading input parameters...");
        InputConfiguration inputConfiguration = Utils.getInputConfiguration();
        inputConfiguration.setBuilderName("GradleBuilder");
        Utils.LOGGER.debug("Test Set-up - instantiating Automatic Builder (SUT)...");
        sut = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration.getBuilderName());
        sut.compile();
        Utils.LOGGER.debug("Test Set-up complete.");
        parser = new PitXMLResultParser();
    }

    @After
    public void tearDown() throws Exception {
        Utils.LOGGER.debug("Test Tear-down...");
        cleanTestEnv();
        Utils.LOGGER.debug("Test Tear-down complete.");
    }

    @Test
    public void compile_whenCleanCompileTestTasksAreAppliedToDSpotTestExampleProject() throws Exception {
        Utils.LOGGER.info("Starting Gradle Automatic Builder compile() test...");
        sut.compile();
        File buildDir = new File("src/test/resources/test-projects/build");
        assertTrue("After compilation, build dir should exist", buildDir.exists());
        Utils.LOGGER.info("Gradle Automatic Builder compile() test complete.");
    }

    @Test
    public void buildClasspath_whenAppliedToDSpotTestExampleProject() throws Exception {
        Utils.LOGGER.info("Starting Gradle Automatic Builder buildClasspath() test...");
        String[] expectedCompilationLibraries = {"log4j-api-2.8.2.jar", "log4j-core-2.8.2.jar"};
        String[] expectedTestCompilationLibraries = {"hamcrest-core-1.3.jar", "junit-4.12.jar"};
        String classPath = sut.buildClasspath();
        assertNotNull(classPath, "Classpath should be null");
        assertTrue("Classpath should contain " + expectedCompilationLibraries[0] + " library as compile/runtime dependency", classPath.contains(expectedCompilationLibraries[0])); // compile dependency
        assertTrue("Classpath should contain " + expectedCompilationLibraries[1] + " library as compile/runtime dependency", classPath.contains(expectedCompilationLibraries[1])); // compile dependency
        assertTrue("Classpath should contain " + expectedTestCompilationLibraries[0] + " library as test dependency", classPath.contains(expectedTestCompilationLibraries[0])); // test compile dependency
        assertTrue("Classpath should contain " + expectedTestCompilationLibraries[1] + " library as test dependency", classPath.contains(expectedTestCompilationLibraries[1])); // test compile dependency
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File("src/test/resources/test-projects/" + GradleInjector.GRADLE_BUILD_FILE)))) {
            assertFalse( reader.lines().collect(Collectors.joining("\n")).contains(GradleInjector.WRITE_CLASSPATH_TASK));
        }
        Utils.LOGGER.info("Gradle Automatic Builder buildClasspath() test complete.");
    }

    @Test
    public void runPit_whenNoTestClassIsSpecified() throws Exception {
        Utils.LOGGER.info("Starting Gradle Automatic Builder runPit() test when no test class is specified...");
        InputConfiguration.get().setDescartesMode(false);
        sut.runPit();
        List<? extends AbstractPitResult> pitResults = parser.parseAndDelete("src/test/resources/test-projects/" + sut.getOutputDirectoryPit());
        assertTrue("PIT results shouldn't be null", pitResults != null);
        assertTrue("PIT results shouldn't be empty", !pitResults.isEmpty());
        Utils.LOGGER.info("Gradle Automatic Builder runPit() test complete when no test class is specified.");
    }

    @Test
    public void runPit_whenTestClassIsSpecified() throws Exception {
        Utils.LOGGER.info("Starting Gradle Automatic Builder runPit() test when a test class is specified...");

        InputConfiguration.get().setDescartesMode(false);

        CtClass<Object> testClass = Utils.getInputConfiguration().getFactory().Class().get("example.TestSuiteExample");

        sut.runPit(testClass);
        List<? extends AbstractPitResult> pitResults = parser.parseAndDelete("src/test/resources/test-projects/"+ sut.getOutputDirectoryPit());

        assertTrue("PIT results shouldn't be null", pitResults != null);
        assertTrue("PIT results shouldn't be empty", !pitResults.isEmpty());

        Utils.LOGGER.info("Gradle Automatic Builder runPit() test complete when a test class is specified.");
    }

    private void cleanTestEnv() throws IOException {
        File classPathFile = new File("src/test/resources/test-projects/gjp_cp");
        if (classPathFile.exists()) {
            Utils.LOGGER.debug("Cleaning Test Env - Deleting java classpath file...");
            classPathFile.delete();
        }

        File buildDir = new File("src/test/resources/test-projects/build");
        if (buildDir.exists()) {
            Utils.LOGGER.debug("Cleaning Test Env - Deleting Gradle Java project build dir...");
            FileUtils.deleteDirectory(buildDir);
        }

        File gradlewDir = new File("src/test/resources/test-projects/gradle");
        if (gradlewDir.exists()) {
            Utils.LOGGER.debug("Cleaning Test Env - Deleting Gradle Java project gradle dir...");
            FileUtils.deleteDirectory(gradlewDir);
        }

        File gradleHiddenDir = new File("src/test/resources/test-projects/.gradle");
        if (gradleHiddenDir.exists()) {
            Utils.LOGGER.debug("Cleaning Test Env - Deleting Gradle Java project gradle hidden dir...");
            FileUtils.deleteDirectory(gradleHiddenDir);
        }

        Utils.LOGGER.debug("Test Env cleaning complete.");
    }

}
