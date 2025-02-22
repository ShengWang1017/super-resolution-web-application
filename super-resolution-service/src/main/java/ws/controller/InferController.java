package ws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ws.service.Imp.InferServiceImp;

@RestController
public class InferController {

    @Autowired
    private InferServiceImp inferServiceImp;

    @PostMapping(path = "/infer")
    public String inferController(@RequestParam MultipartFile file) {
        return inferServiceImp.infer(file);
    }
}
