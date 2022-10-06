package com;

import software.amazon.awssdk.regions.Region;

public class Constants {

    public static final String ACCESS_KEY = "********************";

    public static final String SECRET_KEY = "****************************************";

    public static final Region REGION = Region.US_EAST_1;

    public static final String INPUT_BUCKET_NAME = "proj-input-bucket";

    public static final String BASE_URL_S3 = "https://" + INPUT_BUCKET_NAME + ".s3.amazonaws.com";

    public static final String INPUTQUEUENAME = "RequestQueue";

    public static final String OUTPUTQUEUENAME = "ResponseQueue";
}
