import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'file:///E:/GraduationProj/WebDAVClient/android/flutter_module/lib/util/RouteUtil.dart';
import 'file:///E:/GraduationProj/WebDAVClient/android/flutter_module/lib/db/databaseHelper.dart';
import 'file:///E:/GraduationProj/WebDAVClient/android/flutter_module/lib/ui/register.dart';
import 'file:///E:/GraduationProj/WebDAVClient/android/flutter_module/lib/bean/userInfo.dart';
import 'package:fluttertoast/fluttertoast.dart';

class LoginPage extends StatefulWidget {
  @override
  createState() => new LoginPageState();
}

class LoginPageState extends State<LoginPage> {

  static const nativeChannel =
      const MethodChannel('com.darrenyuen.webDAVClient/catalog');

  // 焦点
  FocusNode _focusNodeUserName = FocusNode();
  FocusNode _focusNodePassWord = FocusNode();

  // 用户名输入框控制器，此控制器可以监听用户名输入框的操作
  TextEditingController _userNameController = TextEditingController();

  // 表单状态
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();

  // 用户名、密码
  String _username = "", _password = "";

  // 是否显示密码
  bool _isShowPwd = false;

  // 是否显示输入框尾部的清除按钮
  bool _isShowClear = false;

  /// 插入到渲染树时调用，只执行一次。（类似Android Fragment的onCreateView函数）
  @override
  void initState() {
    // TODO: implement initState
    // 设置焦点监听
    _focusNodeUserName.addListener(_focusNodeListener);
    _focusNodePassWord.addListener(_focusNodeListener);
    // 监听用户名框的输入改变
    _focusNodeUserName.addListener(() {
      print(_userNameController.text);

      // 监听输入变化，当有内容的时候，显示尾部清除按钮，否则不显示
      if (_userNameController.text.isNotEmpty) {
        _isShowClear = true;
      } else {
        _isShowClear = false;
      }
      // 调用setState 方法,重新调用build 进行渲染界面
      setState(() {});
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    // //　返回按钮
    // Widget backImageArea = Container(
    //   height: 45,
    //   alignment: Alignment.centerLeft,
    //   margin: EdgeInsets.only(left: 0),
    //   child: IconButton(
    //     icon: ImageIcon(AssetImage("assets/imgs/back.png")),
    //     onPressed: (){
    //       print("返回------");
    //     },
    //   ),
    // );

    // //titleBar
    // Widget titleBar = Container(
    //
    // );

    // 文本输入框区域
    Widget inputTextArea = Container(
      margin: EdgeInsets.only(left: 20, right: 20),
      // 文本输入框区域的装饰
      decoration: BoxDecoration(
          borderRadius: BorderRadius.all(Radius.circular(8)),
          color: Colors.white),
      // 使用Form将两个输入框包起来做控制
      child: Form(
        key: _formKey,
        // Form里面是一个垂直布局
        child: Column(
          mainAxisSize: MainAxisSize.min,
          // 控件
          children: <Widget>[
            // 用户名
            TextFormField(
              controller: _userNameController,
              // 焦点控制
              focusNode: _focusNodeUserName,
              // 设置键盘类型
              keyboardType: TextInputType.number,
              // 输入框的装饰
              decoration: InputDecoration(
                  labelText: "用户名",
                  hintText: "请输入账号",
                  prefixIcon: Icon(Icons.person),
                  // 尾部添加清除按钮
                  suffixIcon: (_isShowClear)
                      ? IconButton(
                          icon: Icon(Icons.clear),
                          onPressed: () {
                            // 清空文本框的内容
                            _userNameController.clear();
                          })
                      : null),
              // 校验用户名
              validator: validateUserName,
              // 保持数据
              onSaved: (String value) {
                _username = value;
              },
            ),
            // 间隔
            SizedBox(height: 10),
            // 密码
            TextFormField(
              focusNode: _focusNodePassWord,
              decoration: InputDecoration(
                  labelText: "密码",
                  hintText: "请输入密码",
                  prefixIcon: Icon(Icons.lock),
                  // 是否显示密码
                  suffixIcon: IconButton(
                      icon: Icon(
                          _isShowPwd ? Icons.visibility : Icons.visibility_off),
                      onPressed: () {
                        setState(() {
                          _isShowPwd = !_isShowPwd;
                        });
                      })),
              obscureText: !_isShowPwd,
              // 校验密码
              // validator: validatePassWord,
              // 保存数据
              onSaved: (String value) {
                _password = value;
              },
            )
          ],
        ),
      ),
    );

    // 忘记密码
    // Widget forgetPwdArea = Container(
    //   margin: EdgeInsets.only(right: 20),
    //   child: Row(
    //     mainAxisSize: MainAxisSize.max,
    //     mainAxisAlignment: MainAxisAlignment.end,
    //     children: <Widget>[
    //       FlatButton(
    //           child: Text(
    //             "忘记密码？",
    //             style: TextStyle(color: Colors.black54),
    //           ),
    //           onPressed: () {
    //             print('找回密码');
    //           })
    //     ],
    //   ),
    // );

    // 登录按钮区域
    Widget loginButtonArea = Container(
      margin: EdgeInsets.only(left: 20, right: 20),
      height: 45,
      child: RaisedButton(
          color: Colors.blue[500],
          child: Text(
            "登录",
            style: TextStyle(color: Colors.white),
          ),
          // 设置圆角
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(10.0)),
          onPressed: () {
            // 点击登录按钮，解除焦点，回收键盘
            _focusNodeUserName.unfocus();
            _focusNodePassWord.unfocus();
            _formKey.currentState.save();
            print("username: $_username, password: $_password");
            if (_formKey.currentState.validate() && _username == "dev" && _password == "yuan") {
              // 输入验证通过
              // todo 等录接口相关操作
              print("登陆------");
              Map<String, dynamic> result = {'message': 'Back from flutter page >>> login'};
              DatabaseHelper.instance.insert(new UserInfo(_username, _password));
              DatabaseHelper.instance.queryAllRows().then((value) {
                print("database size: " + value.length.toString());
                if (value.length > 0) nativeChannel.invokeMethod('com.darrenyuen.webDAVClient/catalog', result);
              });
              // DatabaseHelper.instance.clearTable();
            } else {
              Fluttertoast.showToast(msg: "该用户不存在");
            }
          }),
    );

    Widget registerButtonArea = Container(
      margin: EdgeInsets.only(left: 20, right: 20),
      height: 45,
      child: ElevatedButton(
        // color: Colors.blue[500],
        child: Text(
          "注册",
          style: TextStyle(color: Colors.white),
        ),
        // shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10.0)),
        style: ButtonStyle(
          backgroundColor: MaterialStateProperty.resolveWith<Color>((Set<MaterialState> states) {
            return Colors.blue[500];
          }),
          shape: MaterialStateProperty.resolveWith<RoundedRectangleBorder>((Set<MaterialState> states) {
            return RoundedRectangleBorder(borderRadius: BorderRadius.circular(10.0));
          })
          // shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10.0)) as MaterialStateProperty
        ),
        onPressed: () {
          //点击注册按钮，接触焦点，收回键盘
          _focusNodeUserName.unfocus();
          _focusNodePassWord.unfocus();
          //路由到注册页面
          // RouteUtil.push(context, RegisterPage());
          // Navigator.push(context, MaterialPageRoute(builder: (context) => RegisterPage()));
          Fluttertoast.showToast(
              msg: "请联系开发者进行新用户配置",
              toastLength: Toast.LENGTH_SHORT,
              gravity: ToastGravity.CENTER,
              fontSize: 16);
          // print("jump to register page");
        },
      ),
    );

    // 组装widget组件，形成界面
    return MaterialApp(
        title: 'Login',
        theme: ThemeData(
          primarySwatch: Colors.blue,
        ),
        home: Scaffold(
            appBar: AppBar(
              title: Text('登录'),
              centerTitle: true,
            ),
            backgroundColor: Colors.white,
            // 外层添加一个手势，用于点击空白部分，回收键盘
            body: GestureDetector(
              onTap: () {
                // 点击空白区域，回收键盘
                _focusNodeUserName.unfocus();
                _focusNodePassWord.unfocus();
              },
              child: ListView(
                children: <Widget>[
                  // titleBar,
                  // backImageArea,
                  SizedBox(height: 60),
                  inputTextArea,
                  // forgetPwdArea,
                  SizedBox(height: 14),
                  loginButtonArea,
                  SizedBox(height: 14),
                  registerButtonArea
                ],
              ),
            )));
  }

  /// 销毁(类似于Android的onDestroy， 在执行Navigator.pop后会调用该办法， 表示组件已销毁；)
  @override
  void dispose() {
    // 移除焦点监听
    _focusNodeUserName.removeListener(_focusNodeListener);
    _focusNodePassWord.removeListener(_focusNodeListener);
    _userNameController.dispose();
    super.dispose();
  }

  // 监听焦点
  _focusNodeListener() async {
    if (_focusNodeUserName.hasFocus) {
      print("用户名框获取焦点");
      // 取消密码框的焦点状态
      _focusNodePassWord.unfocus();
    }

    if (_focusNodePassWord.hasFocus) {
      // 取消用户名框焦点状态
      _focusNodeUserName.unfocus();
    }
  }

  // 验证用户名
  String validateUserName(String value) {
    // 验证手机号
    if (value.isEmpty) {
      return "用户名不能为空！";
    } else {
      return null;
    }
  }

  // 验证密码
  String validatePassWord(String pwd) {
    // 正则验证密码
    RegExp exp = RegExp("[a-zA-Z0-9_]{6,20}");
    if (pwd.isEmpty) {
      return "密码不能为空";
    } else if (!exp.hasMatch(pwd)) {
      return "请输入正确的6-20位数字、字母或下划线的密码";
    } else {
      return null;
    }
  }
}
