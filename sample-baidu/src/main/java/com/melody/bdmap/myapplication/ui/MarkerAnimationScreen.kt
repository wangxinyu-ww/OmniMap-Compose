// MIT License
//
// Copyright (c) 2023 被风吹过的夏天
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.melody.bdmap.myapplication.viewmodel.MarkerAnimationViewModel
import com.melody.map.baidu_compose.BDMap
import com.melody.map.baidu_compose.overlay.MarkerInfoWindowContent
import com.melody.map.baidu_compose.overlay.rememberMarkerState
import com.melody.map.baidu_compose.position.rememberCameraPositionState

/**
 * MarkerAnimationScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/22 14:58
 */
@Composable
internal fun MarkerAnimationScreen() {
    val viewModel: MarkerAnimationViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberMarkerState(position = uiState.markerDefaultLocation)

    LaunchedEffect(uiState.mapLoaded) {
        if(uiState.mapLoaded) {
            markerState.showInfoWindow()
        }
    }

    LaunchedEffect(uiState.markerDefaultLocation) {
        cameraPositionState.animate(MapStatusUpdateFactory.newLatLng(uiState.markerDefaultLocation))
    }

    LaunchedEffect(uiState.runAnimation) {
        if(uiState.runAnimation) {
            markerState.hideInfoWindow()
        } else {
            markerState.showInfoWindow()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BDMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiState.mapUiSettings,
            onMapLoaded = viewModel::handleMapLoaded
        ) {
            MarkerInfoWindowContent(
                state = markerState,
                animation = uiState.markerAnimation,
                runAnimation = uiState.runAnimation,
                icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                isClickable = uiState.isClickable, //防止重复点击显示InfoWindow
                content = {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = "点击Marker查看动画"
                    )
                },
                onClick = {
                    viewModel.startMarkerAnimation()
                    true
                }
            )
        }
    }
}