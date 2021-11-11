package kb.inventory.adapter

import android.content.Context
import android.content.Intent
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kb.inventory.R
import kb.inventory.ViewAllItemsActivity
import kb.inventory.ViewCategoriesActivity
import kb.inventory.data.Category
import org.json.JSONObject
import java.nio.charset.Charset


class CategoryAdapter(var categories: MutableList<Category>, var categoryPath: String):
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
                putExtra("category_path", categoryPath + "/" +holder.category?.name)
            }
            v.context.startActivity(intent)
        })

        holder.btnViewCategoryItems.setOnClickListener (View.OnClickListener{ v ->
            val intent = Intent(v.context, ViewAllItemsActivity::class.java).apply{
                Log.v("mylog","holder category code")
                Log.v("mylog",holder.category?.code!!)
                putExtra("category_id", holder.category?.code )
            }
            v.context.startActivity(intent)
        })

        holder.btnRenameCategory.setOnClickListener(View.OnClickListener { v ->
            val renameCategoryDialog = AlertDialog.Builder(v.context)
            renameCategoryDialog.setTitle("Kategória átnevezése")

            var oldCategoryName = holder.tvSubcategoryName.text.toString()

            val newCategoryNameInput = EditText(v.context)
            newCategoryNameInput.inputType = InputType.TYPE_CLASS_TEXT
            newCategoryNameInput.setText(oldCategoryName)

            renameCategoryDialog.setView(newCategoryNameInput)

            renameCategoryDialog.setPositiveButton("OK") { dialogInterface, i ->
                Log.v("mylog", "ok button")
                val newCategoryName = newCategoryNameInput.text.toString()

                if(oldCategoryName == newCategoryName) {
                    Toast.makeText(v.context, "Nem változott!", Toast.LENGTH_SHORT).show()
                } else {
                    val queue = Volley.newRequestQueue(v.context)
                    val url = "http://192.168.137.1:3000/category/rename"

                    //val requestBody = "code="+ itemCode + "&quantity="
                    val reqMap: MutableMap<Any?, Any?> = mutableMapOf()
                    reqMap["category_id"] = holder.category?.code
                    reqMap["new_name"] = newCategoryName

                    val reqBody : JSONObject = JSONObject(reqMap)

                    Log.v("mylog", reqBody.toString())
                    Log.v("mylog", reqBody.toString().toByteArray(Charset.defaultCharset()).toString())

                    val stringReq : StringRequest =
                        object : StringRequest(
                            Method.POST, url,
                            Response.Listener { response ->
                                // response
                                var strResp = response.toString()
                                Log.v("mylog", "RESP:" +"["+strResp+"]")
                                Log.d("API", strResp)
                                holder.tvSubcategoryName.text = newCategoryName
                                Toast.makeText(v.context, "Sikeres átnevezés", Toast.LENGTH_SHORT).show()

                            },
                            Response.ErrorListener { error ->
                                Log.d("API", "error => $error")
                            }
                        ){
                            override fun getBody(): ByteArray {
                                return reqBody.toString().toByteArray(Charset.defaultCharset())
                            }
                        }
                    queue.add(stringReq)
                }

            }

            renameCategoryDialog.setNegativeButton("Mégse") { dialogInterface, i -> dialogInterface.cancel() }
            renameCategoryDialog.show()
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