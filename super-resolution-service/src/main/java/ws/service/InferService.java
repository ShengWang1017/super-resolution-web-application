package ws.service;

import org.springframework.web.multipart.MultipartFile;

public interface InferService {

    String infer(MultipartFile image);
}
