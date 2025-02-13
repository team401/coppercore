/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5Suite.java to edit this template
 */

package coppercore.parameter_tools.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import coppercore.parameter_tools.json.JSONHandler;
import coppercore.parameter_tools.path_provider.EnvironmentHandler;


/**
 *
 * @author avidraccoon
 */
public class PathProviderTests {
   
    private UnitTestingPathProvider pathProvider;
    private JSONHandler jsonHandler;
    private EnvironmentHandler environmentHandler;
    private PathProviderTestData blank;

    @BeforeAll
    public void TestPrep(){
        pathProvider = new UnitTestingPathProvider().getDirectory("PathProviderTests");
        blank = new PathProviderTestData();
    }

    @BeforeEach
    public void CleanTest(){
        environmentHandler = EnvironmentHandler.getEnvironmentHandler(pathProvider.resolvePath("config.json"));
        settupJsonHandler();
    }

    public void settupJsonHandler(){
        jsonHandler = new JSONHandler(environmentHandler.getEnvironmentPathProvider());
    }

    @Test
    public void getFileNormalBehavior(){
        PathProviderTestData data = jsonHandler.getObject(blank, "NormalFile.json");
        Assertions.assertEquals("Test Value 1", data.getTestValue());
    }

}