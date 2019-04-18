package kozuma.shun.techacademy.taskapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_input_category.*
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import kotlinx.android.synthetic.main.content_input.*

class InputCategoryActivity : AppCompatActivity() {

    private var mCategory: Category? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_category)


        done_category.setOnClickListener {
            addCategory()
            val intent = Intent(this@InputCategoryActivity, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun addCategory(){
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        if (mCategory == null) {
            // 新規作成の場合
            mCategory = Category()

            val taskRealmResults = realm.where(Category::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mCategory!!.id = identifier

        }

        val category = category_text.text.toString()
        mCategory!!.name = category

        realm.copyToRealmOrUpdate(mCategory!!)
        realm.commitTransaction()

        Log.d("Categoryid", mCategory!!.id.toString())
        Log.d("Category", mCategory!!.name)

        realm.close()


    }
}
