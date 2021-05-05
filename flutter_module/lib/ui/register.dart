import 'package:flutter/material.dart';

class RegisterPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return new RegisterPageState();
  }

}

class RegisterPageState extends State<RegisterPage> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Register',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: Text('注册'),
          centerTitle: true,
        ),
        backgroundColor: Colors.white,
        body: GestureDetector(
          onTap: () {

          },
          child: ListView(),
        ),
      ),
    );
  }

}