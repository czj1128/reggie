package com.czj.reggie.controller;

import com.czj.reggie.common.CustomException;
import com.czj.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 文件上传和下载
 */
@RequestMapping("/common")
@RestController
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;
    @Autowired(required = false)
    private HttpServletResponse response;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        log.info("文件上传，file：{}",file.toString());
        //获取原始文件名
        String fileName = file.getOriginalFilename();
        //获取原始文件名后缀类型
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //为防止文件名重复，导致文件覆盖，使用uuid进行新文件名生成
        String newFileName=UUID.randomUUID().toString()+suffix;
        //判断路径目录是否存在
        File dir = new File(basePath);
        if (!dir.exists()){
            //路径目录不存在，创建
            dir.mkdirs();
        }
        //file是临时文件，需要转储到指定位置保存，否则本次请求结束将删除临时文件
        try {
            file.transferTo(new File(basePath+newFileName));
        } catch (IOException e) {
            e.printStackTrace();
            return R.error("文件上传失败");
        }
        return R.success(newFileName);
    }


    /**
     * 文件下载
     * @param name
     */
    @GetMapping("download")
    public void download(String name){

        log.info("文件下载：{}",name);
        //输入流，读取文件内容
        BufferedInputStream buffInput=null;
        //输出流，将文件内容回写至浏览器，在浏览器进行展示
        BufferedOutputStream buffOutput=null;
        try {
            buffInput=new BufferedInputStream(new FileInputStream(basePath+name));
            buffOutput=new BufferedOutputStream(response.getOutputStream());
            int len=-1;
            byte[] buf=new byte[1024];
            while ( (len=buffInput.read(buf))!=-1 ){
                buffOutput.write(buf,0,len);
                buffOutput.flush();
            }
        }catch (FileNotFoundException e){
            String filepath = e.toString().substring(e.toString().indexOf(":") + 1);
            //log.error("文件下载失败找不到路径文件:"+filepath);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                //不判断是否为空，会造成空指针异常
                if (buffInput!=null)buffInput.close();
                if (buffOutput!=null)buffOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
