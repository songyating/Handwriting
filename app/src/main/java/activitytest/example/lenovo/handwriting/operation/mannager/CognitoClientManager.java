/**
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * <p>
 * http://aws.amazon.com/apache2.0
 * <p>
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package activitytest.example.lenovo.handwriting.operation.mannager;

import android.content.Context;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

public class CognitoClientManager {

    private static final String TAG = "CognitoClientManager";
    /**
     * Enter here the Identity Pool associated with your app and the AWS
     * region where it belongs. Get this information from the AWS console.
     */
    private static final String IDENTITY_POOL_ID = "us-east-1:7e9b26a1-758d-4897-bd12-c36c69511129";
    private static final Regions REGION = Regions.US_EAST_1;
    /*
        * You should replace these values with your own. See the README for details
        * on what to fill in.
        */
    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "us-east-1";
    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "timer4meplus-userdata";
    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "us-east-1";
    private static AmazonS3Client sS3Client;
    private static TransferUtility sTransferUtility;



    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @param context An Context instance.
     * @return A default S3 client.
     */
    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client();
            sS3Client.setRegion(Region.getRegion(Regions.fromName(BUCKET_REGION)));
        }
        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context
     * @return a TransferUtility instance
     */
    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                    context.getApplicationContext());
        }

        return sTransferUtility;
    }
}

