package coppercore.paremeter_tools.test;

import coppercore.parameter_tools.JSONSync;


public class ExampleJsonSyncClass {
    
    public static JSONSync<ExampleJsonSyncClass> synced = new JSONSync<ExampleJsonSyncClass>(
        new ExampleJsonSyncClass(),
        "D:/coppercore/coppercore/paremeter_tools/src/test/resources/ExampleJsonSyncClass.json",
        new JSONSync.JSONSyncConfigBuilder().build()
    );

    public final String testText = "";
    public final Double testDouble = 0.0;
    public final Integer testInt = 0;
    public final BasicMotorDataHolder motorData = null;

    public class BasicMotorDataHolder {
        public final Double maxVoltage = 0.0;
        public final Double minVoltage = 0.0;
        public Double currentVoltage = 0.0;

        public String toString(){
            return "minVoltage: " + minVoltage + "\nmaxVoltage: "+maxVoltage+"\ncurrentVoltage: "+currentVoltage;
        }
    }

    public String toString(){
        return "testText: " + testText + "\ntestInt: "+testInt+"\ntestDouble: "+
        testDouble+"\nmotorData: "+motorData;
    }
}