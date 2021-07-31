package com.tanhua.server.test;


import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.TanhuaServerApplication;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TanhuaServerApplication.class)
public class FastDFSTest {

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Test
    public void tetUploadFile()throws IOException{
        File file=new File("C:\\itheima\\java114_SEPlus\\day66_探花交友_day07\\资料\\01-视频图片\\超人跳.mp4");
        StorePath storePath = client.uploadFile(FileUtils.openInputStream(file), file.length(), "mp4", null);

        System.out.println(storePath.getFullPath());
        System.out.println(storePath.getPath());

        //获取文件的请求地址
        String url = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();
        System.out.println(url);
    }
}
