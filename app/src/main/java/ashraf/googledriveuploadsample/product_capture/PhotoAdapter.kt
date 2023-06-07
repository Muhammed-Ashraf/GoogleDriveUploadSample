package ashraf.googledriveuploadsample.product_capture

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ashraf.googledriveuploadsample.databinding.ItemPhotoBinding

class PhotoAdapter() : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    private val data = ArrayList<Bitmap>(emptyList())

    class PhotoViewHolder(val binding: ItemPhotoBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.binding.photoImageView.setImageBitmap(data[position])
    }

    fun setData(list: ArrayList<Bitmap>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

}