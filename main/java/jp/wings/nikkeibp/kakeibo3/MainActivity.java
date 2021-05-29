/*============================================================議事録============================================================
 *　題名:家計簿アプリ
 *　内容:①日時・項目名・金額を入力する
 *  　　 ②データベースに追加するアプリ。
 *　デザイン:基本的には解答用紙アプリの配置を流用する。
 *　　　　　 主な変更点としては、開始、終了、保存ボタンを、保存、削除、検索にする。
 * 2020/05/06追記.2020/05/08追記
 * 方針:改造をするときはコメントで全部追加してから最後にコメントを外す
 *      変数を宣言するときは配列、変数の順.
 * 　　 できるだけ変数を宣言する(コードの中でできるだけ宣言しない)
 *
 *
 * 2020/04/14 10h
 *  SimpleDatabaseHelperクラスを追加.エラーが出たのでマニュフェストファイルにSimpleDatabaseHelperのactivityを追加した。
 *  Layoutを元の解答用紙アプリから改造するよりもサンプルソースコードを書いてそれを改変するほうがミスも少ないと思うのでそうする。
 * (『はじめてのAndroidアプリ開発 第3版 Android Studio 3対応』p473参照)
 * 2020/04/20 2h
 *  検索条件に応じてデータベースからその列の値を取りだすメソッドにうまく修正しました。
 * 2020/05/02 3h
 *  リストから検索条件を3つの中から選べる機能を追加
 * 2020/05/03
 *  参照先のDBが変わらない不具合を発見。わからない。。。
 * 2020/05/05 4h
 *  同名の新規クラスを作ってソースコードをそのままコピペしたら治った。ok
 *  削除と検索で条件をセットするところに不足していた部分があったので修正 ok
 *  最初に検索画面をタッチしていない状態で検索ボタンを押したときに落ちるバグを修正 ok
 *  日時は実質指定できないので指定できるようにTextViewからEdittextに変更するok
 *  色を登録するok
 *  →colors.xmlクラスとstyles.xmlに色を登録してmainActivityで使おうとしたらなんかできなかった。
 *  リソースに登録しようとしたがstrings.xmltの時みたいに登録ボタン？がわからなかったので断念。
 *  Edittextにあらかじめ何が表示されるかを表示しておくok
 *  色とかデザインを工夫しようとしたけど、なんか文字が細くて微妙な出来になった。
 * 2020/05/06 6h
 *  ソースコードの方針を追加ok
 *  デザインを工夫ok
 *  データが重複してる場合にトーストする機能を追加ok
 *  →主キーを作ってなかったからデータベースに追加ok
 *      →主番号のウィジェットを追加ok
 *  入力フォームに影文字を表示させるやつを追加(android:hint)ok
 *  activity_main.xmlで頻繁に使うやつを上にした。ok
 *  主番号追加したけど、登録するときにデータベースにある主番号にない番号をつけるようにする処理を記述する
 * 2020/05/07
 *  getCountメソッドについて、行数をちゃんと格納する、格納したデータの扱い方を調べて保存ボタンと検索処理について調べる
 *  主番号を勝手につけられないようにする
 * 2020/05/14
 *  検索ボタンを押すと落ちるバグが発生。よくわからない
 *
 */

