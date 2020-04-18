//package com.example.ekanek;
//
//
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
//import com.google.api.client.http.FileContent;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.store.FileDataStoreFactory;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.File;
//import com.google.api.services.drive.model.FileList;
//import org.apache.commons.fileupload.FileItemIterator;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.view.RedirectView;
//
//import javax.annotation.PostConstruct;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static com.google.common.io.Files.getFileExtension;
//
//
//@Controller
//public class FileUploadController {
//    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
//    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//
//    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
//
//    private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";
//
//    @Value("${google.oauth.callback.uri}")
//    private String CALLLBACK_URI;
//
//    @Value("${google.secret.key.path}")
//    private Resource gdSecretKeys;
//
//    @Value("${google.credentials.folder.path}")
//    private Resource credentialsFolder;
//
//    private GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;
//
//    @PostConstruct
//    public void init() throws Exception {
//        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY,new InputStreamReader(gdSecretKeys.getInputStream()));
//        googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,JSON_FACTORY,secrets,SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
//    }
//    private List<Files> filesList = new ArrayList<Files>();
//    private static Long index=1L;
//
//    @GetMapping(value={"/oldhome"})
//    public ModelAndView showhomepage() throws IOException {
//        boolean isUserAuth = false;
//        Credential credentials = googleAuthorizationCodeFlow.loadCredential(USER_IDENTIFIER_KEY);
//        if(credentials!=null) {
//            boolean tokenValid = credentials.refreshToken();
//            if(tokenValid){
//                isUserAuth = true;
//            }
//        }
//        String viewName;
//        Map<String,Object> model = new HashMap<String,Object>();
//        if(isUserAuth){
//            filesList.clear();
//            Credential credential = googleAuthorizationCodeFlow.loadCredential(USER_IDENTIFIER_KEY);
//            Drive drive = new Drive.Builder(HTTP_TRANSPORT,JSON_FACTORY,credential).setApplicationName("springboottest").build();
//            Drive.Files.List fileList = drive.files().list();
//            fileList.setPageSize(10);
//            fileList.setQ("'1SGjolAwQJ30srW_aRdpULoYZfCqQ68Hz' in parents and trashed=false");
//            fileList.setFields("nextPageToken, files(*)");
//            FileList fileList1 = fileList.execute();
//            for(File file: fileList1.getFiles()) {
//                Files fileDto= new Files(file.getId(),file.getName(),file.getDescription(),file.getWebContentLink());
//                filesList.add(fileDto);
//            }
//            viewName="home";
//            model.put("filesList",filesList);
//        } else{
//            viewName="index";
//        }
//        return new ModelAndView(viewName,model);
//    }
//
//    @GetMapping(value = {"/googlesignin"})
//    public void doGoogleSignIn(HttpServletResponse response) throws IOException {
//        GoogleAuthorizationCodeRequestUrl url = googleAuthorizationCodeFlow.newAuthorizationUrl();
//        String redirectUrl = url.setRedirectUri(CALLLBACK_URI).setAccessType("offline").build();
//        response.sendRedirect(redirectUrl);
//    }
//
//    @GetMapping(value = {"/oauth"})
//    public String saveAuthCode(HttpServletRequest request) throws IOException {
//        String code =request.getParameter("code");
//        if(code!=null){
//            saveToken(code);
//            return "home";
//        }
//        return "index";
//    }
//
//    private void saveToken(String code) throws IOException {
//        GoogleTokenResponse response = googleAuthorizationCodeFlow.newTokenRequest(code).setRedirectUri(CALLLBACK_URI).execute();
//        googleAuthorizationCodeFlow.createAndStoreCredential(response,USER_IDENTIFIER_KEY);
//    }
//
//    @GetMapping(value={"/upload"})
//    public ModelAndView createFile(HttpServletRequest request) throws Exception {
//        Credential credential = googleAuthorizationCodeFlow.loadCredential(USER_IDENTIFIER_KEY);
//        Drive drive = new Drive.Builder(HTTP_TRANSPORT,JSON_FACTORY,credential).setApplicationName("springboottest").build();
//        File file = new File();
//        file.setName("sample.jpg");
//        file.setParents(Arrays.asList("1SGjolAwQJ30srW_aRdpULoYZfCqQ68Hz"));
//        //figure out file type and compress
//        String fileType = getFileExtension("/Users/shubhamkumar/Downloads/shubh.jpg");
//        FileContent content = new FileContent("image/jpg", new java.io.File("/Users/shubhamkumar/Downloads/shubh.jpg"));
//        File uploadedFile = drive.files().create(file,content).setFields("id").execute();
//        RedirectView redirectView = new RedirectView();
//        redirectView.setUrl("/");
//        return new ModelAndView(redirectView);
//    }
//
//    @GetMapping("/delete")
//    public ModelAndView deleteFile(@RequestParam(required = false) String id) throws Exception{
//        RedirectView redirectView = new RedirectView();
//        Credential credential = googleAuthorizationCodeFlow.loadCredential(USER_IDENTIFIER_KEY);
//        Drive drive = new Drive.Builder(HTTP_TRANSPORT,JSON_FACTORY,credential).setApplicationName("springboottest").build();
//        drive.files().delete(id).execute();
//        redirectView.setUrl("/");
//        return new ModelAndView(redirectView);
//    }
//
//}