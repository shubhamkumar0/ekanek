package com.example.ekanek;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private globalVariables globalVariables;

    @RequestMapping("/")
    public ModelAndView home() {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/login");
        return new ModelAndView(redirectView);
    }

    @RequestMapping("/login")
    public ModelAndView loginForm(){
        String viewName="loginForm";
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("user",new User());
        return new ModelAndView(viewName,model);
    }

    @PostMapping("/login")
    public ModelAndView submitLoginForm(User user) {
        RedirectView redirectView = new RedirectView();
        Map<String,Object> model = new HashMap<String,Object>();
        User temp = userRepository.findByEmail(user.getEmail());
        if(temp!= null && user.getPassword().equals(temp.getPassword())) {
            globalVariables.setGLOBAL_USER(user);
            redirectView.setUrl("/home");
        }else {
            redirectView.setUrl("/login");
        }
        return new ModelAndView(redirectView);
    }

    @RequestMapping("/register")
    public ModelAndView registerUser(){
        String viewName="RegisterForm";
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("user",new User());
        return new ModelAndView(viewName,model);
    }

    @PostMapping("/register")
    public ModelAndView submitRegisterForm(User user) {
        RedirectView redirectView = new RedirectView();
        if(userRepository.findByEmail(user.getEmail())!=null){
            redirectView.setUrl("/register");
        } else{
            userRepository.save(user);
            redirectView.setUrl("/login");
        }
        return new ModelAndView(redirectView);
    }


}