package jp.wings.nikkeibp.kakeibo3;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    //配列
    final java.lang.String[] selQueryOption = {"primNum = ?","spendDate = ?","items = ?","spendMoney = ?"};

    //データベースクラスを使うための変数
    private SimpleDatabaseHelper helper = null;

    //『入力用』主番号、時刻、項目名、金額
    private EditText txtPrimNum = null;
    private EditText txtDate = null;
    private EditText txtItems = null;
    private EditText txtSpendMoney = null;

    //『表示用』主番号、時刻、項目名、金額
    private EditText printPrimNum = null;
    private EditText printTimeText = null;
    private EditText printItems = null;
    private EditText printMoney = null;

    //ListViewの選択に応じた値を入れる変数.
    private java.lang.String selOption = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ヘルパーを準備
        helper = new SimpleDatabaseHelper(this);

        //主番号、日付、項目名、金額
        txtPrimNum = findViewById(R.id.editTextPrimNum);
        txtDate = findViewById(R.id.editTextDatePrint);
        txtItems = findViewById(R.id.editTextInputTitle);
        txtSpendMoney = findViewById(R.id.editTextInputQuestion);

        printTime();        //アプリ起動時に現在時刻をセット
        listCreate();       //アプリ起動時にlistを生成
    }
    //終了ボタンを押したときにアプリケーションを終了します。
    //public void onButtonClickFinish(View view){
    //    System.exit(0);
    //}

    //保存ボタンを押したときに入力されたデータをデータベースに保存します。
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onButtonClickReserve(View view) {
        //条件式に必要な配列を記述
        final java.lang.String[] cols = {"primNum","spendDate", "items", "spendMoney"};
        final String[] params = {txtPrimNum.getText().toString(),txtDate.getText().toString(),txtItems.getText().toString(),txtSpendMoney.getText().toString()};
        //int lineCounter = 0;                    //行数をカウントする変数
        //int primNumSetter = lineCounter + 1;//データを新たに保存するときに主番号をつける変数

        //データベースの行数をカウントして、カウントした数より1大きい値を主番号に登録するようにする
        //検索条件のselOptionに関して調べるところ
        /*try (SQLiteDatabase db = helper.getReadableDatabase();
             //==========検索条件→テーブル名/取得する列(配列)/条件式/条件値(配列)/グループ化/グループ絞り込み条件/ソート式/LIMIT句
             Cursor cs = db.query("books", cols, selOption, params, null, null, null, null)) {
            // getCountで取得したデータの格納の仕方がわからない.この書き方だと全部の行数を取得できないようになりそう。
            // 主番号を勝手につけられないようにする。
            lineCounter = cs.getCount();
        }
         */
        //主番号とかにnullがあると登録できないようにする
        //if(!(txtPrimNum.getText().toString()).equals("")||!(txtDate.getText().toString()).equals("")||
                //!(txtItems.getText().toString()).equals("")||!(txtSpendMoney.getText().toString()).equals("")) {
            try (SQLiteDatabase db = helper.getWritableDatabase()) {
                ContentValues cv = new ContentValues();
                cv.put("primNum", txtPrimNum.getText().toString());//ここも修正する
                cv.put("spendDate", txtDate.getText().toString());
                cv.put("items", txtItems.getText().toString());
                cv.put("spendMoney", txtSpendMoney.getText().toString());
                //重複したデータがあったときに該当の行毎更新する。重複時に重複した時を知らせるときは
                db.insertWithOnConflict("books", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                Toast.makeText(this, "データの登録に成功しました。", Toast.LENGTH_SHORT).show();
            }
        //}else{
            //Toast.makeText(this, "空白以外のデータを登録してください。", Toast.LENGTH_SHORT).show();
        //}
    }

    //データベースのデータを削除する。
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onButtonClickDelete(View view) {
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            final String[] params = {txtPrimNum.getText().toString(),txtDate.getText().toString(),txtItems.getText().toString(),txtSpendMoney.getText().toString()};
            db.delete("books", selOption, params);
            Toast.makeText(this, "データの削除に成功しました。", Toast.LENGTH_SHORT).show();
        }
    }

    //検索ボタンを押した時に呼び出されるコード。
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onButtonClickQuery(View view) {
        //条件式に必要な配列を記述
        final String[] cols = {"primNum","spendDate", "items", "spendMoney"};
        final String[] params = {txtPrimNum.getText().toString(),txtDate.getText().toString(),txtItems.getText().toString(),txtSpendMoney.getText().toString()};

        //selOptionが画面上で選択されていないときにそれを知らせる
        if(!selOption.equals("")) {
            //検索条件のselOptionに関して調べるところ
            try (SQLiteDatabase db = helper.getReadableDatabase();
                 //==========検索条件→テーブル名/取得する列(配列)/条件式/条件値(配列)/グループ化/グループ絞り込み条件/ソート式/LIMIT句
                 Cursor cs = db.query("books", cols, selOption, params, null, null, null, null)) {
                /*重複していた場合はデータを表示しないでトーストを表示する
                    ⓪クエリで重複してる場合も含めて条件を設定して検索して結果セットを作る
                    ①結果セットの行数を数える
                    ②①が2つ以上あったらトーストする。
                    ③②があったら表示しないようにする
                 */
                //2020/05/07　
                // getCountメソッドについて、行数をちゃんと格納する、格納したデータの扱い方を調べて保存ボタンと検索処理について調べる
                if (cs.moveToFirst()) {
                    //検索条件をもとに選んだ2次元結果テーブルの行のカラムを
                    EditText et0 = findViewById(R.id.printPrimNum);
                    et0.setText(cs.getString(0));
                    EditText et1 = findViewById(R.id.printTimeText);
                    et1.setText(cs.getString(1));
                    EditText et2 = findViewById(R.id.printItems);
                    et2.setText(cs.getString(2));
                    EditText et3 = findViewById(R.id.printMoney);
                    et3.setText(cs.getString(3));
                    Toast.makeText(this, "データがありました。", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "データが存在しません。", Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            Toast.makeText(this, "検索条件を指定してください。", Toast.LENGTH_SHORT).show();
        }
    }


    /*表示ボタンを押したらデータベースの中身を表示する。
    public void onButtonClickPrint(View view) {
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            Toast.makeText(this, "データの表示に成功しました。", Toast.LENGTH_SHORT).show();
        }
    }
    */

    //時刻表示するコードを追加
    public void printTime() {
        Calendar cal = Calendar.getInstance();

        int iYear = cal.get(Calendar.YEAR);     //年取得
        int iMonth = cal.get(Calendar.MONTH);   //月取得
        int iDate = cal.get(Calendar.DATE);     //日取得
        //int iHour = cal.get(Calendar.HOUR);     //時取得
        //int iMinute = cal.get(Calendar.MINUTE); //分取得

        String strDay = iYear + "年" + iMonth + "月" + iDate + "日";     //日付を表示形式で設定
        //String strTime = iHour + "時" + iMinute + "分";                  //時刻を表示形式で設定

        EditText et = findViewById(R.id.editTextDatePrint);
        et.setText(strDay);
    }

    //検索条件を指定するコードを追加.onButtonClickQueryで使う.
    public String selQueryOption(CharSequence msg){
        String selOptionName = "";

        //項目名に応じて数字をセット。下のやつは短縮版。
        //msg == "主番号" ? selNumber = 0:msg == "日時" ? selNumber = 1:msg == "項目名" ? selNumber = 2:msg == "金額"? selNumber = 3: Toast.makeText(this, "データがありません。", Toast.LENGTH_SHORT).show();
        if(msg == "主番号"){
            selOptionName = selQueryOption[0];
        }else if(msg == "日時"){
            selOptionName = selQueryOption[1];
        }else if(msg == "項目名"){
            selOptionName = selQueryOption[2];
        }else if(msg == "金額"){
            selOptionName = selQueryOption[3];
        }else{
            Toast.makeText(this, "データがありません。", Toast.LENGTH_SHORT).show();
        }
        return selOptionName;
    }

    //リスト項目の呼び出し関数
    public void listCreate(){
        ArrayAdapter<String> adapter;
        ListView list;

        //検索条件を選択できるリスト項目をArraylistとして準備
        final ArrayList<String> data = new ArrayList<>();
        data.add("主番号");
        data.add("日時");
        data.add("項目名");
        data.add("金額");

        //配列アダプターを作成＆ListViewに登録.simple_list_item_single_choiceを変えるとウィジェットがる。
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_single_choice,data);
        list = findViewById(R.id.findList);
        list.setAdapter(adapter);

        //リストをクリックした時の処理を記述。
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //項目をタッチしたときにトーストを表示
                CharSequence msg = ((TextView) view).getText();
                Toast.makeText(MainActivity.this,String.format("選択したのは%s",msg.toString()),Toast.LENGTH_SHORT).show();
                selOption = selQueryOption(msg);//グローバル変数にセット
            }
        });
    }
}
