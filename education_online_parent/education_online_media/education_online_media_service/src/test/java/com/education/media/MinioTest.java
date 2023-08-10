package com.education.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.util.ArrayList;

/**
 * @author yang
 * @create 2023-08-02 16:12
 */
@SpringBootTest(classes = MinioTest.class)
public class MinioTest {

    private MinioClient minioClient = MinioClient.builder().endpoint("http://192.168.1.200:9000")
            .credentials("miniouseryq","yqabc123").build();

    //minio文件上传测试
    @Test
    public void upload() throws Exception {

        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = extensionMatch.getMimeType();

        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("miniotest").object("vedio/1.mp4")
                .filename("C:\\Users\\yq\\Desktop\\1.mp4")
                .contentType(mimeType)
                .build();
        minioClient.uploadObject(uploadObjectArgs);
    }

    //minio文件下载测试
    @Test
    public void download() throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                        .bucket("miniotest").object("vedio/1.mp4").build();
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\yq\\Desktop\\abc.mp4"));
        IOUtils.copy(inputStream,outputStream);
    }

    //minio文件删除测试
    @Test
    public void delete() throws Exception {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                        .bucket("miniotest")
                                .object("vedio/1.mp4").build();
        minioClient.removeObject(removeObjectArgs);
    }

    //minio合并分块测试
    @Test
    public void merge() throws Exception{
        //将文件上传到 minion
        for (int i = 0; i < 9; i++) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("miniotest").object("bucket/" + i)
                    .filename("C:\\Users\\yq\\Desktop\\test\\block\\" + i )
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
        }

        //合并文件
        ArrayList<ComposeSource> composeSources = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ComposeSource build = ComposeSource.builder().bucket("miniotest").object("bucket/" + i).build();
            composeSources.add(build);
        }
        ComposeObjectArgs build = ComposeObjectArgs.builder().bucket("miniotest").sources(composeSources).object("2.mp4").build();
        minioClient.composeObject(build);
    }

}