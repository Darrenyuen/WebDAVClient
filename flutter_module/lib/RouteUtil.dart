import 'package:flutter/cupertino.dart';

class RouteUtil {
  static void push(BuildContext context, Widget page) async {
    if (context == null || page == null) return;
    await Navigator.push(context, new CupertinoPageRoute<void>(builder: (context) => page));
  }
}