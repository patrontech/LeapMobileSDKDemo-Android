package com.greencopper.ticketing.ticketsscan.ui

import android.graphics.drawable.AnimatedVectorDrawable
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.interfacekit.ui.setShadowColor
import com.greencopper.ticketing.TicketingColor
import com.greencopper.ticketing.databinding.TicketsScanItemBinding
import com.greencopper.ticketing.models.Ticket
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.d

public class TicketsScanListAdapter :
    RecyclerView.Adapter<TicketsScanListAdapter.TicketsScanViewHolder>() {

    private var ticketsItems = emptyList<Ticket>()

    init {
        setHasStableIds(true)
    }

    public inner class TicketsScanViewHolder(
        private val ticketsScanBinding: TicketsScanItemBinding
    ) : RecyclerView.ViewHolder(ticketsScanBinding.root) {
        internal fun bind(ticket: Ticket) {
            with(ticketsScanBinding) {

                if (ticket.primarySubtitle == null) {
                    primaryInfoCardTitleOneLine.text = ticket.primaryTitle
                    primaryInfoCardTwoLines.visibility = View.INVISIBLE
                    primaryInfoCardTitleOneLine.visibility = View.VISIBLE
                } else {
                    primaryInfoCardTitle.text = ticket.primaryTitle
                    primaryInfoCardSubtitle.text = ticket.primarySubtitle
                    primaryInfoCardTitleOneLine.visibility = View.GONE
                    primaryInfoCardTwoLines.visibility = View.VISIBLE
                }

                qrCodeView.setBarcodeValue(ticket.qrCode)
                (qrCodeContainer.foreground as? AnimatedVectorDrawable)?.start()
                codeNumber.text = ticket.qrCode

                ticket.secondaryTitle?.let {
                    secondaryInfoCard.visibility = View.VISIBLE
                    secondaryInfoCardTitle.text = it
                } ?: run { secondaryInfoCard.visibility = View.GONE }

                val ticketColor = TicketingColor.ticketsScan.tickets.ticket

                qrCodeCard.apply {
                    strokeColor = ticketColor.qrCodeCard.border
                    setShadowColor(ticketColor.qrCodeCard.shadow)
                }

                primaryInfoCard.strokeColor = ticketColor.secondaryInfoCard.border
                secondaryInfoCard.strokeColor = ticketColor.primaryInfoCard.border
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketsScanViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TicketsScanItemBinding.inflate(inflater, parent, false)
        return TicketsScanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketsScanViewHolder, position: Int) {
        val current: Ticket = ticketsItems[position]
        holder.bind(current)
    }

    public fun setTicketItems(ticketsListItems: List<Ticket>) {
        this.ticketsItems = ticketsListItems
        notifyDataSetChanged()
        App.log.d("Content Activity list updated with ${ticketsListItems.size} items.")
    }

    override fun getItemCount(): Int = ticketsItems.size

    override fun getItemId(position: Int): Long =
        ticketsItems[position].hashCode().toLong()
}
