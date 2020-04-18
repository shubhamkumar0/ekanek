package com.example.ekanek;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class S3FileUploadController {

    private AmazonClient amazonClient;

    private static User sessionUser;

    private Logger logger = LoggerFactory.getLogger(AmazonClient.class);

    @Autowired
    S3FileUploadController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }

    @Autowired
    private globalVariables globalVariables;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    S3Services s3Services;

    @PostMapping(value = "/uploadFile")
    @Async
    public ModelAndView uploadFiles(@RequestParam(value = "file") MultipartFile file, @RequestParam(value="title",required = false) String title,
                              @RequestParam(value="desc",required = false) String desc) throws IOException {
        RedirectView redirectView = new RedirectView();
        if(globalVariables.getGLOBAL_USER()!=null && globalVariables.getGLOBAL_USER().getEmail()!=null){
            Stopwatch stopwatch = Stopwatch.createStarted();
//            String filename = this.amazonClient.uploadFile(file,globalVariables.getGLOBAL_USER().getEmail());
            String filename = s3Services.uploadFile(file,globalVariables.getGLOBAL_USER().getEmail());
            stopwatch.stop();
            logger.info(String.valueOf(stopwatch.elapsed(TimeUnit.SECONDS)));
            fileRepository.saveFile(title,globalVariables.getGLOBAL_USER().getEmail(),filename,desc);
            redirectView.setUrl("/home");
        }
        else{
            redirectView.setUrl("/login");
            return new ModelAndView(redirectView);
        }
        return new ModelAndView(redirectView);
    }

    @GetMapping("/deleteFile")
    @Async
    public ModelAndView deleteFile(@RequestParam(value="link") String link) {
        RedirectView redirectView = new RedirectView();
        String response = this.amazonClient.deleteFileFromS3Bucket(link);
        if(response!=null) {
            for(Files i:filesList){
                if(i.getLink().equals(link))fileRepository.delete(i);
            }
        }
        redirectView.setUrl("/home");
        return new ModelAndView(redirectView);
    }

    @GetMapping(value= "/downloadFile")
    public String downloadFile(@RequestParam(value= "link") final String link) {
        return amazonClient.downloadFile(link);
    }

    private List<Files> filesList = new ArrayList<Files>();

    @GetMapping(value={"/home"})
    public ModelAndView showHomePage() {
        String viewName = "home";
        Map<String,Object> model = new HashMap<String,Object>();
        List<String> list = new ArrayList<>();
        if(globalVariables.getGLOBAL_USER()!=null && globalVariables.getGLOBAL_USER().getEmail()!=null){
            filesList.clear();
            filesList.addAll(fileRepository.findByEmail(globalVariables.getGLOBAL_USER().getEmail()));
            model.put("filesList",filesList);
            return new ModelAndView(viewName,model);
        } else{
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl("/login");
            return new ModelAndView(redirectView);
        }
    }
}
