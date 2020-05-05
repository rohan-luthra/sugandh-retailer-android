package com.sbw.auder.Models;


public class UserModelStatic {

    static UserModel userModel = null;

    public static UserModel getInstance(){
        if(userModel!=null)
            return (UserModel)userModel;
        else {
            userModel = new UserModel();
            return userModel;
        }
    }

    public static void delete(){
       // userModel.setAllNull();
        if(userModel!=null)
        userModel = null;
    }
}
