package com.greencopper.event.scheduleItem.ui.timeline

import android.graphics.Point
import android.graphics.Rect
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.event.scheduleItem.ui.timeline.TimelineAdapter.ItemAttributes
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.w
import kotlinx.parcelize.Parcelize

/**
 * LayoutManager heavily tied to [TimelineAdapter]
 */
internal class TimelineLayoutManager(
    private val timelineAdapter: TimelineAdapter,
    private val onFirstLoadFinished: () -> Unit,
) : RecyclerView.LayoutManager() {
    private val currentScrollOffset = Point(0, 0)
    private var scrollContentWidth = 0
    private var scrollContentHeight = 0
    private var canScrollVertically = true
    private var canScrollHorizontally = true
    private var firstLoadFinished = false
    private var recycler: RecyclerView.Recycler? = null

    private var restoredScrollState: State? = null

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        this.recycler = recycler
        scrollContentWidth = timelineAdapter.contentWidth
        scrollContentHeight = timelineAdapter.contentHeight
        canScrollVertically = scrollContentHeight > height
        canScrollHorizontally = scrollContentWidth > width

        //We have nothing to show for an empty data set but clear any existing views
        val itemCount = itemCount
        if (itemCount == 0) {
            removeAllViews()
            return
        }
        layoutChildren(recycler)
        if (!firstLoadFinished) {
            firstLoadFinished = true
            onFirstLoadFinished()
        }
    }

    private fun layoutChildren(recycler: RecyclerView.Recycler) {
        readjustStickyHeaders()
        normalizeScrollOffset(currentScrollOffset)
        detachAndScrapAttachedViews(recycler)

        val childrenPositionToAdd = childrenPositionToAdd
        for (position in childrenPositionToAdd) {
            if (position <= itemCount) {
                val itemAttributes: ItemAttributes = timelineAdapter[position]
                    ?: continue
                val view = try {
                    recycler.getViewForPosition(position)
                } catch (throwable: Throwable) {
                    //Usually happens because we scrolled while Adapter is changing data
                    App.log.w("View couldn't be retrieved from RecyclerView")
                    return
                }
                addView(view, position)
                val currentCoordinates = Rect(itemAttributes.rect)
                offsetItemAttributesRectToChildViewRect(
                    currentCoordinates,
                    itemAttributes.isOffsetEnabledHorizontally,
                    itemAttributes.isOffsetEnabledVertically
                )
                view.right = currentCoordinates.right
                view.left = currentCoordinates.left
                view.top = currentCoordinates.top
                view.bottom = currentCoordinates.bottom
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(currentCoordinates.width(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(currentCoordinates.height(), View.MeasureSpec.EXACTLY)
                )
                view.elevation = itemAttributes.type.ordinal.toFloat()
                layoutDecorated(view, view.left, view.top, view.right, view.bottom)
            }
        }
    }

    /**
     * This is used for the "scrolling back up" scenario, where you've already scrolled through a bunch of stages.
     * When scrolling to go up, we need to place the "previous" stage right above the current stage, so that the view is created just on top of it.
     * Otherwise, if we were only using the initial coordinates calculated within [prepareLayout][TimelineAdapter.prepareLayout],
     * the "previous" stage would just appear when arriving to the top of its section.
     * The heavy lifting for sticky headers is mainly done by [scrollVerticallyBy].
     */
    private fun readjustStickyHeaders() {
        val initialStages = timelineAdapter.initialItemAttributesStagesPerPosition
        val yOffset = currentScrollOffset.y + timelineAdapter.virtualTop

        for (index in 0 until initialStages.size() - 1) {
            val stagePosition = initialStages.keyAt(index)

            val nextStagePosition = initialStages.keyAt(initialStages.indexOfKey(stagePosition) + 1)
            val nextTopStage = initialStages[nextStagePosition].rect.top

            timelineAdapter.itemAttributesList[stagePosition]?.rect?.let { stageRect ->
                //If we scrolled passed the next stage, we place current stage right above the next stage
                if (yOffset > nextTopStage) {
                    stageRect.top = nextTopStage - timelineAdapter.stageHeight
                    stageRect.bottom = nextTopStage
                }
            }
        }
    }

    /**
     * Get the position of all items that should be shown within the visible portion of the RecyclerView
     */
    private val childrenPositionToAdd: Set<Int>
        get() {
            val visibleChildrenPosition = mutableSetOf<Int>()
            val currentScreenPosition = visibleRect
            for (i in 0 until timelineAdapter.itemCount) {
                val itemAttributes: ItemAttributes = timelineAdapter[i]
                    ?: continue
                val currentChild = Rect(itemAttributes.rect)
                offsetItemAttributesRectToChildViewRect(
                    currentChild,
                    itemAttributes.isOffsetEnabledHorizontally,
                    itemAttributes.isOffsetEnabledVertically
                )
                if (checkIfRectsIntersect(currentScreenPosition, currentChild)) {
                    visibleChildrenPosition.add(i)
                }
            }
            return visibleChildrenPosition
        }

    /**
     * Limit scroll offset to possible value according to current layout.
     */
    private fun normalizeScrollOffset(scrollOffset: Point) {
        val x = maxOf(0, minOf(scrollableWidth - width, scrollOffset.x))
        val y = maxOf(0, minOf(scrollableHeight - height, scrollOffset.y))
        scrollOffset.set(x, y)
    }

    /**
     * Convert coordinate of rect from itemAttributes to child view position.
     */
    private fun offsetItemAttributesRectToChildViewRect(
        rect: Rect,
        isOffsetEnabledHorizontally: Boolean,
        isOffsetEnabledVertically: Boolean,
    ) {
        val xOffset = if (isOffsetEnabledHorizontally) -currentScrollOffset.x + paddingLeft else paddingLeft
        val yOffset = if (isOffsetEnabledVertically) -currentScrollOffset.y + paddingTop else paddingTop
        rect.offset(xOffset, yOffset)
    }

    //region Scrolling
    override fun canScrollHorizontally(): Boolean {
        return canScrollHorizontally
    }

    override fun canScrollVertically(): Boolean {
        return canScrollVertically
    }

    private val scrollableHeight: Int
        get() = scrollContentHeight + paddingTop + paddingBottom
    private val scrollableWidth: Int
        get() = scrollContentWidth + paddingLeft + paddingRight

    internal fun scrollTo(dx: Int, dy: Int) {
        recycler?.let {
            scrollVerticallyBy(dy - currentScrollOffset.y, it, RecyclerView.State())
            scrollHorizontallyBy(dx - currentScrollOffset.x, it)
        }
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        fun scrollChild(childIndex: Int, actualDy: Int) {
            val view = getChildAt(childIndex) ?: return
            val position = getPosition(view)

            val bottomHourline = currentScrollOffset.y + timelineAdapter.virtualTop
            val itemAttributes = timelineAdapter.itemAttributesList[position]
            if (itemAttributes?.type == TimelineAdapter.Type.STAGE) {
                val stagesLAperPosition = timelineAdapter.initialItemAttributesStagesPerPosition
                val indexOfKey = stagesLAperPosition.indexOfKey(position)
                val nextTopStage =
                    (indexOfKey + 1).takeIf { it < stagesLAperPosition.size() }
                        ?.let { nextIndex ->
                            stagesLAperPosition[stagesLAperPosition.keyAt(nextIndex)].rect.top
                        } ?: scrollContentHeight

                //Figuring what we should assign as view.top for this stage
                val viewTop = when (bottomHourline) {
                    //If stage is between it's initial position and the next stage (but not touching the next stage), it sticks to hourline
                    in stagesLAperPosition[position].rect.top..minOf(
                        nextTopStage - timelineAdapter.stageHeight,
                        scrollContentHeight
                    ),
                    -> timelineAdapter.virtualTop

                    //If next stage is within range of touching the current stage, we offset the current stage
                    in nextTopStage - timelineAdapter.stageHeight..minOf(
                        nextTopStage,
                        scrollContentHeight
                    ),
                    -> timelineAdapter.virtualTop - (timelineAdapter.stageHeight - (nextTopStage - bottomHourline))

                    //This is the "normal state", stage is positioned simply according to scrolling
                    else -> stagesLAperPosition[position].rect.top - currentScrollOffset.y
                }

                //We set the new view position
                view.top = viewTop
                view.bottom = viewTop + timelineAdapter.stageHeight
                /** We also adjust the itemAttributes so that the view isn't accidentally removed (because too far from its original position, the view won't be added again in [layoutChildren]*/
                itemAttributes.rect.apply {
                    top = viewTop + currentScrollOffset.y
                    bottom = viewTop + currentScrollOffset.y + timelineAdapter.stageHeight
                }

            } else if (timelineAdapter.isOffsetEnabledVertically(position)) {
                view.offsetTopAndBottom(-actualDy)
            }
        }

        if (firstLoadFinished) {
            val actualDy = if (dy > 0) {
                val remainingY = scrollableHeight - height - currentScrollOffset.y
                minOf(dy, remainingY)
            } else {
                -minOf(-dy, currentScrollOffset.y)
            }

            currentScrollOffset.offset(0, actualDy)
            for (i in 0 until childCount) {
                scrollChild(i, actualDy)
            }
            layoutChildren(recycler)
            return actualDy
        }
        return 0
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        return scrollHorizontallyBy(dx, recycler)
    }

    private fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler): Int {
        if (firstLoadFinished) {
            val actualDx = if (dx > 0) {
                val remainingX = maxOf(0, scrollableWidth - width - currentScrollOffset.x)
                minOf(dx, remainingX)
            } else {
                -minOf(-dx, currentScrollOffset.x)
            }
            currentScrollOffset.offset(actualDx, 0)
            for (i in 0 until childCount) {
                getChildAt(i)?.let {
                    if (timelineAdapter.isOffsetEnabledHorizontally(getPosition(it))) {
                        it.offsetLeftAndRight(-actualDx)
                    }
                }
            }
            layoutChildren(recycler)
            return actualDx
        }
        return 0
    }
    //endregion

    override fun onSaveInstanceState(): Parcelable = State(currentScrollOffset.x, currentScrollOffset.y)

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? State)?.let {
            restoredScrollState = state
            scrollTo(state.xOffset, state.yOffset)
        }
    }

    fun tryRestoreScroll(): Boolean {
        return restoredScrollState?.let {
            scrollTo(it.xOffset, it.yOffset)
            restoredScrollState = null
            true
        } ?: false
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onAdapterChanged(oldAdapter: RecyclerView.Adapter<*>?, newAdapter: RecyclerView.Adapter<*>?) {
        super.onAdapterChanged(oldAdapter, newAdapter)
        recycler = null
        firstLoadFinished = false
    }

    /**
     * Get visible rect to layout
     */
    private val visibleRect: Rect
        get() = createRect(0, 0, width, height)

    @Parcelize
    data class State(val xOffset: Int, val yOffset: Int) : Parcelable

    companion object {
        private fun createRect(x: Int, y: Int, width: Int, height: Int): Rect {
            return Rect(x, y, x + width, y + height)
        }

        private fun checkIfRectsIntersect(rect1: Rect, rect2: Rect): Boolean {
            // other intersect family methods are destructive...
            return rect1.intersects(rect2.left, rect2.top, rect2.right, rect2.bottom)
        }
    }
}

