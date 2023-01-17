package uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.PostConstruct;

import static com.microsoft.azure.storage.blob.DeleteSnapshotsOption.INCLUDE_SNAPSHOTS;
import static java.util.Objects.isNull;

@Lazy
@Component
@ContextConfiguration(classes = {
    AzureBlobConfig.class, BlobStorageCredentials.class}, initializers = ConfigDataApplicationContextInitializer.class)
public class CommonDataBlobSupport {


    @Autowired
    @Qualifier("credscloudStorageAccount")
    CloudStorageAccount acc;

    CloudBlobClient cloudBlobClient;

    CloudBlobContainer cloudBlobContainer;

    CloudBlobContainer cloudBlobArchContainer;

    @Value("${archival-date-format}")
    private String archivalDateFormat;

    /**
     * Configure Cloud Variables.
     *
     * @throws Exception Exception
     */
    @PostConstruct
    public void init() throws Exception {
        cloudBlobClient = acc.createCloudBlobClient();
        cloudBlobContainer = cloudBlobClient.getContainerReference("rd-common-data");
        cloudBlobArchContainer = cloudBlobClient.getContainerReference("rd-common-data-archive");
    }

    /**
     * Upload File to Blob Storage.
     *
     * @param blob       File Name
     * @param sourceFile File Path
     * @throws Exception Exception
     */
    public void uploadFile(String blob, InputStream sourceFile) throws Exception {
        CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(blob);
        cloudBlockBlob.upload(sourceFile, 8 * 1024 * 1024);
    }

    /**
     * Delete File From blob storage.
     *
     * @param blob   File Name
     * @param status file status
     * @throws Exception Exception
     */
    public void deleteBlob(String blob, boolean... status) throws Exception {
        Thread.sleep(1000);
        CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(blob);
        if (cloudBlockBlob.exists()) {
            cloudBlockBlob.delete(INCLUDE_SNAPSHOTS, null, null, null);
            String date = new SimpleDateFormat(archivalDateFormat).format(new Date());

            //Skipped for Stale non existing files as not archived
            if (isNull(status)) {
                cloudBlockBlob = cloudBlobArchContainer.getBlockBlobReference(blob.concat(date));
                if (cloudBlockBlob.exists()) {
                    cloudBlockBlob.delete(INCLUDE_SNAPSHOTS, null, null, null);
                }
            }
        }
    }

    /**
     * To Check file exist or not.
     *
     * @param blob File Name
     * @return return true if file exist
     * @throws Exception Exception
     */
    public boolean isBlobPresent(String blob) throws Exception {
        CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(blob);
        return cloudBlockBlob.exists();
    }
}
