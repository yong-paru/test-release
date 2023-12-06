package com.example.finalexam3

import android.content.ClipDescription
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QueryDocumentSnapshot

data class Item(
    val id: String,
    val name: String,
    val price: Int,
    val userEmail: String,
    val sold: Boolean, // Boolean 타입으로 변경
    val description: String
) {
    constructor(doc: QueryDocumentSnapshot) : this(
        doc.id,
        doc["name"].toString(),
        doc["price"].toString().toIntOrNull() ?: 0,
        doc["userEmail"].toString(),
        doc["sold"] as? Boolean ?: false, // 문자열을 Boolean으로 안전하게 변환
        doc["description"].toString()
    )

    constructor(key: String, map: Map<*, *>) : this(
        key,
        map["name"].toString(),
        map["price"].toString().toIntOrNull() ?: 0,
        map["userEmail"].toString(),
        map["sold"] as? Boolean ?: false, // 문자열을 Boolean으로 안전하게 변환
        map["description"].toString()
    )
}

class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

class MyAdapter(private val context: Context, private var items: List<Item>)
    : RecyclerView.Adapter<MyViewHolder>() {

    // 아이템 클릭 시 호출할 콜백 정의
    var onItemClick: ((Item) -> Unit)? = null

    fun interface OnItemClickListener {
        fun onItemClick(student_id: String)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun updateList(newList: List<Item>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]
        with(holder.view) {
            findViewById<TextView>(R.id.textID).text = item.userEmail
            findViewById<TextView>(R.id.textName).text = item.name
            findViewById<TextView>(R.id.textPrice).text = item.price.toString()
            findViewById<TextView>(R.id.textDescription).text = item.description

            // 판매 상태를 "판매중" 또는 "판매완료"로 설정
            val soldStatusText = if (item.sold) "판매완료" else "판매중"
            findViewById<TextView>(R.id.textSaleStatus).text = soldStatusText

            // 클릭 리스너 설정
            setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    override fun getItemCount() = items.size
}