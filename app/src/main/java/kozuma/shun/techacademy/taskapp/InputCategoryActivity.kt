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
import io.realm.Sort
import kotlinx.android.synthetic.main.content_input.*

class InputCategoryActivity : AppCompatActivity() {

    private var mCategory: Category? = null

    var judge: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_category)


        done_category.setOnClickListener {
            addCategory()
            if(judge == true){
                errer.visibility = View.VISIBLE
            }else{
                val intent = Intent(this@InputCategoryActivity, InputActivity::class.java)
                startActivity(intent)
                finish()
            }


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

            //

        }

        val category = category_text.text.toString()

        val category_find = realm.where(Category::class.java).equalTo("name",category).findAll()
        Log.d("category_find", category_find.toString())


        if(category_find.toString().equals("[]")){
            mCategory!!.name = category
            realm.copyToRealmOrUpdate(mCategory!!)
            judge = false
        }else{

            judge = true
        }

        realm.commitTransaction()


        Log.d("Categoryid", mCategory!!.id.toString())
        Log.d("Category", mCategory!!.name)



        realm.close()

    }
}
