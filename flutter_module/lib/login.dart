import 'package:flutter/material.dart';

class LoginPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new LoginPageState();
}

class LoginPageState extends State<LoginPage> {

  String username;
  String password;

  //全局key用来获取From表单组件
  GlobalKey<FormState> loginKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('Form表单提交'),
        ),
        body: Column(
          children: <Widget>[
            Container(
              padding: EdgeInsets.all(16.0),
              child: Form(
                key: loginKey,
                child: Column(
                  children: <Widget>[
                    TextFormField(
                      decoration: InputDecoration(labelText: '请输入用户名'),
                      onSaved: (value) {
                        username = value;
                      },
                      onFieldSubmitted: (value) {},
                    ),
                    TextFormField(
                      decoration: InputDecoration(labelText: '请输入密码'),
                      obscureText: true,
                      validator: (value) {
                        return value.length < 6 ? '密码长度不够六位' : null;
                      },
                      onSaved: (value) {
                        password = value;
                      },
                    )
                  ],
                ),
              ),
            ),
            SizedBox(
              width: 340.0,
              height: 42.0,
              child: RaisedButton(
                onPressed: () {
                  var loginForm = loginKey.currentState;
                  print(loginForm);
                  if(loginForm.validate()){
                    loginForm.save();
                    print("username:"+username + 'password:'+password);
                  }
                },
                child: Text(
                  '登录',
                  style: TextStyle(fontSize: 18.0),
                ),
              ),
            )
          ],
        ),
    );
  }

}