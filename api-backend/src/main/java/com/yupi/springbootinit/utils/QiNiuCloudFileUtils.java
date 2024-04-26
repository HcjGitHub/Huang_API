package com.yupi.springbootinit.utils;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用七牛云服务器实现照片上传
 *
 * @author 兕神
 * @version 1.0
 * DateTime: 2023/4/15
 */
@Component
public class QiNiuCloudFileUtils {

    //...生成上传凭证，然后准备上传
    @Value("${qiniuclould.config.accessKey}")
    private String accessKey;

    @Value("${qiniuclould.config.secretKey}")
    private String secretKey;

    @Value("${qiniuclould.config.domain}")
    private String domain;

    @Value("${qiniuclould.config.bucket}")
    private String bucket;

    @Value("${qiniuclould.config.deadline}")
    private Long deadline;

    /**
     * 七牛云实现图片上传
     * @param fileStream 图片的 InputStream
     * @param mimeType 图片的 ContentType
     * @param fileName 图片原名
     * @return 访问路径
     */
    public String upload(InputStream fileStream, String mimeType, String fileName) {

        String[] fileNameSplit = fileName.split("\\.");
        if (fileNameSplit.length > 1) {
//            fileName = UUID.randomUUID().toString() + "." + fileNameSplit[fileNameSplit.length - 1];
            fileName = getFileName(mimeType);
        }
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huanan());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        String url = null;

        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = fileName;

        // 获取授权
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(fileStream, key, upToken, null, mimeType);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            //另一种获取地址
//            String encodedFileName = null;
//            String finalUrl = null;
//            encodedFileName = URLEncoder.encode(key, "utf-8").replace("+", "%20");
//            String publicUrl = String.format("%s/%s", domain, encodedFileName);
//            //1小时，可以自定义链接过期时间
//            long expireInSeconds = 3600;
//            finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
            if (putRet != null && putRet.hash != null) {
                DownloadUrl downloadUrl = new DownloadUrl(domain, false, key);
                url = downloadUrl.buildURL(auth,deadline);
                String[] split = url.split("\\?");
                url = split[0];
                return url;
            }
        } catch (QiniuException ex) {
        }
        return null;
    }

    /**
     * 生成图片名字 时间+ _ + a-z和0-9的任意5个加图片后缀
     * @param contentType 图片的 ContentType
     * @return 图片名字
     */
    public String getFileName(String contentType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        String[] ss = contentType.split("/");
        String str = RandomStringUtils.random(5,
                "abcdefghijklmnopqrstuvwxyz1234567890");
        String name = timeStr + "_" + str + "." + ss[1];
        return name;
    }
}
