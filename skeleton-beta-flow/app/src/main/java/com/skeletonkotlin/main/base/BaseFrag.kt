package com.skeletonkotlin.main.base

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.skeletonkotlin.AppConstants.Api.ResponseCode.UNAUTHORIZED_CODE
import com.skeletonkotlin.BR
import com.skeletonkotlin.R
import com.skeletonkotlin.helper.util.LocationFetchUtil
import com.skeletonkotlin.helper.util.PrefUtil
import com.skeletonkotlin.helper.util.ToastUtil
import com.skeletonkotlin.main.base.BaseRepo.ApiResultType.CANCELLED
import com.skeletonkotlin.main.common.ApiRenderState
import com.smokelaboratory.freedom.Freedom
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class BaseFrag<binding : ViewDataBinding, VM : BaseVM>(
    @LayoutRes private val layoutId: Int, private val fragFactory: FragmentFactory? = null
) : Fragment(), View.OnClickListener {

    protected lateinit var binding: binding

    protected val prefs by inject<PrefUtil>()

    protected var locationFetchUtil: LocationFetchUtil? = null
    protected var freedom: Freedom? = null

    private var progress: ObservableField<Boolean>? = null

    protected abstract fun renderState(apiRenderState: ApiRenderState)
    protected abstract val hasProgress: Boolean
    protected abstract val vm: VM?
    protected abstract fun init()

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * If a parent Fragment contains nested Fragments or multiple levels of nested Fragments,
         * then by default, they all use the same FragmentFactory from the parent Fragment unless overridden.
         */

        fragFactory?.let {
            childFragmentManager.fragmentFactory = it
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<binding>(inflater, layoutId, container, false).apply {
            lifecycleOwner = this@BaseFrag
            vm?.let {
                setVariable(BR.vm, it)

                lifecycleScope.launch {
                    it.state().collect {
                        renderState(it)
                    }
                }

                lifecycleScope.launch {
                    it.apiError.collect {
                        if (it.resCode == UNAUTHORIZED_CODE)
                            (requireActivity() as BaseAct<ViewDataBinding, BaseVM>).logout(true)

                        else
                            if (it.resultType != CANCELLED) {
                                hideProgress()
                                it.error?.let {
                                    errorToast(it)
                                }
                            }
                    }
                }
            }
            setVariable(BR.click, this@BaseFrag)
        }

        if (hasProgress) {
            progress = ObservableField()
            binding.setVariable(BR.progress, progress)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun startActivity(
        act: Class<*>,
        bundle: Bundle? = null,
        flags: List<Int>? = null,
        shouldAnimate: Boolean = true
    ) {
        val intent = Intent(requireActivity(), act)

        if (bundle != null)
            intent.putExtras(bundle)

        if (!flags.isNullOrEmpty())
            flags.forEach {
                intent.addFlags(it)
            }

        if (shouldAnimate)
            startActivity(
                intent,
                ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
                    .toBundle()
            )
        else
            startActivity(intent)
    }

    fun startActivityForResult(
        act: Class<*>,
        requestCode: Int = 0,
        bundle: Bundle? = null,
        flags: List<Int>? = null,
        shouldAnimate: Boolean = true
    ) {
        val intent = Intent(requireActivity(), act)

        if (bundle != null)
            intent.putExtras(bundle)

        if (!flags.isNullOrEmpty())
            flags.forEach {
                intent.addFlags(it)
            }

        if (shouldAnimate)
            startActivityForResult(
                intent,
                requestCode,
                ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
                    .toBundle()
            )
        else
            startActivityForResult(intent, requestCode)
    }

    inline fun <reified T : Fragment> addFrag(
        container: Int,
        addToBackStack: Boolean = false,
        shouldAnimate: Boolean = true,
        bundle: Bundle? = null
    ) {
        childFragmentManager.commit {
            if (shouldAnimate)
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            if (addToBackStack)
                addToBackStack(T::class.java.name)
            add<T>(container, args = bundle)
        }
    }

    fun addFrag(
        fragment: Fragment,
        container: Int,
        addToBackStack: Boolean = false,
        shouldAnimate: Boolean = true,
        bundle: Bundle? = null
    ) {
        childFragmentManager.commit {
            if (shouldAnimate)
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            if (addToBackStack)
                addToBackStack(fragment::class.java.name)
            if (bundle != null)
                fragment.arguments = bundle

            add(container, fragment)
        }
    }

    inline fun <reified T : Fragment> replaceFrag(
        container: Int,
        addToBackStack: Boolean = false,
        shouldAnimate: Boolean = true,
        bundle: Bundle? = null
    ) {
        childFragmentManager.commit {
            if (shouldAnimate)
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            if (addToBackStack)
                addToBackStack(T::class.java.name)
            replace<T>(container, args = bundle)
        }
    }

    fun replaceFrag(
        fragment: Fragment,
        container: Int,
        addToBackStack: Boolean = false,
        shouldAnimate: Boolean = true,
        bundle: Bundle? = null
    ) {
        childFragmentManager.commit {
            if (shouldAnimate)
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            if (addToBackStack)
                addToBackStack(fragment::class.java.name)
            if (bundle != null)
                fragment.arguments = bundle

            replace(container, fragment)
        }
    }

    fun showProgress() {
        progress?.set(true)
    }

    fun hideProgress() {
        progress?.set(false)
    }

    fun errorToast(message: String, callback: ((Boolean) -> Unit)? = null) {
        ToastUtil.errorSnackbar(message, binding.root, callback)
    }

    fun successToast(message: String, callback: ((Boolean) -> Unit)? = null) {
        ToastUtil.successSnackbar(message, binding.root, callback)
    }

    fun delayedExecutor(millis: Long, executable: () -> Unit) {
        lifecycleScope.launch {
            delay(millis)
            executable.invoke()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        locationFetchUtil?.onActivityResult(requestCode, resultCode, data)
        freedom?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locationFetchUtil?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        freedom?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onClick(v: View) {

    }
}
