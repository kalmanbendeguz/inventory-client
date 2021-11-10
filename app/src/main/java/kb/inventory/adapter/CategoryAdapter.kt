package kb.inventory.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import kb.inventory.R
import kb.inventory.ViewCategoriesActivity
import kb.inventory.data.Category


class CategoryAdapter(var categories: MutableList<Category>):
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    //private val categories = mutableListOf<Category>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.categories_list_item, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.tvSubcategoryName.text = category.name

        holder.category = category

        holder.itemView.setOnClickListener(View.OnClickListener { v ->
            val intent = Intent(v.context, ViewCategoriesActivity::class.java).apply{
                putExtra("category_code", holder.category?.code )
            }
            v.context.startActivity(intent)
        })
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    /*interface ShoppingItemClickListener {
        fun onItemChanged(item: ShoppingItem)
    }*/

    inner class CategoryViewHolder(categoryView: View) : RecyclerView.ViewHolder(categoryView){
        val tvSubcategoryName: TextView
        val btnRenameCategory: ImageButton
        val btnViewCategoryItems: ImageButton

        var category: Category? = null

        init {
            tvSubcategoryName = categoryView.findViewById(R.id.tvSubcategoryName)
            btnRenameCategory = categoryView.findViewById(R.id.btnRenameCategory)
            btnViewCategoryItems = categoryView.findViewById(R.id.btnViewCategoryItems)

        }
    }
}