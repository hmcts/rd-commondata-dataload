package uk.gov.hmcts.reform.rd.commondata.cameltest.testsupport;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.azure.storage.blob.models.DeleteSnapshotsOptionType.INCLUDE;
import static java.util.Objects.isNull;

@Component
@ContextConfiguration(classes = {
    AzureBlobConfig.class, BlobStorageCredentials.class}, initializers = ConfigDataApplicationContextInitializer.class)
public class CommonDataBlobSupport {

    @Autowired
    AzureBlobConfig azureBlobConfig;

    @Autowired
    BlobServiceClientBuilder blobServiceClientBuilder;

    BlobServiceClient blobServiceClient;

    BlobContainerClient cloudBlobContainer;

    BlobContainerClient cloudBlobArchContainer;

    @Value("${archival-date-format}")
    private String archivalDateFormat;

    /**
     * Configure Cloud Variables.
     *
     * @throws Exception Exception
     */
    @PostConstruct
    public void init() throws Exception {
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(
            azureBlobConfig.getAccountName(), azureBlobConfig.getAccountKey());
        String uri = String.format("https://%s.blob.core.windows.net", azureBlobConfig.getAccountName());

        blobServiceClient = blobServiceClientBuilder
            .endpoint(uri)
            .credential(credential)
            .buildClient();
        cloudBlobContainer = blobServiceClient.createBlobContainer("rd-common-data");
        cloudBlobArchContainer = blobServiceClient.createBlobContainer("rd-common-data-archive");
    }

    /**
     * Upload File to Blob Storage.
     *
     * @param blob       File Name
     * @param sourceFile File Path
     * @throws Exception Exception
     */
    public void uploadFile(String blob, InputStream sourceFile) throws Exception {
        BlobClient cloudBlockBlob = cloudBlobContainer.getBlobClient(blob);
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
        BlobClient cloudBlockBlob = cloudBlobContainer.getBlobClient(blob);
        if (cloudBlockBlob.exists()) {
            cloudBlockBlob.deleteWithResponse(INCLUDE, null, null, null);
            String date = new SimpleDateFormat(archivalDateFormat).format(new Date());

            //Skipped for Stale non existing files as not archived
            if (isNull(status)) {
                cloudBlockBlob = cloudBlobArchContainer.getBlobClient(blob.concat(date));
                if (cloudBlockBlob.exists()) {
                    cloudBlockBlob.deleteWithResponse(INCLUDE, null, null, null);
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
        BlobClient cloudBlockBlob = cloudBlobContainer.getBlobClient(blob);
        return cloudBlockBlob.exists();
    }
}
