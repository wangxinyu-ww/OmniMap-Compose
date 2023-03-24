// MIT License
// 
// Copyright (c) 2022 被风吹过的夏天
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.melody.bdmap.myapplication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.model.LatLng
import com.melody.bdmap.myapplication.viewmodel.MultiPointOverlayViewModel
import com.melody.map.baidu_compose.BDMap
import com.melody.map.baidu_compose.model.BDCameraPosition
import com.melody.map.baidu_compose.overlay.Marker
import com.melody.map.baidu_compose.overlay.MultiPointOverlay
import com.melody.map.baidu_compose.overlay.rememberMarkerState
import com.melody.map.baidu_compose.position.rememberCameraPositionState
import com.melody.ui.components.RedCenterLoading
import kotlinx.coroutines.flow.filterNotNull

/**
 * MultiPointOverlayScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/21 11:26
 */
@Composable
internal fun MultiPointOverlayScreen() {
    val viewModel: MultiPointOverlayViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()
    val markerState = rememberMarkerState()
    val cameraPositionState = rememberCameraPositionState {
        position = BDCameraPosition(LatLng(39.91, 116.40), 4F, 0f, 0f)
    }
    LaunchedEffect(Unit) {
        snapshotFlow { currentState.clickPointLatLng }.filterNotNull().collect { markerState.position = it }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BDMap(
            modifier = Modifier.matchParentSize(),
            uiSettings = currentState.uiSettings,
            cameraPositionState = cameraPositionState,
            onMapLoaded = viewModel::initMultiPointData
        ) {
            MultiPointOverlay(
                icon = currentState.multiPointIcon,
                multiPointItems = currentState.multiPointItems,
                onClick = viewModel::onMultiPointItemClick
            )
            Marker(
                icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                state = markerState,
                visible = null != currentState.clickPointLatLng,
                isClickable = false
            )
        }
        if(currentState.isLoading) {
            RedCenterLoading()
        }
    }

}