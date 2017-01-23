package com.huya.v.util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AWS-S3工具类
 *
 * @example http://docs.ceph.com/docs/jewel/radosgw/s3/java/
 * 			http://docs.aws.amazon.com/zh_cn/AmazonS3/latest/dev/HLuploadFileJava.html
 * @author Jaward
 */
public class AwsS3Utils {

	private static final Logger LOG = LoggerFactory.getLogger(AwsS3Utils.class);

	private static final String ACCESS_KEY = "9TGVU7D9W70ESJZ54OVX";
    private static final String SECRET_KEY = "UxyOPqwGAIQxVLDCLhaQf0PrFHqM7iowvffuTDc7";
    private static final String END_POINT = "http://183.2.198.128:80";
    private static final String BUCKET_NAME = "v-huya-bucket";
    private static AmazonS3 s3Client = null;

    static {
        AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        s3Client = new AmazonS3Client(credentials);
        s3Client.setEndpoint(END_POINT);
    }

    /**
     * 小文件上传(public)
     * @param file
     * @param key
     * @throws Exception
     */
    public static void fileUpload(File file, String key) {
        try {
			Bucket bucket = s3Client.createBucket(BUCKET_NAME);
			s3Client.putObject(bucket.getName(), key, file);
			s3Client.setObjectAcl(bucket.getName(), key, CannedAccessControlList.PublicRead);
			// ByteArrayInputStream input = new ByteArrayInputStream("Hello World!".getBytes());
			// s3Client.putObject(bucket.getName(), "hello.txt", input, new ObjectMetadata());
			// s3Client.setObjectAcl(bucket.getName(), "hello.txt", CannedAccessControlList.Private);
		} catch (Exception e) {
			LOG.warn("", e);
		}
    }

    /**
     * 大文件分片上传(高级别API)
     * @param file
     * @param key
     * @throws Exception
     */
    public static void fileHlUpload(File file, String key) {
    	TransferManager tm = new TransferManager(s3Client);
    	LOG.info("Upload install.");
        // TransferManager processes all transfers asynchronously,
        // so this call will return immediately.
        Upload upload = tm.upload(BUCKET_NAME, key, file);
        LOG.info("Upload start.");

        try {
            // Or you can block and wait for the upload to finish
            upload.waitForCompletion();
            LOG.info("Upload complete.");
        } catch (AmazonClientException amazonClientException) {
        	LOG.warn("Unable to upload file, upload was aborted.", amazonClientException);
        } catch (Exception e) {
        	LOG.warn("", e);
        }
    }

    /**
     * 流分片上传(高级别API)
     * @param in
     * @param key
     * @throws Exception
     */
    public static void streamHlUpload(InputStream in, String key) {
    	TransferManager tm = new TransferManager(s3Client);
    	LOG.info("Upload install.");
        // TransferManager processes all transfers asynchronously,
        // so this call will return immediately.
        Upload upload = tm.upload(BUCKET_NAME, key, in, new ObjectMetadata());
        LOG.info("Upload start.");

        try {
            // Or you can block and wait for the upload to finish
            upload.waitForCompletion();
            LOG.info("Upload complete.");
        } catch (AmazonClientException amazonClientException) {
        	LOG.warn("Unable to upload stream, upload was aborted.", amazonClientException);
        } catch (Exception e) {
        	LOG.warn("", e);
        }
    }

    /**
     * 流分片上传(低级别API)
     * @param in
     * @param key
     * @throws Exception
     */
    public static void streamLlUpload(InputStream in, String key) {
    	AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
    	AmazonS3 s3Client = new AmazonS3Client(credentials);
    	s3Client.setEndpoint(END_POINT);

        // Create a list of UploadPartResponse objects. You get one of these
        // for each part upload.
        List<PartETag> partETags = new ArrayList<PartETag>();

        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(BUCKET_NAME, key);
        InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);

