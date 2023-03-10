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

package com.melody.map.baidu_compose.overlay

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.adapter.ComposeInfoWindowAdapter
import com.baidu.mapapi.animation.Animation
import com.melody.map.baidu_compose.model.BDMapComposable

internal class MarkerNode(
    val compositionContext: CompositionContext,
    val marker: Marker,
    val mapApplier: MapApplier,
    val markerState: MarkerState,
    var onMarkerClick: (Marker) -> Boolean,
    var infoWindow: (@Composable (Marker) -> Unit)?,
    var infoContent: (@Composable (Marker) -> Unit)?,
) : MapNode {
    override fun onAttached() {
        markerState.markerNode = this
    }
    override fun onRemoved() {
        markerState.markerNode = null
        marker.cancelAnimation()
        marker.remove()
    }

    override fun onCleared() {
        markerState.markerNode = null
        marker.cancelAnimation()
        marker.remove()
    }
}

@Immutable
enum class DragState {
    START, DRAG, END
}

/**
 * 控制和观察[Marker]状态的状态对象。
 *
 * @param position Marker覆盖物的位置坐标
 */
class MarkerState(
    position: LatLng = LatLng(0.0, 0.0)
) {
    /**
     * 当前Marker覆盖物的位置
     */
    var position: LatLng by mutableStateOf(position)

    /**
     * 当前Marker拖拽的状态
     */
    var dragState: DragState by mutableStateOf(DragState.END)
        internal set

    // The MarkerNode associated with this MarkerState.
    internal var markerNode: MarkerNode? = null
        set(value) {
            if (field == null && value == null) return
            if (field != null && value != null) {
                error("MarkerState may only be associated with one MarkerNode at a time.")
            }
            field = value
        }

    /**
     * 显示 Marker 覆盖物的信息窗口
     */
    fun showInfoWindow() {
        markerNode?.let { node ->
            if(node.infoWindow != null) {
                node.marker.showInfoWindow(node.mapApplier.getInfoWindow(node.marker,node))
            } else if(node.infoContent != null) {
                node.marker.showInfoWindow(node.mapApplier.getInfoContents(node.marker,node))
            }
        }
    }

    /**
     * 隐藏Marker覆盖物的信息窗口。如果Marker本身是不可以见的，此方法将不起任何作用
     */
    fun hideInfoWindow() {
        markerNode?.marker?.hideInfoWindow()
    }

    companion object {
        /**
         * The default saver implementation for [MarkerState]
         */
        val Saver: Saver<MarkerState, LatLng> = Saver(
            save = { it.position },
            restore = { MarkerState(it) }
        )
    }
}

/**
 * MarkerCustomAnimation
 * @param animateType 设置Marker动画类型，见[MarkerAnimateType]，默认无动画，**设置此参数，单独触发内置类型的动画**
 * @param runAnimation 【只有配置了**animation**才有效】设置为true，启动动画，设置为false取消动画，设置为null，不触发动画
 * @param animation 设置Marker动画类: [com.baidu.mapapi.animation.AlphaAnimation]、[com.baidu.mapapi.animation.AnimationSet]、[com.baidu.mapapi.animation.RotateAnimation]、[com.baidu.mapapi.animation.ScaleAnimation]、[com.baidu.mapapi.animation.SingleScaleAnimation]、[com.baidu.mapapi.animation.Transformation]
 */
class MarkerCustomAnimation private constructor(
    val animateType: MarkerAnimateType,
    val animation: Animation?,
    val runAnimation :Boolean?
) {
    companion object {
        /**
         * @param animType 设置Marker动画类型，见[MarkerAnimateType]，默认无动画，**设置此参数，单独触发内置类型的动画**
         * @param runAnimation 【只有配置了**animation**才有效】设置为true，启动动画，设置为false取消动画，设置为null，不触发动画
         * @param animation 设置Marker动画类: [com.baidu.mapapi.animation.AlphaAnimation]、[com.baidu.mapapi.animation.AnimationSet]、[com.baidu.mapapi.animation.RotateAnimation]、[com.baidu.mapapi.animation.ScaleAnimation]、[com.baidu.mapapi.animation.SingleScaleAnimation]、[com.baidu.mapapi.animation.Transformation]
         */
        fun create(
            animType: MarkerAnimateType = MarkerAnimateType.none,
            runAnimation: Boolean? = null,
            animation: Animation? = null
        ): MarkerCustomAnimation {
            return MarkerCustomAnimation(
                animateType = animType,
                animation = animation,
                runAnimation = runAnimation
            )
        }
    }
}

@Composable
@BDMapComposable
fun rememberMarkerState(
    key: String? = null,
    position: LatLng = LatLng(0.0, 0.0)
): MarkerState = rememberSaveable(key = key, saver = MarkerState.Saver) {
    MarkerState(position)
}

