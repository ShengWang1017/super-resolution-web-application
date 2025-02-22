import utils.FileUpload;

import java.io.File;

public class Test {
    public static void main(String[] args) throws Exception {
        File file = new File("F:\\code\\imageSR-web\\2.jpg");
        FileUpload.uploadJPGFile(file);
    }
}
