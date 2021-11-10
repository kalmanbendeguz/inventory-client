package kb.inventory.adapter

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import kb.inventory.R
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