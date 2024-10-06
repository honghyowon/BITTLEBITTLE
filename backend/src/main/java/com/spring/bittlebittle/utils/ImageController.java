package com.spring.bittlebittle.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ImageController {

    Logger log = LogManager.getLogger(ImageController.class);

    @GetMapping("/image")
    public void getImage(@RequestParam("path") String path,
                         @RequestParam("name") String imageName,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        // IMAGE_DIRECTORY는 프로젝트의 절대 경로를 기준으로 설정됩니다.
        String IMAGE_DIRECTORY = request.getServletContext().getRealPath("/resources/static/image/") + path;

        log.debug("IMAGE_DIRECTORY : " + IMAGE_DIRECTORY.toString());
        log.debug("imageName : " + imageName.toString());
        // 이미지 파일의 절대 경로를 생성합니다.
        Path imagePath = Paths.get(IMAGE_DIRECTORY, imageName);
        log.debug("imagePath : " + imagePath.toString());

        // 이미지 파일이 존재하지 않는 경우 404 에러를 반환합니다.
        if (!Files.exists(imagePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            log.error("Image not found: " + imagePath.toString());
            return;
        }

        // 이미지의 MIME 타입을 설정합니다.
        String contentType = Files.probeContentType(imagePath);
        if (contentType == null) {
            contentType = "application/octet-stream"; // 알 수 없는 경우 기본 타입 설정
        }
        response.setContentType(contentType);

        // 이미지 파일을 응답으로 전송합니다.
        try (OutputStream out = response.getOutputStream();
             InputStream in = Files.newInputStream(imagePath)) {
            byte[] buffer = new byte[4096];  // 버퍼 크기 증가
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("Error reading or writing image file: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing image");
        }
    }
}
//@Controller
//public class ImageController {
//
//	Logger log = LogManager.getLogger("case3");
//
//    @GetMapping("image")
//    public void getImage( @RequestParam("path") String path,
//                        @RequestParam("name") String imageName,
//                         HttpServletRequest request,
//                         HttpServletResponse response) throws IOException {
//
//        String IMAGE_DIRECTORY = request.getServletContext().getResource("/resources/static/image/"+path+"/").getPath();
//        // IMAGE_DIRECTORY 상수는 이미지가 저장된 디렉토리의 경로
//
//        File imageFile = new File(IMAGE_DIRECTORY + File.separator + imageName);
//        if (!imageFile.exists()) {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        }
//
//        String contentType = URLConnection.guessContentTypeFromName(imageName);
//        response.setContentType(contentType);
//
//        try (OutputStream out = response.getOutputStream();
//             InputStream in = new FileInputStream(imageFile)) {
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = in.read(buffer)) != -1) {
//                out.write(buffer, 0, bytesRead);
//            }
//        }
//    }
//}
