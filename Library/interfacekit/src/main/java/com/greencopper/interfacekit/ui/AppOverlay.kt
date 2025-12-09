package com.greencopper.interfacekit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.draftcontent.DraftContentManager
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.ui.compose.LocalLocalizationAccess
import com.greencopper.interfacekit.ui.compose.MainCompositionLocalProvider
import com.greencopper.interfacekit.ui.compose.mockStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

public interface AppOverlay {
    public fun setOverlayOn(view: ComposeView, scope: LifecycleCoroutineScope)
}

internal class ConcreteAppOverlay(
    private val contentManager: ContentManager,
    private val draftContentManager: DraftContentManager,
    private val routeController: RouteController,
    private val localizationService: LocalizationService,
    private val viewBuilder: IKViewBuilder,
    private val localStorage: LocalStorage,
) : AppOverlay {

    override fun setOverlayOn(view: ComposeView, scope: LifecycleCoroutineScope) {
        view.setContent {
            viewBuilder.buildContent {
                contentManager.currentContentFlow.collectAsStateWithLifecycle(null).value?.let { content ->
                    if (content.type == OTAContent.Type.Draft) {
                        DraftContentOverlay()
                    }
                }
            }
        }

        scope.launch {
            draftContentManager.passcodeFlow
                .combine(RootLayoutHolder.rootLayoutHolder) { passcode, rootLayout -> Pair(passcode, rootLayout) }
                .collectLatest { (passcode, rootLayout) ->
                    if (rootLayout == null) return@collectLatest // UI shouldn't observe anything yet

                    var lastObservedPasscode = localStorage.app.core.lastUIObservedDraftContentPasscode.value

                    if (passcode == null && passcode != lastObservedPasscode) {
                        routeController.showAlert(
                            title = localizationService.getString("interfaceKit.toggle_draft_content.signed_out.title"),
                            message = localizationService.getString("interfaceKit.toggle_draft_content.signed_out.message"),
                            positiveText = localizationService.getString("common.ok"),
                        )
                    }

                    localStorage.app.core.lastUIObservedDraftContentPasscode.value = passcode
            }
        }
    }
}

@Composable
private fun DraftContentOverlay() {
    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White),
        colors = CardDefaults.cardColors(containerColor = Color(0x66DF0000)),
        modifier = Modifier
            .wrapContentSize(align = Alignment.BottomCenter)
            .padding(bottom = 65.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = null,
                modifier = Modifier.semantics { invisibleToUser() }
            )

            Spacer(Modifier.size(8.dp))

            Text(
                text = LocalLocalizationAccess.current.getString("interfaceKit.draft_content_banner.title"),
                color = Color.White,
                fontSize = 12.sp,
            )
        }
    }
}

@Preview
@Composable
private fun DraftContentOverlayPreview() {
    MainCompositionLocalProvider(
        mockStrings(
            mapOf("interfaceKit.draft_content_banner.title" to "Draft Content"),
        )
    ) {
        DraftContentOverlay()
    }
}
