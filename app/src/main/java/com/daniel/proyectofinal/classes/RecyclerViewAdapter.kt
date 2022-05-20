package com.daniel.proyectofinal.classes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList


open class RecyclerViewAdapter<T>(
  @LayoutRes private val itemLayout: Int,
  private val list: ArrayList<T> = arrayListOf(),
): RecyclerView.Adapter<RecyclerViewAdapter<T>.ViewHolder>() {

  private var onItemCLickListener = OnItemClickListener<T> { _, _, _ -> }
  private var onItemLongCLickListener = OnItemLongClickListener<T> { _, _, _ -> false }
  private var onBindViewHolderListener = OnBindViewHolderListener<T> { _, _, _ -> }

  fun interface OnItemClickListener<T> {
    fun onItemClick(v: View?, selectedItem: T, position: Int)
  }

  fun interface OnItemLongClickListener<T> {
    fun onItemLongClick(v: View?, selectedItem: T, position: Int): Boolean
  }

  fun interface OnBindViewHolderListener<T> {
    fun onBindViewHolder(view: View, item: T, position: Int)
  }

  fun clearList() {
    list.clear()
    notifyDataSetChanged()
  }

  fun replaceList(newList: List<T>) {
    this.list.clear()
    this.list.addAll(newList)
    notifyDataSetChanged()
  }

  fun addItem(item: T) {
    list.add(item)
    notifyItemInserted(itemCount)
  }

  fun insertItem(position: Int, item: T) {
    list.add(position, item)
    notifyItemInserted(itemCount)
  }

  fun insertItems(insertPosition: Int, items: List<T>) {
    list.addAll(insertPosition, items)
    notifyItemRangeInserted(insertPosition, items.size)
  }

  fun updateItem(position: Int, updatedItem: T) {
    list[position] = updatedItem
    notifyItemChanged(position)
  }

  fun moveItem(fromPosition: Int, toPosition: Int, item: T) {
    list.removeAt(fromPosition)
    list.add(toPosition, item)
    notifyItemMoved(fromPosition, toPosition)
  }

  /**
   * @return False if the list doesn't contains the item.
   */
  fun removeItem(item: T): Boolean {
    if (list.contains(item))
    {
      val position = list.indexOf(item)

      list.remove(item)
      notifyItemRemoved(position)

      return true
    }

    return false
  }

  fun removeItem(position: Int) {
    list.removeAt(position)
    notifyItemRemoved(position)
  }

  fun removeItems(fromPosition: Int, count: Int) {
    val toPosition = fromPosition + count

    list.subList(fromPosition, toPosition).clear()
    notifyItemRangeRemoved(fromPosition, toPosition)
  }

  fun setOnItemClickListener(listener: OnItemClickListener<T>) {
    onItemCLickListener = listener
  }

  fun setOnItemLongClickListener(listener: OnItemLongClickListener<T>) {
    onItemLongCLickListener = listener
  }

  fun setOnBindViewHolderListener(listener: OnBindViewHolderListener<T>) {
    onBindViewHolderListener = listener
  }

  //region RecyclerView.Adapter

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)

    val view = layoutInflater.inflate(itemLayout, parent, false)

    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = list[position]

    holder.itemView.setOnClickListener(holder)
    holder.itemView.setOnLongClickListener(holder)

    onBindViewHolderListener.onBindViewHolder(holder.itemView, item, position)
  }

  override fun getItemCount(): Int = list.size

  //endregion

  inner class ViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
    override fun onClick(v: View?) {
      onItemCLickListener.onItemClick(v, list[layoutPosition], layoutPosition)
    }

    override fun onLongClick(v: View?): Boolean {
      return onItemLongCLickListener.onItemLongClick(v, list[layoutPosition], layoutPosition)
    }
  }
}
