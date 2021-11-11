package kb.inventory.adapter

import android.content.Intent
import android.content.SharedPreferences
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kb.inventory.R
import kb.inventory.ViewAllItemsActivity
import kb.inventory.ViewItemActivity
import kb.inventory.data.Item


class ItemAdapter(var items: MutableList<Item>):
        RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    lateinit var sharedPref: SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView: View = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.items_list_item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]

        holder.tvItemName.text = item.name
        holder.tvItemQuantity.text = item.quantity.toString()

        holder.item = item

        holder.itemView.setOnClickListener(View.OnClickListener { v ->
            val intent = Intent(v.context, ViewItemActivity::class.java).apply{
                putExtra("itemCode", holder.item?.code )
            }
            v.context.startActivity(intent)
        })

    }

    override fun getItemCount(): Int {
        return items.size
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvItemName : TextView
        val tvItemQuantity : TextView

        var item: Item? = null

        init {
            tvItemName = itemView.findViewById(R.id.tvItemName)
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity)

        }
    }
}