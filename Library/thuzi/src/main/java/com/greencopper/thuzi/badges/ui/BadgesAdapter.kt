package com.greencopper.thuzi.badges.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.setShadowColor
import com.greencopper.thuzi.badges.BadgeViewData
import com.greencopper.thuzi.style.ThuziColor
import com.greencopper.thuzi.databinding.BadgesItemBinding
import com.greencopper.thuzi.style.ThuziTextStyle

internal class BadgesAdapter(val onBadgeClickListener: (BadgeViewData) -> Unit) :
    RecyclerView.Adapter<BadgesAdapter.ViewHolder>() {

    private val badges: MutableList<BadgeViewData> = mutableListOf()

    class ViewHolder(private val binding: BadgesItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(badgeViewData: BadgeViewData) {
            with(binding.textView) {
                if (badgeViewData.isEarned) {
                    setTextColor(ThuziColor.badges.card.label.unlocked)
                    setFont(ThuziTextStyle.badges.card.label.unlocked)
                } else {
                    setTextColor(ThuziColor.badges.card.label.locked)
                    setFont(ThuziTextStyle.badges.card.label.locked)
                }
                text = badgeViewData.name
            }
            badgeViewData.image?.let {
                binding.shapeableImageView.setImageDrawable(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BadgesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val cardColors = ThuziColor.badges.card
        binding.root.apply {
            setCardBackgroundColor(cardColors.background)
            setShadowColor(ThuziColor.badges.card.shadow)
            strokeColor = ThuziColor.badges.card.border
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val badgeViewData = badges[position]
        holder.bind(badgeViewData)
        holder.itemView.setOnSafeClickListener {
            onBadgeClickListener(badgeViewData)
        }
    }

    override fun getItemCount(): Int = badges.size
    fun setBadges(badges: List<BadgeViewData>) {
        this.badges.clear()
        this.badges.addAll(badges)
        notifyDataSetChanged()
    }
}
