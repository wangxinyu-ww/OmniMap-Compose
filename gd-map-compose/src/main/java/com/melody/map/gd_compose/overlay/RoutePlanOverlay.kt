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

package com.melody.map.gd_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.Polyline
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.BusPathV2
import com.amap.api.services.route.DrivePathV2
import com.amap.api.services.route.RidePath
import com.amap.api.services.route.WalkPath
import com.melody.map.gd_compose.MapApplier
import com.melody.map.gd_compose.MapNode
import com.melody.map.gd_compose.kernel.KernelBusRouteOverlay
import com.melody.map.gd_compose.kernel.KernelDrivingRouteOverlay
import com.melody.map.gd_compose.kernel.KernelRideRouteOverlay
import com.melody.map.gd_compose.kernel.KernelWalkRouteOverlay
import com.melody.map.gd_compose.model.GDMapComposable

internal class RoutePlanOverlayNode(
    val polylineList: MutableList<Polyline>,
    val drivingRouteOverlay: KernelDrivingRouteOverlay? = null,
    val busRouteOverlay: KernelBusRouteOverlay? = null,
    val rideRouteOverlay: KernelRideRouteOverlay? = null,
    val walkRouteOverlay: KernelWalkRouteOverlay? = null,
    var onPolylineClick: (Polyline) -> Unit,
    var onMarkerClick: (Marker) -> Boolean
) : MapNode {
    override fun onRemoved() {
        drivingRouteOverlay?.removeFromMap()
        busRouteOverlay?.removeFromMap()
        rideRouteOverlay?.removeFromMap()
        walkRouteOverlay?.removeFromMap()
        polylineList.clear()
    }
}

/**
 * 步行路径规划V2接口
 * @param startPoint 起点的位置
 * @param endPoint 终点位置
 * @param routeWidth 路径的宽度
 * @param walkLineSelectedTexture 选中状态下步行线路纹理图片
 * @param walkLineUnSelectedTexture 未选中状态下步行线路纹理图片
 * @param startMarkerIcon 起点位置的Marker图标
 * @param endMarkerIcon 终点位置的Marker图标
 * @param walkNodeIcon 节点位置的图标
 * @param nodeIconVisible 节点是否可见
 * @param isSelected 路径是否被选中
 * @param walkPath v2接口的Walk路径
 * @param onPolylineClick polyline的点击事件回调
 * @param onMarkerClick Marker的点击事件回调
 */
@Composable
@GDMapComposable
fun WalkRouteOverlay(
    startPoint: LatLng,
    endPoint: LatLng,
    routeWidth: Float = 18F,
    polylineColor: Color = Color(0XFF5584EB),
    walkLineSelectedTexture: BitmapDescriptor,
    walkLineUnSelectedTexture: BitmapDescriptor,
    startMarkerIcon: BitmapDescriptor? = null,
    endMarkerIcon: BitmapDescriptor? = null,
    walkNodeIcon: BitmapDescriptor? = null,
    nodeIconVisible: Boolean = false,
    isSelected: Boolean = true,
    walkPath: WalkPath,
    onPolylineClick: (Polyline) -> Unit = {},
    onMarkerClick: (Marker) -> Boolean = { false }
){
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<RoutePlanOverlayNode, MapApplier>(
        factory = {
            val aMap = mapApplier?.map?: error("Error adding WalkRouteOverlay")
            val walkRouteOverlay = KernelWalkRouteOverlay(
                aMap = aMap,
                isSelected = isSelected,
                routeWidth = routeWidth,
                polylineColor = polylineColor,
                startMarkerIcon = startMarkerIcon,
                endMarkerIcon = endMarkerIcon,
                startPoint = startPoint,
                endPoint = endPoint,
                walkLineSelectedTexture = walkLineSelectedTexture,
                walkLineUnSelectedTexture = walkLineUnSelectedTexture,
                walkNodeIcon = walkNodeIcon,
                walkPath = walkPath
            )
            walkRouteOverlay.setNodeIconVisibility(nodeIconVisible)
            walkRouteOverlay.removeFromMap()
            walkRouteOverlay.addToMap()
            if(isSelected) {
                walkRouteOverlay.zoomToSpan()
            }
            RoutePlanOverlayNode(
                polylineList = walkRouteOverlay.allPolyLines,
                walkRouteOverlay = walkRouteOverlay,
                onMarkerClick = onMarkerClick,
                onPolylineClick = onPolylineClick
            )
        },
        update = {
            update(onMarkerClick) { this.onMarkerClick = it }
            update(onPolylineClick) { this.onPolylineClick = it }

            set(nodeIconVisible) { this.walkRouteOverlay?.setNodeIconVisibility(it) }
            set(isSelected) { this.walkRouteOverlay?.setPolylineSelected(it) }
        }
    )
}

