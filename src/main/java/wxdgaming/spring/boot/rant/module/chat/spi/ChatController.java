package wxdgaming.spring.boot.rant.module.chat.spi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import wxdgaming.spring.boot.core.io.FileUtil;
import wxdgaming.spring.boot.core.io.FileWriteUtil;
import wxdgaming.spring.boot.core.lang.RunResult;
import wxdgaming.spring.boot.core.util.StringsUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 聊天接口
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-11-29 14:29
 **/
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @RequestMapping("/upload")
    public RunResult handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return RunResult.error("not file");
        }
        String newFileName = "";
        try {
            // 保存文件到指定路径
            byte[] bytes = file.getBytes();
            newFileName = StringsUtil.getRandomString(32) + "-" + file.getOriginalFilename();
            newFileName = newFileName.toLowerCase();
            if (newFileName.endsWith(".jpg")
                || newFileName.endsWith(".jpeg")
                || newFileName.endsWith(".png")
                || newFileName.endsWith(".gif")) {
                File path = new File("upload/" + newFileName);
                FileWriteUtil.writeBytes(path, bytes);
                return RunResult.ok().data(newFileName);
            } else {
                return RunResult.error("文件格式不支持");
            }
        } catch (Exception e) {
            log.error("上传文件异常 {}", newFileName, e);
            return RunResult.error("Failed file");
        }
    }

    /** 每个小时执行一次定时器，清理上传超过10天的文件 */
    @Scheduled(cron = "0 0 * * * *")
    public void clearUploadFiles() throws IOException {
        FileUtil.walkFiles("upload")
                .filter(path -> System.currentTimeMillis() - path.toFile().lastModified() < TimeUnit.DAYS.toMillis(10))
                .forEach(path -> FileUtil.del(path));
    }

}
