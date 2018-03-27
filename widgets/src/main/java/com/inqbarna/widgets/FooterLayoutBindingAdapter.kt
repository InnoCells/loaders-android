package com.inqbarna.widgets

import android.databinding.*

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 26/03/2018
 */
class FooterLayoutBindingAdapter {

    @BindingMethods(
            BindingMethod(type = FooterLayout::class, attribute = "footerEnabled", method = "setEnabled")
    )
    @InverseBindingMethods(
            InverseBindingMethod(type = FooterLayout::class, attribute = "footerEnabled", method = "isEnabled")
    )
    companion object {

        @JvmStatic
        @BindingAdapter("enabledListener", "footerEnabledAttrChanged", requireAll = false)
        fun footerEnable(footerLayout: FooterLayout, onFooterEnabledListener: FooterLayout.OnFooterEnabledListener?, inverseBindingListener: InverseBindingListener?) {
            footerLayout.onFooterEnabledListener = object : FooterLayout.OnFooterEnabledListener {
                override fun onFooterEnabledState(enabled: Boolean) {
                    onFooterEnabledListener?.onFooterEnabledState(enabled)
                    inverseBindingListener?.onChange()
                }
            }
        }

    }
}