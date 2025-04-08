package com.manager.freelancer_management_api.domain.user.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException() {
        super("User not found.");
    }
    public UserNotFoundException(String message){
        super(message);
    }
}