/**
 * 默认覆盖物Marker， [Marker]是在地图上的一个点绘制图标。这个图标和屏幕朝向一致，和地图朝向无关，也不会受地图的旋转、倾斜、缩放影响
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例，X范围：[0.0f , 1.0f]，Y范围：[0.0f , 1.0f]
 * @param isPerspective Marker覆盖物是否开启**近大远小效果，默认关闭**，如需要开启，请自行设置为true
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param isFlat Marker覆盖物是否平贴在地图上
 * @param icon Marker覆盖物的图标
 * @param animation 设置Marker动画
 * @param rotation Marker覆盖物基于锚点旋转的角度，百度地图逆时针
 * @param bundle Marker覆盖物的额外信息
 * @param visible Marker 覆盖物的可见属性
 * @param zIndex Marker覆盖物的z轴值
 * @param onClick 标注点击事件回调
 */
@Composable
@BDMapComposable
fun Marker(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    draggable: Boolean = false,
    isClickable: Boolean = true,
    isPerspective: Boolean = false,
    isFlat: Boolean = false,
    animation: MarkerCustomAnimation? = null,
    bundle: Bundle? = null,
    icon: BitmapDescriptor,
    rotation: Float = 0.0f,
    visible: Boolean = true,
    zIndex: Int = 0,
    onClick: (Marker) -> Boolean = { false }
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        infoWindowYOffset = 0,
        isClickable = isClickable,
        animation = animation,
        isPerspective = isPerspective,
        icon = icon,
        rotation = rotation,
        isFlat = isFlat,
        bundle = bundle,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        infoContent = null,
        infoWindow = null
    )
}

/**
 * 覆盖物[Marker]，此组合项可定制整个InfoWindow信息窗口，如果不需要此自定义，请使用 [com.melody.map.tencent_compose.overlay.Marker].
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例，X范围：[0.0f , 1.0f]，Y范围：[0.0f , 1.0f]
 * @param infoWindowYOffset 设置InfoWindow的y轴偏移量
 * @param isPerspective Marker覆盖物是否开启**近大远小效果，默认关闭**，如需要开启，请自行设置为true
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param isFlat Marker覆盖物是否平贴在地图上
 * @param icon Marker覆盖物的图标
 * @param animation 设置Marker动画
 * @param rotation Marker覆盖物基于锚点旋转的角度，百度地图逆时针
 * @param bundle Marker覆盖物的额外信息
 * @param visible Marker 覆盖物的可见属性
 * @param zIndex Marker覆盖物的z轴值
 * @param onClick 标注点击事件回调
 * @param content 【可选】，用于自定义整个信息窗口，【里面动态的内容，通过extraInfo的方式获取】
 */
@Composable
@BDMapComposable
fun MarkerInfoWindow(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    infoWindowYOffset: Int = -52,
    draggable: Boolean = false,
    isClickable: Boolean = true,
    isPerspective: Boolean = false,
    isFlat: Boolean = false,
    animation: MarkerCustomAnimation? = null,
    bundle: Bundle? = null,
    icon: BitmapDescriptor,
    rotation: Float = 0.0f,
    visible: Boolean = true,
    zIndex: Int = 0,
    onClick: (Marker) -> Boolean = { false },
    content: (@Composable (Marker) -> Unit)? = null
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        isClickable = isClickable,
        isPerspective = isPerspective,
        animation = animation,
        infoWindowYOffset = infoWindowYOffset,
        icon = icon,
        isFlat = isFlat,
        rotation = rotation,
        bundle = bundle,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        infoWindow = content,
        infoContent = null,
    )
}

/**
 * 覆盖物[Marker]，此组合项可定制InfoWindow信息窗口内容，如果不需要此自定义，请使用 [com.melody.map.tencent_compose.overlay.Marker].
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例，X范围：[0.0f , 1.0f]，Y范围：[0.0f , 1.0f]
 * @param infoWindowYOffset 设置InfoWindow的y轴偏移量
 * @param isPerspective Marker覆盖物是否开启**近大远小效果，默认关闭**，如需要开启，请自行设置为true
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param isFlat Marker覆盖物是否平贴在地图上
 * @param icon Marker覆盖物的图标
 * @param animation 设置Marker动画
 * @param rotation Marker覆盖物基于锚点旋转的角度，百度地图逆时针
 * @param bundle Marker覆盖物的额外信息
 * @param visible Marker 覆盖物的可见属性
 * @param zIndex Marker覆盖物的z轴值
 * @param onClick 标注点击事件回调
 * @param content (可选)，用于自定义信息窗口的内容，【里面动态的内容，通过extraInfo的方式获取】
*/
@Composable
@BDMapComposable
fun MarkerInfoWindowContent(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(0.5f, 1.0f),
    infoWindowYOffset: Int = -52,
    draggable: Boolean = false,
    isClickable: Boolean = true,
    icon: BitmapDescriptor,
    isPerspective: Boolean = false,
    isFlat: Boolean = false,
    animation: MarkerCustomAnimation? = null,
    bundle: Bundle? = null,
    rotation: Float = 0.0f,
    visible: Boolean = true,
    zIndex: Int = 0,
    onClick: (Marker) -> Boolean = { false },
    content: (@Composable (Marker) -> Unit)? = null
) {
    MarkerImpl(
        state = state,
        alpha = alpha,
        anchor = anchor,
        draggable = draggable,
        isClickable = isClickable,
        isPerspective = isPerspective,
        infoWindowYOffset = infoWindowYOffset,
        isFlat = isFlat,
        icon = icon,
        bundle = bundle,
        rotation = rotation,
        animation = animation,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
        infoContent = content,
        infoWindow = null
    )
}

