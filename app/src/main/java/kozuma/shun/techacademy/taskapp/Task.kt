package kozuma.shun.techacademy.taskapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*



open class Task: RealmObject(), Serializable {
    var title: String = ""
    var contents: String = ""
    var date: Date = Date()
    var categoryId: Int = Category().id

    //idをプライマーキーとして設定
    @PrimaryKey
    var id: Int = 0
}
