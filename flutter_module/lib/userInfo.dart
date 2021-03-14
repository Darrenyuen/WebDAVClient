class UserInfo {

  String account;
  String password;

  UserInfo(
    this.account,
    this.password
  );

  Map<String, dynamic> toMap() => {'account': account, 'password': password};

  @override
  String toString() {
    return "account: $account, password: $password";
  }

}