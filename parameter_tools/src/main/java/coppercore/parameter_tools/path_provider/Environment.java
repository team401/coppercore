package coppercore.parameter_tools.path_provider;

public class Environment {
    String name;
    String filepath;
    String[] files;

    public String getName() {
        return name;
    }

    public String getPath() {
        return filepath;
    }

    public boolean hasFile(String file) {
        for (int i = 0; i < files.length; i++) {
            if (files[i].compareTo(file) == 0) {
                return true;
            }
        }
        return false;
    }
}