/**
 * 骑行路径规划V2接口
 * @param startPoint 起点的位置
 * @param endPoint 终点位置
 * @param routeWidth 路径的宽度
 * @param rideLineSelectedTexture 选中状态下骑行线路纹理图片
 * @param rideLineUnSelectedTexture 未选中状态下骑行线路纹理图片
 * @param startMarkerIcon 起点位置的Marker图标
 * @param endMarkerIcon 终点位置的Marker图标
 * @param rideStationNodeIcon 节点位置的图标
 * @param nodeIconVisible 节点是否可见
 * @param isSelected 是否选中路径
 * @param ridePath v2接口的Ride路径
 * @param onPolylineClick polyline的点击事件回调
 * @param onMarkerClick Marker的点击事件回调
 */
@Composable
@GDMapComposable
fun RideRouteOverlay(
    startPoint: LatLng,
    endPoint: LatLng,
    routeWidth: Float = 18F,
    polylineColor: Color = Color(0XFF5584EB),
    rideLineSelectedTexture: BitmapDescriptor,
    rideLineUnSelectedTexture: BitmapDescriptor,
    startMarkerIcon: BitmapDescriptor? = null,
    endMarkerIcon: BitmapDescriptor? = null,
    rideStationNodeIcon: BitmapDescriptor? = null,
    nodeIconVisible: Boolean = false,
    isSelected: Boolean = true,
    ridePath: RidePath,
    onPolylineClick: (Polyline) -> Unit = {},
    onMarkerClick: (Marker) -> Boolean = { false }
){
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<RoutePlanOverlayNode, MapApplier>(
        factory = {
            val aMap = mapApplier?.map?: error("Error adding RideRouteOverlay")
            val rideRouteOverlay = KernelRideRouteOverlay(
                aMap = aMap,
                isSelected = isSelected,
                routeWidth = routeWidth,
                polylineColor = polylineColor,
                startMarkerIcon = startMarkerIcon,
                endMarkerIcon = endMarkerIcon,
                rideStationDescriptor = rideStationNodeIcon,
                rideLineSelectedTexture = rideLineSelectedTexture,
                rideLineUnSelectedTexture = rideLineUnSelectedTexture,
                startPoint = startPoint,
                endPoint = endPoint,
                ridePath = ridePath
            )
            rideRouteOverlay.setNodeIconVisibility(nodeIconVisible)
            rideRouteOverlay.removeFromMap()
            rideRouteOverlay.addToMap()
            if(isSelected) {
                rideRouteOverlay.zoomToSpan()
            }
            RoutePlanOverlayNode(
                polylineList = rideRouteOverlay.allPolyLines,
                rideRouteOverlay = rideRouteOverlay,
                onMarkerClick = onMarkerClick,
                onPolylineClick = onPolylineClick
            )
        },
        update = {
            update(onMarkerClick) { this.onMarkerClick = it }
            update(onPolylineClick) { this.onPolylineClick = it }

            set(nodeIconVisible) { this.rideRouteOverlay?.setNodeIconVisibility(it) }
            set(isSelected) { this.rideRouteOverlay?.setPolylineSelected(it) }
        }
    )
}

