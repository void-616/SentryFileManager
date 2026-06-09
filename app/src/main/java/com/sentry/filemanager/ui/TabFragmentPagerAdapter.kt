/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

@file:Suppress("DEPRECATION")
package com.sentry.filemanager.ui


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
@Suppress("DEPRECATION")

class TabFragmentPagerAdapter(
    fragmentManager: FragmentManager,
    private vararg val tabs: Pair<CharSequence?, () -> Fragment>
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment = tabs[position].second()

    override fun getCount(): Int = tabs.size

    override fun getPageTitle(position: Int): CharSequence? = tabs[position].first
}
