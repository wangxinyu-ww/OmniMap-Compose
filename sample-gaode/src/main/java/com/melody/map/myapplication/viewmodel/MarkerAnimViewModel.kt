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

package com.melody.map.myapplication.viewmodel

import com.amap.api.maps.model.LatLng
import com.melody.map.myapplication.contract.MarkerAnimContract
import com.melody.map.myapplication.repo.MarkerAnimRepository
import com.melody.sample.common.base.BaseViewModel
import kotlinx.coroutines.Dispatchers

/**
 * MarkerAnimViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/21 15:48
 */
class MarkerAnimViewModel: BaseViewModel<MarkerAnimContract.Event,MarkerAnimContract.State,MarkerAnimContract.Effect>() {
    override fun createInitialState(): MarkerAnimContract.State {
        return MarkerAnimContract.State(
            isLoading = true,
            centerLatLng = LatLng(31.819738, 119.969761),
            uiSettings = MarkerAnimRepository.initMapUiSettings(),
            markerLatLngList = emptyList()
        )
    }

    override fun handleEvents(event: MarkerAnimContract.Event) {
    }

    init {
        asyncLaunch(Dispatchers.IO) {
            val latLngList = MarkerAnimRepository.randomLatLngList(LatLng(31.819738, 119.969761))
            setState { copy(markerLatLngList = latLngList) }
        }
    }
}