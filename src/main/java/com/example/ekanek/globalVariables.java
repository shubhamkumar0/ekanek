package com.example.ekanek;

import org.springframework.stereotype.Component;

@Component
public class globalVariables {

    private User GLOBAL_USER;

    public User getGLOBAL_USER() {
        return GLOBAL_USER;
    }

    public void setGLOBAL_USER(User GLOBAL_USER) {
        this.GLOBAL_USER = GLOBAL_USER;
    }
}
