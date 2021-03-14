import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import 'userInfo.dart';

class DatabaseHelper {

  static final _databaseName = "info.db";
  static final _databaseVersion = 1;
  static final table = 'UserInfo';
  static final columnAccount = 'account';
  static final columnPassword = 'password';

  DatabaseHelper._privateConstructor();
  static final DatabaseHelper instance = DatabaseHelper._privateConstructor();

  static Database _database;

  Future<Database> get database async {
    if(_database != null) return _database;
    _database = await _initDatabase();
    return _database;
  }

  _initDatabase() async {
     String path = join(await getDatabasesPath(), _databaseName);
     return await openDatabase(path, version: _databaseVersion, onCreate: _onCreate);
  }

  Future _onCreate(Database db, int version) async {
    String sql = "CREATE TABLE $table ($columnAccount VARCHAR(100) NOT NULL, $columnPassword VARCHAR(100) NOT NULL)";
     await db.execute(sql);
  }

  Future<int> insert(UserInfo userInfo) async {
     Database db = await instance.database;
     return await db.insert(table, userInfo.toMap());
  }

  Future<List<Map<String, dynamic>>> queryAllRows() async {
    Database db = await instance.database;
    return await db.query(table);
  }

  Future<void> clearTable() async {
    Database db = await instance.database;
    return await db.rawQuery("DELETE FROM $table");
  }

}