/**
 * 公交车路径规划V2接口
 * @param startPoint 起点的位置
 * @param endPoint 终点位置
 * @param routeWidth 路径的宽度
 * @param busLineSelectedTexture 选中状态下公交线路纹理图片
 * @param busLineUnSelectedTexture 未选中状态下公交线路纹理图片
 * @param walkLineSelectedTexture 未选中状态下步行线路纹理图片
 * @param walkLineUnSelectedTexture 未选中状态下步行路纹理图片
 * @param startMarkerIcon 起点位置的Marker图标
 * @param endMarkerIcon 终点位置的Marker图标
 * @param busNodeIcon 节点位置的图标
 * @param nodeIconVisible 节点是否可见
 * @param isSelected 路径是否被选中
 * @param busPath v2接口的Bus路径
 * @param onPolylineClick Polyline点击的回调
 * @param onMarkerClick Marker的点击事件回调
 */
@Composable
@GDMapComposable
fun BusRouteOverlay(
    startPoint: LatLng,
    endPoint: LatLng,
    routeWidth: Float = 18F,
    polylineColor: Color = Color(0XFF5584EB),
    busLineSelectedTexture: BitmapDescriptor,
    busLineUnSelectedTexture: BitmapDescriptor,
    walkLineSelectedTexture: BitmapDescriptor?,
    walkLineUnSelectedTexture: BitmapDescriptor?,
    startMarkerIcon: BitmapDescriptor? = null,
    endMarkerIcon: BitmapDescriptor? = null,
    busNodeIcon: BitmapDescriptor? = null,
    walkNodeIcon: BitmapDescriptor? = null,
    nodeIconVisible: Boolean = false,
    isSelected: Boolean = true,
    busPath: BusPathV2,
    onPolylineClick: (Polyline) -> Unit = {},
    onMarkerClick: (Marker) -> Boolean = { false }
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<RoutePlanOverlayNode, MapApplier>(
        factory = {
            val aMap = mapApplier?.map?: error("Error adding BusRouteOverlay")
            val busRouteOverlay = KernelBusRouteOverlay(
                aMap = aMap,
                isSelected = isSelected,
                routeWidth = routeWidth,
                polylineColor = polylineColor,
                walkLineSelectedTexture = walkLineSelectedTexture,
                walkLineUnSelectedTexture = walkLineUnSelectedTexture,
                busLineSelectedTexture = busLineSelectedTexture,
                busLineUnSelectedTexture = busLineUnSelectedTexture,
                startMarkerIcon = startMarkerIcon,
                endMarkerIcon = endMarkerIcon,
                busNodeIcon = busNodeIcon,
                walkNodeIcon = walkNodeIcon,
                startPoint = startPoint,
                endPoint = endPoint,
                busPath = busPath
            )
            busRouteOverlay.setNodeIconVisibility(nodeIconVisible)
            busRouteOverlay.removeFromMap()
            busRouteOverlay.addToMap()
            if(isSelected) {
                busRouteOverlay.zoomToSpan()
            }
            RoutePlanOverlayNode(
                polylineList = busRouteOverlay.allPolyLines,
                busRouteOverlay = busRouteOverlay,
                onMarkerClick = onMarkerClick,
                onPolylineClick = onPolylineClick
            )
        },
        update = {
            update(onMarkerClick) { this.onMarkerClick = it }
            update(onPolylineClick) { this.onPolylineClick = it }

            set(nodeIconVisible) { this.busRouteOverlay?.setNodeIconVisibility(it) }
            set(isSelected) { this.busRouteOverlay?.setPolylineSelected(it) }
        }
    )
}

