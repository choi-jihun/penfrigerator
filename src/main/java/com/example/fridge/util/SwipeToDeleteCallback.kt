package com.example.fridge.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fridge.R
import com.example.fridge.src.fridge.FoodViewModel
import com.example.fridge.src.fridge.MainAdapter
import com.example.fridge.src.myrecipe.MyRecipeAdapter
import com.example.fridge.src.myrecipe.RecipeViewModel

class SwipeToDeleteCallback(
    context: Context,
    private val adapter: RecyclerView.Adapter<*>,
    private val foodViewModel: FoodViewModel? = null,
    private val recipeViewModel: RecipeViewModel? = null
) : ItemTouchHelper.Callback() {

    private val deleteIcon: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.baseline_delete_24)
    private val intrinsicWidth = deleteIcon?.intrinsicWidth ?: 0
    private val intrinsicHeight = deleteIcon?.intrinsicHeight ?: 0
    private val paint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val swipeFlags = ItemTouchHelper.LEFT
        return makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition

        when (adapter) {
            is MainAdapter -> {
                val food = adapter.currentList[position]
                foodViewModel?.deleteFood(food)
                adapter.removeItem(position)
            }

            is MyRecipeAdapter -> {
                val recipe = adapter.currentList[position]
                recipeViewModel?.deleteUserRecipe(recipe)
                adapter.removeItem(position)
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top

        if (dX < 0) {
            val cardView = viewHolder.itemView as androidx.cardview.widget.CardView
            val cornerRadius = cardView.radius

            val path = Path()
            path.moveTo(itemView.right + dX, itemView.top.toFloat())
            path.lineTo(itemView.right - cornerRadius, itemView.top.toFloat())
            path.arcTo(
                RectF(
                    itemView.right - 2 * cornerRadius,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.top + 2 * cornerRadius
                ), 270f, 90f
            )
            path.lineTo(itemView.right.toFloat(), itemView.bottom - cornerRadius)
            path.arcTo(
                RectF(
                    itemView.right - 2 * cornerRadius,
                    itemView.bottom - 2 * cornerRadius,
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                ), 0f, 90f
            )
            path.lineTo(itemView.right + dX, itemView.bottom.toFloat())
            path.close()

            c.drawPath(path, paint)

            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
            val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
            val deleteIconRight = itemView.right - deleteIconMargin
            val deleteIconBottom = deleteIconTop + intrinsicHeight

            deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon?.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
