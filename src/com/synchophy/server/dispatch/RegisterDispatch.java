package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ParamException;
import com.synchophy.server.User;


public class RegisterDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    String username = request.getParameter("u");
    if(username == null || username.trim().length() == 0) {
      throw new ParamException("You must specify a value for the username parameter 'u'.");
    }
    if(!User.isUsernameUnique(username)) {
      throw new ParamException("Username already registered.");
    }
    String password = request.getParameter("p");
    if(password == null || password.trim().length() == 0) {
      throw new ParamException("You must specify a value for the password parameter 'p'.");
    }
    

    return User.register(username, password);
  }

}