/**
 * 驾车规划的路径V2接口
 * @param startPoint 起点的位置
 * @param endPoint 终点位置
 * @param routeWidth 路径的宽度
 * @param driveLineSelectedTexture 选中状态下驾车路径的纹理图片
 * @param driveLineUnSelectedTexture 未选中状态下驾车路径的纹理图片
 * @param startMarkerIcon 起点的Marker图标
 * @param endMarkerIcon 终点的Marker图标
 * @param driveNodeIcon 每站节点的图标Marker
 * @param showColorFulLine 是否显示交通拥堵情况
 * @param nodeIconVisible 是否显示节点的Marker
 * @param throughMarkerIcon 途经站的Marker
 * @param throughPointMarkerVisible 途经站的Marker是否可见
 * @param throughPointList 途径站的点List
 * @param isSelected 路径是否被选中
 * @param drivePath v2接口的Drive路径
 * @param onPolylineClick polyline的点击事件回调
 * @param onMarkerClick Marker的点击事件回调
 */
@Composable
@GDMapComposable
fun DrivingRouteOverlay(
    startPoint: LatLng,
    endPoint: LatLng,
    routeWidth: Float = 18F,
    polylineColor: Color = Color(0XFF5584EB),
    driveLineSelectedTexture: BitmapDescriptor,
    driveLineUnSelectedTexture: BitmapDescriptor,
    startMarkerIcon: BitmapDescriptor? = null,
    endMarkerIcon: BitmapDescriptor? = null,
    driveNodeIcon: BitmapDescriptor? = null,
    showColorFulLine: Boolean = true,
    nodeIconVisible: Boolean = false,
    isSelected: Boolean = true,
    throughPointMarkerVisible: Boolean = false,
    throughMarkerIcon: BitmapDescriptor?,
    throughPointList: List<LatLonPoint>,
    drivePath: DrivePathV2,
    onPolylineClick: (Polyline) -> Unit = {},
    onMarkerClick: (Marker) -> Boolean = { false }
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<RoutePlanOverlayNode, MapApplier>(
        factory = {
            val aMap = mapApplier?.map?: error("Error adding DrivingRouteOverlay")
            val drivingRouteOverlay = KernelDrivingRouteOverlay(
                aMap = aMap,
                isSelected = isSelected,
                routeWidth = routeWidth,
                polylineColor = polylineColor,
                driveLineSelectedTexture = driveLineSelectedTexture,
                driveLineUnSelectedTexture = driveLineUnSelectedTexture,
                startMarkerIcon = startMarkerIcon,
                endMarkerIcon = endMarkerIcon,
                driveNodeIcon = driveNodeIcon,
                throughMarkerIcon = throughMarkerIcon,
                startPoint = startPoint,
                endPoint = endPoint,
                drivePath = drivePath,
                throughPointList = throughPointList
            )
            // 是否用颜色展示交通拥堵情况，默认true
            drivingRouteOverlay.setIsColorfulline(showColorFulLine)
            drivingRouteOverlay.setThroughPointIconVisibility(throughPointMarkerVisible)
            drivingRouteOverlay.setNodeIconVisibility(nodeIconVisible)
            drivingRouteOverlay.removeFromMap()
            drivingRouteOverlay.addToMap()
            if(isSelected) {
                drivingRouteOverlay.zoomToSpan()
            }
            RoutePlanOverlayNode(
                polylineList = drivingRouteOverlay.allPolyLines,
                drivingRouteOverlay = drivingRouteOverlay,
                onMarkerClick = onMarkerClick,
                onPolylineClick = onPolylineClick
            )
        },
        update = {
            update(onMarkerClick) { this.onMarkerClick = it }
            update(onPolylineClick) { this.onPolylineClick = it }

            set(nodeIconVisible) { this.drivingRouteOverlay?.setNodeIconVisibility(it) }
            set(throughPointMarkerVisible) { this.drivingRouteOverlay?.setThroughPointIconVisibility(it) }
            set(isSelected) { this.drivingRouteOverlay?.setPolylineSelected(it) }
        }
    )
}

