package kozuma.shun.techacademy.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.content_input.*


const val EXTRA_TASK = "kozuma.shun.techacademy.taskapp.TASK"

class MainActivity : AppCompatActivity(){

    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm>{
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onResume() {
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener{ view ->
            //Snackbar.make(view,"Replace with your own action", Snackbar.LENGTH_LONG)
             //   .setAction("Action", null).show()
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }

        //Realmの設定

        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        //ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        //ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            //入力・編集する画面に遷移する
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)

        }

        //ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            //タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        //アプリ起動時に表示テスト用のタスクを作成する
        //addTaskForTest()

        categoryView()
        reloadListView()

        /*find.setOnClickListener {
            reloadListView()
        }
        */

        category_spinner_search.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            //　アイテムが選択された時
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reloadListView()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }


    private fun categoryView(){


        //read関数でカテゴリデータを全て呼び出し
        val getCategoryData = mRealm.where(Category::class.java).findAll()

        //name用の配列を作成
        val categoryname = mutableListOf<String>()

        categoryname.add("全てを表示")

        //回してnameを配列に格納
        getCategoryData.forEach {
            Log.d("debug", "id :" + it.id.toString() + " name :" + it.name )
            categoryname.add(it.name)
        }

        // ArrayAdapter
        val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, categoryname)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // spinner に adapter をセット
        // Kotlin Android Extensions
        category_spinner_search.adapter = adapter

    }



    private fun reloadListView(){
        //後でTaskクラスに変更する
        //val taskList = mutableListOf("aaa","bbb","ccc")

        if(category_spinner_search.selectedItem.toString().equals("全てを表示")){
            Log.d("list", category_spinner_search.selectedItemId.toString() )

            //Realmデータベースから、/ 条件指定して取得の場合は
            val categoryRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

            //Log.d("cate", category_find_text.text.toString())
            //上記の結果をTaskListとしてセットする
            mTaskAdapter.taskList = mRealm.copyFromRealm(categoryRealmResults)

            //mTaskAdapter.taskList = taskList

            //TaskのListView用のアダプタに渡す
            listView1.adapter = mTaskAdapter

            //表示を更新するために、アダプターにデータが変更されたことを知らせる
            mTaskAdapter.notifyDataSetChanged()
        }else{
            //Realmデータベースから、/ 条件指定して取得の場合は
            Log.d("list", category_spinner_search.selectedItemId.toString())

            val categoryRealmResults = mRealm.where(Task::class.java).equalTo("categoryId", category_spinner_search.selectedItemId).findAll().sort("date", Sort.DESCENDING)



            //Log.d("cate", category_find_text.text.toString())
            //上記の結果をTaskListとしてセットする
            mTaskAdapter.taskList = mRealm.copyFromRealm(categoryRealmResults)

            //mTaskAdapter.taskList = taskList

            //TaskのListView用のアダプタに渡す
            listView1.adapter = mTaskAdapter

            //表示を更新するために、アダプターにデータが変更されたことを知らせる
            mTaskAdapter.notifyDataSetChanged()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }

    /*private fun addTaskForTest(){
        val task = Task()
        task.title = "作業"
        task.contents = "プログラムを書いてPUSHする"
        task.date = Date()
        task.id = 0
        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(task)
        mRealm.commitTransaction()

    }
    */
}
