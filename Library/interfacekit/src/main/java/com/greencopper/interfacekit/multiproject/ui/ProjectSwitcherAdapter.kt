package com.greencopper.interfacekit.multiproject.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.CellProjectSelectionBinding
import com.greencopper.interfacekit.databinding.ProjectSwitcherHeaderItemBinding
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.toolkit.App
import com.greencopper.toolkit.extensions.getFormattedDateTime
import com.greencopper.toolkit.logging.e
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.FormatStyle

internal class ProjectSwitcherAdapter(
    private val origin: Layout,
    private val itemProvider: ItemProvider,
    private val localizationService: LocalizationService,
    private val metricsService: AggregateMetricsService,
    private val analyticsScreenName: String,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = itemProvider.items.size

    override fun getItemViewType(position: Int): Int =
        if (itemProvider.items[position] is HeaderItem) {
            HEADER_VIEW_TYPE
        } else {
            PROJECT_VIEW_TYPE
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            HEADER_VIEW_TYPE -> HeaderViewHolder(
                ProjectSwitcherHeaderItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            PROJECT_VIEW_TYPE -> ProjectViewHolder(
                CellProjectSelectionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("View type $viewType not recognized.")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is HeaderViewHolder -> holder.bindView(itemProvider.items.first() as HeaderItem)
        is ProjectViewHolder -> holder.bindView(itemProvider.items[position] as ProjectItem)
        else -> throw IllegalArgumentException("View holder $holder not recognized.")
    }

    override fun getItemId(position: Int): Long =
        itemProvider.items[position].hashCode().toLong()

    private fun selectProject(project: ProjectItem) {
        metricsService.track(ProjectSwitcherProjectTapEvent(project, analyticsScreenName))

        itemProvider.selectedItemId.let { selectedItemId ->
            notifyItemChanged(
                itemProvider.items.indexOfFirst {
                    selectedItemId == (it as? ProjectItem)?.id
                },
                project
            ) //Passing in project as payload prevents entire cell from animating the change
        }

        val newIndex = itemProvider.items.indexOf(project)
        itemProvider.selectedItemId = project.id
        notifyItemChanged(newIndex)
    }

    inner class HeaderViewHolder(private val binding: ProjectSwitcherHeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(header: HeaderItem) {
            itemView.isClickable = false
            val colors = InterfaceKitColor.projectSwitcher
            val textStyle = InterfaceKitTextStyle.projectSwitcher
            with(binding.tvMultiProjectSwitcherTitle) {
                text = localizationService.getString(header.title)
                setTextColor(colors.title)
                setFont(textStyle.title)
            }

            with(binding.tvMultiProjectSwitcherSubtitle) {
                text = header.subtitle?.let {
                    localizationService.getString(it)
                }
                setTextColor(colors.subtitle)
                setFont(textStyle.subtitle)
            }
        }
    }

    inner class ProjectViewHolder(private val binding: CellProjectSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(project: ProjectItem) {
            bindTextAppearances()
            with(binding) {
                tvProjectSelectionTitle.setOtaText(project.name)
                tvProjectSelectionSubtitle.setOtaTextOrGone(
                    localizationService,
                    project.subtitle.takeUnless { it.isNullOrBlank() })

                ivProjectSelection.setImageFrom(
                    project.thumbnailUrl,
                    origin.viewLifecycleOwner.lifecycleScope,
                )

                ivProjectSelectionChecked.setImageResource(
                    when (project.id) {
                        itemProvider.selectedItemId -> R.drawable.radiobutton_checked
                        else -> R.drawable.radiobutton_unchecked
                    }
                )

                root.setOnClickListener { selectProject(project) }
            }
        }

        private fun bindTextAppearances() {
            with(binding) {
                val colors = InterfaceKitColor.projectSwitcher.project
                val textStyle = InterfaceKitTextStyle.projectSwitcher.project
                backgroundProjectSelection.setCardBackgroundColor(colors.background)
                tvProjectSelectionTitle.setTextColor(colors.title)
                tvProjectSelectionTitle.setFont(textStyle.title)
                tvProjectSelectionSubtitle.setTextColor(colors.subtitle)
                tvProjectSelectionSubtitle.setFont(textStyle.subtitle)
                vProjectSelectionSeparator.setBackgroundColor(colors.separator)
                ivProjectSelectionChecked.setColorFilter(colors.checkbox)
            }
        }
    }

    open class Item(val title: String, val subtitle: String? = null)

    class HeaderItem(title: String, subtitle: String) : Item(title, subtitle)

    class ProjectItem(
        val id: String,
        val name: String,
        val startDate: ZonedDateTime? = null,
        val endDate: ZonedDateTime? = null,
        val thumbnailUrl: String?,
        zoneId: ZoneId,
    ) : Item(
        name,
        formatDates(startDate, endDate, zoneId)
    )

    interface ItemProvider {
        val items: List<Item>
        var selectedItemId: String?
    }

    companion object {
        const val HEADER_VIEW_TYPE: Int = 1
        const val PROJECT_VIEW_TYPE: Int = 0

        private fun formatDates(
            startDate: ZonedDateTime?,
            endDate: ZonedDateTime?,
            zoneId: ZoneId,
        ): String {
            val formattedStartDate =
                startDate?.getFormattedDateTime(FormatStyle.MEDIUM, null, zoneId)
            val formattedEndDate =
                endDate?.getFormattedDateTime(FormatStyle.MEDIUM, null, zoneId)

            return formattedStartDate?.let { startDateFormatted ->
                startDateFormatted + (formattedEndDate?.let { " - $it" } ?: "")
            } ?: "".also {
                App.log.e("Project start and end dates couldn't be parsed.")
            }
        }
    }

    private data class ProjectSwitcherProjectTapEvent(
        private val project: ProjectItem,
        private val analyticsScreenName: String,
    ) : MappedMetrics {
        private val parameters = mapOf(
            EventParameter.itemId to project.id,
            EventParameter.itemName to project.name,
            EventParameter.screenName to analyticsScreenName,
        )

        override fun track(provider: MappedProvider) {
            provider.track(EventName("project_switcher/project_tap"), parameters)
        }
    }
}
