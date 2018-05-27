package utils;

public class SimpleVersionIncrement implements IVersionIncrement {
    private final int step;
    private final String firstVersion;

    public  SimpleVersionIncrement(){
        this(1);
    }

    public SimpleVersionIncrement(int step){
        this.step = step;
        firstVersion = "1.0";
    }
    @Override
    public String increment(String current) {
        String[] newVersion = current.split("\\.");
        int newSuffix = incrementByOne(newVersion[1]);
        if (newSuffix >= 10){
            newVersion[0] = Integer.toString(incrementByOne(newVersion[0]));
            newSuffix = 0;
        }
        return newVersion[0] + "." + newSuffix;
    }

    @Override
    public String getFirst() {
        return firstVersion;
    }

    private int incrementByOne(String version){
        return Integer.parseInt(version) + step;
    }

    public String getFirstVersion() {
        return firstVersion;
    }
}
