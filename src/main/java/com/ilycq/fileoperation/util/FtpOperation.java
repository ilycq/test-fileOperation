package com.ilycq.fileoperation.util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class FtpOperation {

    public static void main(String[] args) throws URISyntaxException, IOException {
        ftpAsJpgDownload();
    }

    private static void ftpAsJpgDownload() throws URISyntaxException, IOException {
        String link = "ftp://10.1.22.217/REPCS/2025/8/20/304432/817810/30310961.pdf";

        // 解析URL获取各个部分
        URI uri = new URI(link);
        String server = uri.getHost(); // 主机地址
        int port = 21; // 端口号
        String user = "ftpadmin"; // 用户名
        String pwd = "FTP_dlyy2024"; // 密码
        String remoteFilePath = uri.getPath(); // 远程文件路径
        byte[] bytes = downloadFileFromFTP(server, port, user, pwd, remoteFilePath);
        String fileName = link.substring(link.lastIndexOf("/") + 1);
        //保存为临时文件
        String userDir = System.getProperty("user.dir");
        Path userDirPath = Paths.get(userDir);
        Path uploadPath = userDirPath.resolve("upload");
        if (!Files.exists(uploadPath)) {
            Files.createDirectory(uploadPath);
        }
        String tmpFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS"))+"_"+ ThreadLocalRandom.current().nextInt(10000);
        Path tmpFile = Files.createTempFile(uploadPath,tmpFileName+"_", fileName);
        try (FileOutputStream fos = new FileOutputStream(tmpFile.toFile())) {
            fos.write(bytes);
        }
        if (fileName.toLowerCase().endsWith(".pdf")) {
            Path tmpJpg = Files.createTempFile(uploadPath,tmpFileName+"_", ".jpg");
            try (
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    FileOutputStream fos = new FileOutputStream(tmpJpg.toFile());
                    PDDocument document = PDDocument.load(new ByteArrayInputStream(bytes))
            ) {
                PDFRenderer renderer = new PDFRenderer(document);
                for (int page = 0; page < document.getNumberOfPages(); page++) {
                    // 设置 DPI（分辨率），默认 72，建议 300 以获得高清图片
                    BufferedImage image = renderer.renderImageWithDPI(page, 300);

                    // 4. 保存为 JPG
                    ImageIO.write(image, "jpg", baos);
                    bytes = baos.toByteArray();
                    fos.write(bytes);
                }
            }
        }

    }

    private static void ftpToJpgDownload() throws URISyntaxException, IOException {
        String link = "ftp://10.1.22.217/REPCS/2025/8/20/304432/817810/30310961.pdf";

        // 解析URL获取各个部分
        URI uri = new URI(link);
        String server = uri.getHost(); // 主机地址
        int port = 21; // 端口号
        String user = "ftpadmin"; // 用户名
        String pwd = "FTP_dlyy2024"; // 密码
        String remoteFilePath = uri.getPath(); // 远程文件路径
        byte[] bytes = downloadFileFromFTP(server, port, user, pwd, remoteFilePath);
        String fileName = link.substring(link.lastIndexOf("/") + 1);
        //保存为临时文件
        String userDir = System.getProperty("user.dir");
        Path userDirPath = Paths.get(userDir);
        Path uploadPath = userDirPath.resolve("upload");
        if (!Files.exists(uploadPath)) {
            Files.createDirectory(uploadPath);
        }
        String tmpFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS"))+"_"+ ThreadLocalRandom.current().nextInt(10000);

        Path tmpFile = Files.createTempFile(uploadPath,tmpFileName+"_", fileName);
        try (FileOutputStream fos = new FileOutputStream(tmpFile.toFile())) {
            fos.write(bytes);
        }
        if (fileName.toLowerCase().endsWith(".pdf")) {
            Path tmpJpg = Files.createTempFile(uploadPath,tmpFileName+"_", ".jpg");
            try (FileOutputStream fos = new FileOutputStream(tmpJpg.toFile())) {
                //转为jpg
                try (PDDocument document = PDDocument.load(bytes)) {
                    PDFRenderer renderer = new PDFRenderer(document);
                    for (int page = 0; page < document.getNumberOfPages(); page++) {
                        // 设置 DPI（分辨率），默认 72，建议 300 以获得高清图片
                        BufferedImage image = renderer.renderImageWithDPI(page, 300);

                        // 4. 保存为 JPG
                        ImageIO.write(image, "jpg", fos);
                        fos.flush();
                    }
                }
            }
        }

    }

    private static byte[] downloadFileFromFTP(String server, int port, String user, String pwd, String remoteFilePath)
            throws IOException {
        FTPClient ftpClient = new FTPClient();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ftpClient.connect(server, port);
            ftpClient.login(user, pwd);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // 切换工作目录
            int lastindex =
                    remoteFilePath.contains("/") ? remoteFilePath.lastIndexOf("/") : remoteFilePath.lastIndexOf("\\");
            ftpClient.changeWorkingDirectory(remoteFilePath.substring(0, lastindex - 1));
            boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);
            if (success) {
                return outputStream.toByteArray();
            } else {
                throw new IOException("下载FTP文件失败," + remoteFilePath);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