/**
 * Marker覆盖物的内部实现
 *
 * @param state [MarkerState]控制和观察[Marker]状态的状态对象。
 * @param alpha Marker覆盖物的透明度,透明度范围[0,1] 1为不透明,默认值为1
 * @param anchor Marker覆盖物的锚点比例，X范围：[0.0f , 1.0f]，Y范围：[0.0f , 1.0f]
 * @param infoWindowYOffset 设置InfoWindow的y轴偏移量
 * @param isPerspective Marker覆盖物是否开启**近大远小效果，默认关闭**，如需要开启，请自行设置为true
 * @param draggable Marker覆盖物是否允许拖拽
 * @param isClickable Marker覆盖物是否可以点击
 * @param isFlat Marker覆盖物是否平贴在地图上
 * @param icon Marker覆盖物的图标
 * @param animation 设置Marker动画
 * @param rotation Marker覆盖物基于锚点旋转的角度，百度地图逆时针
 * @param bundle Marker覆盖物的额外信息
 * @param visible Marker 覆盖物的可见属性
 * @param zIndex Marker覆盖物的z轴值
 * @param onClick 标注点击事件回调
 * @param infoWindow 【可选】，用于自定义整个信息窗口。如果此值为非空，则[infoContent]中的值将被忽略。
 * @param infoContent 【可选】，用于自定义信息窗口的内容。如果此值为非 null，则 [infoWindow] 必须为 null。
 */
@Composable
@BDMapComposable
private fun MarkerImpl(
    state: MarkerState,
    alpha: Float,
    anchor: Offset,
    infoWindowYOffset: Int,
    isPerspective: Boolean,
    draggable: Boolean,
    isClickable: Boolean,
    isFlat: Boolean,
    icon: BitmapDescriptor,
    animation: MarkerCustomAnimation?,
    rotation: Float,
    bundle: Bundle?,
    visible: Boolean,
    zIndex: Int,
    onClick: (Marker) -> Boolean,
    infoWindow: (@Composable (Marker) -> Unit)?,
    infoContent: (@Composable (Marker) -> Unit)?,
) {
    val mapApplier = currentComposer.applier as? MapApplier
    val compositionContext = rememberCompositionContext()
    ComposeNode<MarkerNode, MapApplier>(
        factory = {
            val marker = mapApplier?.map?.addOverlay(
                MarkerOptions().apply {
                    alpha(alpha)
                    anchor(anchor.x, anchor.y)
                    draggable(draggable)
                    icon(icon)
                    flat(isFlat)
                    rotate(rotation)
                    perspective(isPerspective)
                    position(state.position)
                    animateType(animateType)
                    bundle?.let {
                        extraInfo(it.apply { putInt(ComposeInfoWindowAdapter.KEY_INFO_WINDOW_Y_OFFSET, infoWindowYOffset) })
                    }
                    visible(visible)
                    zIndex(zIndex)
                }
            ) as? Marker ?: error("Error adding marker")
            marker.isClickable = isClickable
            MarkerNode(
                compositionContext = compositionContext,
                marker = marker,
                mapApplier = mapApplier,
                markerState = state,
                onMarkerClick = onClick,
                infoContent = infoContent,
                infoWindow = infoWindow,
            )
        },
        update = {
            update(onClick) { this.onMarkerClick = it }
            update(infoContent) { this.infoContent = it }
            update(infoWindow) { this.infoWindow = it }

            set(alpha) { this.marker.alpha = it }
            set(isClickable) { this.marker.isClickable = it }
            set(anchor) { this.marker.setAnchor(it.x, it.y) }
            set(isPerspective) { this.marker.isPerspective = it }
            set(draggable) { this.marker.isDraggable = it }
            set(icon) { this.marker.icon = it }
            set(rotation) { this.marker.rotate = it }
            set(isFlat) { this.marker.isFlat = it }
            set(animation) { this.marker.customAnimation(it) }
            set(state.position) { this.marker.position = it }
            set(bundle) {
                if(null != it) {
                    this.marker.extraInfo = it.apply {
                        putInt(ComposeInfoWindowAdapter.KEY_INFO_WINDOW_Y_OFFSET, infoWindowYOffset)
                    }
                }
            }
            set(visible) { this.marker.isVisible = it }
            set(zIndex) { this.marker.zIndex = it }
        }
    )
}

private fun Marker.customAnimation(animation: MarkerCustomAnimation?) {
    if(null == animation) return
    if(animation.runAnimation == false) {
        cancelAnimation()
    }
    setAnimateType(animation.animateType.ordinal)
    setAnimation(animation.animation)
    if(animation.runAnimation == true) {
        startAnimation()
    }
}