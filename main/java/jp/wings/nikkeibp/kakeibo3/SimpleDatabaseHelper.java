package jp.wings.nikkeibp.kakeibo3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleDatabaseHelper extends SQLiteOpenHelper{
    static final private String DBNAME = "kakeibo";
    static final private int VERSION = 1;

    //コンストラクタ―。DBを開くために必要。
    SimpleDatabaseHelper(Context context){
        super(context,DBNAME,null,VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
    }

    //データベース作成時にテーブルとテストデータを作成.
    @Override
    public void onCreate(SQLiteDatabase db){
        /*
        db.execSQL("CREATE TABLE books(" + "spendDate TEXT PRIMARY KEY,items TEXT,spendMoney INTEGER)");
        db.execSQL("INSERT INTO books(spendDate,items,spendMoney)"+"VALUES('2020年3月19日','php',3000)");
        db.execSQL("INSERT INTO books(spendDate,items,spendMoney)"+"VALUES('2020年3月24日','C',3500)");
        db.execSQL("INSERT INTO books(spendDate,items,spendMoney)"+"VALUES('2020年3月25日','Java',2680)");
        db.execSQL("INSERT INTO books(spendDate,items,spendMoney)"+"VALUES('2020年3月27日','Swift',2880)");
        db.execSQL("INSERT INTO books(spendDate,items,spendMoney)"+"VALUES('2020年3月20日','python',3200)");
        */
        ///*
        db.execSQL("CREATE TABLE books(" + "primNum INTEGER PRIMARY KEY,spendDate TEXT,items TEXT,spendMoney INTEGER)");
        db.execSQL("INSERT INTO books(primNum,spendDate,items,spendMoney)"+"VALUES(1,'2020年3月19日','php',3000)");
        db.execSQL("INSERT INTO books(primNum,spendDate,items,spendMoney)"+"VALUES(2,'2020年3月24日','C',3500)");
        db.execSQL("INSERT INTO books(primNum,spendDate,items,spendMoney)"+"VALUES(3,'2020年3月25日','Java',2680)");
        db.execSQL("INSERT INTO books(primNum,spendDate,items,spendMoney)"+"VALUES(4,'2020年3月27日','Swift',2880)");
        db.execSQL("INSERT INTO books(primNum,spendDate,items,spendMoney)"+"VALUES(5,'2020年3月20日','python',3200)");
        //*/

    }

    //データベースをアップデートした時、テーブルを再作成
    @Override
    public void onUpgrade(SQLiteDatabase db,int old_v,int new_v){
        db.execSQL("DROP TABLE IF EXISTS books");
        onCreate(db);
    }
}