        try {
        	long contentLength = in.available();
        	long partSize = 5242880; // Set part size to 5 MB.

            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
            	partSize = Math.min(partSize, (contentLength - filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(BUCKET_NAME).withKey(key)
                    .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withInputStream(in)
                    .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(s3Client.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
            }

            // Step 3: Complete.
			CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
					BUCKET_NAME, key, initResponse.getUploadId(), partETags);

            s3Client.completeMultipartUpload(compRequest);
        } catch (Exception e) {
			s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(
					BUCKET_NAME, key, initResponse.getUploadId()));
        }
    }

    /**
     * 监听上传
     * @param in
     * @param key
     */
    public static void streamUploadListener(InputStream in, String key) {
    	try {
			TransferManager tm = new TransferManager(s3Client);

			// For more advanced uploads, you can create a request object
			// and supply additional request parameters (ex: progress listeners,
			// canned ACLs, etc.)
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(in.available());
			PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, key, in, metadata);

			// You can ask the upload for its progress, or you can
			// add a ProgressListener to your request to receive notifications
			// when bytes are transferred.
			request.setGeneralProgressListener(new ProgressListener() {
				@Override
				public void progressChanged(ProgressEvent progressEvent) {
					LOG.info("Transferred bytes: " + progressEvent.getBytesTransferred());
				}
			});

			// TransferManager processes all transfers asynchronously, so this call will return immediately.
			Upload upload = tm.upload(request);

			try {
				// You can block and wait for the upload to finish
				upload.waitForCompletion();
			} catch (AmazonClientException amazonClientException) {
				LOG.warn("Unable to upload file, upload aborted.", amazonClientException);
			}
		} catch (Exception e) {
			LOG.warn("", e);
		}
    }

    /**
     * 查询bucket里的文件
     * @return
     */
    public static List<S3ObjectSummary> queryBucket() {
    	List<S3ObjectSummary> list = new ArrayList<S3ObjectSummary>();
		try {
			Bucket bucket = s3Client.createBucket(BUCKET_NAME);
			ObjectListing objects = s3Client.listObjects(bucket.getName());
			do {
				for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
					list.add(objectSummary);
				}
				objects = s3Client.listNextBatchOfObjects(objects);
			} while (objects.isTruncated());
		} catch (Exception e) {
			LOG.warn("", e);
		}
		return list;
    }

    /**
     * 下载到本地指定位置
     * @param key
     * @param downloadPath
     * @return
     */
    public static ObjectMetadata download(String key, String downloadPath) {
		try {
			// TransferManager tm = new TransferManager(s3Client);
			// tm.download(BUCKET_NAME, key, new File(downloadPath));
			Bucket bucket = s3Client.createBucket(BUCKET_NAME);
			return s3Client.getObject(new GetObjectRequest(bucket.getName(), key), new File(downloadPath));
		} catch (Exception e) {
			LOG.warn("", e);
		}
		return null;
    }

    /**
     * 中止存储桶里所有在n天前启动的并且仍在进行的分段上传
     * @param days
     */
    public static void abortMPUUsingHlUpload(int days) {
        TransferManager tm = new TransferManager(s3Client);

        int specifyDays = 1000 * 60 * 60 * 24 * days;
		Date specifyAgo = new Date(System.currentTimeMillis() - specifyDays);

        try {
        	tm.abortMultipartUploads(BUCKET_NAME, specifyAgo);
        } catch (AmazonClientException amazonClientException) {
        	LOG.warn("Unable to abort upload.", amazonClientException);
        }
    }

    /**
     * 删除指定资源
     * @param key
     */
    public static void delete(String key) {
        try {
			Bucket bucket = s3Client.createBucket(BUCKET_NAME);
			s3Client.deleteObject(bucket.getName(), key);
		} catch (Exception e) {
			LOG.warn("", e);
		}
    }

    /**
     * 删除指定bucket
     */
    public static void deleteBucket() {
        try {
            Bucket bucket = s3Client.createBucket(BUCKET_NAME);
            s3Client.deleteBucket(bucket.getName());
        } catch (Exception e) {
            LOG.warn("", e);
        }
    }

    /**
     * 生成下载地址
     * @param key
     * @return
     */
    public static URL generatePresignedUrl(String key) {
        try {
			Bucket bucket = s3Client.createBucket(BUCKET_NAME);
			GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket.getName(), key);
			return s3Client.generatePresignedUrl(request);
		} catch (Exception e) {
			LOG.warn("", e);
		}
        return null;
    }

    /**
     * 生成下载地址(有效时间)
     * @param key
     * @param expires
     * @return
     */
    public static URL generatePresignedUrl(String key, long expires) {
        try {
			Bucket bucket = s3Client.createBucket(BUCKET_NAME);
			long time = System.currentTimeMillis() + expires;
			return s3Client.generatePresignedUrl(bucket.getName(), key, new Date(time));
		} catch (Exception e) {
			LOG.warn("", e);
		}
        return null;
    }

    public static void main(String[] args) throws Exception {
    	AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
    	ClientConfiguration config = new ClientConfiguration();
    	AmazonS3 s3 = new AmazonS3Client(credentials, config);
        s3.setEndpoint(END_POINT);
    	TransferManager tm = new TransferManager(s3);
        System.out.println("Upload install.");
        // TransferManager processes all transfers asynchronously,
        // so this call will return immediately.
        File file = new File("F:\\bak\\duowan.mp4");
//        InputStream is = new FileInputStream(file);
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentLength(is.available());
        PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, "livetest.flv", file);
        // PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, "test.mp4", is, metadata);

        // You can ask the upload for its progress, or you can
        // add a ProgressListener to your request to receive notifications
        // when bytes are transferred.
        request.setGeneralProgressListener(new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                System.out.println("Transferred bytes: " + progressEvent.getBytesTransferred());
            }
        });

        // TransferManager processes all transfers asynchronously, so this call will return immediately.
        Upload upload = tm.upload(request);
        System.out.println("Upload start.");

        try {
            // Or you can block and wait for the upload to finish
            upload.waitForCompletion();
            System.out.println("Upload complete.");
        } catch (AmazonClientException amazonClientException) {
            System.out.println("Unable to upload file, upload was aborted.");
            amazonClientException.printStackTrace();
        }
	}
}
