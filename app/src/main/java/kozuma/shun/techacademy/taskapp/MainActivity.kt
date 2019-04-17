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
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*



const val EXTRA_TASK = "kozuma.shun.techacademy.taskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm>{
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

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

        reloadListView()

        find.setOnClickListener {
            reloadListView()
        }

    }



    private fun reloadListView(){
        //後でTaskクラスに変更する
        //val taskList = mutableListOf("aaa","bbb","ccc")

       //if(category_find_text.text.toString().equals("")){
            //Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
            val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

            //上記の結果をTaskListとしてセットする
            mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

            //mTaskAdapter.taskList = taskList

            //TaskのListView用のアダプタに渡す
            listView1.adapter = mTaskAdapter

            //表示を更新するために、アダプターにデータが変更されたことを知らせる
            mTaskAdapter.notifyDataSetChanged()
        /*}else{
            //Realmデータベースから、/ 条件指定して取得の場合は
            val categoryRealmResults = mRealm.where(Task::class.java).equalTo("category", category_find_text.text.toString()).findAll().sort("date", Sort.DESCENDING)

            Log.d("cate", category_find_text.text.toString())
            //上記の結果をTaskListとしてセットする
            mTaskAdapter.taskList = mRealm.copyFromRealm(categoryRealmResults)

            //mTaskAdapter.taskList = taskList

            //TaskのListView用のアダプタに渡す
            listView1.adapter = mTaskAdapter

            //表示を更新するために、アダプターにデータが変更されたことを知らせる
            mTaskAdapter.notifyDataSetChanged()
        }*/



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
