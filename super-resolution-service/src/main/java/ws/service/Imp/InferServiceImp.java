package ws.service.Imp;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ws.common.utils.FileUpload;
import ws.service.InferService;

import java.io.*;

@Slf4j
@Service
public class InferServiceImp implements InferService {
    private String outputFilePath = "output_image.jpg";
    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${torchserve.url}")
    private String torchserveUrl;

    @Override
    public String infer(MultipartFile image) {
        try {
            return sendPostRequest(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String sendPostRequest(MultipartFile image) throws IOException {
        // 创建临时文件
        File tempImage = File.createTempFile("upload-", image.getOriginalFilename());
        image.transferTo(tempImage);

        // 构造RequestBody
        RequestBody requestBody = RequestBody.create(tempImage, MediaType.parse("image/jpeg"));
        Request request = new Request.Builder()
                .url(torchserveUrl)
                .post(requestBody)
                .build();

        // 发送请求并处理响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("请求成功，开始保存图片...");
                File outputFile = new File(outputFilePath);
                
                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.println("图片已保存到: " + outputFilePath);
                    
                    // 上传到阿里云OSS并获取URL
                    String imageUrl = FileUpload.uploadJPGFile(outputFile);
                    
                    // 清理临时文件
                    tempImage.delete();
                    outputFile.delete();
                    
                    return imageUrl;
                }
            } else {
                throw new RuntimeException("获得响应失败：" + response.body().string());
            }
        } catch (Exception e) {
            throw new RuntimeException("处理请求失败: " + e.getMessage());
        }
    }
}
