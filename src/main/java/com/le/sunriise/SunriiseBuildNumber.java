package com.le.sunriise;

public class SunriiseBuildNumber {
    private static final String implementationVendorId = "com.le.sunriise";
    private static String defaultBuildNumber = "0.0.0";
    private static final String buildNumber = BuildNumber.findBuilderNumber(implementationVendorId, defaultBuildNumber);

    public static String getBuildnumber() {
        return buildNumber;
    }
}
