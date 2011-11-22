package com.synchophy.server.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synchophy.server.ParamException;
import com.synchophy.server.User;


public class LoginDispatch extends AbstractDispatch {

  public Object execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    
    String username = request.getParameter("u");
    if(username == null || username.trim().length() == 0) {
      throw new ParamException("You must specify a value for the username parameter 'u'.");
    }
    String password = request.getParameter("p");
    if(password == null || password.trim().length() == 0) {
      throw new ParamException("You must specify a value for the password parameter 'p'.");
    }
    

    request.getSession().setAttribute("CURRENT_USER", User.login(username, password));
    return (User) request.getSession().getAttribute("CURRENT_USER"); 
  }

}
