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

    @Autowired
    private FileUpload fileUpload;

    @Override
    public String infer(MultipartFile image) {
        try {
            return sendPostRequest(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String sendPostRequest(MultipartFile image) throws IOException {
        File tempImage = null;
        File outputFile = null;
        try {
            // 创建临时文件
            tempImage = File.createTempFile("upload-", image.getOriginalFilename());
            image.transferTo(tempImage);

            // 构造RequestBody
            RequestBody requestBody = RequestBody.create(tempImage, MediaType.parse("image/jpeg"));
            Request request = new Request.Builder()
                    .url(torchserveUrl)
                    .post(requestBody)
                    .build();

            // 发送请求并处理响应
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("请求失败: " + response.code() + " " + response.message());
                }

                log.info("请求成功，开始保存图片...");
                outputFile = new File("output_image.jpg");
                
                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    log.info("图片已保存到: {}", outputFile.getAbsolutePath());
                    
                    return fileUpload.uploadJPGFile(outputFile);
                }
            }
        } catch (Exception e) {
            log.error("处理请求失败", e);
            throw new RuntimeException("处理请求失败: " + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempImage != null && tempImage.exists()) {
                tempImage.delete();
            }
            if (outputFile != null && outputFile.exists()) {
                outputFile.delete();
            }
        }
    }
}
