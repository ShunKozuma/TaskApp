package kozuma.shun.techacademy.taskapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class Category: RealmObject(), Serializable {
    var name: String = "全選択"

    //idをプライマーキーとして設定
    @PrimaryKey
    var id: Int = 1



}

